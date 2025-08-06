//package com.roomstack.config;
//
//import com.roomstack.service.UserSessionService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//@Component
//public class IpCheckFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private UserSessionService sessionService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
//            String username = auth.getName();
//            String currentIp = request.getRemoteAddr();
//
//            String storedIp = sessionService.getUserIp(username);
//
//            if (storedIp == null) {
//                sessionService.addUser(username, currentIp); // login pe store karo
//            } else if (!storedIp.equals(currentIp)) {
//                response.sendRedirect("/login?expired=true");
//                return;
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
