package com.cms.service.page;

import com.cms.entity.page.DynamicPage;
import com.cms.repository.page.DynamicPageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * YouTube Reels fetcher — integrated into Page Management.
 * Fetches videos from YouTube Data API v3 and caches per page.
 */
@Service
public class YouTubeReelsService {

    @Value("${youtube.api.key:}")
    private String apiKey;

    @Autowired private DynamicPageRepository pageRepo;
    @Autowired private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get YouTube videos for a page. Uses cache if valid.
     */
    public List<Map<String, String>> getVideosForPage(DynamicPage page) {
        if (page == null || !Boolean.TRUE.equals(page.getYoutubeEnabled())) {
            return Collections.emptyList();
        }

        // Check cache
        if (page.getYoutubeCacheData() != null && !page.getYoutubeCacheData().equals("[]") 
            && page.getYoutubeCacheUpdated() != null) {
            int cacheMinutes = page.getYoutubeCacheTime() != null ? page.getYoutubeCacheTime() : 60;
            if (page.getYoutubeCacheUpdated().plusMinutes(cacheMinutes).isAfter(LocalDateTime.now())) {
                List<Map<String, String>> cached = parseCachedData(page.getYoutubeCacheData());
                if (!cached.isEmpty()) return cached;
            }
        }

        // Fetch fresh data
        List<Map<String, String>> videos = fetchFromYouTube(page);

        // If still empty, try to use channelId as a direct URL
        if (videos.isEmpty() && page.getYoutubeChannelId() != null) {
            String channelVal = page.getYoutubeChannelId().trim();
            
            // If it's a full YouTube URL instead of a channel ID, extract video and embed directly
            if (channelVal.contains("youtube.com") || channelVal.contains("youtu.be")) {
                videos = createDirectEmbedsFromUrl(channelVal, page);
            }
        }

        // Update cache
        try {
            page.setYoutubeCacheData(objectMapper.writeValueAsString(videos));
            page.setYoutubeCacheUpdated(LocalDateTime.now());
            pageRepo.save(page);
        } catch (Exception e) { /* cache update failed, still return data */ }

        return videos;
    }

    /**
     * When user pastes a direct video/shorts URL instead of channel ID,
     * create embed entries directly from that URL and try to get more from the channel.
     */
    private List<Map<String, String>> createDirectEmbedsFromUrl(String url, DynamicPage page) {
        List<Map<String, String>> videos = new ArrayList<>();
        
        // Extract video ID from URL
        String videoId = extractVideoIdFromUrl(url);
        if (videoId != null && !videoId.isBlank()) {
            Map<String, String> video = new HashMap<>();
            video.put("videoId", videoId);
            video.put("title", "Video");
            video.put("description", "");
            video.put("url", "https://www.youtube.com/watch?v=" + videoId);
            video.put("embedUrl", "https://www.youtube.com/embed/" + videoId);
            video.put("shortsUrl", "https://www.youtube.com/shorts/" + videoId);
            video.put("thumbnail", "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
            video.put("publishedAt", "");
            videos.add(video);
        }

        // Try to find channel and get more videos
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // Extract channel ID from page
            String channelId = null;
            org.jsoup.nodes.Element meta = doc.selectFirst("meta[itemprop=channelId]");
            if (meta != null) channelId = meta.attr("content");
            
            if (channelId == null) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"channelId\"\\s*:\\s*\"(UC[^\"]+)\"").matcher(doc.html());
                if (m.find()) channelId = m.group(1);
            }

