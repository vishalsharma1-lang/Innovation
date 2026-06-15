package com.cms.repository.auto;

import com.cms.entity.auto.DealerDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerDealRepository extends JpaRepository<DealerDeal, Long> {

    List<DealerDeal> findByIsDeletedFalseOrderByPriorityDescCreatedAtDesc();

    List<DealerDeal> findByIsActiveTrueAndIsDeletedFalseOrderByPriorityDesc();

    @Query("SELECT d FROM DealerDeal d WHERE d.vehicleId = :vehicleId AND d.isActive = true AND d.isDeleted = false ORDER BY d.priority DESC, d.totalSavings DESC")
    List<DealerDeal> findByVehicleIdAndIsActiveTrueAndIsDeletedFalseOrderByTotalSavingsDesc(@Param("vehicleId") Long vehicleId);

    List<DealerDeal> findByDealerIdAndIsDeletedFalseOrderByCreatedAtDesc(Long dealerId);

    @Query("SELECT d FROM DealerDeal d WHERE d.city = :city AND d.isActive = true AND d.isDeleted = false ORDER BY d.priority DESC, d.totalSavings DESC")
    List<DealerDeal> findByCityAndIsActiveTrueAndIsDeletedFalseOrderByTotalSavingsDesc(@Param("city") String city);

    @Query("SELECT d FROM DealerDeal d WHERE d.isActive = true AND d.isDeleted = false AND d.isFeatured = true AND (d.endDate IS NULL OR d.endDate >= CURRENT_DATE) ORDER BY d.priority DESC, d.totalSavings DESC")
    List<DealerDeal> findFeaturedActiveDeals();

    @Query("SELECT d FROM DealerDeal d WHERE d.isActive = true AND d.isDeleted = false AND (d.endDate IS NULL OR d.endDate >= CURRENT_DATE) ORDER BY d.priority DESC, d.createdAt DESC")
    List<DealerDeal> findActiveDeals();

    @Query("SELECT d FROM DealerDeal d WHERE d.isActive = true AND d.isDeleted = false AND (d.endDate IS NULL OR d.endDate >= CURRENT_DATE) AND LOWER(d.vehicleName) LIKE LOWER(CONCAT('%', :brand, '%')) ORDER BY d.priority DESC, d.totalSavings DESC")
    List<DealerDeal> findActiveDealsByBrand(@Param("brand") String brand);

    @Query("SELECT COUNT(d) FROM DealerDeal d WHERE d.isActive = true AND d.isDeleted = false AND (d.endDate IS NULL OR d.endDate >= CURRENT_DATE)")
    long countActiveDeals();
}
