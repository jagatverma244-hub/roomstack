package com.roomstack.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.roomstack.dao.*;
import com.roomstack.entity.*;
import com.roomstack.service.SmsServices;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import java.util.*;

@Controller
@RequestMapping("/User")
public class UserController {
    @Autowired private UserRepository userRepository;
    @Autowired private LoginsRepository loginRepository;
    @Autowired private BCryptPasswordEncoder bc;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ImageRepository imageRepository;
    @Autowired private PamentRepository pamentRepository;
    @Autowired private SearchRoomPaymentRepository searchRoomPaymentRepository;
    @Autowired private SmsServices smssErvices;

    @Value("${razorpay.key_id}") private String razorpayKeyId;
    @Value("${razorpay.key_secret}") private String razorpayKeySecret;


    @GetMapping("/newUser")
    public String showUserRegistrationForm(Model model,
                                           @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("newuser", new User());
        if ("emailmismatch".equals(error)) {
            model.addAttribute("errorMessage", "Username and Email must be the same.");
        } else if ("emailtaken".equals(error)) {
            model.addAttribute("errorMessage", "Email is already registered.");
        } else if ("usertaken".equals(error)) {
            model.addAttribute("errorMessage", "Username is already taken.");
        }
        return "new_user";
    }

    @PostMapping("/save-user")
    public String useSave(@Valid @ModelAttribute("newuser") User user,
                          @RequestParam("file") MultipartFile file,
                          Model model,

                          @RequestParam String username,
                          @RequestParam String password) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            username = user.getEmail();
        }
        if (!user.getEmail().equalsIgnoreCase(username)) {
            return "redirect:/User/newUser?error=emailmismatch";
        }
        if (userRepository.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            return "redirect:/User/newUser?error=emailtaken";
        }
        if (loginRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return "redirect:/User/newUser?error=usertaken";
        }
        Login login = new Login();
        login.setRole("ROLE_USER");
        login.setUsername(username);
        login.setPassword(bc.encode(password));
        loginRepository.save(login);

        user.setProfileImage(file.getBytes());
        user.setCreatedAt(new Date());


        user.setLogin(login);
        userRepository.save(user);

        return "redirect:/login?success=true";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();

        if (user == null) {
            System.out.println("Principal: " + principal.getName());
            return "fail";
        }

        String base64Image = "";
        if (user.getProfileImage() != null) {
            base64Image = Base64.getEncoder().encodeToString(user.getProfileImage());
        }

        int totalRooms = roomRepository.countByUserId(user.getId());

        model.addAttribute("profile", user);
        model.addAttribute("profileImageBase64", base64Image);
        model.addAttribute("totalRoomCount", totalRooms);

        return "profile";
    }


    @GetMapping("/updateprofile/{id}")
    public String updateProfile(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        model.addAttribute("updateprofile", user);
//        if (user != null && user.getProfileImage() != null) {
//            String imageBase64 = Base64.getEncoder().encodeToString(user.getProfileImage());
////            model.addAttribute("imageBase64", imageBase64);
//        }
        return "update_profile";
    }

    @PostMapping("/updateprocess")
    public String updateProcess(@ModelAttribute User user,
                                @RequestParam("file") MultipartFile file,
                                Model model) {
        try {
            if (!file.isEmpty()) {
                user.setProfileImage(file.getBytes());
            } else {
                User existingUser = userRepository.findById(user.getId()).orElse(null);
                if (existingUser != null) {
                    user.setProfileImage(existingUser.getProfileImage());
                }
            }
            userRepository.save(user);
            return "redirect:/User/profile";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/fillRoom")
    public String fillRoomDetail(Model model) {
        Room room = new Room();
        room.setLocation(new Location());
        model.addAttribute("fillRoomDetails", room);
        return "fill_room";
    }

    @PostMapping("/create-room-payment")
    public String createRoomPayment(@ModelAttribute("fillRoomDetails") Room room,
                                    @RequestParam("imageFiles") List<MultipartFile> files,
                                    Principal principal,
                                    Model model,
                                    HttpSession session) throws RazorpayException, IOException {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", 14900);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 20));
        orderRequest.put("payment_capture", 1);
        Order order = razorpay.orders.create(orderRequest);

        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
        if (user == null) return "redirect:/User/fillRoom?error=userNotFound";

        session.setAttribute("pendingRoom", room);
        String uploadDir = "temp-uploads/";
        File tempFolder = new File(uploadDir);
        if (!tempFolder.exists()) tempFolder.mkdirs();
        List<String> tempPaths = new ArrayList<>();
        for (MultipartFile f : files) {
            if (!f.isEmpty()) {
                String fn = UUID.randomUUID() + "_" + f.getOriginalFilename();
                Path fp = Paths.get(uploadDir + fn);
                Files.copy(f.getInputStream(), fp, StandardCopyOption.REPLACE_EXISTING);
                tempPaths.add(fp.toString());
            }
        }
        session.setAttribute("tempImagePaths", tempPaths);

        model.addAttribute("razorpayOrderId", order.get("id"));
        model.addAttribute("amount", 14900);
        model.addAttribute("userEmail", user.getName());
        model.addAttribute("roomTitle", room.getTitle());
        model.addAttribute("razorpayKey", razorpayKeyId);
        return "razorpay_payment_page";
    }

    @PostMapping("/saveroomdetails")
    public String saveRoomAfterPayment(@RequestParam("razorpay_payment_id") String paymentId,
                                       @RequestParam("razorpay_order_id") String orderId,
                                       @RequestParam("razorpay_signature") String signature,
                                       HttpSession session,
                                       Principal principal) throws IOException {
        Room room = (Room) session.getAttribute("pendingRoom");
        if (room == null) return "redirect:/User/fillRoom?error=sessionExpired";

        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
        if (user == null) return "redirect:/User/fillRoom?error=userNotFound";

        room.setUser(user);
        if (room.getLocation() != null) room.getLocation().setRoom(room);

        List<Image> imageList = new ArrayList<>();
        List<String> tempImagePaths = (List<String>) session.getAttribute("tempImagePaths");
        if (tempImagePaths != null) {
            for (String p : tempImagePaths) {
                Path path = Paths.get(p);
                if (Files.exists(path)) {
                    Image img = new Image();
                    img.setImage(Files.readAllBytes(path));
                    img.setRoom(room);
                    imageList.add(img);
                    Files.deleteIfExists(path);
                }
            }
        }
        room.setImages(imageList);
        Room savedRoom = roomRepository.save(room);

        Roompayments payment = new Roompayments();
        payment.setRazorpayOrderId(orderId);
        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus("SUCCESS");
        payment.setAmount(149);
        payment.setPaymentTime(new Timestamp(System.currentTimeMillis()));
        payment.setRoom(savedRoom);
        pamentRepository.save(payment);

        session.removeAttribute("pendingRoom");
        session.removeAttribute("tempImagePaths");
        return "redirect:/User/fillRoom?success=true";
    }


    @GetMapping("/searchroom")
    public String searchroom(Model model,
                             @RequestParam(defaultValue = "") String keyword,
                             @RequestParam(defaultValue = "0") int page) {
        Page<Room> rom;
        if (!keyword.isBlank()) {
            rom = roomRepository.searchRooms(keyword, PageRequest.of(page, 10));
            model.addAttribute("keyword", keyword);
        } else {
            rom = roomRepository.findAll(PageRequest.of(page, 10));
            model.addAttribute("keyword", "");
        }
        model.addAttribute("rooms", rom);
        model.addAttribute("user", rom.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rom.getTotalPages());
        return "show_room";
    }



    @PostMapping("/room-payment")
    public String createSearchRoomPayment(
            @RequestParam("roomId") Long roomId,
            Principal principal,
            Model model,
            HttpSession session) throws RazorpayException {
        Room room = roomRepository.findById(roomId).orElseThrow();
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
//        User currentUser = userRepository.findByEmail(principal.getName());
        User roomOwner = room.getUser();
        session.setAttribute("roomId", room.getId());
        session.setAttribute("currentUserId", user.getId());
        session.setAttribute("roomOwnerId", roomOwner.getId());

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", 900);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0,20));
        orderRequest.put("payment_capture", 1);
        Order order = razorpay.orders.create(orderRequest);

        model.addAttribute("razorpayOrderId", order.get("id"));
        model.addAttribute("amount", 900);
        model.addAttribute("userEmail", user.getName());
        model.addAttribute("roomTitle", room.getTitle());
        model.addAttribute("razorpayKey", razorpayKeyId);
        return "roomsearch_razorpay_payment_page";
    }

    @GetMapping("/searchroomafterpayment")
    public String handlePaymentSuccess(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_order_id") String orderId,
            @RequestParam("razorpay_signature") String signature,
            HttpSession session,
            Principal principal,
            Model model) {
        Long roomId = (Long) session.getAttribute("roomId");
        Long userId = (Long) session.getAttribute("currentUserId");
        Long ownerId = (Long) session.getAttribute("roomOwnerId");
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        User user = userRepository.findById(userId).orElse(null);
        User roomOwner = userRepository.findById(ownerId).orElse(null);

        if (roomOpt.isPresent() && user != null && roomOwner != null) {
            searchroompaments payment = new searchroompaments();
            payment.setRazorpayOrderId(orderId);
            payment.setRazorpayPaymentId(paymentId);
            payment.setRazorpaySignature(signature);
            payment.setStatus("SUCCESS");
            payment.setAmount(9);
            payment.setPaymentTime(new Timestamp(System.currentTimeMillis()));
            payment.setRoom(roomOpt.get());
            payment.setUser(user);
            payment.setRoomOwner(roomOwner);
            searchRoomPaymentRepository.save(payment);

            Room bookedRoom = roomOpt.get();
            bookedRoom.setAvailable(false);
            roomRepository.save(bookedRoom);

            try {
                smssErvices.sendBookingSMS(user.getMobile(), bookedRoom.getTitle());
            } catch (Exception e) {
                System.out.println("SMS failed: " + e.getMessage());
            }

            model.addAttribute("rooms", roomOpt.get());
            model.addAttribute("payment", payment);
            return "show_roompament";
        }
        return "redirect:/User/searchroom?error=true";
    }

    @GetMapping("/uploadroomdeail")
    public String uploadRoomDetail(Model model, Principal principal) {
        Login loginUser = (Login) ((Authentication) principal).getPrincipal();
        User user = loginUser.getUser();
        if (user == null) return "redirect:/login";
        model.addAttribute("rooms", roomRepository.findByUser(user));
        return "user_Total_Rooms";
    }
    @PostMapping("/deleteRoom/{id}")
    public String deleteRoom(@PathVariable("id") Long id) {
        roomRepository.deleteById(id);
        return "redirect:/User/uploadroomdeail";
    }

    @GetMapping("/updateroomactivite/{id}")
    public String updateRoomActivity(@PathVariable("id") Long id, Model model) {
        Room room = roomRepository.findById(id).orElse(null);
        model.addAttribute("updateroomactivity", room);
        return "user_Total_Rooms";
    }

    @PostMapping("/processroomactivicty")
    public String saveUserActivity(@ModelAttribute Room room) {
        Room existing = roomRepository.findById(room.getId()).orElse(null);
        if (existing != null) {
            existing.setAvailable(room.getAvailable());
            roomRepository.save(existing);
        }
        return "redirect:/User/uploadroomdeail";
    }

    @GetMapping("/roomImage/{id}")
    public String roomImage(@PathVariable("id") Long id, Model model) {
        List<Image> images = imageRepository.findByRoomId(id);
        model.addAttribute("roomImages", images);
        return "roomImages";
    }
    @GetMapping("/roomoneimage/{imgId}")
    @ResponseBody
    public ResponseEntity<byte[]> showRoomImage(@PathVariable Long imgId) {
        Image image = imageRepository.findById(imgId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        byte[] imageData = image.getImage();
        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String mimeType = "image/jpeg"; // default fallback
        try {
            Path temp = Files.createTempFile("roomimg", ".tmp");
            Files.write(temp, imageData);
            mimeType = Files.probeContentType(temp);
            Files.delete(temp);
        } catch (IOException e) {
            System.out.println("Could not determine image type: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "image/jpeg"))
                .body(imageData);
    }


    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] serveImage(@PathVariable("id") Long id) {
        return imageRepository.findById(id).map(Image::getImage).orElse(null);
    }

    @GetMapping("/allpaymentcancelled")
    public String showCancelledPage() {
        return "allpaymentcancelled";
    }


}