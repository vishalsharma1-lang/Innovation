package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleColorRepository extends JpaRepository<VehicleColor, Long> {

    List<VehicleColor> findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long vehicleId);
}
