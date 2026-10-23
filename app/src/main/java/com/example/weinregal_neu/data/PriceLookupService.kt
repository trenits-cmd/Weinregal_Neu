package com.example.weinregal_neu.data

import android.util.Log
import com.example.weinregal_neu.model.Wine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PriceOffer(
    val merchant: String,
    val title: String,
    val price: Double,
    val url: String,
    val available: Boolean = true
)

data class PriceSourceResult(
    val name: String,
    val ok: Boolean,
    val offers: List<PriceOffer> = emptyList(),
    val info: String? = null
)

data class PriceLookup(
    val query: String,
    val offers: List<PriceOffer>,
    val sources: List<PriceSourceResult>
) {
    val hasOffers: Boolean get() = offers.isNotEmpty()
    val lowestOffer: PriceOffer? get() = offers.minByOrNull { it.price }
}

/**
 * Echte Preisabfrage bei mehreren Quellen.
 *
 * Aktuell werden die öffentlichen Shopify-Such-Endpunkte zweier Weinhaendler
 * genutzt. Beide liefern Produktname, Preis und Link. Zusaetzlich wird immer
 * eine Websuche zum Oeffnen im Browser angeboten, falls kein Treffer dabei ist.
 *
 * Es gibt bewusst keinen erfundenen Schaetzwert: Wenn keine Quelle antwortet,
 * sagt die App das offen.
 */
object PriceLookupService {

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"

    private data class ShopifyShop(val merchant: String, val baseUrl: String)

    private val shops = listOf(
        ShopifyShop("Vinocentral", "https://www.vinocentral.de"),
        ShopifyShop("Weinpaket", "https://www.weinpaket.de")
    )

    suspend fun lookup(wine: Wine): PriceLookup = withContext(Dispatchers.IO) {
        val queries = buildQueries(wine)
        val sourceResults = mutableListOf<PriceSourceResult>()

        for (shop in shops) {
            sourceResults += queryShop(shop, queries)
        }

        val offers = sourceResults
            .flatMap { it.offers }
            .filter { offer -> isRelevant(wine, offer.title) }
            .sortedBy { it.price }
            .distinctBy { it.merchant + it.title + it.price }

        PriceLookup(
            query = queries.firstOrNull().orEmpty(),
            offers = offers,
            sources = sourceResults
        )
    }

    /**
     * Prueft, ob ein Shop-Treffer zum gesuchten Wein passt. So werden
     * Preisvorschlaege fuer voellig andere Weine vermieden.
     */
    internal fun isRelevant(wine: Wine, productTitle: String): Boolean {
        val stopWords = setOf(
            "wein", "weisswein", "rotwein", "rose", "reserve", "trocken",
            "halbtrocken", "lieblich", "spatlese", "auslese", "kabinett", "bio"
        )
        val terms = normalizeForMatch("${wine.name} ${wine.winery} ${wine.grapeVariety}")
            .split(' ')
            .filter { term -> term.length >= 4 && term !in stopWords && term.toIntOrNull() == null }
        if (terms.isEmpty()) return true
        val haystack = normalizeForMatch(productTitle)
        return terms.any { term -> haystack.contains(term) }
    }

    internal fun normalizeForMatch(text: String): String {
        val german = text.lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
        val withoutAccents = java.text.Normalizer
            .normalize(german, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutAccents
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }

    private fun buildQueries(wine: Wine): List<String> {
        val queries = linkedSetOf<String>()
        val name = wine.name.trim()
        val winery = wine.winery.trim()
        if (name.isNotBlank()) {
            queries += listOfNotNull(name, winery.takeIf { it.isNotBlank() }?.let { "$it $name" })
            if (wine.year > 0) queries += "$name ${wine.year}"
        }
        if (wine.grapeVariety.isNotBlank()) {
            queries += wine.grapeVariety.trim()
        }
        return queries.filter { it.isNotBlank() }.take(3)
    }

    private fun queryShop(shop: ShopifyShop, queries: List<String>): PriceSourceResult {
        var lastError: String? = null
        for (query in queries) {
            val url = "${shop.baseUrl}/search/suggest.json" +
                "?q=${URLEncoder.encode(query, "UTF-8")}" +
                "&resources[type]=product&resources[limit]=6"
            try {
                val body = httpGet(url, timeoutMs = 6000)
                val offers = parseShopifyProducts(body, shop)
                if (offers.isNotEmpty()) {
                    return PriceSourceResult(shop.merchant, ok = true, offers = offers.take(3))
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Netzwerkfehler"
                Log.w("PriceLookup", "${shop.merchant}: $lastError")
                break
            }
        }
        return PriceSourceResult(
            name = shop.merchant,
            ok = lastError == null,
            info = lastError ?: "kein Treffer"
        )
    }

    /** Intern, damit die JSON-Verarbeitung testbar bleibt. */
    internal fun parseShopifyProducts(body: String, merchant: String, baseUrl: String): List<PriceOffer> {
        val root = JSONObject(body)
        val products = root
            .optJSONObject("resources")
            ?.optJSONObject("results")
            ?.optJSONArray("products")
            ?: return emptyList()

        val offers = mutableListOf<PriceOffer>()
        for (index in 0 until products.length()) {
            val product = products.optJSONObject(index) ?: continue
            val title = product.optString("title").trim()
            if (title.isBlank()) continue

            val price = product.optString("price").replace(",", ".").toDoubleOrNull()
                ?: product.optDouble("price", Double.NaN).takeIf { !it.isNaN() }
                ?: continue
            if (price <= 0.0) continue

            val productUrl = product.optString("url")
            val absoluteUrl = if (productUrl.startsWith("http")) {
                productUrl
            } else {
                baseUrl.trimEnd('/') + "/" + productUrl.trimStart('/')
            }

            offers += PriceOffer(
                merchant = merchant,
                title = title,
                price = price,
                url = absoluteUrl,
                available = product.optBoolean("available", true)
            )
        }
        return offers
    }

    private fun parseShopifyProducts(body: String, shop: ShopifyShop): List<PriceOffer> =
        parseShopifyProducts(body, shop.merchant, shop.baseUrl)

    private fun httpGet(url: String, timeoutMs: Int): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("Accept-Language", "de-DE,de;q=0.9")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    /** Link fuer die manuelle Websuche, wenn keine Quelle einen Preis liefert. */
    fun webSearchUrl(wine: Wine): String {
        val query = listOfNotNull(
            wine.winery.takeIf { it.isNotBlank() },
            wine.name.takeIf { it.isNotBlank() },
            wine.year.takeIf { it > 0 }?.toString()
        ).joinToString(" ") + " Preis"
        return "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8")
    }
}
