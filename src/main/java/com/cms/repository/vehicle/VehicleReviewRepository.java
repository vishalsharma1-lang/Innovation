package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleReviewRepository extends JpaRepository<VehicleReview, Long> {

    List<VehicleReview> findByVehicleIdAndIsDeletedFalseOrderByCreatedAtDesc(Long vehicleId);

    Page<VehicleReview> findByVehicleIdAndIsDeletedFalse(Long vehicleId, Pageable pageable);
}
