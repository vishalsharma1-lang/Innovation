package com.cms.service.vehicle;

import com.cms.entity.vehicle.*;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service to scrape vehicle data from external URLs.
 * Supports common car listing sites.
 * Creates a preview DTO — does NOT save automatically.
 */
@Service
public class VehicleImportService {

    public VehicleImportData fetchFromUrl(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(20000)
                .followRedirects(true)
                .get();

        VehicleImportData data = new VehicleImportData();

        // Extract vehicle name from h1 or page title
        data.name = extractFirst(doc, "h1", ".gsc_col-sm-12 h1", ".modelName", ".car-name", "[data-car-name]", ".heading h1", ".cN");
        if (data.name == null || data.name.isBlank()) {
            data.name = doc.title().replaceAll("\\s*[-|].*", "").trim();
        }

        // Extract brand and model
        extractBrandModel(data, doc);

        // Meta description
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) data.shortDescription = metaDesc.attr("content");

        // Full description - try multiple selectors with broader coverage
        data.fullDescription = extractFirst(doc,
            ".readMore", ".overview-content", ".description", ".about-car",
            "[data-description]", ".carDesc", ".modelDesc", ".mainContent p",
            ".overview p", ".about-section p", ".model-overview", ".car-overview",
            "#overview p", ".modelOverview", ".gsc_col-sm-12 p", ".car-description",
            "article p", ".content-area p", ".model-description");

