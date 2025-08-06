//package com.roomstack.config;
//
//import com.roomstack.entity.Login;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.Collections;
//
//public class CustomUserDetails implements UserDetails {
//
//    private final Login login;
//
//    public CustomUserDetails(Login login) {
//        this.login = login;
//    }
//
//    public Login getLogin() {
//        return login;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // Assuming your Login entity has a field `role` like "ROLE_USER"
//        return Collections.singletonList(new SimpleGrantedAuthority(login.getRole()));
//    }
//
//    @Override
//    public String getPassword() {
//        return login.getPassword();
//    }
//
//    @Override
//    public String getUsername() {
//        return login.getUsername(); // or getUsername(), depending on your field
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true; // or true if you don't have this field
//    }
//}
