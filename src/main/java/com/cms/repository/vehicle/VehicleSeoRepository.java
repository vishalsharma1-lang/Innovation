package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleSeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleSeoRepository extends JpaRepository<VehicleSeo, Long> {

    Optional<VehicleSeo> findByVehicleIdAndIsDeletedFalse(Long vehicleId);
}
