package com.cms.repository;

import com.cms.entity.BannerSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BannerSettingsRepository extends JpaRepository<BannerSettings, Long> {
    List<BannerSettings> findByPageNameOrderByDisplayOrderAsc(String pageName);
    List<BannerSettings> findByPageNameAndIsActiveTrueOrderByDisplayOrderAsc(String pageName);
    List<BannerSettings> findByPageNameIgnoreCaseAndIsActiveTrueOrderByDisplayOrderAsc(String pageName);

    Page<BannerSettings> findByIsDeletedFalse(Pageable pageable);
    List<BannerSettings> findByPageNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc(String pageName);

    @Query("SELECT b FROM BannerSettings b WHERE " +
           "(LOWER(b.pageName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.bannerTitle) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND b.isDeleted = false")
    Page<BannerSettings> searchBanners(@Param("search") String search, Pageable pageable);
}
