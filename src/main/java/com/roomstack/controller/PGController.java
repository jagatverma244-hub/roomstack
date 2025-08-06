package com.roomstack.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.roomstack.dao.*;
import com.roomstack.entity.*;
import com.roomstack.service.SmsServices;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/PG")
public class PGController {
    @Autowired private UserRepository userRepository;
    @Autowired private PGRepository pgRepository;
    @Autowired private PGpamentsRepository pGpamentsRepository;
    @Autowired private PGImageRepository pgImageRepository;
    @Autowired private SmsServices smssErvices;
    @Autowired private SearchPGPamentsRepository searchpgPamentsRepository;

    @Value("${razorpay.key_id}") private String razorpayKeyId;
    @Value("${razorpay.key_secret}") private String razorpayKeySecret;

    @GetMapping("/fillpg")
    public String fillpg(Model model) {
        PGEntity pg = new PGEntity();
        model.addAttribute("fillpg", pg);
        return "pg/fill_pg";
    }

    @PostMapping("/create-pg-paments")
    public String fillpaments(@ModelAttribute("fillpgdetails") PGEntity pgEntity,
                              @RequestParam("imagefiles") List<MultipartFile> files,
                              Principal principal,
                              Model model,
                              HttpSession session) throws RazorpayException, IOException {

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", 19900);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 20));
        orderRequest.put("payment_capture", 1);
        Order order = razorpay.orders.create(orderRequest);

        session.setAttribute("pendingpg", pgEntity);

        String uploadDir = "temp-uploads/";
        File tempfolder = new File(uploadDir);
        if (!tempfolder.exists()) tempfolder.mkdirs();

        List<String> tempPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filepath = Paths.get(uploadDir + filename);
                Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
                tempPaths.add(filepath.toString());
            }
        }
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
        session.setAttribute("tempImagePaths", tempPaths);

        model.addAttribute("razorpayOrderId", order.get("id"));
        model.addAttribute("amount", 19900);
        model.addAttribute("userEmail", user.getName());
        model.addAttribute("roomTitle", pgEntity.getGenderType());
        model.addAttribute("razorpayKey", razorpayKeyId);

        return "pg/razorpay_payment_page_pg";
    }

    @PostMapping("/savepgDetails")
    public String savepgdetails(@RequestParam("razorpay_payment_id") String paymentId,
                                @RequestParam("razorpay_order_id") String orderId,
                                @RequestParam("razorpay_signature") String signature,
                                HttpSession session,
                                Principal principal) throws IOException {

        PGEntity pg = (PGEntity) session.getAttribute("pendingpg");
        if (pg == null) {
            return "redirect:/PG/fillpg?error=sessionExpired";
        }
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
//        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/PG/fillpg?error=userNotFound";
        }

        List<PGImage> imageList = new ArrayList<>();
        List<String> tempImagePaths = (List<String>) session.getAttribute("tempImagePaths");
        if (tempImagePaths != null) {
            for (String pathstr : tempImagePaths) {
                Path path = Paths.get(pathstr);
                if (Files.exists(path)) {
                    PGImage pgImage = new PGImage();
                    pgImage.setImage(Files.readAllBytes(path));
                    pgImage.setPgEntity(pg);
                    imageList.add(pgImage);
                    Files.deleteIfExists(path);
                }
            }
        }

        pg.setUser(user);
        pg.setPgimages(imageList);
        PGEntity savedPg = pgRepository.save(pg);

        PGPaments pgPaments = new PGPaments();
        pgPaments.setRazorpayOrderId(orderId);
        pgPaments.setRazorpayPaymentId(paymentId);
        pgPaments.setRazorpaySignature(signature);
        pgPaments.setStatus("SUCCESS");
        pgPaments.setAmount(199);
        pgPaments.setPaymentTime(new Timestamp(System.currentTimeMillis()));
        pgPaments.setPgEntity(savedPg);
        pGpamentsRepository.save(pgPaments);

        session.removeAttribute("pendingpg");
        session.removeAttribute("tempImagePaths");
        return "redirect:/PG/fillpg?success=true";
    }

    @GetMapping("/uploadpgdetails")
    public String uploadpgdetails(Model model, Principal principal) {
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
//        User email = userRepository.findByEmail(user.getName());
        List<PGEntity> byuser = pgRepository.findByUser(user);
        model.addAttribute("pg", byuser);
        return "pg/user_total_pg";
    }

    @GetMapping("/updatepgavailble/{id}")
    public String updatepgactivity(@PathVariable("id") Long id, Model model) {
        Optional<PGEntity> byId = pgRepository.findById(id);
        if (byId.isPresent()) {
            model.addAttribute("uploadpgactivity", byId.get());
            return "pg/user_total_pg";
        }
        return "redirect:/error";
    }

    @PostMapping("/processpgactivity")
    public String savepgactivity(@ModelAttribute PGEntity pg) {
        Optional<PGEntity> optionalPg = pgRepository.findById(pg.getId());
        if (optionalPg.isPresent()) {
            PGEntity existingPg = optionalPg.get();
            existingPg.setAvailable(pg.getAvailable());
            pgRepository.save(existingPg);
        }
        return "redirect:/PG/uploadpgdetails";
    }

    @PostMapping("/deletePG/{id}")
    public String deletePG(@PathVariable("id") Long id) {
        pgRepository.deleteById(id);
        return "redirect:/PG/uploadpgdetails";
    }

    @GetMapping("/searchPG")
    public String searchpg(Model model,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "") String gender,
                           @RequestParam(defaultValue = "") String roomType,
                           @RequestParam(required = false) Integer minPrice,
                           @RequestParam(required = false) Integer maxPrice,
                           @RequestParam(defaultValue = "0") int page) {

        Page<PGEntity> pg;
        boolean hasSearch = !keyword.isBlank() || !gender.isBlank() || !roomType.isBlank() || minPrice != null || maxPrice != null;
        if (hasSearch) {
            pg = pgRepository.advancedSearch(keyword, gender, roomType, minPrice, maxPrice, PageRequest.of(page, 10));
        } else {
            pg = pgRepository.findAll(PageRequest.of(page, 10));
        }

        model.addAttribute("pgs", pg);
        model.addAttribute("user", pg.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);
        model.addAttribute("roomType", roomType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pg.getTotalPages());
        return "pg/show_pg";
    }

    @GetMapping("/pgImage/{id}")
    public String roomImage(@PathVariable("id") Long id, Model model) {
        List<PGImage> images = pgImageRepository.findByPgEntityId(id);
        model.addAttribute("pgImages", images);
        return "pg/pgimages";
    }

    @GetMapping("/pgimage/{imgId}")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable Long imgId) {
        PGImage image = pgImageRepository.findById(imgId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        byte[] imgData = image.getImage();
        if (imgData == null || imgData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Try detecting MIME type
        String mimeType = "image/jpeg"; // default
        try {
            Path tempFile = Files.createTempFile("img", ".tmp");
            Files.write(tempFile, imgData);
            mimeType = Files.probeContentType(tempFile);
            Files.delete(tempFile);
        } catch (IOException ignored) {
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "image/jpeg"))
                .body(imgData);
    }

    @GetMapping("/pg-payment")
    public String createOrderAndRedirectToPayment(@RequestParam("pgId") Long pgId,
                                                  Principal principal,
                                                  Model model,
                                                  HttpSession session) throws RazorpayException {
        PGEntity pgEntity = pgRepository.findById(pgId).orElseThrow();
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
//        User byEmail = userRepository.findByEmail(principal.getName());
        User pgOwner = pgEntity.getUser();

        session.setAttribute("pgId", pgEntity.getId());
        session.setAttribute("currentUserId", user.getId());
        session.setAttribute("pgOwnerId", pgOwner.getId());

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", 900);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 20));
        orderRequest.put("payment_capture", 1);
        Order order = razorpay.orders.create(orderRequest);

        model.addAttribute("razorpayOrderId", order.get("id"));
        model.addAttribute("amount", 900);
        model.addAttribute("userEmail", user.getName());
        model.addAttribute("pggender", pgEntity.getGenderType());
        model.addAttribute("razorpayKey", razorpayKeyId);
        return "pg/pgsearch_razorpay_payment_page";
    }

    @GetMapping("/searchpgafterpayment")
    public String handlepaymentsuccess(@RequestParam("razorpay_payment_id") String paymentId,
                                       @RequestParam("razorpay_order_id") String orderId,
                                       @RequestParam("razorpay_signature") String signature,
                                       HttpSession session,
                                       Principal principal,
                                       Model model) {

        Long pgId = (Long) session.getAttribute("pgId");
        Long currentUserId = (Long) session.getAttribute("currentUserId");
        Long pgOwnerId = (Long) session.getAttribute("pgOwnerId");

        Optional<PGEntity> pgopt = pgRepository.findById(pgId);
        User user = userRepository.findById(currentUserId).orElse(null);
        User pgowner = userRepository.findById(pgOwnerId).orElse(null);

        if (pgopt.isPresent() && user != null && pgowner != null) {
            searchPGpaments payment = new searchPGpaments();
            payment.setRazorpayOrderId(orderId);
            payment.setRazorpayPaymentId(paymentId);
            payment.setRazorpaySignature(signature);
            payment.setStatus("SUCCESS");
            payment.setAmount(9);
            payment.setPaymentTime(new Timestamp(System.currentTimeMillis()));
            payment.setPgEntity(pgopt.get());
            payment.setUser(user);
            payment.setPgOwner(pgowner);
            searchpgPamentsRepository.save(payment);

            try {
                smssErvices.sendBookingSMS(user.getMobile(), pgopt.get().getPGName());
            } catch (Exception e) {
                System.out.println("SMS failed: " + e.getMessage());
            }

            model.addAttribute("pgs", pgopt.get());
            model.addAttribute("payment", payment);
            return "pg/show_pgpament";
        }
        return "redirect:/PG/searchPG?error=true";
    }
}
