package com.binarycliff.kitchenlab.menu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time menu updates.
 * Configures STOMP endpoints and message brokers for menu management.
 * 
 * @author BinaryCliff Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class MenuWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for menu-related topics
        config.enableSimpleBroker("/topic/menu-updates", "/topic/restaurant", "/topic/admin");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint for menu WebSocket connections
        registry.addEndpoint("/ws/menu")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Register additional endpoint for admin dashboard
        registry.addEndpoint("/ws/admin/menu")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
