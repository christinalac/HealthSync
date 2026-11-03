package com.example.healthsync

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

// Talks to Open Food Facts. Docs: https://openfoodfacts.github.io/openfoodfacts-server/api/
//
//  - Text search uses Search-a-licious (search.openfoodfacts.org), which the docs recommend.
//    If it fails, we fall back to the older /cgi/search.pl endpoint.
//  - The API asks for a custom User-Agent in the form AppName/Version (ContactEmail).
//  - Search is limited to ~10 requests/min per user, so the UI only searches on submit,
//    never while the user is typing.
object OpenFoodFactsApi {

    // User agent the api asks for
    private const val USER_AGENT = "HealthSync/1.0 (healthsync0708@gmail.com)"
    private const val PAGE_SIZE = 20
    private const val FIELDS =
        "code,product_name,brands,image_front_small_url,image_small_url,image_front_url," +
        "image_url,images,serving_size,serving_quantity,nutriments"

    class RateLimitedException : IOException("Rate limited by Open Food Facts")

    suspend fun search(rawQuery: String): List<FoodItem> = withContext(Dispatchers.IO) {
        val query = sanitize(rawQuery)
        if (query.isEmpty()) return@withContext emptyList()
        try {
            parse(get(searchALiciousUrl(query)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: RateLimitedException) {
            throw e
        } catch (e: Exception) {
            parse(get(legacyUrl(query)))   // fallback
        }
    }

    // ---------- URLs ----------
    private fun searchALiciousUrl(q: String) = Uri.Builder()
        .scheme("https").authority("search.openfoodfacts.org").appendPath("search")
        .appendQueryParameter("q", q)
        .appendQueryParameter("page_size", PAGE_SIZE.toString())
        .appendQueryParameter("fields", FIELDS)
        .build().toString()

    private fun legacyUrl(q: String) = Uri.Builder()
        .scheme("https").authority("world.openfoodfacts.org")
        .appendPath("cgi").appendPath("search.pl")
        .appendQueryParameter("search_terms", q)
        .appendQueryParameter("search_simple", "1")
        .appendQueryParameter("action", "process")
        .appendQueryParameter("json", "1")
        .appendQueryParameter("page_size", PAGE_SIZE.toString())
        .appendQueryParameter("fields", FIELDS)
        .build().toString()

    // Search-a-licious uses Lucene syntax, so strip characters that have special meaning
    private fun sanitize(q: String) =
        q.replace(Regex("""[+\-!(){}\[\]^"~*?:\\/&|]"""), " ").replace(Regex("\\s+"), " ").trim()

    // ---------- HTTP ----------
    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.setRequestProperty("Accept", "application/json")
            val code = conn.responseCode
            if (code == 429) throw RateLimitedException()
            if (code != 200) throw IOException("HTTP $code")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    // ---------- Parsing ----------
    // Search-a-licious returns {"hits":[...]}, the legacy endpoint returns {"products":[...]}
    private fun parse(text: String): List<FoodItem> {
        val root = JSONObject(text)
        val arr: JSONArray = root.optJSONArray("hits") ?: root.optJSONArray("products") ?: return emptyList()
        val out = ArrayList<FoodItem>()
        for (i in 0 until arr.length()) {
            parseProduct(arr.optJSONObject(i) ?: continue)?.let { out.add(it) }
        }
        return out
    }

    private fun parseProduct(p: JSONObject): FoodItem? {
        val name = p.optString("product_name").trim()
        if (name.isEmpty() || name == "null") return null

        // Skip products with no calorie data: they can't be tracked meaningfully
        val n = p.optJSONObject("nutriments") ?: return null
        val kcal100 = n.num("energy-kcal_100g", "energy-kcal")
            ?: n.num("energy_100g")?.let { it / 4.184 }      // energy_100g is in kJ
            ?: return null
        val carbs100 = n.num("carbohydrates_100g") ?: 0.0
        val protein100 = n.num("proteins_100g") ?: 0.0
        val fat100 = n.num("fat_100g") ?: 0.0

        // "1 serving" = the product's serving size if it has one, otherwise 100 g
        val servingText = p.optString("serving_size").trim().takeIf { it.isNotEmpty() && it != "null" }
        val servingGrams = p.optDouble("serving_quantity", Double.NaN).takeIf { !it.isNaN() && it > 0 }
            ?: servingText?.let { Regex("""(\d+(?:[.,]\d+)?)\s*(?:g|ml)""", RegexOption.IGNORE_CASE).find(it) }
                ?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it > 0 }
        val grams = servingGrams ?: 100.0
        val label = if (servingGrams != null) (servingText ?: "${grams.fmt1()} g") else "100 g"
        val factor = grams / 100.0

        val brand = when (val b = p.opt("brands")) {
            is JSONArray -> b.optString(0)
            is String -> b.split(",").first()
            else -> ""
        }.trim()

        val code = p.optString("code")
        return FoodItem(
            code = code,
            name = name,
            brand = brand,
            imageUrl = findImage(p, code),
            servingLabel = label,
            kcal = kcal100 * factor,
            carbsG = carbs100 * factor,
            proteinG = protein100 * factor,
            fatG = fat100 * factor
        )
    }

    private fun JSONObject.num(vararg keys: String): Double? {
        for (k in keys) {
            if (has(k) && !isNull(k)) {
                val v = optDouble(k, Double.NaN)
                if (!v.isNaN()) return v
            }
        }
        return null
    }

    private fun findImage(p: JSONObject, code: String): String? {
        val direct = listOf(
            "image_front_small_url", "image_small_url", "image_front_thumb_url",
            "image_front_url", "image_url"
        ).firstNotNullOfOrNull { key -> p.optString(key).takeIf { it.startsWith("http") } }
        if (direct != null) return direct.replace("http://", "https://")

        // Fallback: build the URL from the product's "images" object
        val images = p.optJSONObject("images") ?: return null
        val key = if (images.has("front_en")) "front_en"
                  else images.keys().asSequence().firstOrNull { it.startsWith("front_") } ?: return null
        val rev = images.optJSONObject(key)?.optString("rev").orEmpty()
        if (rev.isEmpty() || code.isEmpty()) return null
        return "https://images.openfoodfacts.org/images/products/${barcodeFolder(code)}/$key.$rev.200.jpg"
    }

    // 3017620422003 -> 301/762/042/2003 (barcodes of 8 digits or fewer are used as-is)
    private fun barcodeFolder(code: String): String {
        if (code.length <= 8) return code
        val c = code.padStart(13, '0')
        return "${c.substring(0, 3)}/${c.substring(3, 6)}/${c.substring(6, 9)}/${c.substring(9)}"
    }
}
