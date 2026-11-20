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
        } catch (RateLimitedException rateLimitedException) {
            throw rateLimitedException;
        } catch (Exception exception) {
            // Fallback to the older endpoint
            try {
                return parse(get(legacyUrl(query)));
            } catch (JSONException jsonException) {
                throw new IOException("Bad response from Open Food Facts", jsonException);
            }
        }
    }

    // ---------- URLs ----------
    private static String searchALiciousUrl(String searchText) {
        return new Uri.Builder()
                .scheme("https").authority("search.openfoodfacts.org").appendPath("search")
                .appendQueryParameter("q", searchText)
                .appendQueryParameter("page_size", String.valueOf(PAGE_SIZE))
                .appendQueryParameter("fields", FIELDS)
                .build().toString();
    }

    private static String legacyUrl(String searchText) {
        return new Uri.Builder()
                .scheme("https").authority("world.openfoodfacts.org")
                .appendPath("cgi").appendPath("search.pl")
                .appendQueryParameter("search_terms", searchText)
                .appendQueryParameter("search_simple", "1")
                .appendQueryParameter("action", "process")
                .appendQueryParameter("json", "1")
                .appendQueryParameter("page_size", String.valueOf(PAGE_SIZE))
                .appendQueryParameter("fields", FIELDS)
                .build().toString();
    }

    // Search-a-licious uses Lucene syntax, so strip characters that have special meaning
    private static String sanitize(String rawText) {
        if (rawText == null) return "";
        return rawText.replaceAll("[+\\-!(){}\\[\\]^\"~*?:\\\\/&|]", " ").replaceAll("\\s+", " ").trim();
    }

    // ---------- HTTP ----------
    private static String get(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        StringBuilder responseBuilder = new StringBuilder();
        String line = null;
        int code = 0;
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(15_000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");
            code = connection.getResponseCode();
            if (code == 429) throw new RateLimitedException();
            if (code != 200) throw new IOException("HTTP " + code);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                while ((line = reader.readLine()) != null) responseBuilder.append(line);
            }
            return responseBuilder.toString();
        } finally {
            connection.disconnect();
        }
    }

    // ---------- Parsing ----------
    // Search-a-licious returns {"hits":[...]}, the legacy endpoint returns {"products":[...]}
    private static List<FoodItem> parse(String text) throws JSONException {
        JSONObject root = new JSONObject(text);
        JSONArray productsArray = root.optJSONArray("hits");
        List<FoodItem> foods = new ArrayList<>();
        JSONObject product = null;
        FoodItem item = null;
        if (productsArray == null) productsArray = root.optJSONArray("products");
        if (productsArray == null) return foods;
        for (int i = 0; i < productsArray.length(); i++) {
            product = productsArray.optJSONObject(i);
            if (product == null) continue;
            item = parseProduct(product);
            if (item != null) foods.add(item);
        }
        return foods;
    }

    private static FoodItem parseProduct(JSONObject product) {
        String name = product.optString("product_name", "").trim();
        JSONObject nutriments = product.optJSONObject("nutriments");
        Double kcal100 = null;
        Double kilojoules = null;
        Double carbs = null;
        Double protein = null;
        Double fat = null;
        double carbs100 = 0.0;
        double protein100 = 0.0;
        double fat100 = 0.0;
        String servingText = null;
        Double servingGrams = null;
        double servingQuantity = Double.NaN;
        Matcher servingMatcher = null;
        double parsedGrams = 0.0;
        double grams = 100.0;
        String label = "100 g";
        double factor = 1.0;
        String brand = "";
        Object brandValue = null;
        String code = "";

        if (name.isEmpty() || name.equals("null")) return null;

        // Skip products with no calorie data: they can't be tracked meaningfully
        if (nutriments == null) return null;
        kcal100 = num(nutriments, "energy-kcal_100g", "energy-kcal");
        if (kcal100 == null) {
            // energy_100g is in kJ
            kilojoules = num(nutriments, "energy_100g");
            if (kilojoules == null) return null;
            kcal100 = kilojoules / 4.184;
        }
        carbs = num(nutriments, "carbohydrates_100g");
        protein = num(nutriments, "proteins_100g");
        fat = num(nutriments, "fat_100g");
        if (carbs != null) {
            carbs100 = carbs;
        } else {
            carbs100 = 0.0;
        }
        if (protein != null) {
            protein100 = protein;
        } else {
            protein100 = 0.0;
        }
        if (fat != null) {
            fat100 = fat;
        } else {
            fat100 = 0.0;
        }

        // "1 serving" = the product's serving size if it has one, otherwise 100 g
        servingText = product.optString("serving_size", "").trim();
        if (servingText.isEmpty() || servingText.equals("null")) servingText = null;

        servingQuantity = product.optDouble("serving_quantity", Double.NaN);
        if (!Double.isNaN(servingQuantity) && servingQuantity > 0) {
            servingGrams = servingQuantity;
        } else if (servingText != null) {
            servingMatcher = SERVING_REGEX.matcher(servingText);
            if (servingMatcher.find()) {
                try {
                    parsedGrams = Double.parseDouble(servingMatcher.group(1).replace(',', '.'));
                    if (parsedGrams > 0) servingGrams = parsedGrams;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (servingGrams != null) {
            grams = servingGrams;
        } else {
            grams = 100.0;
        }
        if (servingGrams == null) {
            label = "100 g";
        } else if (servingText != null) {
            label = servingText;
        } else {
            label = Format.fmt1(grams) + " g";
        }
        factor = grams / 100.0;

        brandValue = product.opt("brands");
        if (brandValue instanceof JSONArray) brand = ((JSONArray) brandValue).optString(0);
        else if (brandValue instanceof String) brand = ((String) brandValue).split(",")[0];
        brand = brand.trim();

        code = product.optString("code", "");
        return new FoodItem(code, name, brand, findImage(product, code), label,
                kcal100 * factor, carbs100 * factor, protein100 * factor, fat100 * factor);
    }

    // First key that holds a real number, or null
    private static Double num(JSONObject json, String... keys) {
        double value = Double.NaN;
        for (String key : keys) {
            if (json.has(key) && !json.isNull(key)) {
                value = json.optDouble(key, Double.NaN);
                if (!Double.isNaN(value)) return value;
            }
        }
        return null;
    }

    private static String findImage(JSONObject product, String code) {
        String[] keys = {"image_front_small_url", "image_small_url", "image_front_thumb_url",
                "image_front_url", "image_url"};
        String key = null;
        String rev = "";
        JSONObject images = null;
        JSONObject image = null;
        Iterator<String> keyIterator = null;
        String imageKey = null;
        for (String candidateKey : keys) {
            String url = product.optString(candidateKey, "");
            if (url.startsWith("http")) return url.replace("http://", "https://");
        }

        // Fallback: build the URL from the product's "images" object
        images = product.optJSONObject("images");
        if (images == null || code.isEmpty()) return null;
        if (images.has("front_en")) {
            key = "front_en";
        } else {
            keyIterator = images.keys();
            while (keyIterator.hasNext()) {
                imageKey = keyIterator.next();
                if (imageKey.startsWith("front_")) { key = imageKey; break; }
            }
        }
        if (key == null) return null;
        image = images.optJSONObject(key);
        if (image == null) {
            rev = "";
        } else {
            rev = image.optString("rev", "");
        }
        if (rev.isEmpty()) return null;
        return "https://images.openfoodfacts.org/images/products/" + barcodeFolder(code)
                + "/" + key + "." + rev + ".200.jpg";
    }

    // 3017620422003 -> 301/762/042/2003 (barcodes of 8 digits or fewer are used as-is)
    private static String barcodeFolder(String code) {
        if (code.length() <= 8) return code;
        StringBuilder paddedBuilder = new StringBuilder(code);
        String padded = "";
        while (paddedBuilder.length() < 13) paddedBuilder.insert(0, '0');
        padded = paddedBuilder.toString();
        return padded.substring(0, 3) + "/" + padded.substring(3, 6) + "/" + padded.substring(6, 9) + "/" + padded.substring(9);
    }
}
