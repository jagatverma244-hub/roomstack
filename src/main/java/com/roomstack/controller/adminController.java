package com.roomstack.controller;

import com.roomstack.dao.*;
import com.roomstack.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class adminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private PGRepository pgRepository;
    @Autowired
    private PGpamentsRepository pGpamentsRepository;;
    @Autowired
    private SearchPGPamentsRepository searchPGPamentsRepository;
    @Autowired
    private PGImageRepository pgImageRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private PamentRepository pamentRepository;
    @Autowired
    private SearchRoomPaymentRepository searchRoomPaymentRepositor;

    @Autowired
    private SearchTiffenPaymentRepository searchTiffenPaymentRepository;
    @Autowired
    private TIffenImageRepository tIffenImageRepository;
    @Autowired
    private TiffenRepository tiffenRepository;
    @Autowired
    private TiffenPaymentRepository tiffenPaymentRepository;


    @GetMapping("/adminHome")
    public String adminhome(Model model){
        List<Room> allroom = roomRepository.findAll();
        List<User> alluser = userRepository.findAll();
        List<PGEntity> allpg = pgRepository.findAll();
         List<Tiffen> alltiffen = tiffenRepository.findAll();
         model.addAttribute("alltiffen",alltiffen);
        model.addAttribute("allpgs",allpg);
        model.addAttribute("alluser",alluser);
        model.addAttribute("rooms",allroom);
        return"admin/adminhome";

    }
    @GetMapping("/roomImage/{id}")
    public String roomImage(@PathVariable("id") Long id, Model model) {
        List<Image> images = imageRepository.findByRoomId(id);
        model.addAttribute("roomImages", images);
        return "admin/roomimageadmin";
    }
    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] serveImage(@PathVariable("id") Long id) {
        Optional<Image> image = imageRepository.findById(id);
        return image.map(Image::getImage).orElse(null);
    }

    @GetMapping("/deleteroom/{id}")
    @Transactional
    public String deleteroom(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return roomRepository.findById(id).map(room -> {
            roomRepository.delete(room);
            redirectAttributes.addFlashAttribute("message", "PG deleted successfully!");
            return "redirect:/admin/oneuserdetails/"+room.getUser().getId();
//            return "redirect:/admin/viewuser/" + pg.getUser().getId();

        }).orElse("redirect:/admin/allusers");
    }

    @GetMapping("/allusers")
    public String userdetails(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0" )int page){
        Page<User> user;
        if(keyword != null && ! keyword.isBlank()){
            user = userRepository.searchUser(keyword, PageRequest.of(page, 10));
            model.addAttribute("keyword",keyword);

        }else {
         user =   userRepository.findAll(PageRequest.of(page,10));
            model.addAttribute("keyword","");
        }

//        List<User> all = userRepository.findAll();
        List<Room> allroom = roomRepository.findAll();
        List<PGEntity> allpg = pgRepository.findAll();
        model.addAttribute("allpg",allpg);
        model.addAttribute("rooms",allroom);
        model.addAttribute("users",user.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", user.getTotalPages());

        return "admin/user_list";
    }


    @GetMapping("/oneuserdetails/{id}")
    public String oneuserdetails(@PathVariable("id") Long id, Model model) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Room> rooms = roomRepository.findByUserId(id);// or user.getRooms() if mapped
            List<PGEntity> pg = pgRepository.findByUserId(id);
            List<Tiffen> tiffen = tiffenRepository.findByUserId(id);
            model.addAttribute("tiffen",tiffen);

model.addAttribute("pg",pg);
            model.addAttribute("user", user);
            model.addAttribute("rooms", rooms);
        } else {
            model.addAttribute("error", "User not found.");
        }

     return "admin/viewuserdetails"; // ✅ Correct spelling

    }


    @GetMapping("/deleteUser/{id}")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok("User deleted successfully");
        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
        );
    }





    @GetMapping("/pamentsdetails")
    public String allpaments(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0")int page){
Page<Roompayments> paments;
if(keyword!= null && !keyword.isBlank()){
    paments = pamentRepository.searchPayments(keyword,PageRequest.of(page,10));
    model.addAttribute("keyword",keyword);

}else{
    paments = pamentRepository.findAll(PageRequest.of(page,10));
    model.addAttribute("keyword","");
}
//        List<Roompayments> all = pamentRepository.findAll();
        model.addAttribute("allpaments",paments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paments.getTotalPages());


        return "admin/pamentsdetails";

    }

    @GetMapping("/bookingsearchpaments")
    public String allsearchpaments(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0")int page){
        Page<searchroompaments> paments;
        if(keyword!= null && !keyword.isBlank()){
            paments = searchRoomPaymentRepositor.bookingsearchPayments(keyword,PageRequest.of(page,10));
            model.addAttribute("keyword",keyword);

        }else{
            paments = searchRoomPaymentRepositor.findAll(PageRequest.of(page,10));
            model.addAttribute("keyword","");
        }
//        List<Roompayments> all = pamentRepository.findAll();
        model.addAttribute("allpaments",paments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paments.getTotalPages());


        return "admin/bookingpamentsdetails";

    }
