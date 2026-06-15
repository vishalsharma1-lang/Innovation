package com.cms.repository;

import com.cms.entity.ImageSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImageSettingsRepository extends JpaRepository<ImageSettings, Long> {
    List<ImageSettings> findByIsActiveTrue();
    List<ImageSettings> findByCategory(String category);
    List<ImageSettings> findByCategoryAndIsActiveTrue(String category);

    Page<ImageSettings> findByIsDeletedFalse(Pageable pageable);
    List<ImageSettings> findByIsActiveTrueAndIsDeletedFalse();

    @Query("SELECT i FROM ImageSettings i WHERE " +
           "(LOWER(i.imageName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.altText) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND i.isDeleted = false")
    Page<ImageSettings> searchImages(@Param("search") String search, Pageable pageable);
}
