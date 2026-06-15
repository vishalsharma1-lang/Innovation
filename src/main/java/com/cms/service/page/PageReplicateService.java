package com.cms.service.page;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PageReplicateService {

    public PageExtractResult extractFromUrl(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(20000)
                .followRedirects(true)
                .get();

        PageExtractResult result = new PageExtractResult();

        // SEO extraction
        result.pageTitle = doc.title();
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) result.seoDescription = metaDesc.attr("content");
        Element metaKeys = doc.selectFirst("meta[name=keywords]");
        if (metaKeys != null) result.seoKeywords = metaKeys.attr("content");
        result.seoTitle = result.pageTitle;

        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) result.seoTitle = ogTitle.attr("content");
        Element ogImg = doc.selectFirst("meta[property=og:image]");
        if (ogImg != null) result.ogImage = ogImg.attr("content");

        // Extract sections from page body
        result.sections = new ArrayList<>();
        Element body = doc.body();
        if (body == null) return result;

        // Find main content area
        Element main = body.selectFirst("main, article, .main-content, #content, .content, [role=main]");
        if (main == null) main = body;

        int order = 0;

        // Extract hero/banner sections
        Elements heroes = main.select("section:first-child, .hero, .banner, [class*=hero], [class*=banner], header + section");
        for (Element hero : heroes) {
            Map<String, Object> section = new HashMap<>();
            section.put("sectionType", "hero");
            section.put("title", getFirstText(hero, "h1, h2"));
            section.put("subtitle", getFirstText(hero, "h3, h4, p:first-of-type, .subtitle"));
            section.put("plainText", getFirstText(hero, "p"));
            section.put("imageUrl", getFirstImage(hero));
            section.put("buttonText", getFirstText(hero, "a.btn, button, [class*=btn]"));
            section.put("buttonUrl", getFirstHref(hero, "a.btn, a[class*=btn], a[class*=cta]"));
            section.put("displayOrder", order++);
            section.put("isVisible", true);
            if (section.get("title") != null) result.sections.add(section);
            break; // only first hero
        }

        // Extract headings as sections
        Elements headings = main.select("h1, h2");
        Set<String> seenTexts = new HashSet<>();
        for (Element h : headings) {
            String text = h.text().trim();
            if (text.isBlank() || text.length() > 200 || seenTexts.contains(text)) continue;
            seenTexts.add(text);

            // Get the parent section/container
            Element section = h.parent();
            // Try to find a larger containing section
            Element sectionParent = h.closest("section, article, .section, [class*=section], [class*=block], [class*=container], div.row, div.col");
            if (sectionParent != null && sectionParent != main) section = sectionParent;

            // Get ALL paragraphs and text content from the section
            StringBuilder contentHtml = new StringBuilder();
            StringBuilder plainContent = new StringBuilder();
            Elements allParas = section.select("p, li, span.text, .description, .content, .text-content");
            for (Element p : allParas) {
                String pText = p.text().trim();
                if (!pText.isBlank() && pText.length() > 10 && !seenTexts.contains(pText)) {
                    plainContent.append(pText).append("\n\n");
                    contentHtml.append("<p>").append(pText).append("</p>");
                    seenTexts.add(pText);
                }
            }

            // Also get direct sibling paragraphs after the heading
            Element sibling = h.nextElementSibling();
            int sibCount = 0;
            while (sibling != null && sibCount < 5) {
                if (sibling.tagName().matches("p|div|span|ul|ol")) {
                    String sText = sibling.text().trim();
                    if (!sText.isBlank() && sText.length() > 10 && !seenTexts.contains(sText)) {
                        plainContent.append(sText).append("\n\n");
                        contentHtml.append("<p>").append(sText).append("</p>");
                        seenTexts.add(sText);
                    }
                }
                if (sibling.tagName().matches("h[1-6]")) break; // stop at next heading
                sibling = sibling.nextElementSibling();
                sibCount++;
            }

            String img = getFirstImage(section);

            Map<String, Object> sectionMap = new HashMap<>();
            if (img != null && !img.isBlank()) {
                sectionMap.put("sectionType", "image");
                sectionMap.put("imageUrl", img);
            } else if (contentHtml.length() > 0) {
                sectionMap.put("sectionType", "text");
            } else {
                sectionMap.put("sectionType", "heading");
            }
            sectionMap.put("title", text);
            sectionMap.put("content", contentHtml.toString());
            sectionMap.put("plainText", plainContent.toString().length() > 2000 ? plainContent.substring(0, 2000) : plainContent.toString());
            sectionMap.put("displayOrder", order++);
            sectionMap.put("isVisible", true);
            result.sections.add(sectionMap);

            if (result.sections.size() >= 25) break;
        }

        // Also extract standalone paragraphs not under any heading
        Elements standaloneParagraphs = main.select("> p, > div > p, .content > p, article > p");
        StringBuilder extraContent = new StringBuilder();
        for (Element p : standaloneParagraphs) {
            String pText = p.text().trim();
            if (!pText.isBlank() && pText.length() > 30 && !seenTexts.contains(pText)) {
                extraContent.append("<p>").append(pText).append("</p>");
                seenTexts.add(pText);
            }
        }
        if (extraContent.length() > 0) {
            Map<String, Object> textSection = new HashMap<>();
            textSection.put("sectionType", "text");
            textSection.put("title", "Content");
            textSection.put("content", extraContent.toString());
            textSection.put("plainText", extraContent.toString().replaceAll("<[^>]+>", ""));
            textSection.put("displayOrder", order++);
            textSection.put("isVisible", true);
            result.sections.add(textSection);
        }

        // Extract images as gallery
        Elements images = main.select("img[src]");
        List<String> imgUrls = new ArrayList<>();
        for (Element img : images) {
            String src = img.absUrl("src");
            if (src.isBlank()) src = img.absUrl("data-src");
            if (!src.isBlank() && !src.contains("logo") && !src.contains("icon") && !src.contains("1x1") && src.length() > 10) {
                imgUrls.add(src);
                if (imgUrls.size() >= 8) break;
            }
        }
        if (!imgUrls.isEmpty()) {
            Map<String, Object> gallery = new HashMap<>();
            gallery.put("sectionType", "gallery");
            gallery.put("title", "Gallery");
            gallery.put("imageUrl", imgUrls.get(0));
            gallery.put("content", imgUrls.stream().map(u -> "<img src='" + u + "' style='width:200px;height:150px;object-fit:cover;border-radius:8px;margin:4px'>").reduce("", String::concat));
            gallery.put("displayOrder", order++);
            gallery.put("isVisible", true);
            result.sections.add(gallery);
        }

        // Extract videos
        Elements videos = main.select("iframe[src*=youtube], iframe[src*=vimeo], video");
        for (Element vid : videos) {
            Map<String, Object> section = new HashMap<>();
            section.put("sectionType", "video");
            section.put("videoUrl", vid.absUrl("src"));
            section.put("title", "Video");
            section.put("displayOrder", order++);
            section.put("isVisible", true);
            result.sections.add(section);
            if (result.sections.size() >= 30) break;
        }

        // Extract FAQs
        Elements faqItems = main.select("[itemtype*=FAQ] [itemprop=mainEntity], .faq-item, .accordion-item, [class*=faq] [class*=item]");
        if (!faqItems.isEmpty()) {
            StringBuilder faqHtml = new StringBuilder();
            for (Element faq : faqItems) {
                Element q = faq.selectFirst("[itemprop=name], h3, h4, button, .question");
                Element a = faq.selectFirst("[itemprop=text], p, .answer, .panel-body");
                if (q != null && a != null) {
                    faqHtml.append("<div style='border:1px solid #e8ecf0;border-radius:8px;padding:12px;margin-bottom:8px'>");
                    faqHtml.append("<strong>").append(q.text()).append("</strong><br>");
                    faqHtml.append("<span style='color:#6b7280'>").append(a.text()).append("</span></div>");
                }
            }
            if (faqHtml.length() > 0) {
                Map<String, Object> section = new HashMap<>();
                section.put("sectionType", "faq");
                section.put("title", "Frequently Asked Questions");
                section.put("content", faqHtml.toString());
                section.put("displayOrder", order++);
                section.put("isVisible", true);
                result.sections.add(section);
            }
        }

        return result;
    }

    private String getFirstText(Element parent, String selectors) {
        for (String sel : selectors.split(",")) {
            Element el = parent.selectFirst(sel.trim());
            if (el != null && !el.text().isBlank()) return el.text().trim();
        }
        return null;
    }

    private String getFirstImage(Element parent) {
        Element img = parent.selectFirst("img[src]");
        if (img != null) {
            String src = img.absUrl("src");
            if (!src.isBlank()) return src;
        }
        // Check background image
        Elements withBg = parent.select("[style*=background-image]");
        if (!withBg.isEmpty()) {
            String style = withBg.first().attr("style");
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("url\\(['\"]?([^'\"\\)]+)['\"]?\\)").matcher(style);
            if (m.find()) return m.group(1);
        }
        return null;
    }

    private String getFirstHref(Element parent, String selectors) {
        for (String sel : selectors.split(",")) {
            Element el = parent.selectFirst(sel.trim());
            if (el != null) return el.absUrl("href");
        }
        return null;
    }

    public static class PageExtractResult {
        public String pageTitle;
        public String seoTitle;
        public String seoDescription;
        public String seoKeywords;
        public String ogImage;
        public List<Map<String, Object>> sections;
    }
}
