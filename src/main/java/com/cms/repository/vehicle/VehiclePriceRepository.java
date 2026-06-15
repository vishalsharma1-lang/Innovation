package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehiclePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiclePriceRepository extends JpaRepository<VehiclePrice, Long> {

    List<VehiclePrice> findByVehicleIdAndIsDeletedFalse(Long vehicleId);

    List<VehiclePrice> findByVehicleIdAndCityAndIsDeletedFalse(Long vehicleId, String city);
}
