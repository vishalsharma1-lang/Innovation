package com.cms.entity.auto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_deals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dealer_id", nullable = false)
    private Long dealerId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "vehicle_name", length = 255)
    private String vehicleName;

    @Column(name = "dealer_name", length = 255)
    private String dealerName;

    @Column(length = 100)
    private String city;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Offer amounts
    @Column(name = "cash_discount", precision = 12, scale = 2)
    private BigDecimal cashDiscount;

    @Column(name = "exchange_bonus", precision = 12, scale = 2)
    private BigDecimal exchangeBonus;

    @Column(name = "corporate_discount", precision = 12, scale = 2)
    private BigDecimal corporateDiscount;

    @Column(name = "finance_benefit", precision = 12, scale = 2)
    private BigDecimal financeBenefit;

    @Column(name = "insurance_benefit", precision = 12, scale = 2)
    private BigDecimal insuranceBenefit;

    @Column(name = "total_savings", precision = 12, scale = 2)
    private BigDecimal totalSavings;

    // Dates
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Priority & Status
    @Column
    private Integer priority = 0;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

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
        calculateTotalSavings();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalSavings();
    }

    private void calculateTotalSavings() {
        BigDecimal total = BigDecimal.ZERO;
        if (cashDiscount != null) total = total.add(cashDiscount);
        if (exchangeBonus != null) total = total.add(exchangeBonus);
        if (corporateDiscount != null) total = total.add(corporateDiscount);
        if (financeBenefit != null) total = total.add(financeBenefit);
        if (insuranceBenefit != null) total = total.add(insuranceBenefit);
        this.totalSavings = total;
    }
}