        // If still no description, try combining multiple paragraphs from main content
        if (data.fullDescription == null || data.fullDescription.isBlank()) {
            Elements paragraphs = doc.select(".overview p, .about p, .description p, article p, .content p, main p");
            StringBuilder descBuilder = new StringBuilder();
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (text.length() > 30 && !text.contains("©") && !text.toLowerCase().contains("cookie")) {
                    if (descBuilder.length() > 0) descBuilder.append(" ");
                    descBuilder.append(text);
                    if (descBuilder.length() > 500) break;
                }
            }
            if (descBuilder.length() > 50) data.fullDescription = descBuilder.toString();
        }

        // Fallback: use meta description as full description if nothing else found
        if ((data.fullDescription == null || data.fullDescription.isBlank()) && data.shortDescription != null) {
            data.fullDescription = data.shortDescription;
        }

        // ─── PRICE extraction (multiple patterns) ──────────
        String priceText = extractFirst(doc,
            ".price", ".priceValue", ".car-price", ".ex-showroom-price",
            "[data-price]", ".gsc_price", ".onRoadPrice", ".priceRange",
            ".startPrice", ".modelPrice", "span[class*=price]", ".kv_subCont .price",
            ".keyValue li:contains(Price)", ".topSummary .price");
        if (priceText != null) data.startingPrice = parsePrice(priceText);

        // Also try from structured data / JSON-LD
        if (data.startingPrice == null) {
            Elements scripts = doc.select("script[type=application/ld+json]");
            for (Element script : scripts) {
                String json = script.html();
                if (json.contains("price") || json.contains("Price")) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"price\"\\s*:\\s*\"?([\\d,.]+)\"?").matcher(json);
                    if (m.find()) {
                        data.startingPrice = parsePrice(m.group(1));
                    }
                    m = java.util.regex.Pattern.compile("\"lowPrice\"\\s*:\\s*\"?([\\d,.]+)\"?").matcher(json);
                    if (m.find()) {
                        data.startingPrice = parsePrice(m.group(1));
                    }
                    // Extract high/max price
                    m = java.util.regex.Pattern.compile("\"highPrice\"\\s*:\\s*\"?([\\d,.]+)\"?").matcher(json);
                    if (m.find()) {
                        data.maxPrice = parsePrice(m.group(1));
                    }
                    if (data.startingPrice != null) break;
                }
            }
        }

        // ─── MAX PRICE extraction ──────────────────────────
        if (data.maxPrice == null) {
            // Try to find max/top price from page text
            String maxPriceText = extractFirst(doc,
                ".maxPrice", ".topPrice", ".high-price", "[data-max-price]",
                ".priceRange .max", ".price-range .high");
            if (maxPriceText != null) data.maxPrice = parsePrice(maxPriceText);
        }

        // Try from price range text (e.g., "₹ 5.99 - 10.45 Lakh")
        if (data.maxPrice == null && priceText != null) {
            java.util.regex.Matcher rangeMatcher = java.util.regex.Pattern.compile(
                "([\\d,.]+)\\s*(?:lakh|Lakh|L)?\\s*[-–to]+\\s*(?:₹\\s*)?(?:Rs\\.?\\s*)?([\\d,.]+)\\s*(lakh|Lakh|L|crore|Crore)?")
                .matcher(priceText);
            if (rangeMatcher.find()) {
                String suffix = rangeMatcher.group(3) != null ? rangeMatcher.group(3) : "";
                data.maxPrice = parsePrice(rangeMatcher.group(2) + " " + suffix);
            }
        }

        // Also scan page for price range patterns
        if (data.maxPrice == null) {
            String pageText = doc.text();
            java.util.regex.Matcher priceRangeMatcher = java.util.regex.Pattern.compile(
                "(?:₹|Rs\\.?)\\s*([\\d,.]+)\\s*(Lakh|lakh|L)?\\s*[-–to]+\\s*(?:₹|Rs\\.?)?\\s*([\\d,.]+)\\s*(Lakh|lakh|L|Crore|crore)?")
                .matcher(pageText);
            if (priceRangeMatcher.find()) {
                String startSuffix = priceRangeMatcher.group(2) != null ? priceRangeMatcher.group(2) : "";
                String endSuffix = priceRangeMatcher.group(4) != null ? priceRangeMatcher.group(4) : startSuffix;
                if (data.startingPrice == null) {
                    data.startingPrice = parsePrice(priceRangeMatcher.group(1) + " " + startSuffix);
                }
                data.maxPrice = parsePrice(priceRangeMatcher.group(3) + " " + endSuffix);
            }
        }

        // ─── KEY SPECIFICATIONS (table-based extraction) ───
        data.specifications = new ArrayList<>();

        // Pattern 1: Key-value tables (most common)
        Elements specTables = doc.select("table, .specificationTable, .specification-table, .keySpec, .quickSpec");
        for (Element table : specTables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cells = row.select("td, th");
                if (cells.size() >= 2) {
                    addSpec(data, cells.get(0).text().trim(), cells.get(1).text().trim());
                }
            }
        }

        // Pattern 2: dl/dt/dd pairs
        Elements dlElements = doc.select("dl, .spec-list, .key-specs");
        for (Element dl : dlElements) {
            Elements dts = dl.select("dt, .spec-key, .key, .label");
            Elements dds = dl.select("dd, .spec-val, .value, .val");
            for (int i = 0; i < Math.min(dts.size(), dds.size()); i++) {
                addSpec(data, dts.get(i).text().trim(), dds.get(i).text().trim());
            }
        }

        // Pattern 3: div-based key-value (CardDekho/CarWale style)
        Elements kvPairs = doc.select(".kv_subCont li, .keyValue li, .quickOverview li, .topSummary li, .key-spec-item, .quickspec-item");
        for (Element kv : kvPairs) {
            String fullText = kv.text();
            Elements children = kv.children();
            if (children.size() >= 2) {
                addSpec(data, children.first().text().trim(), children.last().text().trim());
            } else if (fullText.contains(":")) {
                String[] parts = fullText.split(":", 2);
                addSpec(data, parts[0].trim(), parts[1].trim());
            }
        }

        // Pattern 4: Generic spec rows
        Elements specRows = doc.select("[class*=spec] [class*=row], [class*=spec] li, .overview-list li");
        for (Element row : specRows) {
            Elements parts = row.children();
            if (parts.size() >= 2) {
                addSpec(data, parts.first().text().trim(), parts.last().text().trim());
            }
        }

        // ─── Extract specific fields from specs ────────────
        data.fuelType = findInSpecs(data, "fuel");
        data.transmissionType = findInSpecs(data, "transmission");
        data.engineCC = findInSpecs(data, "engine", "displacement", "capacity", "cc");
        data.mileage = findInSpecs(data, "mileage", "kmpl", "range", "km/l");
        String seatsStr = findInSpecs(data, "seat", "seating");
        if (seatsStr != null) {
            try { data.seatingCapacity = Integer.parseInt(seatsStr.replaceAll("[^0-9]", "")); } catch (Exception e) {}
        }
        // Also try from page content directly
        if (data.seatingCapacity == null) {
            String pageText = doc.text();
            java.util.regex.Matcher seatMatcher = java.util.regex.Pattern.compile("(\\d)\\s*(?:seater|seats|seat)").matcher(pageText.toLowerCase());
            if (seatMatcher.find()) {
                try { data.seatingCapacity = Integer.parseInt(seatMatcher.group(1)); } catch (Exception e) {}
            }
        }

        // ─── IMAGES ────────────────────────────────────────
        data.images = new ArrayList<>();
        Elements imgElements = doc.select("img[src*=car], img[src*=vehicle], img[data-src*=car], .gallery img, .car-gallery img, .image-gallery img, .slick-slide img, picture img, .modelGallery img, [class*=gallery] img");
        Set<String> seenUrls = new HashSet<>();
        for (Element img : imgElements) {
            String src = img.absUrl("src");
            if (src.isBlank()) src = img.absUrl("data-src");
            if (src.isBlank()) src = img.absUrl("data-lazy");
            if (src.isBlank()) src = img.absUrl("data-original");
            if (!src.isBlank() && !src.contains("logo") && !src.contains("icon") && !src.contains("1x1") && src.contains("http") && seenUrls.add(src)) {
                VehicleImportData.ImportImage importImg = new VehicleImportData.ImportImage();
                importImg.url = src;
                importImg.alt = img.attr("alt");
                data.images.add(importImg);
                if (data.images.size() >= 20) break;
            }
        }

        // Hero image
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null && !ogImage.attr("content").isBlank()) {
            data.heroImage = ogImage.attr("content");
        } else if (!data.images.isEmpty()) {
            data.heroImage = data.images.get(0).url;
        }

        // ─── FEATURES ──────────────────────────────────────
        data.features = new ArrayList<>();
        Elements featureElements = doc.select(".feature-item, .features li, .feature-list li, .key-feature, [class*=feature] li, .topFeature li, .featureList li");
        for (Element feat : featureElements) {
            String text = feat.text().trim();
            if (!text.isBlank() && text.length() > 3 && text.length() < 200) {
                data.features.add(text);
            }
            if (data.features.size() >= 30) break;
        }

        // ─── COLORS ───────────────────────────────────────
        data.colors = new ArrayList<>();
        Elements colorElements = doc.select(".color-item, .color-option, [data-color], .colorList li, [class*=color] [class*=item], .carColor li");
        for (Element col : colorElements) {
            VehicleImportData.ImportColor color = new VehicleImportData.ImportColor();
            color.name = col.attr("title");
            if (color.name.isBlank()) color.name = col.attr("data-name");
            if (color.name.isBlank()) color.name = col.text().trim();
            color.code = col.attr("data-color");
            if (color.code.isBlank()) {
                String style = col.attr("style");
                if (style.contains("background")) {
                    java.util.regex.Matcher cm = java.util.regex.Pattern.compile("#[0-9a-fA-F]{3,6}|rgb[^)]+\\)").matcher(style);
                    if (cm.find()) color.code = cm.group();
                }
            }
            // Check child elements for color swatch
            if (color.code.isBlank()) {
                Element swatch = col.selectFirst("[style*=background]");
                if (swatch != null) {
                    java.util.regex.Matcher cm = java.util.regex.Pattern.compile("#[0-9a-fA-F]{3,6}|rgb[^)]+\\)").matcher(swatch.attr("style"));
                    if (cm.find()) color.code = cm.group();
                }
            }
            if (!color.name.isBlank() && color.name.length() < 50) data.colors.add(color);
            if (data.colors.size() >= 15) break;
        }

        // ─── FAQS ─────────────────────────────────────────
        data.faqs = new ArrayList<>();
        // Schema.org FAQ
        Elements faqSchema = doc.select("[itemtype*=FAQPage] [itemprop=mainEntity], [itemtype*=Question]");
        for (Element faq : faqSchema) {
            VehicleImportData.ImportFaq f = new VehicleImportData.ImportFaq();
            Element q = faq.selectFirst("[itemprop=name]");
            Element a = faq.selectFirst("[itemprop=text], [itemprop=acceptedAnswer] [itemprop=text]");
            if (q != null && a != null) {
                f.question = q.text().trim();
                f.answer = a.text().trim();
                if (!f.question.isBlank()) data.faqs.add(f);
            }
        }
        // Generic FAQ patterns
        if (data.faqs.isEmpty()) {
            Elements faqItems = doc.select(".faq-item, .accordion-item, .faqSection .item, [class*=faq] [class*=item]");
            for (Element faq : faqItems) {
                VehicleImportData.ImportFaq f = new VehicleImportData.ImportFaq();
                Element q = faq.selectFirst("h3, h4, button, .question, .faq-q, [class*=question]");
                Element a = faq.selectFirst("p, .answer, .faq-a, [class*=answer], .panel-body");
                if (q != null && a != null) {
                    f.question = q.text().trim();
                    f.answer = a.text().trim();
                    if (!f.question.isBlank() && !f.answer.isBlank()) data.faqs.add(f);
                }
                if (data.faqs.size() >= 20) break;
            }
        }

        // Category detection
        String fullText = ((data.name != null ? data.name : "") + " " + (data.shortDescription != null ? data.shortDescription : "")).toLowerCase();
        if (fullText.contains("suv") || fullText.contains("compact suv")) data.category = "SUV";
        else if (fullText.contains("sedan")) data.category = "Sedan";
        else if (fullText.contains("hatchback")) data.category = "Hatchback";
        else if (fullText.contains("mpv") || fullText.contains("van")) data.category = "MPV";
        else if (fullText.contains("pickup") || fullText.contains("truck")) data.category = "Pickup";
        else data.category = "SUV";

        // ─── PROS & CONS ──────────────────────────────────
        Elements prosElements = doc.select("[class*=pros] li, [class*=Pros] li, .proscons .pros li, .pros-list li, [data-pros] li");
        if (!prosElements.isEmpty()) {
            StringBuilder prosBuilder = new StringBuilder();
            for (Element p : prosElements) {
                if (!p.text().isBlank()) {
                    if (prosBuilder.length() > 0) prosBuilder.append("; ");
                    prosBuilder.append(p.text().trim());
                }
            }
            data.pros = prosBuilder.toString();
        }

        Elements consElements = doc.select("[class*=cons] li, [class*=Cons] li, .proscons .cons li, .cons-list li, [data-cons] li");
        if (!consElements.isEmpty()) {
            StringBuilder consBuilder = new StringBuilder();
            for (Element c : consElements) {
                if (!c.text().isBlank()) {
                    if (consBuilder.length() > 0) consBuilder.append("; ");
                    consBuilder.append(c.text().trim());
                }
            }
            data.cons = consBuilder.toString();
        }

        // ─── USER REVIEWS ─────────────────────────────────
        data.userReviews = new ArrayList<>();
        Elements reviewElements = doc.select(".user-review, .review-item, [class*=review][class*=card], .reviewCard, .userReview");
        for (Element review : reviewElements) {
            VehicleImportData.ImportUserReview ur = new VehicleImportData.ImportUserReview();
            Element titleEl = review.selectFirst("[class*=title], h4, h5, strong");
            Element contentEl = review.selectFirst("[class*=content], [class*=text], p, .desc");
            Element ratingEl = review.selectFirst("[class*=rating], [class*=stars], [data-rating]");
            Element nameEl = review.selectFirst("[class*=name], [class*=author], .reviewer");

            if (titleEl != null) ur.title = titleEl.text().trim();
            if (contentEl != null) ur.content = contentEl.text().trim();
            if (nameEl != null) ur.reviewerName = nameEl.text().trim();
            if (ratingEl != null) {
                String rText = ratingEl.attr("data-rating");
                if (rText.isBlank()) rText = ratingEl.text().replaceAll("[^0-9.]", "");
                try { ur.rating = (int) Math.round(Double.parseDouble(rText)); } catch (Exception e) {}
            }

            if (ur.title != null && !ur.title.isBlank() && ur.title.length() < 200) {
                data.userReviews.add(ur);
            }
            if (data.userReviews.size() >= 10) break;
        }

        // ─── EXPERT REVIEWS ───────────────────────────────
        data.expertReviews = new ArrayList<>();
        Elements expertElements = doc.select(".expert-review, .editorial-review, [class*=expert][class*=review], .verdict-section");
        for (Element expert : expertElements) {
            VehicleImportData.ImportExpertReview er = new VehicleImportData.ImportExpertReview();
            Element titleEl = expert.selectFirst("h3, h4, .title, [class*=title]");
            Element contentEl = expert.selectFirst("p, .content, [class*=content], [class*=desc]");
            Element verdictEl = expert.selectFirst("[class*=verdict], .verdict");
            Element nameEl = expert.selectFirst("[class*=author], [class*=reviewer], .name");

            if (titleEl != null) er.title = titleEl.text().trim();
            if (contentEl != null) er.content = contentEl.text().trim();
            if (verdictEl != null) er.verdict = verdictEl.text().trim();
            if (nameEl != null) er.reviewerName = nameEl.text().trim();
            er.sourceName = doc.baseUri().replaceAll("https?://", "").replaceAll("/.*", "");

            if (er.title != null && !er.title.isBlank()) {
                data.expertReviews.add(er);
            }
            if (data.expertReviews.size() >= 5) break;
        }

        // ─── NEWS ─────────────────────────────────────────
        data.news = new ArrayList<>();
        Elements newsElements = doc.select(".news-item, .latest-news li, [class*=news][class*=item], .updateItem, .news-card");
        for (Element newsEl : newsElements) {
            VehicleImportData.ImportNews n = new VehicleImportData.ImportNews();
            Element titleEl = newsEl.selectFirst("h3, h4, h5, a, .title, [class*=title]");
            Element descEl = newsEl.selectFirst("p, .desc, [class*=desc]");
            Element imgEl = newsEl.selectFirst("img");
            Element linkEl = newsEl.selectFirst("a[href]");

            if (titleEl != null) n.title = titleEl.text().trim();
            if (descEl != null) n.content = descEl.text().trim();
            if (imgEl != null) n.imageUrl = imgEl.absUrl("src");
            if (linkEl != null) n.sourceUrl = linkEl.absUrl("href");
            n.sourceName = doc.baseUri().replaceAll("https?://", "").replaceAll("/.*", "");
            n.category = "news";

            if (n.title != null && !n.title.isBlank() && n.title.length() > 5 && n.title.length() < 300) {
                data.news.add(n);
            }
            if (data.news.size() >= 10) break;
        }

        // ─── VARIANTS ─────────────────────────────────────
        data.variants = new ArrayList<>();

        // Pattern 1: Variant tables (most common on car listing sites)
        Elements variantTables = doc.select(".variantTable, .variant-table, [class*=variant] table, .modelVariant table, #variants table, .priceTable, table[class*=variant]");
        for (Element table : variantTables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() >= 2) {
                    VehicleImportData.ImportVariant v = new VehicleImportData.ImportVariant();
                    v.variantName = cells.get(0).text().trim();
                    // Try to extract price from last or second cell
                    for (int ci = cells.size() - 1; ci >= 1; ci--) {
                        String cellText = cells.get(ci).text();
                        if (cellText.contains("₹") || cellText.contains("Lakh") || cellText.contains("lakh") || cellText.matches(".*\\d+[.,]\\d+.*")) {
                            v.price = parsePrice(cellText);
                            break;
                        }
                    }
                    // Try to find fuel/transmission in middle columns
                    for (int ci = 1; ci < cells.size(); ci++) {
                        String cellText = cells.get(ci).text().toLowerCase();
                        if (cellText.contains("petrol") || cellText.contains("diesel") || cellText.contains("electric") || cellText.contains("cng") || cellText.contains("hybrid")) {
                            v.fuelType = cells.get(ci).text().trim();
                        }
                        if (cellText.contains("manual") || cellText.contains("automatic") || cellText.contains("amt") || cellText.contains("cvt") || cellText.contains("dct")) {
                            v.transmission = cells.get(ci).text().trim();
                        }
                    }
                    if (!v.variantName.isBlank() && v.variantName.length() > 2 && v.variantName.length() < 150) {
                        data.variants.add(v);
                    }
                }
                if (data.variants.size() >= 30) break;
            }
            if (!data.variants.isEmpty()) break;
        }

        // Pattern 2: Variant list items (div-based layout)
        if (data.variants.isEmpty()) {
            Elements variantItems = doc.select(".variantList li, .variant-item, [class*=variant] [class*=item], .modelVariant li, .priceList li, [class*=variantRow]");
            for (Element item : variantItems) {
                VehicleImportData.ImportVariant v = new VehicleImportData.ImportVariant();
                Element nameEl = item.selectFirst("[class*=name], [class*=title], strong, h4, h5, a");
                Element priceEl = item.selectFirst("[class*=price], [class*=Price], .price");
                Element fuelEl = item.selectFirst("[class*=fuel]");
                Element transEl = item.selectFirst("[class*=trans]");

                if (nameEl != null) v.variantName = nameEl.text().trim();
                else v.variantName = item.ownText().trim();

                if (priceEl != null) v.price = parsePrice(priceEl.text());
                if (fuelEl != null) v.fuelType = fuelEl.text().trim();
                if (transEl != null) v.transmission = transEl.text().trim();

                if (!v.variantName.isBlank() && v.variantName.length() > 2 && v.variantName.length() < 150) {
                    data.variants.add(v);
                }
                if (data.variants.size() >= 30) break;
            }
        }

        // Pattern 3: JSON-LD structured data for variants/offers
        if (data.variants.isEmpty()) {
            Elements scripts = doc.select("script[type=application/ld+json]");
            for (Element script : scripts) {
                String json = script.html();
                if (json.contains("\"offers\"") || json.contains("\"variant\"")) {
                    java.util.regex.Matcher vm = java.util.regex.Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\".*?\"price\"\\s*:\\s*\"?([\\d,.]+)\"?").matcher(json);
                    while (vm.find() && data.variants.size() < 30) {
                        VehicleImportData.ImportVariant v = new VehicleImportData.ImportVariant();
                        v.variantName = vm.group(1);
                        v.price = parsePrice(vm.group(2));
                        if (v.variantName.length() > 3 && v.variantName.length() < 150) {
                            data.variants.add(v);
                        }
                    }
                }
            }
        }

        // Assign default fuel/transmission to variants from vehicle-level data
        for (VehicleImportData.ImportVariant v : data.variants) {
            if ((v.fuelType == null || v.fuelType.isBlank()) && data.fuelType != null) v.fuelType = data.fuelType;
            if ((v.transmission == null || v.transmission.isBlank()) && data.transmissionType != null) v.transmission = data.transmissionType;
        }

        data.sourceUrl = url;
        return data;
    }

    private void addSpec(VehicleImportData data, String name, String value) {
        if (name.isBlank() || value.isBlank() || name.length() > 100 || value.length() > 300) return;
        // Avoid duplicates
        for (VehicleImportData.ImportSpec s : data.specifications) {
            if (s.name.equalsIgnoreCase(name)) return;
        }
        VehicleImportData.ImportSpec spec = new VehicleImportData.ImportSpec();
        spec.name = name;
        spec.value = value;
        // Categorize
        String lower = name.toLowerCase();
        if (lower.contains("engine") || lower.contains("power") || lower.contains("torque") || lower.contains("displacement") || lower.contains("cylinder")) spec.category = "Engine";
        else if (lower.contains("length") || lower.contains("width") || lower.contains("height") || lower.contains("wheelbase") || lower.contains("weight") || lower.contains("boot")) spec.category = "Dimensions";
        else if (lower.contains("mileage") || lower.contains("speed") || lower.contains("acceleration") || lower.contains("range")) spec.category = "Performance";
        else if (lower.contains("fuel") || lower.contains("tank")) spec.category = "Fuel";
        else if (lower.contains("brake") || lower.contains("airbag") || lower.contains("safety") || lower.contains("abs")) spec.category = "Safety";
        else spec.category = "General";
        data.specifications.add(spec);
        if (data.specifications.size() >= 60) return;
    }

    private String findInSpecs(VehicleImportData data, String... keywords) {
        for (VehicleImportData.ImportSpec s : data.specifications) {
            String lower = s.name.toLowerCase();
            for (String kw : keywords) {
                if (lower.contains(kw.toLowerCase())) return s.value;
            }
        }
        return null;
    }

    private String extractFirst(Document doc, String... selectors) {
        for (String sel : selectors) {
            try {
                Element el = doc.selectFirst(sel.trim());
                if (el != null && !el.text().isBlank()) return el.text().trim();
            } catch (Exception e) { /* skip invalid selector */ }
        }
        return null;
    }

    private void extractBrandModel(VehicleImportData data, Document doc) {
        // Try from structured data
        Element brand = doc.selectFirst("[itemprop=brand], [data-brand]");
        if (brand != null) data.brand = brand.text().trim();

        Element model = doc.selectFirst("[itemprop=model], [data-model]");
        if (model != null) data.model = model.text().trim();

        // Fallback: split name
        if ((data.brand == null || data.brand.isBlank()) && data.name != null) {
            String[] parts = data.name.split("\\s+", 2);
            if (parts.length >= 2) {
                data.brand = parts[0];
                data.model = parts[1];
            } else {
                data.brand = data.name;
                data.model = data.name;
            }
        }
    }

    private BigDecimal parsePrice(String text) {
        try {
            String cleaned = text.replaceAll("[^0-9.]", "");
            if (text.toLowerCase().contains("lakh")) {
                return new BigDecimal(cleaned).multiply(new BigDecimal("100000"));
            } else if (text.toLowerCase().contains("crore")) {
                return new BigDecimal(cleaned).multiply(new BigDecimal("10000000"));
            }
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Import Data DTO ──────────────────────────────────

    public static class VehicleImportData {
        public String sourceUrl;
        public String name;
        public String brand;
        public String model;
        public String category;
        public String fuelType;
        public String transmissionType;
        public String engineCC;
        public String mileage;
        public Integer seatingCapacity;
        public BigDecimal startingPrice;
        public BigDecimal maxPrice;
        public String shortDescription;
        public String fullDescription;
        public String heroImage;
        public List<ImportImage> images;
        public List<ImportSpec> specifications;
        public List<String> features;
        public List<ImportColor> colors;
        public List<ImportFaq> faqs;
        public List<ImportVariant> variants;
        public List<ImportExpertReview> expertReviews;
        public List<ImportNews> news;
        public List<ImportUserReview> userReviews;
        public String pros;
        public String cons;

        public static class ImportImage { public String url; public String alt; }
        public static class ImportSpec { public String name; public String value; public String category; }
        public static class ImportColor { public String name; public String code; }
        public static class ImportFaq { public String question; public String answer; }
        public static class ImportVariant { public String variantName; public BigDecimal price; public String fuelType; public String transmission; public String engineCC; public String mileage; }
        public static class ImportExpertReview { public String title; public String content; public String reviewerName; public String sourceName; public String sourceUrl; public Integer rating; public String pros; public String cons; public String verdict; }
        public static class ImportNews { public String title; public String content; public String imageUrl; public String sourceName; public String sourceUrl; public String category; }
        public static class ImportUserReview { public String reviewerName; public Integer rating; public String title; public String content; public String pros; public String cons; }
    }
}
