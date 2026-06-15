package com.cms.entity.vehicle;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String slug;

    @Column(length = 255)
    private String brand;

    @Column(length = 255)
    private String model;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(length = 100)
    private String category; // SUV, Sedan, Hatchback, etc.

    @Column(name = "fuel_type", length = 100)
    private String fuelType; // Petrol, Diesel, Electric, Hybrid

    @Column(name = "transmission_type", length = 100)
    private String transmissionType; // Manual, Automatic, CVT

    @Column(name = "body_type", length = 100)
    private String bodyType;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(name = "starting_price", precision = 12, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "hero_image", length = 500)
    private String heroImage;

    @Column(name = "thumbnail_image", length = 500)
    private String thumbnailImage;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    // YouTube Reels Settings
    @Column(name = "youtube_enabled")
    private Boolean youtubeEnabled = false;

    @Column(name = "youtube_channel_id", length = 100)
    private String youtubeChannelId;

    @Column(name = "youtube_layout", length = 30)
    private String youtubeLayout = "grid";

    @Column(name = "youtube_limit")
    private Integer youtubeLimit = 6;

    // ── Used Car Fields ──────────────────────────────────────
    @Column(name = "vehicle_type", length = 10)
    private String vehicleType = "NEW"; // NEW or USED

    @Column(name = "km_driven")
    private Integer kmDriven;

    @Column(name = "ownership_count")
    private Integer ownershipCount; // 1 = 1st owner, 2 = 2nd, etc.

    @Column(name = "condition", length = 50)
    private String condition; // Excellent, Good, Fair, Poor

    @Column(name = "registration_year")
    private Integer registrationYear;

    @Column(name = "registration_state", length = 100)
    private String registrationState;

    @Column(name = "insurance_type", length = 100)
    private String insuranceType; // Comprehensive, Third Party, Expired

    @Column(name = "seller_type", length = 50)
    private String sellerType; // Dealer, Individual

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
