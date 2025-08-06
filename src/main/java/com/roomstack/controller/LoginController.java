package com.roomstack.controller;

import com.roomstack.config.IpUtils;
import com.roomstack.entity.Login;
import com.roomstack.service.JwtService;
import com.roomstack.service.UserSessionService;
import com.roomstack.dao.LoginsRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserSessionService sessionService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LoginsRepository loginsRepository;

    // GET: Show login page or redirect if already logged in
    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {

        if (logout != null) {
            model.addAttribute("logout", true);
        }

        if (jwtService.hasValidAccessToken(request) || jwtService.hasValidRefreshToken(request)) {
            return "redirect:/deshboard";
        }

        return "loginfile"; // Thymeleaf template
    }

    // POST: Handle login
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletResponse response,
                          Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Login user = loginsRepository.findByUsernameIgnoreCase(username).orElseThrow();

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(user, 2 * 60);       // 2 hours in minutes
            String refreshToken = jwtService.generateRefreshToken(user, 5);          // 5 days
            // Set cookies
            Cookie accessCookie = new Cookie("accessToken", accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(2 * 60 * 60); // 1 min

            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(5 * 24 * 60 * 60); // 5 mins

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);

            return "redirect:/deshboard";

        } catch (AuthenticationException ex) {
            model.addAttribute("error", true);
            return "loginfile";
        }
    }

    // GET: Dashboard redirection logic
    @GetMapping("/deshboard")
    public String deshboard(Authentication auth, HttpServletRequest request) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        String username = auth.getName();
        String ip = IpUtils.getClientIP(request);
        String sessionId = request.getSession().getId();

        System.out.println(username + " ------------ " + ip + " " + sessionId);

        sessionService.storeUser(username, ip, sessionId);

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/adminHome";
        } else if (role.equals("ROLE_SUPERADMIN")) {
            return "redirect:/superadmin/dashboard";
        } else {

            return "redirect:/User/profile";
        }
    }

    // GET: Logout and delete JWT cookies
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie access = new Cookie("accessToken", null);
        access.setMaxAge(0);
        access.setPath("/");

        Cookie refresh = new Cookie("refreshToken", null);
        refresh.setMaxAge(0);
        refresh.setPath("/");

        response.addCookie(access);
        response.addCookie(refresh);

        return "redirect:/login?logout=true";
    }
}
