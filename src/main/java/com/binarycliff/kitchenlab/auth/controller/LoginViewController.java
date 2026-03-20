package com.binarycliff.kitchenlab.auth.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    public String login(Authentication authentication) {

        if (isAuthenticated(authentication)) {
            return "redirect:/admin/dashboard";
        }

        return "auth/login";
    }

    /**
     * Serve the forgot password page.
     */
    @GetMapping("/forgot-password")
    public String forgotPassword(Authentication authentication) {

        if (isAuthenticated(authentication)) {
            return "redirect:/admin/dashboard";
        }

        return "auth/forgot-password";
    }

    /**
     * Serve the reset password page.
     */
    @GetMapping("/reset-password")
    public String resetPassword(Authentication authentication) {

        if (isAuthenticated(authentication)) {
            return "redirect:/admin/dashboard";
        }

        return "auth/reset-password";
    }

    /**
     * Common authentication check
     */
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