//    ----------------pg----------------------------
    @GetMapping("/pgImage/{id}")
    public String pgImage(@PathVariable("id") Long id, Model model) {
        List<PGImage> images = pgImageRepository.findByPgEntityId(id);
        model.addAttribute("pgImages", images);
        return "admin/pgimageadmin";
    }
    @GetMapping("/pgimage/{id}")
    @ResponseBody
    public byte[] servepgImage(@PathVariable("id") Long id) {
        Optional<PGImage> image = pgImageRepository.findById(id);
        return image.map(PGImage::getImage).orElse(null);
    }
    @GetMapping("/pgpamentsdetails")
    public String pgallpaments(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0")int page){
        Page<PGPaments> paments;
        if(keyword!= null && !keyword.isBlank()){
            paments = pGpamentsRepository.pgsearchPayments(keyword,PageRequest.of(page,10));
            model.addAttribute("keyword",keyword);

        }else{
            paments = pGpamentsRepository.findAll(PageRequest.of(page,10));
            model.addAttribute("keyword","");
        }
//        List<Roompayments> all = pamentRepository.findAll();
        model.addAttribute("allpgpaments",paments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paments.getTotalPages());


        return "admin/pg_pamentsdetails";

    }

    @GetMapping("/pgbookingsearchpaments")
    public String pgallsearchpaments(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0")int page){
        Page<searchPGpaments> paments;
        if(keyword!= null && !keyword.isBlank()){
            paments = searchPGPamentsRepository.pgbookingsearchPayments(keyword,PageRequest.of(page,10));
            model.addAttribute("keyword",keyword);

        }else{
            paments = searchPGPamentsRepository.findAll(PageRequest.of(page,10));
            model.addAttribute("keyword","");
        }
//        List<Roompayments> all = pamentRepository.findAll();
        model.addAttribute("allpgpaments",paments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paments.getTotalPages());


        return "admin/pgbookingdetails_payment";

    }
    @GetMapping("/deletepg/{id}")
    @Transactional
    public String deletepg(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return pgRepository.findById(id).map(pg -> {
            pgRepository.delete(pg);
            redirectAttributes.addFlashAttribute("message", "PG deleted successfully!");
            return "redirect:/admin/oneuserdetails/"+pg.getUser().getId();
//            return "redirect:/admin/viewuser/" + pg.getUser().getId();

        }).orElse("redirect:/admin/allusers");
    }


//    ---------------------------------------------------------------pg complate----------------------------------




    @GetMapping("/tiffenImage/{id}")
    public String tiffenImage(@PathVariable("id") Long id, Model model) {
        List<tiffenimage> images = tIffenImageRepository.findByTiffenId(id);
        model.addAttribute("tiffenImages", images);
        return "admin/tiffenimageadmin";
    }
    @GetMapping("/tiffenimage/{id}")
    @ResponseBody
    public byte[] servetiffenImage(@PathVariable("id") Long id) {
        Optional<tiffenimage> image = tIffenImageRepository.findById(id);
        return image.map(tiffenimage::getImage).orElse(null);
    }
    @GetMapping("/tiffenpamentsdetails")
    public String tiffenallpaments(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0")int page){
        Page<TiffenPayments> paments;
        if(keyword!= null && !keyword.isBlank()){
            paments = tiffenPaymentRepository.TiffensearchPayments(keyword,PageRequest.of(page,10));
            model.addAttribute("keyword",keyword);

        }else{
            paments = tiffenPaymentRepository.findAll(PageRequest.of(page,10));
            model.addAttribute("keyword","");
        }
//        List<Roompayments> all = pamentRepository.findAll();
        model.addAttribute("alltiffenpaments",paments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paments.getTotalPages());


        return "admin/tiffen_pamentsdetails";

    }

    @GetMapping("/tiffenbookingsearchpaments")
    public String tiffenallsearchpaments(Model model,@RequestParam(defaultValue = "") String keyword,@RequestParam(defaultValue = "0")int page){
        Page<TiffenSearchPayments> paments;
        if(keyword!= null && !keyword.isBlank()){
            paments = searchTiffenPaymentRepository.tiffenbookingsearchPayments(keyword,PageRequest.of(page,10));
            model.addAttribute("keyword",keyword);

        }else{
            paments = searchTiffenPaymentRepository.findAll(PageRequest.of(page,10));
            model.addAttribute("keyword","");
        }
//        List<Roompayments> all = pamentRepository.findAll();
        model.addAttribute("alltiffenpaments",paments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paments.getTotalPages());


        return "admin/tiffenbookingdetails_payment";

    }
    @GetMapping("/deletetiffen/{id}")
    @Transactional
    public String deletetiffen(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        return tiffenRepository.findById(id).map(tiffen -> {
            tiffenRepository.delete(tiffen);
            redirectAttributes.addFlashAttribute("message", "tiffen deleted successfully!");
            return "redirect:/admin/oneuserdetails/"+tiffen.getUser().getId();
//            return "redirect:/admin/viewuser/" + pg.getUser().getId();

        }).orElse("redirect:/admin/allusers");
    }
}