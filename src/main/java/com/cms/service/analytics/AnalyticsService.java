package com.cms.service.analytics;

import com.cms.entity.analytics.AnalyticsEvent;
import com.cms.repository.analytics.AnalyticsEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsEventRepository repo;

    public void trackEvent(Map<String, Object> payload, HttpServletRequest request) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventName(str(payload, "eventName"));
        event.setDealId(longVal(payload, "dealId"));
        event.setDealName(str(payload, "dealName"));
        event.setVehicleId(longVal(payload, "vehicleId"));
        event.setVehicleName(str(payload, "vehicleName"));
        event.setPageUrl(str(payload, "pageUrl"));
        event.setSearchQuery(str(payload, "searchQuery"));
        event.setExtraData(str(payload, "extraData"));

        // Detect device type from User-Agent
        String ua = request.getHeader("User-Agent");
        if (ua != null) {
            ua = ua.toLowerCase();
            if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
                event.setDeviceType("mobile");
            } else if (ua.contains("tablet") || ua.contains("ipad")) {
                event.setDeviceType("tablet");
            } else {
                event.setDeviceType("desktop");
            }
            // Browser detection
            if (ua.contains("chrome") && !ua.contains("edg")) event.setBrowser("Chrome");
            else if (ua.contains("firefox")) event.setBrowser("Firefox");
            else if (ua.contains("safari") && !ua.contains("chrome")) event.setBrowser("Safari");
            else if (ua.contains("edg")) event.setBrowser("Edge");
            else event.setBrowser("Other");
        }

        // Get real IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        event.setUserIp(ip);

        repo.save(event);
    }

    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        LocalDateTime last30d = LocalDateTime.now().minusDays(30);

        stats.put("totalEvents", repo.count());
        stats.put("pageViews", repo.countByEventName("page_view"));
        stats.put("dealClicks", repo.countByEventName("deal_click"));
        stats.put("leads", repo.countByEventName("lead_form_submit"));
        stats.put("getbestpriceClicks", repo.countByEventName("get_best_price_click"));
        stats.put("callbackClicks", repo.countByEventName("request_callback_click"));
        stats.put("eventsLast24h", repo.countByCreatedAtAfter(last24h));
        stats.put("uniqueVisitors30d", repo.countUniqueVisitorsSince(last30d));

        List<Object[]> eventCounts = repo.findEventCounts();
        stats.put("eventCounts", eventCounts);

        List<Object[]> deviceTypes = repo.findByDeviceType();
        stats.put("deviceTypes", deviceTypes);

        List<Object[]> daily = repo.findDailyEventCounts(last30d);
        stats.put("dailyTrend", daily);

        stats.put("recentEvents", repo.findTop50ByOrderByCreatedAtDesc());

        return stats;
    }

    public Map<String, Object> getDealAnalytics() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topDeals", repo.findTopDealsByClicks());
        data.put("dealClicks", repo.countByEventName("deal_click"));
        data.put("detailViews", repo.countByEventName("deal_detail_view"));
        data.put("getBestPrice", repo.countByEventName("get_best_price_click"));
        data.put("callbackRequests", repo.countByEventName("request_callback_click"));
        data.put("whatsappClicks", repo.countByEventName("whatsapp_click"));
        return data;
    }

    public Map<String, Object> getVehicleAnalytics() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topVehicles", repo.findTopVehiclesByViews());
        data.put("modelViews", repo.countByEventName("model_detail_view"));
        data.put("modelClicks", repo.countByEventName("model_click"));
        data.put("brandClicks", repo.countByEventName("brand_click"));
        return data;
    }

    public Map<String, Object> getFunnelAnalytics() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<Object[]> eventCounts = repo.findEventCounts();
        data.put("eventCounts", eventCounts);

        // Build funnel steps
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] row : eventCounts) {
            countMap.put((String) row[0], (Long) row[1]);
        }
        List<Map<String, Object>> funnel = new ArrayList<>();
        funnel.add(funnelStep("Page View", "page_view", countMap));
        funnel.add(funnelStep("Deal Click", "deal_click", countMap));
        funnel.add(funnelStep("Deal Detail View", "deal_detail_view", countMap));
        funnel.add(funnelStep("Get Best Price", "get_best_price_click", countMap));
        funnel.add(funnelStep("Request Callback", "request_callback_click", countMap));
        funnel.add(funnelStep("Lead Submitted", "lead_form_submit", countMap));
        data.put("funnel", funnel);
        return data;
    }

    public Map<String, Object> getSearchAnalytics() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topQueries", repo.findTopSearchQueries());
        data.put("totalSearches", repo.countByEventName("search_query"));
        return data;
    }

    private Map<String, Object> funnelStep(String label, String eventName, Map<String, Long> countMap) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("label", label);
        step.put("eventName", eventName);
        step.put("count", countMap.getOrDefault(eventName, 0L));
        return step;
    }

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Long longVal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return null; }
    }
}
