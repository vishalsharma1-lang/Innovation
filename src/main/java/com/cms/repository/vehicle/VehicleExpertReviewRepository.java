package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleExpertReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleExpertReviewRepository extends JpaRepository<VehicleExpertReview, Long> {
    List<VehicleExpertReview> findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long vehicleId);
}
