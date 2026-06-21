package com.cms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class UsedCarExtractorService {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    private static final String SYSTEM_PROMPT = """
You are a Used Car Data Extraction & Deal Processing Engine for a car marketplace CMS.

Your task is to extract structured used car information from a given webpage content (HTML/text) from any used car website (e.g., Spinny, Cars24, OLX, dealer sites).

You must return ONLY valid JSON. No explanations.

OUTPUT JSON FORMAT:
{
  "vehicleType": "USED",
  "vehicle": {
    "brand": "",
    "model": "",
    "variant": "",
    "year": null,
    "price": null,
    "kmDriven": null,
    "fuelType": "Petrol | Diesel | CNG | Electric | Hybrid | null",
    "transmission": "Manual | Automatic | null",
    "ownerType": "1st | 2nd | 3rd | 4th+ | null",
    "city": "",
    "color": "",
    "registrationState": ""
  },
  "seller": {
    "sellerName": "",
    "phone": "",
    "listingType": "Dealer | Individual | Unknown",
    "dealerName": "",
    "whatsappAvailable": true
  },
  "media": {
    "images": []
  },
  "source": {
    "url": "",
    "sourceWebsite": ""
  },
  "deal": {
    "dealTag": "BEST_DEAL | GOOD_DEAL | NORMAL | OVERPRICED",
    "dealScore": 0,
    "recommendedAction": "CREATE_DEAL | SAVE_INVENTORY | NEED_REVIEW"
  }
}

RULES:
1. Extract only real visible data — do NOT guess or hallucinate.
2. Normalize: price → numeric INR only, kmDriven → integer only, year → 4-digit integer
3. fuelType must be only: Petrol, Diesel, CNG, Electric, Hybrid, null
4. transmission must be: Manual or Automatic or null
5. ownerType must be: 1st, 2nd, 3rd, 4th+
6. If data is missing, use null or empty string.
7. Extract seller phone ONLY if clearly visible in content.
8. Ignore ads, banners, recommended listings, unrelated content.
9. Identify sourceWebsite (e.g., spinny, cars24, olx, dealer site).
10. Do NOT fabricate any information.

DEAL SCORING LOGIC:
- Newer year → higher score
- Lower KM → higher score
- Lower price vs expected market → higher score
- 1st owner → bonus score
- Diesel in high demand cities → slight bonus

SCORE RANGE: 80-100 → BEST_DEAL | 60-79 → GOOD_DEAL | 40-59 → NORMAL | below 40 → OVERPRICED
RECOMMENDED ACTION: BEST_DEAL or GOOD_DEAL → CREATE_DEAL | NORMAL → SAVE_INVENTORY | low confidence or missing data → NEED_REVIEW

OUTPUT RULE: Return ONLY valid JSON. No markdown. No explanation. No extra text.
""";

    private HttpClient http;

    private HttpClient getHttp() {
        if (http == null) {
            http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        }
        return http;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    public ExtractionResult extract(String content, String sourceUrl) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            return ExtractionResult.error("Claude API key not configured. Add anthropic.api.key in application.properties.");
        }

        String userMessage = (sourceUrl != null && !sourceUrl.isBlank())
                ? "Source URL: " + sourceUrl + "\n\n<<<\n" + content + "\n>>>"
                : "<<<\n" + content + "\n>>>";

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", 1024,
                "system", SYSTEM_PROMPT,
                "messages", new Object[]{Map.of("role", "user", "content", userMessage)}
        );

        String bodyJson = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = getHttp().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return ExtractionResult.error("API error " + response.statusCode() + ": " + response.body());
        }

        JsonNode resp = mapper.readTree(response.body());
        String text = resp.path("content").get(0).path("text").asText().trim();

        // Strip markdown code fences if model adds them
        if (text.startsWith("```")) {
            text = text.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
        }

        // Validate it's valid JSON
        JsonNode parsed = mapper.readTree(text);
        return ExtractionResult.success(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed));
    }

    // ── Fetch HTML from URL ────────────────────────────────────
    public String fetchPageContent(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();
        HttpResponse<String> resp = getHttp().send(req, HttpResponse.BodyHandlers.ofString());
        String html = resp.body();
        // Strip script/style/head tags to reduce noise
        html = html.replaceAll("(?is)<script[^>]*>.*?</script>", "")
                   .replaceAll("(?is)<style[^>]*>.*?</style>", "")
                   .replaceAll("(?is)<head[^>]*>.*?</head>", "")
                   .replaceAll("<[^>]+>", " ")
                   .replaceAll("\\s{2,}", " ")
                   .trim();
        // Truncate to 8000 chars to stay within context limits
        return html.length() > 8000 ? html.substring(0, 8000) : html;
    }

    // ── Result wrapper ─────────────────────────────────────────
    public record ExtractionResult(boolean ok, String json, String error) {
        static ExtractionResult success(String json) { return new ExtractionResult(true, json, null); }
        static ExtractionResult error(String msg)    { return new ExtractionResult(false, null, msg); }
    }
}
