package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleBrochure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleBrochureRepository extends JpaRepository<VehicleBrochure, Long> {

    List<VehicleBrochure> findByVehicleIdAndIsDeletedFalse(Long vehicleId);
}
