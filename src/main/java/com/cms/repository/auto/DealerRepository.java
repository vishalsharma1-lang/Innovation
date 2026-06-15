package com.cms.repository.auto;

import com.cms.entity.auto.Dealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    Page<Dealer> findByIsDeletedFalse(Pageable pageable);
    List<Dealer> findByIsActiveTrueAndIsDeletedFalse();
    List<Dealer> findByCityIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String city);
    List<Dealer> findByBrandIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String brand);
    List<Dealer> findByBrandIgnoreCaseAndCityIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String brand, String city);
    long countByIsActiveTrueAndIsDeletedFalse();
}
