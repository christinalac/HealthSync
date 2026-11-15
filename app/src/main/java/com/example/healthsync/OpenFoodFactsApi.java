//******************************************
// Program Name: OpenFoodFactsApi.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Searches the Open Food Facts API and converts the results into FoodItem objects with calories, macros, and photos per serving.
//******************************************

package com.example.healthsync;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
// search() blocks, so call it from a background thread (FoodSearchViewModel does this).
public final class OpenFoodFactsApi {
    private OpenFoodFactsApi() {}

    // TODO: replace with your real contact email (Open Food Facts asks for this)
    private static final String USER_AGENT = "HealthSync/1.0 (your-email@example.com)";
    private static final int PAGE_SIZE = 20;
    private static final String FIELDS =
            "code,product_name,brands,image_front_small_url,image_small_url,image_front_url,"
                    + "image_url,images,serving_size,serving_quantity,nutriments";
    private static final Pattern SERVING_REGEX =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:g|ml)", Pattern.CASE_INSENSITIVE);

    public static class RateLimitedException extends IOException {
        public RateLimitedException() {
            super("Rate limited by Open Food Facts");
        }
    }

    public static List<FoodItem> search(String rawQuery) throws IOException {
        String query = sanitize(rawQuery);
        if (query.isEmpty()) return new ArrayList<>();
        try {
            return parse(get(searchALiciousUrl(query)));
        } catch (RateLimitedException e) {
            throw e;
        } catch (Exception e) {
            // Fallback to the older endpoint
            try {
                return parse(get(legacyUrl(query)));
            } catch (JSONException je) {
                throw new IOException("Bad response from Open Food Facts", je);
            }
        }
    }

    // ---------- URLs ----------
    private static String searchALiciousUrl(String q) {
        return new Uri.Builder()
                .scheme("https").authority("search.openfoodfacts.org").appendPath("search")
                .appendQueryParameter("q", q)
                .appendQueryParameter("page_size", String.valueOf(PAGE_SIZE))
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
                .appendQueryParameter("page_size", String.valueOf(PAGE_SIZE))
                .appendQueryParameter("fields", FIELDS)
                .build().toString();
    }

    // Search-a-licious uses Lucene syntax, so strip characters that have special meaning
    private static String sanitize(String q) {
        if (q == null) return "";
        return q.replaceAll("[+\\-!(){}\\[\\]^\"~*?:\\\\/&|]", " ").replaceAll("\\s+", " ").trim();
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
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

    private static FoodItem parseProduct(JSONObject p) {
        String name = p.optString("product_name", "").trim();
        if (name.isEmpty() || name.equals("null")) return null;

        // Skip products with no calorie data: they can't be tracked meaningfully
        JSONObject n = p.optJSONObject("nutriments");
        if (n == null) return null;
        Double kcal100 = num(n, "energy-kcal_100g", "energy-kcal");
        if (kcal100 == null) {
            Double kj = num(n, "energy_100g");      // energy_100g is in kJ
            if (kj == null) return null;
            kcal100 = kj / 4.184;
        }
        Double carbs = num(n, "carbohydrates_100g");
        Double protein = num(n, "proteins_100g");
        Double fat = num(n, "fat_100g");
        double carbs100 = carbs != null ? carbs : 0.0;
        double protein100 = protein != null ? protein : 0.0;
        double fat100 = fat != null ? fat : 0.0;

        // "1 serving" = the product's serving size if it has one, otherwise 100 g
        String servingText = p.optString("serving_size", "").trim();
        if (servingText.isEmpty() || servingText.equals("null")) servingText = null;

        Double servingGrams = null;
        double q = p.optDouble("serving_quantity", Double.NaN);
        if (!Double.isNaN(q) && q > 0) {
            servingGrams = q;
        } else if (servingText != null) {
            Matcher m = SERVING_REGEX.matcher(servingText);
            if (m.find()) {
                try {
                    double g = Double.parseDouble(m.group(1).replace(',', '.'));
                    if (g > 0) servingGrams = g;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        double grams = servingGrams != null ? servingGrams : 100.0;
        String label;
        if (servingGrams == null) label = "100 g";
        else label = servingText != null ? servingText : Format.fmt1(grams) + " g";
        double factor = grams / 100.0;

        String brand = "";
        Object b = p.opt("brands");
        if (b instanceof JSONArray) brand = ((JSONArray) b).optString(0);
        else if (b instanceof String) brand = ((String) b).split(",")[0];
        brand = brand.trim();

        String code = p.optString("code", "");
        return new FoodItem(code, name, brand, findImage(p, code), label,
                kcal100 * factor, carbs100 * factor, protein100 * factor, fat100 * factor);
    }

    // First key that holds a real number, or null
    private static Double num(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                double v = o.optDouble(k, Double.NaN);
                if (!Double.isNaN(v)) return v;
            }
        }
        return null;
    }

    private static String findImage(JSONObject p, String code) {
        String[] keys = {"image_front_small_url", "image_small_url", "image_front_thumb_url",
                "image_front_url", "image_url"};
        for (String key : keys) {
            String url = p.optString(key, "");
            if (url.startsWith("http")) return url.replace("http://", "https://");
        }

        // Fallback: build the URL from the product's "images" object
        JSONObject images = p.optJSONObject("images");
        if (images == null || code.isEmpty()) return null;
        String key = null;
        if (images.has("front_en")) {
            key = "front_en";
        } else {
            Iterator<String> it = images.keys();
            while (it.hasNext()) {
                String k = it.next();
                if (k.startsWith("front_")) { key = k; break; }
            }
        }
        if (key == null) return null;
        JSONObject img = images.optJSONObject(key);
        String rev = img == null ? "" : img.optString("rev", "");
        if (rev.isEmpty()) return null;
        return "https://images.openfoodfacts.org/images/products/" + barcodeFolder(code)
                + "/" + key + "." + rev + ".200.jpg";
    }

    // 3017620422003 -> 301/762/042/2003 (barcodes of 8 digits or fewer are used as-is)
    private static String barcodeFolder(String code) {
        if (code.length() <= 8) return code;
        StringBuilder c = new StringBuilder(code);
        while (c.length() < 13) c.insert(0, '0');
        String s = c.toString();
        return s.substring(0, 3) + "/" + s.substring(3, 6) + "/" + s.substring(6, 9) + "/" + s.substring(9);
    }
}
