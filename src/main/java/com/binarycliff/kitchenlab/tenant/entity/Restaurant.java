package com.binarycliff.kitchenlab.tenant.entity;

import com.binarycliff.kitchenlab.common.entity.RootBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Restaurant entity representing a tenant in the multi-tenant system.
 */
@Entity
@Table(name = "restaurants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Restaurant extends RootBaseEntity {
    
    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, nullable = false, length = 50)
    private String subdomain;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(length = 1000)
    private String address;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 500)
    private String logoUrl;
    
    @Column(length = 7)
    private String primaryColor;
    
    @Column(length = 7)
    private String secondaryColor;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column
    private LocalDateTime deactivatedAt;
    
    @Column(length = 500)
    private String website;
    
    @Column(length = 100)
    private String cuisineType;
    
    @Column
    private Integer averageRating;
    
    @Column
    private Integer deliveryRadiusKm;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowsOnlineOrders = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowsDineIn = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowsTakeout = true;
    
    @Column(length = 1000)
    private String operatingHours;
    
    @Column
    private Double deliveryFee;
    
    @Column
    private Double minimumOrderAmount;
    
    @Column
    private Integer preparationTimeMinutes;
    
    @Transient
    public String getFullDomain() {
        return subdomain + ".kitchenlab.com";
    }
    
    @Transient
    public boolean isCurrentlyOpen() {
        if (operatingHours == null || operatingHours.isEmpty()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue() - 1; // 0 = Monday, 6 = Sunday
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();
        int currentTime = currentHour * 60 + currentMinute;
        
        String[] timeSlots = operatingHours.split(";");
        for (String slot : timeSlots) {
            if (slot.trim().isEmpty()) continue;
            
            String[] parts = slot.split(":");
            if (parts.length >= 3) {
                try {
                    int slotDay = Integer.parseInt(parts[0]);
                    if (slotDay == dayOfWeek) {
                        String[] timeRange = parts[2].split("-");
                        if (timeRange.length == 2) {
                            int openTime = parseTime(timeRange[0]);
                            int closeTime = parseTime(timeRange[1]);
                            
                            if (currentTime >= openTime && currentTime <= closeTime) {
                                return true;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid time slots
                }
            }
        }
        
        return false;
    }
    
    private int parseTime(String timeStr) {
        String[] parts = timeStr.trim().split(":");
        if (parts.length == 2) {
            try {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return hours * 60 + minutes;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
