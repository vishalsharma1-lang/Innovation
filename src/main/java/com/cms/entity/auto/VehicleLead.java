package com.cms.entity.auto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(length = 255)
    private String email;

    @Column(length = 100)
    private String city;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_name", length = 255)
    private String vehicleName;

    @Column(length = 255)
    private String variant;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "sms_sent")
    private Boolean smsSent = false;

    @Column(name = "admin_sms_sent")
    private Boolean adminSmsSent = false;

    @Column(name = "call_status", length = 50)
    private String callStatus = "pending"; // pending, triggered, completed, failed

    @Column(name = "contact_status", length = 50)
    private String contactStatus = "new"; // new, assigned, contacted, follow-up, converted, lost

    @Column(name = "dealer_id")
    private Long dealerId;

    @Column(name = "dealer_name", length = 255)
    private String dealerNameAssigned;

    @Column(name = "source_page", length = 255)
    private String sourcePage;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
