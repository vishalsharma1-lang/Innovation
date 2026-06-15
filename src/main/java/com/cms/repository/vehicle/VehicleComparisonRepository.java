package com.cms.repository.vehicle;

import com.cms.entity.vehicle.VehicleComparison;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleComparisonRepository extends JpaRepository<VehicleComparison, Long> {

    List<VehicleComparison> findByIsDeletedFalse();

    Page<VehicleComparison> findByIsDeletedFalse(Pageable pageable);
}
