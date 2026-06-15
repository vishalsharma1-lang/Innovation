package com.cms.repository.auto;

import com.cms.entity.auto.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Page<BlogPost> findByIsDeletedFalse(Pageable pageable);
    Page<BlogPost> findByStatusAndIsDeletedFalse(String status, Pageable pageable);
    Optional<BlogPost> findBySlugAndIsDeletedFalse(String slug);
    List<BlogPost> findByIsFeaturedTrueAndStatusAndIsDeletedFalse(String status);
    List<BlogPost> findByCategoryAndStatusAndIsDeletedFalse(String category, String status);

    @Query("SELECT b FROM BlogPost b WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :q, '%'))) AND b.isDeleted = false")
    Page<BlogPost> searchPosts(@Param("q") String q, Pageable pageable);
}
