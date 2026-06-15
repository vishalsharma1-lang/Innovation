package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleFeatureRepository extends JpaRepository<VehicleFeature, Long> {

    List<VehicleFeature> findByVehicleIdAndIsDeletedFalseOrderByCategoryAsc(Long vehicleId);

    List<VehicleFeature> findByVehicleIdAndCategoryAndIsDeletedFalse(Long vehicleId, String category);
}
