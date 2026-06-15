package com.cms.entity.vehicle;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "variant_name", nullable = false, length = 255)
    private String variantName;

    @Column(name = "fuel_type", length = 100)
    private String fuelType;

    @Column(length = 100)
    private String transmission;

    @Column(name = "engine_cc")
    private Integer engineCC;

    @Column(length = 100)
    private String power;

    @Column(length = 100)
    private String torque;

    @Column(length = 100)
    private String mileage;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

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
