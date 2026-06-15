package com.cms.repository.auto;

import com.cms.entity.auto.VehicleLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleLeadRepository extends JpaRepository<VehicleLead, Long> {
    Page<VehicleLead> findByIsDeletedFalse(Pageable pageable);
    Page<VehicleLead> findByVehicleIdAndIsDeletedFalse(Long vehicleId, Pageable pageable);
    Page<VehicleLead> findByContactStatusAndIsDeletedFalse(String status, Pageable pageable);

    // Duplicate check: same mobile + vehicle within 10 minutes
    @Query("SELECT COUNT(l) FROM VehicleLead l WHERE l.mobile = :mobile AND l.vehicleId = :vehicleId AND l.createdAt > :since")
    long countRecentLeads(@Param("mobile") String mobile, @Param("vehicleId") Long vehicleId, @Param("since") LocalDateTime since);

    @Query("SELECT l FROM VehicleLead l WHERE " +
           "(LOWER(l.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "l.mobile LIKE CONCAT('%', :q, '%') OR " +
           "LOWER(l.vehicleName) LIKE LOWER(CONCAT('%', :q, '%'))) AND l.isDeleted = false")
    Page<VehicleLead> searchLeads(@Param("q") String q, Pageable pageable);

    @Query("SELECT l FROM VehicleLead l WHERE l.createdAt BETWEEN :from AND :to AND l.isDeleted = false")
    List<VehicleLead> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByIsDeletedFalse();
    long countByContactStatusAndIsDeletedFalse(String status);
    List<VehicleLead> findByDealerIdAndIsDeletedFalse(Long dealerId);
}
