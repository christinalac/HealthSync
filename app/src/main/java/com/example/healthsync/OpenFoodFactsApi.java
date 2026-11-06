package com.example.healthsync;

import android.net.Uri;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Talks to Open Food Facts. Docs: https://openfoodfacts.github.io/openfoodfacts-server/api/
//
//  - Text search uses Search-a-licious (search.openfoodfacts.org), which the docs recommend.
//    If it fails, we fall back to the older /cgi/search.pl endpoint.
//  - The API asks for a custom User-Agent in the form AppName/Version (ContactEmail).
//  - Search is limited to ~10 requests/min per user, so the UI only searches on submit,
//    never while the user is typing.
//
// This call blocks; run it on a background thread (see FoodSearchViewModel).
public final class OpenFoodFactsApi {

    private OpenFoodFactsApi() { }

    // User agent the api asks for
    private static final String USER_AGENT = "HealthSync/1.0 (healthsync0708@gmail.com)";
    private static final int PAGE_SIZE = 20;
    private static final String FIELDS =
            "code,product_name,brands,image_front_small_url,image_small_url,image_front_url,"
                    + "image_url,images,serving_size,serving_quantity,nutriments";

    private static final Pattern SANITIZE_SPECIAL = Pattern.compile("[+\\-!(){}\\[\\]^\"~*?:\\\\/&|]");
    private static final Pattern SANITIZE_SPACES = Pattern.compile("\\s+");
    private static final Pattern SERVING_GRAMS =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:g|ml)", Pattern.CASE_INSENSITIVE);

    public static final class RateLimitedException extends IOException {
        public RateLimitedException() {
            super("Rate limited by Open Food Facts");
        }
    }

    public static List<FoodItem> search(String rawQuery) throws IOException, JSONException {
        String query = sanitize(rawQuery);
        if (query.isEmpty()) return new ArrayList<>();
        try {
            return parse(get(searchALiciousUrl(query)));
        } catch (RateLimitedException e) {
            throw e;
        } catch (Exception e) {
            return parse(get(legacyUrl(query)));   // fallback
        }
    }

    // ---------- URLs ----------
    private static String searchALiciousUrl(String q) {
        return new Uri.Builder()
                .scheme("https").authority("search.openfoodfacts.org").appendPath("search")
                .appendQueryParameter("q", q)
                .appendQueryParameter("page_size", Integer.toString(PAGE_SIZE))
                .appendQueryParameter("fields", FIELDS)
                .build().toString();
    }

    private static String legacyUrl(String q) {
        return new Uri.Builder()
                .scheme("https").authority("world.openfoodfacts.org")
                .appendPath("cgi").appendPath("search.pl")
                .appendQueryParameter("search_terms", q)
                .appendQueryParameter("search_simple", "1")
                .appendQueryParameter("action", "process")
                .appendQueryParameter("json", "1")
                .appendQueryParameter("page_size", Integer.toString(PAGE_SIZE))
                .appendQueryParameter("fields", FIELDS)
                .build().toString();
    }

    // Search-a-licious uses Lucene syntax, so strip characters that have special meaning
    private static String sanitize(String q) {
        String replaced = SANITIZE_SPECIAL.matcher(q).replaceAll(" ");
        return SANITIZE_SPACES.matcher(replaced).replaceAll(" ").trim();
    }

    // ---------- HTTP ----------
    private static String get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            if (code == 429) throw new RateLimitedException();
            if (code != 200) throw new IOException("HTTP " + code);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // ---------- Parsing ----------
    // Search-a-licious returns {"hits":[...]}, the legacy endpoint returns {"products":[...]}
    private static List<FoodItem> parse(String text) throws JSONException {
        JSONObject root = new JSONObject(text);
        JSONArray arr = root.optJSONArray("hits");
        if (arr == null) arr = root.optJSONArray("products");
        List<FoodItem> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.optJSONObject(i);
            if (p == null) continue;
            FoodItem item = parseProduct(p);
            if (item != null) out.add(item);
        }
        return out;
    }

    @Nullable
    private static FoodItem parseProduct(JSONObject p) {
        String name = p.optString("product_name").trim();
        if (name.isEmpty() || name.equals("null")) return null;

        // Skip products with no calorie data: they can't be tracked meaningfully
        JSONObject n = p.optJSONObject("nutriments");
        if (n == null) return null;
        Double kcal100 = num(n, "energy-kcal_100g", "energy-kcal");
        if (kcal100 == null) {
            Double energy = num(n, "energy_100g");
            if (energy != null) kcal100 = energy / 4.184;   // energy_100g is in kJ
        }
        if (kcal100 == null) return null;

        double carbs100 = orZero(num(n, "carbohydrates_100g"));
        double protein100 = orZero(num(n, "proteins_100g"));
        double fat100 = orZero(num(n, "fat_100g"));

        // "1 serving" = the product's serving size if it has one, otherwise 100 g
        String servingTextRaw = p.optString("serving_size").trim();
        String servingText = (!servingTextRaw.isEmpty() && !servingTextRaw.equals("null"))
                ? servingTextRaw : null;

        Double servingGrams = null;
        double sq = p.optDouble("serving_quantity", Double.NaN);
        if (!Double.isNaN(sq) && sq > 0) {
            servingGrams = sq;
        } else if (servingText != null) {
            Matcher m = SERVING_GRAMS.matcher(servingText);
            if (m.find()) {
                try {
                    double parsed = Double.parseDouble(m.group(1).replace(',', '.'));
                    if (parsed > 0) servingGrams = parsed;
                } catch (NumberFormatException ignored) { }
            }
        }

        double grams = servingGrams != null ? servingGrams : 100.0;
        String label;
        if (servingGrams != null) {
            label = servingText != null ? servingText : (NutritionFormat.fmt1(grams) + " g");
        } else {
            label = "100 g";
        }
        double factor = grams / 100.0;

        String brand = "";
        Object b = p.opt("brands");
        if (b instanceof JSONArray) {
            brand = ((JSONArray) b).optString(0);
        } else if (b instanceof String) {
            String bs = (String) b;
            int comma = bs.indexOf(',');
            brand = comma >= 0 ? bs.substring(0, comma) : bs;
        }
        brand = brand.trim();

        String code = p.optString("code");
        return new FoodItem(
                code,
                name,
                brand,
                findImage(p, code),
                label,
                kcal100 * factor,
                carbs100 * factor,
                protein100 * factor,
                fat100 * factor
        );
    }

    @Nullable
    private static Double num(JSONObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.isNull(k)) {
                double v = obj.optDouble(k, Double.NaN);
                if (!Double.isNaN(v)) return v;
            }
        }
        return null;
    }

    private static double orZero(@Nullable Double v) {
        return v != null ? v : 0.0;
    }

    @Nullable
    private static String findImage(JSONObject p, String code) {
        String[] directKeys = {
                "image_front_small_url", "image_small_url", "image_front_thumb_url",
                "image_front_url", "image_url"
        };
        for (String key : directKeys) {
            String val = p.optString(key);
            if (val.startsWith("http")) {
                return val.replace("http://", "https://");
            }
        }

        // Fallback: build the URL from the product's "images" object
        JSONObject images = p.optJSONObject("images");
        if (images == null) return null;
        String key;
        if (images.has("front_en")) {
            key = "front_en";
        } else {
            key = null;
            for (Iterator<String> it = images.keys(); it.hasNext(); ) {
                String k = it.next();
                if (k.startsWith("front_")) {
                    key = k;
                    break;
                }
            }
            if (key == null) return null;
        }
        JSONObject img = images.optJSONObject(key);
        String rev = img != null ? img.optString("rev") : "";
        if (rev.isEmpty() || code.isEmpty()) return null;
        return "https://images.openfoodfacts.org/images/products/"
                + barcodeFolder(code) + "/" + key + "." + rev + ".200.jpg";
    }

    // 3017620422003 -> 301/762/042/2003 (barcodes of 8 digits or fewer are used as-is)
    private static String barcodeFolder(String code) {
        if (code.length() <= 8) return code;
        StringBuilder padded = new StringBuilder(code);
        while (padded.length() < 13) padded.insert(0, '0');
        String c = padded.toString();
        return c.substring(0, 3) + "/" + c.substring(3, 6) + "/"
                + c.substring(6, 9) + "/" + c.substring(9);
    }
}
