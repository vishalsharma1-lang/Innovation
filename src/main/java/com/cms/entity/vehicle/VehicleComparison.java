package com.cms.entity.vehicle;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_comparisons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id_1", nullable = false)
    private Long vehicleId1;

    @Column(name = "vehicle_id_2", nullable = false)
    private Long vehicleId2;

    @Column(name = "comparison_title", length = 255)
    private String comparisonTitle;

    @Column(name = "comparison_notes", columnDefinition = "TEXT")
    private String comparisonNotes;

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
