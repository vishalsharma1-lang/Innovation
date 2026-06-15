package com.cms.repository.page;

import com.cms.entity.page.DynamicPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DynamicPageRepository extends JpaRepository<DynamicPage, Long> {
    Optional<DynamicPage> findBySlugAndIsDeletedFalse(String slug);
    Optional<DynamicPage> findFirstBySlugAndStatusAndIsDeletedFalse(String slug, String status);
    Page<DynamicPage> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM DynamicPage p WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.slug) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND p.isDeleted = false")
    Page<DynamicPage> searchPages(@Param("search") String search, Pageable pageable);
}
