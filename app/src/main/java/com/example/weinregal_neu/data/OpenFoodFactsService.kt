package com.example.weinregal_neu.data

import android.util.Log
import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Barcode-Lookup über Open Food Facts (Getränke) und Open Products Facts.
 *
 * Es werden ausschliesslich Daten übernommen, die die Quelle wirklich liefert.
 * Findet sich kein Produkt, wird null zurückgegeben – die App erfindet keinen
 * Wein und keinen Preis mehr.
 */
object OpenFoodFactsService {

    private const val USER_AGENT = "WeinregalApp/2.0 (Android)"

    suspend fun fetchWineByEan(barcode: String, defaultLocation: String): Wine? =
        withContext(Dispatchers.IO) {
            val trimmed = barcode.trim()
            if (trimmed.isEmpty()) return@withContext null

            val sources = listOf(
                "https://world.openfoodfacts.org/api/v2/product/$trimmed.json",
                "https://world.openproductsfacts.org/api/v2/product/$trimmed.json"
            )

            for (source in sources) {
                val wine = query(source, trimmed, defaultLocation)
                if (wine != null) return@withContext wine
            }
            null
        }

    private fun query(apiUrl: String, barcode: String, defaultLocation: String): Wine? {
        try {
            val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", USER_AGENT)
            }

            try {
                if (connection.responseCode !in 200..299) return null
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(response)
                if (root.optInt("status", 0) != 1) return null
                val product = root.optJSONObject("product") ?: return null

                val productName = product.optString("product_name").trim()
                    .ifBlank { product.optString("product_name_de").trim() }
                if (productName.isBlank()) return null

                val brands = product.optString("brands").trim().substringBefore(',')
                val categories = product.optString("categories")
                val countries = product.optString("countries")
                val labels = product.optString("labels")

                val wineType = inferWineType(productName, categories)
                val grape = extractGrape(categories, labels, productName)
                val region = extractRegion(categories, countries)

                return Wine(
                    name = productName,
                    winery = brands,
                    year = extractYear("$productName $categories") ?: 0,
                    grapeVariety = grape,
                    price = 0.0,
                    storageLocation = defaultLocation,
                    notes = "Produktdaten von Open Food Facts (EAN $barcode). Preis bitte prüfen/lookup.",
                    qrCode = barcode,
                    wineType = wineType,
                    region = region,
                    servingTemperature = defaultTemperature(wineType),
                    foodPairing = defaultFoodPairing(wineType)
                )
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.w("WineLookup", "Quelle $apiUrl nicht erreichbar: ${e.message}")
        }
        return null
    }

    private fun inferWineType(name: String, categories: String): String {
        val combined = "$name $categories".lowercase()
        return when {
            combined.contains("champagne") || combined.contains("schaumwein") ||
                combined.contains("sekt") || combined.contains("prosecco") ||
                combined.contains("sparkling") -> "Schaumwein"
            combined.contains("rosé") || combined.contains("rose wine") -> "Rosé"
            combined.contains("weißwein") || combined.contains("white wine") ||
                combined.contains("riesling") || combined.contains("chardonnay") ||
                combined.contains("sauvignon blanc") || combined.contains("grüner veltliner") -> "Weißwein"
            else -> "Rotwein"
        }
    }

    private fun extractGrape(categories: String, labels: String, name: String): String {
        val lower = "$categories $labels $name".lowercase()
        return when {
            lower.contains("riesling") -> "Riesling"
            lower.contains("chardonnay") -> "Chardonnay"
            lower.contains("sauvignon blanc") -> "Sauvignon Blanc"
            lower.contains("cabernet sauvignon") -> "Cabernet Sauvignon"
            lower.contains("merlot") -> "Merlot"
            lower.contains("pinot noir") || lower.contains("spätburgunder") -> "Spätburgunder"
            lower.contains("syrah") || lower.contains("shiraz") -> "Syrah"
            lower.contains("tempranillo") -> "Tempranillo"
            lower.contains("sangiovese") -> "Sangiovese"
            lower.contains("nebbiolo") -> "Nebbiolo"
            lower.contains("grüner veltliner") || lower.contains("veltliner") -> "Grüner Veltliner"
            lower.contains("gewürztraminer") || lower.contains("gewurztraminer") -> "Gewürztraminer"
            lower.contains("grauburgunder") || lower.contains("pinot gris") -> "Grauburgunder"
            lower.contains("weißburgunder") || lower.contains("pinot blanc") -> "Weißburgunder"
            else -> ""
        }
    }

    private fun extractRegion(categories: String, countries: String): String {
        val lower = categories.lowercase()
        val region = when {
            lower.contains("bordeaux") -> "Bordeaux"
            lower.contains("burgund") || lower.contains("burgundy") -> "Burgund"
            lower.contains("toskana") || lower.contains("tuscany") -> "Toskana"
            lower.contains("rioja") -> "Rioja"
            lower.contains("mosel") -> "Mosel"
            lower.contains("baden") -> "Baden"
            lower.contains("pfalz") -> "Pfalz"
            lower.contains("rheinhessen") -> "Rheinhessen"
            lower.contains("champagne") -> "Champagne"
            else -> ""
        }
        return listOf(region, countries.trim()).filter { it.isNotBlank() }.joinToString(", ")
    }

    private fun defaultTemperature(wineType: String): String = when (wineType) {
        "Weißwein" -> "8-12 °C"
        "Rosé" -> "10-12 °C"
        "Schaumwein" -> "6-8 °C"
        else -> "16-18 °C"
    }

    private fun defaultFoodPairing(wineType: String): String = when (wineType) {
        "Weißwein" -> "Fisch, Meeresfrüchte, Geflügel"
        "Rosé" -> "Salate, Grillgemüse, Sommerküche"
        "Schaumwein" -> "Aperitif, Meeresfrüchte, Vorspeisen"
        else -> "Rotes Fleisch, Wild, kräftiger Käse"
    }

    private fun extractYear(text: String): Int? {
        val regex = Regex("\\b(19[5-9]\\d|20[0-3]\\d)\\b")
        return regex.findAll(text).mapNotNull { it.value.toIntOrNull() }.maxOrNull()
    }

    /** Standard-Lagerort für neu gescannte Weine. */
    fun defaultLocation(): String = RackSlots.DEFAULT
}
