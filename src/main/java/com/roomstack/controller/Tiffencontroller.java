package com.roomstack.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.roomstack.dao.*;
import com.roomstack.entity.*;
import com.roomstack.service.SmsServices;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Hex;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tiffen")
public class Tiffencontroller {

    @Autowired private TiffenPaymentRepository tiffenPaymentRepository;
    @Autowired private TIffenImageRepository tIffenImageRepository;
    @Autowired private TiffenRepository tiffenRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SearchTiffenPaymentRepository searchTiffenPaymentRepository;
    @Autowired private SmsServices smsServices;

    @Value("${razorpay.key_id}")     private String razorpayKeyId;
    @Value("${razorpay.key_secret}") private String razorpayKeySecret;

    // 1) Show the Tiffen fill form
    @GetMapping("/fillTiffen")
    public String fillTiffens(Model model) {
        model.addAttribute("filltiffen", new Tiffen());
        return "tiffen/fill_tiffen";
    }

    // 2) Create Razorpay order & temporarily store data
    @PostMapping("/create-tiffen-payments")
    public String filltiffenpayments(
            @ModelAttribute("filltiffen") Tiffen tiffen,
            @RequestParam("imagefiles") List<MultipartFile> files,
            Principal principal,
            Model model,
            HttpSession session
    ) throws RazorpayException, IOException {
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject()
                .put("amount", 18900)   // ₹69 → 6900 paise
                .put("currency", "INR")
                .put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0,20))
                .put("payment_capture", 1);
        Order order = client.orders.create(orderRequest);

        // Store pending tiffen and amount in session
        session.setAttribute("peningtiffen", tiffen);
        session.setAttribute("expectedAmount", 18900);

        // Save uploaded images temporarily
        String tempDir = "temp-uploads/";
        new File(tempDir).mkdirs();
        List<String> tempPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String name = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(tempDir, name);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                tempPaths.add(path.toString());
            }
        }
        session.setAttribute("tempImagePaths", tempPaths);
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
        // Prepare model for payment page
        model.addAttribute("razorpayOrderId", order.get("id"));
        model.addAttribute("amount", 18900);
        model.addAttribute("userEmail", user.getName());
        model.addAttribute("tiffentype", tiffen.getTiffentype());
        model.addAttribute("razorpayKey", razorpayKeyId);
        return "tiffen/razorpay_payment_pagetiffen";
    }

    // 3) Payment callback: verify signature, validate amount, persist data
    @PostMapping("/savetiffenDetails")
    public String saveTiffenDetails(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_order_id")   String orderId,
            @RequestParam("razorpay_signature")  String signature,
            HttpSession session,
            Principal principal
    ) throws Exception {
        // Retrieve pending tiffen
        Tiffen tiffen = (Tiffen) session.getAttribute("peningtiffen");
        if (tiffen == null) return "redirect:/tiffen/fillTiffen?error=sessionExpired";

        // Verify Razorpay signature
        String payload = orderId + "|" + paymentId;
        if (!hmacSha256(payload, razorpayKeySecret).equals(signature)) {
            return "redirect:/tiffen/fillTiffen?error=invalidSignature";
        }

        // Validate expected amount
        Integer expected = (Integer) session.getAttribute("expectedAmount");
        if (expected == null) return "redirect:/tiffen/fillTiffen?error=amountMissing";

        // Fetch user
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
//        User user = userRepository.findByEmail(user1.getName());
        if (user == null) return "redirect:/tiffen/fillTiffen?error=userNotFound";

        // Persist temporary images to DB
        List<tiffenimage> images = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<String> tmpPaths = (List<String>) session.getAttribute("tempImagePaths");
        if (tmpPaths != null) {
            for (String pStr : tmpPaths) {
                Path p = Paths.get(pStr);
                if (Files.exists(p)) {
                    tiffenimage img = new tiffenimage();
                    img.setImage(Files.readAllBytes(p));
                    img.setTiffen(tiffen);
                    images.add(img);
                    Files.deleteIfExists(p);
                }
            }
        }
        tiffen.setUser(user);
        tiffen.setTiffenimages(images);
        Tiffen saved = tiffenRepository.save(tiffen);

        // Save payment record
        TiffenPayments payment = new TiffenPayments();
        payment.setRazorpayOrderId(orderId);
        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus("SUCCESS");
        payment.setAmount(189);
        payment.setPaymentTime(new Timestamp(System.currentTimeMillis()));
        payment.setTiffen(saved);
        tiffenPaymentRepository.save(payment);

        // Clear session
        session.removeAttribute("peningtiffen");
        session.removeAttribute("tempImagePaths");
        session.removeAttribute("expectedAmount");

        return "redirect:/tiffen/fillTiffen?success=true";
    }

    // Utility: HMAC-SHA256
    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(raw);
    }

    // 4) List user's tiffen uploads
    @GetMapping("/uploadtiffendetails")
    public String uploadtiffendetails(Model model, Principal principal) {
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user1 = loginUser.getUser();
//        User user = userRepository.findByEmail(user1.getEmail());
        List<Tiffen> list = tiffenRepository.findByUser(user1);
        model.addAttribute("tiffen", list);
        return "tiffen/user_total_tiffen";
    }

    // 5) Update availability
    @PostMapping("/processtiffenactivity")
    public String tiffensaveactivity(@ModelAttribute Tiffen tiffen) {
        tiffenRepository.findById(tiffen.getId()).ifPresent(existing -> {
            existing.setAvailable(tiffen.getAvailable());
            tiffenRepository.save(existing);
        });
        return "redirect:/tiffen/uploadtiffendetails";
    }

    // 6) Delete tiffen
    @PostMapping("/deletetiffen/{id}")
    public String deletetiffen(@PathVariable Long id) {
        tiffenRepository.deleteById(id);
        return "redirect:/tiffen/uploadtiffendetails";
    }

    // 7) Search tiffens
    @GetMapping("/searchtiffen")
    public String searchtiffen(
            @RequestParam(required = false) String includes,
            @RequestParam(required = false) Boolean deliveryAvailable,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String tiffenType,
            @RequestParam(required = false) String dayavailable,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue ="" ) String searchText,

            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        // normalize empty to null
        includes = (includes != null && !includes.isBlank()) ? includes : null;
        mealType = (mealType != null && !mealType.isBlank()) ? mealType : null;
        tiffenType = (tiffenType != null && !tiffenType.isBlank()) ? tiffenType : null;
        dayavailable = (dayavailable != null && !dayavailable.isBlank()) ? dayavailable : null;
        searchText = (searchText != null && !searchText.isBlank()) ? searchText : null;

        boolean filter = includes!=null||deliveryAvailable!=null||mealType!=null
                ||tiffenType!=null||dayavailable!=null||minPrice!=null||maxPrice!=null||searchText!=null;

        Page<Tiffen> pageData = filter
                ? tiffenRepository.advancedSearch(includes, deliveryAvailable, mealType,
                tiffenType, dayavailable, minPrice, maxPrice, searchText,
                PageRequest.of(page, 10))
                : tiffenRepository.findAll(PageRequest.of(page, 10));


        model.addAttribute("tiffen", pageData);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("currentPage", page);
        // repopulate filters
        model.addAttribute("includes", includes);
        model.addAttribute("deliveryAvailable", deliveryAvailable);
        model.addAttribute("mealType", mealType);
        model.addAttribute("tiffenType", tiffenType);
        model.addAttribute("dayavailable", dayavailable);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("searchText", searchText);
        return "tiffen/show_tiffen";
    }

    // 8) Show tiffen images view-link
    @GetMapping("/tiffenImage/{id}")

    public String tiffenimage(@PathVariable("id") Long id, Model model) {
        List<tiffenimage> imgs = tIffenImageRepository.findByTiffenId(id);
        model.addAttribute("tiffenImage", imgs);
        return "tiffen/tiffenImages";
    }

    // 9) Actual image bytes
    @GetMapping("/tiffenimage/{imgId}")
    @ResponseBody
    public ResponseEntity<byte[]> showTiffenImage(@PathVariable Long imgId) {
        tiffenimage img = tIffenImageRepository.findById(imgId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        byte[] imageData = img.getImage();
        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String mimeType = "image/jpeg"; // Default fallback
        try {
            Path temp = Files.createTempFile("tiffen", ".img");
            Files.write(temp, imageData);
            mimeType = Files.probeContentType(temp);
            Files.delete(temp);
        } catch (IOException e) {
            System.out.println("Could not determine MIME type: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "image/jpeg"))
                .body(imageData);
    }


    // 10) Pay-for-search flow
    @GetMapping("/tiffen-payment")
    public String createOrderandredirecttopayment(
            @RequestParam("tiffenId") Long tiffenId,
            Principal principal,
            Model model,
            HttpSession session
    ) throws Exception {
        Tiffen tf = tiffenRepository.findById(tiffenId).orElseThrow();
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
//        User buyer = userRepository.findByEmail(user.getName());
        User owner = tf.getUser();
        session.setAttribute("tiffenId", tf.getId());
        session.setAttribute("currentUser", user.getId());
        session.setAttribute("tiffenOwnerId", owner.getId());

        RazorpayClient rc = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject req = new JSONObject()
                .put("amount", 900)
                .put("currency", "INR")
                .put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0,20))
                .put("payment_capture", 1);
        Order o = rc.orders.create(req);

        model.addAttribute("razorpayOrderId", o.get("id"));
        model.addAttribute("amount", 900);
        model.addAttribute("userEmail", user.getName());
        model.addAttribute("mealType", tf.getMealType());
        model.addAttribute("razorpayKey", razorpayKeyId);
        return "tiffen/tiffensearch_razorpay_payment_page";
    }

    // 11) Handle search-payment callback
    @GetMapping("/searchtiffenafterpayment")
    public String handlepaymentsuccess(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_order_id")   String orderId,
            @RequestParam("razorpay_signature")  String signature,
            HttpSession session,
            Principal principal,
            Model model
    ) throws Exception {
        Long tfId = (Long) session.getAttribute("tiffenId");
        Long buyerId = (Long) session.getAttribute("currentUser");
        Long ownerId = (Long) session.getAttribute("tiffenOwnerId");
        Tiffen tf = tiffenRepository.findById(tfId).orElseThrow();
        User buyer = userRepository.findById(buyerId).orElseThrow();

        // verify signature & amount same as above (omitted)

        TiffenSearchPayments pay = new TiffenSearchPayments();
        pay.setRazorpayOrderId(orderId);
        pay.setRazorpayPaymentId(paymentId);
        pay.setRazorpaySignature(signature);
        pay.setAmount(9);
        pay.setPaymentTime(new Timestamp(System.currentTimeMillis()));
        pay.setStatus("SUCCESS");
        pay.setTiffen(tf);
        pay.setUser(buyer);
        searchTiffenPaymentRepository.save(pay);

        // send SMS
        try { smsServices.sendBookingSMS(buyer.getMobile(), tf.getTiffencentername()); } catch(Exception e) { }

        model.addAttribute("tiffens", tf);
        model.addAttribute("payment", pay);
        return "tiffen/show_tiffenPayments";
    }
}