            // If we found channel ID, fetch via RSS
            if (channelId != null && !channelId.isBlank()) {
                // Update the page's channel ID for future use
                page.setYoutubeChannelId(channelId);
                
                // Fetch from RSS
                DynamicPage tempPage = new DynamicPage();
                tempPage.setYoutubeChannelId(channelId);
                tempPage.setYoutubeLimit(page.getYoutubeLimit());
                List<Map<String, String>> rssVideos = fetchFromRSS(tempPage);
                if (!rssVideos.isEmpty()) {
                    return rssVideos; // RSS worked, use that instead
                }
            }
        } catch (Exception e) { /* scraping failed, use direct embed */ }

        return videos;
    }

    private String extractVideoIdFromUrl(String url) {
        java.util.regex.Matcher m;
        m = java.util.regex.Pattern.compile("/shorts/([^/?&]+)").matcher(url);
        if (m.find()) return m.group(1);
        m = java.util.regex.Pattern.compile("[?&]v=([^&]+)").matcher(url);
        if (m.find()) return m.group(1);
        m = java.util.regex.Pattern.compile("youtu\\.be/([^?&]+)").matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }

    /**
     * Force refresh cache for a page
     */
    public List<Map<String, String>> syncVideos(DynamicPage page) {
        if (page == null) return Collections.emptyList();
        page.setYoutubeCacheData(null);
        page.setYoutubeCacheUpdated(null);
        pageRepo.save(page);
        return getVideosForPage(page);
    }

    private List<Map<String, String>> fetchFromYouTube(DynamicPage page) {
        List<Map<String, String>> videos = new ArrayList<>();

        if (apiKey == null || apiKey.isBlank()) {
            // No API key — return demo/placeholder data or try RSS feed
            return fetchFromRSS(page);
        }

        try {
            String url;
            int limit = page.getYoutubeLimit() != null ? page.getYoutubeLimit() : 6;

            if (page.getYoutubePlaylistId() != null && !page.getYoutubePlaylistId().isBlank()) {
                // Fetch from playlist
                url = String.format(
                    "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=%d&playlistId=%s&key=%s",
                    limit, page.getYoutubePlaylistId(), apiKey);
            } else if (page.getYoutubeChannelId() != null && !page.getYoutubeChannelId().isBlank()) {
                // Fetch from channel
                url = String.format(
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=%s&maxResults=%d&order=date&type=video&key=%s",
                    page.getYoutubeChannelId(), limit, apiKey);
            } else {
                return videos;
            }

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    Map<String, String> video = new HashMap<>();
                    JsonNode snippet = item.get("snippet");
                    if (snippet == null) continue;

                    video.put("title", snippet.has("title") ? snippet.get("title").asText() : "");
                    video.put("description", snippet.has("description") ? snippet.get("description").asText("").substring(0, Math.min(snippet.get("description").asText("").length(), 200)) : "");

                    // Get video ID
                    String videoId = "";
                    if (item.has("id")) {
                        JsonNode idNode = item.get("id");
                        if (idNode.isTextual()) videoId = idNode.asText();
                        else if (idNode.has("videoId")) videoId = idNode.get("videoId").asText();
                    }
                    if (item.has("snippet") && item.get("snippet").has("resourceId")) {
                        videoId = item.get("snippet").get("resourceId").get("videoId").asText();
                    }
                    video.put("videoId", videoId);
                    video.put("url", "https://www.youtube.com/watch?v=" + videoId);
                    video.put("embedUrl", "https://www.youtube.com/embed/" + videoId);
                    video.put("shortsUrl", "https://www.youtube.com/shorts/" + videoId);

                    // Thumbnail
                    JsonNode thumbs = snippet.get("thumbnails");
                    if (thumbs != null) {
                        if (thumbs.has("high")) video.put("thumbnail", thumbs.get("high").get("url").asText());
                        else if (thumbs.has("medium")) video.put("thumbnail", thumbs.get("medium").get("url").asText());
                        else if (thumbs.has("default")) video.put("thumbnail", thumbs.get("default").get("url").asText());
                    }

                    video.put("publishedAt", snippet.has("publishedAt") ? snippet.get("publishedAt").asText() : "");
                    videos.add(video);
                }
            }
        } catch (Exception e) {
            // API failed — try RSS fallback
            return fetchFromRSS(page);
        }

        return videos;
    }

    /**
     * RSS fallback — works without API key using YouTube's public RSS feed
     */
    private List<Map<String, String>> fetchFromRSS(DynamicPage page) {
        List<Map<String, String>> videos = new ArrayList<>();
        String channelId = page.getYoutubeChannelId();
        if (channelId == null || channelId.isBlank()) return videos;

        try {
            String rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId;
            String xml = restTemplate.getForObject(rssUrl, String.class);

            if (xml == null) return videos;
            int limit = page.getYoutubeLimit() != null ? page.getYoutubeLimit() : 6;

            // Simple XML parsing for YouTube RSS
            String[] entries = xml.split("<entry>");
            for (int i = 1; i < entries.length && videos.size() < limit; i++) {
                Map<String, String> video = new HashMap<>();
                video.put("videoId", extractXml(entries[i], "yt:videoId"));
                video.put("title", extractXml(entries[i], "title"));
                video.put("description", "");
                video.put("publishedAt", extractXml(entries[i], "published"));

                String vid = video.get("videoId");
                video.put("url", "https://www.youtube.com/watch?v=" + vid);
                video.put("embedUrl", "https://www.youtube.com/embed/" + vid);
                video.put("shortsUrl", "https://www.youtube.com/shorts/" + vid);
                video.put("thumbnail", "https://img.youtube.com/vi/" + vid + "/hqdefault.jpg");

                if (vid != null && !vid.isBlank()) videos.add(video);
            }
        } catch (Exception e) { /* RSS also failed */ }

        return videos;
    }

    private String extractXml(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">");
        int end = xml.indexOf("</" + tag + ">");
        if (start == -1 || end == -1) return "";
        return xml.substring(start + tag.length() + 2, end).trim();
    }

    private List<Map<String, String>> parseCachedData(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
