package com.cms.repository.auto;

import com.cms.entity.auto.LeadCapture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadCaptureRepository extends JpaRepository<LeadCapture, Long> {
    Page<LeadCapture> findByIsDeletedFalse(Pageable pageable);
    Page<LeadCapture> findByStatusAndIsDeletedFalse(String status, Pageable pageable);

    @Query("SELECT l FROM LeadCapture l WHERE " +
           "(LOWER(l.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "l.phone LIKE CONCAT('%', :q, '%') OR " +
           "LOWER(l.vehicleName) LIKE LOWER(CONCAT('%', :q, '%'))) AND l.isDeleted = false")
    Page<LeadCapture> searchLeads(@Param("q") String q, Pageable pageable);
}
