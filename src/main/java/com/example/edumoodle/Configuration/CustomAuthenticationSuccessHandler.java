package com.example.edumoodle.Configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Lấy role từ authentication object
        String redirectUrl = null;

        // Nếu người dùng có role ADMIN
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            redirectUrl = "/admin/dashboard";
        }
        // Nếu người dùng có role TEACHER
        else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("editingteach"))) {
            redirectUrl = "/user/home";
        }
        // Nếu người dùng có role STUDENT
        else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("student"))) {
            redirectUrl = "/user/home";
        }

        // Điều hướng tới trang tương ứng
        if (redirectUrl != null) {
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/user/home");
        }
    }
}

