package com.cms.repository.vehicle;

import com.cms.entity.vehicle.ModelFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelFaqRepository extends JpaRepository<ModelFaq, Integer> {

    /**
     * Fetch all active FAQs based on start_date and end_date.
     */
    @Query("SELECT f FROM ModelFaq f WHERE f.startDate <= CURRENT_DATE AND (f.endDate IS NULL OR f.endDate >= CURRENT_DATE) ORDER BY f.questionId ASC")
    List<ModelFaq> findActiveFaqs();

    /**
     * Fetch active FAQs filtered by category.
     */
    @Query("SELECT f FROM ModelFaq f WHERE f.startDate <= CURRENT_DATE AND (f.endDate IS NULL OR f.endDate >= CURRENT_DATE) AND f.category = :category ORDER BY f.questionId ASC")
    List<ModelFaq> findActiveFaqsByCategory(@Param("category") String category);

    /**
     * Fetch active FAQs filtered by filter field (used for vehicle/model matching).
     */
    @Query("SELECT f FROM ModelFaq f WHERE f.startDate <= CURRENT_DATE AND (f.endDate IS NULL OR f.endDate >= CURRENT_DATE) AND (f.filter = :filter OR f.filter = '' OR f.filter IS NULL) ORDER BY f.questionId ASC")
    List<ModelFaq> findActiveFaqsByFilter(@Param("filter") String filter);

    /**
     * Fetch active FAQs by filter and filterCategory.
     */
    @Query("SELECT f FROM ModelFaq f WHERE f.startDate <= CURRENT_DATE AND (f.endDate IS NULL OR f.endDate >= CURRENT_DATE) AND f.filter = :filter AND f.filterCategory = :filterCategory ORDER BY f.questionId ASC")
    List<ModelFaq> findActiveFaqsByFilterAndCategory(@Param("filter") String filter, @Param("filterCategory") String filterCategory);

    /**
     * Fetch all FAQs (for admin management, no date filtering).
     */
    List<ModelFaq> findAllByOrderByQuestionIdAsc();
}
