package com.binarycliff.kitchenlab.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for serving login pages.
 * Handles the rendering of login and authentication-related views.
 */
@Controller
@RequestMapping("/auth")
public class LoginViewController {
    
    /**
     * Serve the login page.
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    /**
     * Serve the forgot password page.
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }
    
    /**
     * Serve the password reset page.
     */
    @GetMapping("/reset-password")
    public String resetPassword() {
        return "auth/reset-password";
    }
}
