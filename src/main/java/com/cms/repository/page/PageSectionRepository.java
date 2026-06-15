package com.cms.repository.page;

import com.cms.entity.page.PageSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageSectionRepository extends JpaRepository<PageSection, Long> {
    List<PageSection> findByPageIdAndIsDeletedFalseOrderByDisplayOrderAsc(Long pageId);
    List<PageSection> findByPageIdAndIsVisibleTrueAndIsDeletedFalseOrderByDisplayOrderAsc(Long pageId);
}
