package com.roomstack.config;

import com.roomstack.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserSessionService sessionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {


        String username = authentication.getName();


        String ip = IpUtils.getClientIP(request);
        String sessionId = request.getSession().getId();
        System.out.println(username+"______"+ip+"---"+sessionId);
        
        sessionService.storeUser(username, ip, sessionId);

        response.sendRedirect("/deshboard");
    }
}
