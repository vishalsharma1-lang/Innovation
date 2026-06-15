package com.cms.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DealerScraperService {

    /**
     * Scrape dealer information from a URL.
     * If the page is a dealer listing (multiple dealers), returns data for the first one.
     * Use fetchAllDealersFromUrl() for bulk extraction.
     */
    public Map<String, String> fetchDealerFromUrl(String url) throws Exception {
        List<Map<String, String>> all = fetchAllDealersFromUrl(url);
        return all.isEmpty() ? new HashMap<>() : all.get(0);
    }

    /**
     * Scrape ALL dealers from a listing page (e.g., CardDekho dealer directory).
     */
    public List<Map<String, String>> fetchAllDealersFromUrl(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(20000).followRedirects(true).get();

        List<Map<String, String>> dealers = new ArrayList<>();

        // Try to detect dealer listing page (multiple dealer cards)
        Elements dealerCards = doc.select(".dealer-card, .dealerCard, [class*=dealer][class*=card], [class*=dealer][class*=item], .showroom-card, .dealer-list-item, [data-dealer-id], .gsc_col-xs-12.gsc_col-md-6");

        // CardDekho specific selectors
        if (dealerCards.isEmpty()) dealerCards = doc.select("[class*=dealerList] > div, .dealer-info, .dealerInfo");
        if (dealerCards.isEmpty()) dealerCards = doc.select("li[class*=dealer], div[class*=showroom]");

        // If we found dealer cards, extract each one
        if (!dealerCards.isEmpty()) {
            // Extract brand from page title/URL
            String pageBrand = "";
            Matcher brandMatcher = Pattern.compile("/([A-Z][a-z]+)/", Pattern.CASE_INSENSITIVE).matcher(url);
            if (brandMatcher.find()) pageBrand = brandMatcher.group(1);
            if (pageBrand.isBlank()) {
                String title = doc.title();
                Matcher tm = Pattern.compile("(\\w+)\\s+(?:dealers|showrooms)", Pattern.CASE_INSENSITIVE).matcher(title);
                if (tm.find()) pageBrand = tm.group(1);
            }

            // Extract city from URL
            String pageCity = "";
            String[] urlParts = url.replaceAll("https?://[^/]+/", "").split("/");
            if (urlParts.length >= 2) pageCity = urlParts[1].replace("-", " ");

            for (Element card : dealerCards) {
                Map<String, String> dealer = new HashMap<>();
                dealer.put("sourceUrl", url);

                // Name
                String name = null;
                Element nameEl = card.selectFirst("h2, h3, h4, .dealer-name, .dealerName, [class*=name] a, [class*=title], strong a");
                if (nameEl != null) name = nameEl.text().trim();
                if (name == null || name.isBlank()) continue; // Skip if no name found
                dealer.put("dealerName", name);
                dealer.put("brand", pageBrand);
                dealer.put("city", pageCity.isEmpty() ? "" : pageCity.substring(0, 1).toUpperCase() + pageCity.substring(1));

                // Address
                Element addrEl = card.selectFirst(".address, [class*=address], [class*=addr], p");
                dealer.put("address", addrEl != null ? addrEl.text().trim() : "");

                // Phone
                Element phoneEl = card.selectFirst("a[href^=tel:], [class*=phone], [class*=mobile], [class*=contact]");
                String phone = "";
                if (phoneEl != null) {
                    phone = phoneEl.attr("href").replace("tel:", "").trim();
                    if (phone.isBlank()) phone = phoneEl.text().replaceAll("[^0-9+]", "");
                }
                dealer.put("phone", phone);

                // Type
                dealer.put("dealerType", "authorized");
                dealer.put("dealerLogo", "");
                dealer.put("email", "");
                dealer.put("state", "");
                dealer.put("website", "");

                dealers.add(dealer);
            }
        }

        // If no dealer cards found, treat as single dealer page
        if (dealers.isEmpty()) {
            Map<String, String> single = extractSingleDealer(doc, url);
            if (single.get("dealerName") != null && !single.get("dealerName").isBlank()) {
                dealers.add(single);
            }
        }

        return dealers;
    }

    private Map<String, String> extractSingleDealer(Document doc, String url) {
        Map<String, String> data = new HashMap<>();
        data.put("sourceUrl", url);

        String name = extractText(doc, "h1", ".company-name", ".dealer-name", "[itemprop=name]", ".brand-name");
        if (name == null || name.isBlank()) name = doc.title().replaceAll("\\s*[-|–].*", "").trim();
        data.put("dealerName", name);

        String brand = extractText(doc, "[itemprop=brand]", ".brand", "[data-brand]");
        data.put("brand", brand != null ? brand : "");

        String address = extractText(doc, "[itemprop=streetAddress]", ".address", "[itemprop=address]", ".location");
        data.put("address", address != null ? address : "");

        String city = extractText(doc, "[itemprop=addressLocality]", ".city", "[data-city]");
        data.put("city", city != null ? city : "");

        String state = extractText(doc, "[itemprop=addressRegion]", ".state");
        data.put("state", state != null ? state : "");

        String phone = null;
        Elements phoneEls = doc.select("a[href^=tel:], [itemprop=telephone], .phone, .contact-number");
        if (!phoneEls.isEmpty()) {
            phone = phoneEls.first().text().replaceAll("[^0-9+]", "");
            if (phone.isBlank()) phone = phoneEls.first().attr("href").replace("tel:", "").trim();
        }
        if (phone == null || phone.isBlank()) {
            Matcher pm = Pattern.compile("(?:\\+91|0)?\\s*[6-9]\\d{9}").matcher(doc.text());
            if (pm.find()) phone = pm.group().replaceAll("\\s+", "");
        }
        data.put("phone", phone != null ? phone : "");

        String email = null;
        Elements emailEls = doc.select("a[href^=mailto:], [itemprop=email]");
        if (!emailEls.isEmpty()) {
            email = emailEls.first().attr("href").replace("mailto:", "").trim();
            if (email.isBlank()) email = emailEls.first().text().trim();
        }
        data.put("email", email != null ? email : "");

        Element logo = doc.selectFirst(".logo img, header img, .navbar-brand img");
        data.put("dealerLogo", logo != null ? logo.absUrl("src") : "");
        data.put("website", url);
        data.put("dealerType", "authorized");

        return data;
    }

    /**
     * Scrape deal/offer information from a URL.
     */
    public Map<String, Object> fetchDealFromUrl(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000).followRedirects(true).get();

        Map<String, Object> data = new HashMap<>();
        data.put("sourceUrl", url);

        // Title
        String title = extractText(doc, "h1", ".offer-title", ".deal-title", "[class*=offer] h2", "[class*=deal] h2");
        if (title == null) title = doc.title().replaceAll("\\s*[-|–].*", "").trim();
        data.put("title", title);

        // Description
        String desc = extractText(doc, ".offer-description", ".deal-desc", ".offer-content p", "[class*=offer] p", "meta[name=description]");
        if (desc == null) {
            Element metaDesc = doc.selectFirst("meta[name=description]");
            if (metaDesc != null) desc = metaDesc.attr("content");
        }
        data.put("description", desc != null ? desc : "");

        // Extract discount amounts from page text
        String pageText = doc.text();
        data.put("cashDiscount", extractAmount(pageText, "cash discount", "cash benefit", "cash offer"));
        data.put("exchangeBonus", extractAmount(pageText, "exchange bonus", "exchange benefit", "exchange offer"));
        data.put("corporateDiscount", extractAmount(pageText, "corporate discount", "corporate benefit", "corporate offer"));
        data.put("financeBenefit", extractAmount(pageText, "finance benefit", "finance offer", "loan benefit"));
        data.put("insuranceBenefit", extractAmount(pageText, "insurance benefit", "insurance offer", "insurance discount"));

        // Try structured offer elements
        Elements offerItems = doc.select(".offer-item, [class*=benefit] li, [class*=discount] li, .offer-list li, .deal-item");
        for (Element item : offerItems) {
            String text = item.text().toLowerCase();
            String amount = extractAmountFromText(item.text());
            if (amount != null) {
                if (text.contains("cash") && data.get("cashDiscount") == null) data.put("cashDiscount", amount);
                else if (text.contains("exchange") && data.get("exchangeBonus") == null) data.put("exchangeBonus", amount);
                else if (text.contains("corporate") && data.get("corporateDiscount") == null) data.put("corporateDiscount", amount);
                else if (text.contains("finance") || text.contains("loan") && data.get("financeBenefit") == null) data.put("financeBenefit", amount);
                else if (text.contains("insurance") && data.get("insuranceBenefit") == null) data.put("insuranceBenefit", amount);
            }
        }

        // Validity dates
        Matcher dateMatcher = Pattern.compile("valid\\s*(?:till|until|upto)\\s*:?\\s*(\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})", Pattern.CASE_INSENSITIVE).matcher(pageText);
        if (dateMatcher.find()) data.put("endDate", dateMatcher.group(1));

        // Dealer name from page
        String dealer = extractText(doc, ".dealer-name", "[itemprop=seller]", ".showroom-name");
        data.put("dealerName", dealer != null ? dealer : "");

        // Brand / Vehicle
        String vehicle = extractText(doc, "h1", ".model-name", "[itemprop=name]");
        data.put("vehicleName", vehicle != null ? vehicle : "");

        return data;
    }

    private String extractText(Document doc, String... selectors) {
        for (String sel : selectors) {
            try {
                Element el = doc.selectFirst(sel);
                if (el != null && !el.text().isBlank()) return el.text().trim();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String extractAmount(String text, String... keywords) {
        for (String kw : keywords) {
            Pattern p = Pattern.compile(kw + "\\s*:?\\s*(?:₹|Rs\\.?|INR)?\\s*([\\d,]+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) return m.group(1).replace(",", "");
        }
        return null;
    }

    private String extractAmountFromText(String text) {
        Matcher m = Pattern.compile("(?:₹|Rs\\.?|INR)\\s*([\\d,]+)").matcher(text);
        if (m.find()) return m.group(1).replace(",", "");
        m = Pattern.compile("([\\d,]+)\\s*(?:₹|Rs|INR)").matcher(text);
        if (m.find()) return m.group(1).replace(",", "");
        return null;
    }
}
