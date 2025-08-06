package com.roomstack.controller;

import com.roomstack.dao.LoginsRepository;
import com.roomstack.entity.Login;
import com.roomstack.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/pforgot")
public class forgotPassword {
    private final Random re = new Random();
    @Autowired
    private BCryptPasswordEncoder bc;
    @Autowired
    private EmailService  emailService;
    @Autowired
    private LoginsRepository loginsRepository;

    @GetMapping("/forgot")
    public String forgotemail(){
        return "forgot_email";
    }

    @GetMapping("/oldtonew")
    public String showOldToNewPage(HttpSession session, Model model)
    {
        Object error = session.getAttribute("error");
        Object formSubmitted = session.getAttribute("formSubmitted");
        if(formSubmitted !=null && error !=null){
            model.addAttribute("error",error);
            model.addAttribute("formSubmitted",true);

        }
        session.removeAttribute("error");
        session.removeAttribute("formSubmitted");
        return "oldtonewpassword";
    }
    @PostMapping("/oldtonewpassword")
    public String oldtonew(@RequestParam("email") String email,@RequestParam("oldpassword")String oldpassword,HttpSession session){
        Optional<Login> optionalLogin = loginsRepository.findByUsernameIgnoreCase(email);
if(optionalLogin.isEmpty()){
    session.setAttribute("error","Email not registered");
    session.setAttribute("formSubmitted",true);
    return "redirect:/pforgot/oldtonew";
}
Login userlog = optionalLogin.get();
if(bc.matches(oldpassword,userlog.getPassword())){
    session.setAttribute("email",email);
    return "password_change";
}
else {
    session.setAttribute("error","old password is incorrect");
    session.setAttribute("formSubmitted",true);
    return "redirect:/pforgot/oldtonew";
}

    }

//    send otp

    @PostMapping("/send-OTP")
    public String sendotp(@RequestParam("email") String email,HttpSession session){
        int otp = re.nextInt(999999);
        String subject = "OTP form ResetPassword";
        String message ="<div style='border:1px solid #e2e2e2;padding:20px'>"
                + "<h1>OTP = <b>" + otp + "</b></h1></div>";
        boolean sendemail = emailService.sendemail(subject,message,email);
        if(sendemail){
            session.setAttribute("myOTP",otp);
            session.setAttribute("email",email);
            session.setAttribute("otpSent",true);
            session.removeAttribute("otpFailed");
            return "verify_OTP";
        }
        else{
            session.setAttribute("message","failed to send OTP.Check your email..");
            return "forgot_email";
        }

    }
    @PostMapping("/verify-otp")
    public  String verifyotp(@RequestParam("otp") int ootp,HttpSession session){
        Integer myotp = (Integer) session.getAttribute("myOTP");
    String email = (String) session.getAttribute("email");
    if(myotp!=null && myotp ==ootp){
        Optional<Login> optionalLogin = loginsRepository.findByUsernameIgnoreCase(email);

if(optionalLogin.isEmpty())
{
    session.setAttribute("message","User does not exists with this email");
    return "forgot_email";

    }
session.removeAttribute("myOTP");
session.removeAttribute("otpFailed");
session.removeAttribute("otpSent");
return "password_change";


    }else{
        session.setAttribute("otpFailed",true);
        return "verify_OTP";
    }
    }

@PostMapping("/change-password")
    public String changepassword(@RequestParam("newpassword") String newpassword,
                                 @RequestParam("conformpassword") String conformpassword,HttpSession session){
        String email = (String) session.getAttribute("email");
        if(email ==null){
            session.setAttribute("error","Session expired or email missing");
            return "redirect:/pforgot/forgot";

        }
    Optional<Login> optionalLogin = loginsRepository.findByUsernameIgnoreCase(email);

    if(optionalLogin.isPresent()){
        Login login = optionalLogin.get();
        if(newpassword.equals(conformpassword)){
            login.setPassword(bc.encode(newpassword));
            loginsRepository.save(login);
            session.removeAttribute("email");
            session.removeAttribute("myOTP");
            return "redirect:/login?change=password changed successfully..";

        }
        else {
            session.setAttribute("error","password and confirm password do not match");
            return "password_change";
        }

    }
    else {
        session.setAttribute("error","Something went wrong. Try again.");
        return "redirect:/pforgot/forgot";
    }

    }

}
