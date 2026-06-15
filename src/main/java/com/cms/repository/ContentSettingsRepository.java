package com.cms.repository;

import com.cms.entity.ContentSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentSettingsRepository extends JpaRepository<ContentSettings, Long> {
    List<ContentSettings> findByPageName(String pageName);
    Optional<ContentSettings> findByPageNameAndSectionName(String pageName, String sectionName);
    List<ContentSettings> findByPageNameAndIsActiveTrue(String pageName);
    List<ContentSettings> findByPageNameIgnoreCaseAndIsActiveTrue(String pageName);

    Page<ContentSettings> findByIsDeletedFalse(Pageable pageable);
    List<ContentSettings> findByPageNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String pageName);

    @Query("SELECT c FROM ContentSettings c WHERE " +
           "(LOWER(c.pageName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.heading) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.sectionName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND c.isDeleted = false")
    Page<ContentSettings> searchContentSettings(@Param("search") String search, Pageable pageable);
}
