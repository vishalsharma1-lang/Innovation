package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {

    List<VehicleImage> findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long vehicleId);

    List<VehicleImage> findByVehicleIdAndCategoryAndIsDeletedFalse(Long vehicleId, String category);
}
