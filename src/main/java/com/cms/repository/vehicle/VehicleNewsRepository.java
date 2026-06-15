package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleNewsRepository extends JpaRepository<VehicleNews, Long> {
    List<VehicleNews> findByVehicleIdAndIsDeletedFalseOrderByPublishedDateDesc(Long vehicleId);
}
