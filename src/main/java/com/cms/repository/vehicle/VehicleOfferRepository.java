package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleOfferRepository extends JpaRepository<VehicleOffer, Long> {

    List<VehicleOffer> findByVehicleIdAndIsActiveTrueAndIsDeletedFalse(Long vehicleId);
}
