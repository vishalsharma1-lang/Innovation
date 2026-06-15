package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleSpecificationRepository extends JpaRepository<VehicleSpecification, Long> {

    List<VehicleSpecification> findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long vehicleId);

    List<VehicleSpecification> findByVehicleIdAndCategoryAndIsDeletedFalse(Long vehicleId, String category);
}
