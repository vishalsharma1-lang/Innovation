package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleVariantRepository extends JpaRepository<VehicleVariant, Long> {

    List<VehicleVariant> findByVehicleIdAndIsDeletedFalseOrderByPriceAsc(Long vehicleId);

    Page<VehicleVariant> findByVehicleIdAndIsDeletedFalse(Long vehicleId, Pageable pageable);
}
