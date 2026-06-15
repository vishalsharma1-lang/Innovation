package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleFaqRepository extends JpaRepository<VehicleFaq, Long> {

    List<VehicleFaq> findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long vehicleId);
}
