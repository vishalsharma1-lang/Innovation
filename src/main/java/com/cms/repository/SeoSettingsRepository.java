package com.cms.repository;

import com.cms.entity.SeoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SeoSettingsRepository extends JpaRepository<SeoSettings, Long> {
    Optional<SeoSettings> findByPageName(String pageName);
    Optional<SeoSettings> findByPageNameIgnoreCase(String pageName);
    boolean existsByPageName(String pageName);

    Page<SeoSettings> findByIsDeletedFalse(Pageable pageable);
    Optional<SeoSettings> findByPageNameAndIsDeletedFalse(String pageName);
    Optional<SeoSettings> findByPageNameIgnoreCaseAndIsDeletedFalse(String pageName);

    @Query("SELECT s FROM SeoSettings s WHERE " +
           "(LOWER(s.pageName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.seoTitle) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND s.isDeleted = false")
    Page<SeoSettings> searchSeoSettings(@Param("search") String search, Pageable pageable);
}
