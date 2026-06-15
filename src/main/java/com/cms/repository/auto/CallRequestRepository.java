package com.cms.repository.auto;

import com.cms.entity.auto.CallRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {
    Page<CallRequest> findByStatus(String status, Pageable pageable);
    Page<CallRequest> findByLeadId(Long leadId, Pageable pageable);
}
