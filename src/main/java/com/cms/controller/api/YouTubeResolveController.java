package com.cms.controller.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves any YouTube URL to Channel ID, Playlist ID, and type.
 * Supports: shorts, videos, channels, playlists, @handles
 */
@RestController
@RequestMapping("/api/pages/youtube")
public class YouTubeResolveController {

    @PostMapping("/resolve")
    public ResponseEntity<?> resolveUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        url = url.trim();
        if (!url.startsWith("http")) url = "https://" + url;

        Map<String, String> result = new HashMap<>();

        try {
            // Detect URL type and extract info
            if (url.contains("/shorts/")) {
                // YouTube Shorts URL
                String videoId = extractVideoId(url);
                result.put("type", "shorts");
                result.put("videoId", videoId);
                // Fetch channel ID from the shorts page
                String channelId = fetchChannelIdFromVideo(url);
                result.put("channelId", channelId != null ? channelId : "");
                result.put("message", "Shorts detected. Channel: " + (channelId != null ? channelId : "paste manually"));

            } else if (url.contains("/playlist") || url.contains("list=")) {
                // Playlist URL
                result.put("type", "playlist");
                String playlistId = extractParam(url, "list");
                result.put("playlistId", playlistId != null ? playlistId : "");
                // Try to get channel too
                String channelId = fetchChannelIdFromPage(url);
                result.put("channelId", channelId != null ? channelId : "");
                result.put("message", "Playlist found: " + playlistId);

            } else if (url.contains("/channel/")) {
                // Direct channel URL
                result.put("type", "channel");
                Matcher m = Pattern.compile("/channel/([^/?&]+)").matcher(url);
                if (m.find()) result.put("channelId", m.group(1));
                result.put("message", "Channel ID found: " + result.get("channelId"));

            } else if (url.contains("/@")) {
                // Handle-based URL (@username)
                result.put("type", "channel");
                String channelId = fetchChannelIdFromPage(url);
                result.put("channelId", channelId != null ? channelId : "");
                result.put("message", "Channel resolved: " + (channelId != null ? channelId : "could not resolve, paste manually"));

            } else if (url.contains("/watch") || url.contains("youtu.be/")) {
                // Video URL
                String videoId = extractVideoId(url);
                result.put("type", "video");
                result.put("videoId", videoId);
                String channelId = fetchChannelIdFromVideo(url);
                result.put("channelId", channelId != null ? channelId : "");
                result.put("message", "Video detected. Channel: " + (channelId != null ? channelId : "paste manually"));

            } else {
                // Generic YouTube URL — try to scrape channel ID
                String channelId = fetchChannelIdFromPage(url);
                result.put("type", "unknown");
                result.put("channelId", channelId != null ? channelId : "");
                result.put("message", channelId != null ? "Channel found: " + channelId : "Could not detect channel. Please enter Channel ID manually.");
            }

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", "Could not resolve URL: " + e.getMessage(),
                    "message", "Please enter the Channel ID manually"));
        }

        return ResponseEntity.ok(result);
    }

    private String extractVideoId(String url) {
        // Shorts: /shorts/VIDEO_ID
        Matcher m = Pattern.compile("/shorts/([^/?&]+)").matcher(url);
        if (m.find()) return m.group(1);

        // Watch: ?v=VIDEO_ID
        m = Pattern.compile("[?&]v=([^&]+)").matcher(url);
        if (m.find()) return m.group(1);

        // youtu.be/VIDEO_ID
        m = Pattern.compile("youtu\\.be/([^?&]+)").matcher(url);
        if (m.find()) return m.group(1);

        return "";
    }

    private String extractParam(String url, String param) {
        Matcher m = Pattern.compile("[?&]" + param + "=([^&]+)").matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }

    private String fetchChannelIdFromVideo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            return extractChannelIdFromDoc(doc);
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchChannelIdFromPage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            return extractChannelIdFromDoc(doc);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractChannelIdFromDoc(Document doc) {
        // Method 1: meta tag
        Element channelMeta = doc.selectFirst("meta[itemprop=channelId]");
        if (channelMeta != null) return channelMeta.attr("content");

        // Method 2: link canonical with channel
        Element canonical = doc.selectFirst("link[rel=canonical]");
        if (canonical != null) {
            Matcher m = Pattern.compile("/channel/([^/?]+)").matcher(canonical.attr("href"));
            if (m.find()) return m.group(1);
        }

        // Method 3: browse_id in page source
        String html = doc.html();
        Matcher m = Pattern.compile("\"channelId\"\\s*:\\s*\"(UC[^\"]+)\"").matcher(html);
        if (m.find()) return m.group(1);

        m = Pattern.compile("\"externalId\"\\s*:\\s*\"(UC[^\"]+)\"").matcher(html);
        if (m.find()) return m.group(1);

        // Method 4: og:url with channel
        Element ogUrl = doc.selectFirst("meta[property=og:url]");
        if (ogUrl != null) {
            m = Pattern.compile("/channel/([^/?]+)").matcher(ogUrl.attr("content"));
            if (m.find()) return m.group(1);
        }

        return null;
    }
}
