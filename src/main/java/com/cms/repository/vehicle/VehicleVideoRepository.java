package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleVideoRepository extends JpaRepository<VehicleVideo, Long> {

    List<VehicleVideo> findByVehicleIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long vehicleId);
}
