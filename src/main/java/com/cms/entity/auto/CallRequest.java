package com.cms.entity.auto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "vehicle_name", length = 255)
    private String vehicleName;

    @Column(length = 50)
    private String status = "pending"; // pending, triggered, completed, failed

    @Column(name = "call_provider", length = 50)
    private String callProvider; // twilio, msg91, fast2sms, manual

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
