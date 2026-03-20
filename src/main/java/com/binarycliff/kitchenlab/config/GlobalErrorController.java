package com.binarycliff.kitchenlab.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Global error controller to handle HTTP errors and display custom error pages.
 */
@Controller
@RequestMapping("/error")
@Slf4j
public class GlobalErrorController {

    @GetMapping
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            log.error("HTTP Error occurred: {} for URI: {}", statusCode, request.getRequestURI());
            
            // Get error details from request attributes
            Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            
            // Add error information to model
            model.addAttribute("status", statusCode);
            model.addAttribute("error", error != null ? error.getClass().getSimpleName() : "Unknown Error");
            model.addAttribute("message", message != null ? message.toString() : getDefaultMessage(statusCode));
            model.addAttribute("path", path != null ? path : request.getRequestURI());
            model.addAttribute("timestamp", System.currentTimeMillis());
            
            // Set icon class based on error type
            String iconClass = switch (statusCode) {
                case 400 -> "fa-exclamation-triangle bounce";
                case 401 -> "fa-lock shake";
                case 403 -> "fa-ban forbidden-icon";
                case 404 -> "fa-search magnifying-glass";
                case 500 -> "fa-server server-icon";
                default -> "fa-exclamation-circle float-animation";
            };
            model.addAttribute("iconClass", iconClass);
            
            // Use single dynamic error template
            return "error/error";
        }
        
        // Fallback for unknown errors
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred");
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", System.currentTimeMillis());
        model.addAttribute("iconClass", "fa-server server-icon");
        return "error/error";
    }
    
    private String getDefaultMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "The request could not be understood by the server";
            case 401 -> "You need to be authenticated to access this resource";
            case 403 -> "You don't have permission to access this resource";
            case 404 -> "The requested resource could not be found";
            case 500 -> "An unexpected error occurred on the server";
            default -> "An error occurred";
        };
    }
}
