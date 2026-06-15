package com.cms.entity.vehicle;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(length = 255)
    private String city;

    @Column(length = 255)
    private String state;

    @Column(name = "ex_showroom_price", precision = 12, scale = 2)
    private BigDecimal exShowroomPrice;

    @Column(name = "on_road_price", precision = 12, scale = 2)
    private BigDecimal onRoadPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal rto;

    @Column(precision = 12, scale = 2)
    private BigDecimal insurance;

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
