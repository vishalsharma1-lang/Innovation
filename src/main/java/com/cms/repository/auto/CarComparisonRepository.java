package com.cms.repository.auto;

import com.cms.entity.auto.CarComparison;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarComparisonRepository extends JpaRepository<CarComparison, Long> {
    Page<CarComparison> findByIsDeletedFalse(Pageable pageable);
    List<CarComparison> findByIsFeaturedTrueAndIsActiveTrueAndIsDeletedFalse();
}
