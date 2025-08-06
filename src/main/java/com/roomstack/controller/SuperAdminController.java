package com.roomstack.controller;

import com.roomstack.dao.LoginsRepository;
import com.roomstack.dao.RoomRepository;
import com.roomstack.dao.UserRepository;
import com.roomstack.entity.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private LoginsRepository loginsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Login> allUsers = loginsRepository.findAll();
        model.addAttribute("users", allUsers);
        return "superadmin_dashboard";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        var optionalUser = loginsRepository.findById(id);

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            if ("ROLE_SUPERADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "❌ Cannot delete a SUPERADMIN user.");
            } else {
                loginsRepository.delete(user);
                redirectAttributes.addFlashAttribute("message", "✅ User deleted successfully.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ User not found.");
        }

        return "redirect:/superadmin/dashboard";
    }



    @PostMapping("/changerole/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam("role") String role, RedirectAttributes redirectAttributes) {
        Login user = loginsRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(role);
            loginsRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "✅ Role updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ User not found.");
        }
        return "redirect:/superadmin/dashboard";
    }

    @PostMapping("/delete-schema")
    @Transactional
    public String deleteSchema(RedirectAttributes redirectAttributes) {
        try {
            roomRepository.deleteAll();
            userRepository.deleteAll();
            loginsRepository.deleteAll();
            redirectAttributes.addFlashAttribute("message", "🗑 Entire schema data deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Failed to delete schema.");
        }
        return "redirect:/superadmin/dashboard";
    }
}
