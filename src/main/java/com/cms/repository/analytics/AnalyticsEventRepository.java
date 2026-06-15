package com.cms.repository.analytics;

import com.cms.entity.analytics.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByEventName(String eventName);

    long countByCreatedAtAfter(LocalDateTime after);

    @Query("SELECT COUNT(DISTINCT a.userIp) FROM AnalyticsEvent a WHERE a.createdAt >= :after")
    long countUniqueVisitorsSince(@Param("after") LocalDateTime after);

    // Top deals by clicks
    @Query("SELECT a.dealId, a.dealName, COUNT(a) as cnt FROM AnalyticsEvent a " +
           "WHERE a.eventName = 'deal_click' AND a.dealId IS NOT NULL " +
           "GROUP BY a.dealId, a.dealName ORDER BY cnt DESC")
    List<Object[]> findTopDealsByClicks();

    // Top vehicles by views
    @Query("SELECT a.vehicleId, a.vehicleName, COUNT(a) as cnt FROM AnalyticsEvent a " +
           "WHERE a.eventName = 'model_detail_view' AND a.vehicleId IS NOT NULL " +
           "GROUP BY a.vehicleId, a.vehicleName ORDER BY cnt DESC")
    List<Object[]> findTopVehiclesByViews();

    // Daily event counts for last 30 days
    @Query("SELECT CAST(a.createdAt AS date), COUNT(a) FROM AnalyticsEvent a " +
           "WHERE a.createdAt >= :since GROUP BY CAST(a.createdAt AS date) ORDER BY CAST(a.createdAt AS date)")
    List<Object[]> findDailyEventCounts(@Param("since") LocalDateTime since);

    // Funnel: counts per event type
    @Query("SELECT a.eventName, COUNT(a) FROM AnalyticsEvent a GROUP BY a.eventName ORDER BY COUNT(a) DESC")
    List<Object[]> findEventCounts();

    // Top search queries
    @Query("SELECT a.searchQuery, COUNT(a) as cnt FROM AnalyticsEvent a " +
           "WHERE a.eventName = 'search_query' AND a.searchQuery IS NOT NULL " +
           "GROUP BY a.searchQuery ORDER BY cnt DESC")
    List<Object[]> findTopSearchQueries();

    // Deal analytics: all events for a deal
    @Query("SELECT a.eventName, COUNT(a) FROM AnalyticsEvent a " +
           "WHERE a.dealId = :dealId GROUP BY a.eventName")
    List<Object[]> findEventCountsForDeal(@Param("dealId") Long dealId);

    // Recent events
    List<AnalyticsEvent> findTop50ByOrderByCreatedAtDesc();

    // Events by device type
    @Query("SELECT a.deviceType, COUNT(a) FROM AnalyticsEvent a GROUP BY a.deviceType ORDER BY COUNT(a) DESC")
    List<Object[]> findByDeviceType();
}
