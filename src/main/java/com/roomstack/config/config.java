package com.roomstack.config;

import com.roomstack.dao.LoginsRepository;
import com.roomstack.entity.Login;
import com.roomstack.service.UserSessionService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;

@Configuration
@EnableWebMvc
@EnableWebSecurity
public class config {

    @Autowired
    private LoginsRepository loginRepository;

    @Autowired
    private UserSessionService sessionService;

    @Autowired
    private  JwtFilter jwtFilter;
    @Autowired
    private CustomAuthenticationFailureHandler customFailureHandler;

    private static final String[] PUBLIC_URLS = {
            "/login",
            "/User/**",
            "/",
            "/logout",
            "/pforgot/**",

            "/PG/**",
            "/manifest.json",
            "/sw.js",
            "/icons/**",
            "/js/**",
            "/favicon.ico"
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/User/**").hasRole("USER")
                        .requestMatchers("/superadmin/**").hasRole("SUPERADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable) // Disable form login
                .logout(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?alreadyLoggedIn=true")
                );

        // 👇 Add JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return server -> server.addAdditionalTomcatConnectors(httpConnector());
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(9091);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Login login = loginRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return new org.springframework.security.core.userdetails.User(
                    login.getUsername(),
                    login.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(login.getRole()))
            );
        };
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                System.out.println(" Session Created: " + se.getSession().getId());
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                System.out.println(" Session Destroyed: " + se.getSession().getId());

                SecurityContext context = (SecurityContext) se.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
                if (context != null) {
                    String username = context.getAuthentication().getName();
                    String sessionId = se.getSession().getId();

                    String ip = (String) se.getSession().getAttribute("USER_IP");
                    if (ip != null) {
                        sessionService.removeUser(ip, sessionId);
                        System.out.println(" IP removed for user: " + username);
                    }
                }
            }

        };
    }
}
