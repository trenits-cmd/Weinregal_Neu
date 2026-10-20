package com.example.weinregal_neu.data

import android.util.Log
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object OpenFoodFactsService {

    suspend fun fetchWineByEan(barcode: String, defaultLocation: String): Wine = withContext(Dispatchers.IO) {
        val trimmed = barcode.trim()

        // 1. Fallback Source 1: Open Food Facts API
        val offResult = queryOpenFoodFacts(trimmed, "https://world.openfoodfacts.org/api/v2/product/$trimmed.json", defaultLocation)
        if (offResult != null) return@withContext offResult

        // 2. Fallback Source 2: Open Products Facts API
        val opfResult = queryOpenFoodFacts(trimmed, "https://world.openproductsfacts.org/api/v2/product/$trimmed.json", defaultLocation)
        if (opfResult != null) return@withContext opfResult

        // 3. Fallback Source 3: EAN Prefix Country Heuristic & Smart Wine Generator
        return@withContext generateWineFromEanPrefix(trimmed, defaultLocation)
    }

    private fun queryOpenFoodFacts(barcode: String, apiUrl: String, defaultLocation: String): Wine? {
        try {
            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "WeinregalApp - Android - Version 1.0")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(responseString)
                
                if (jsonObject.optInt("status", 0) == 1) {
                    val product = jsonObject.optJSONObject("product")
                    if (product != null) {
                        val productName = product.optString("product_name", "").ifBlank { "Unbekannter Wein" }
                        val brands = product.optString("brands", "").ifBlank { "Unbekannter Winzer" }
                        val categories = product.optString("categories", "")
                        val countries = product.optString("countries", "Europa")
                        val genericName = product.optString("generic_name", "")
                        val labels = product.optString("labels", "")

                        val searchContext = "$productName $genericName $categories $labels"
                        
                        val wineType = inferWineType(productName, categories)
                        val grapeVariety = extractGrapeFromCategories(categories)
                        val region = extractRegion(categories, countries)
                        val servingTemp = getServingTemperature(wineType)
                        val foodPairing = getFoodPairing(wineType)
                        val year = extractYearFromText(searchContext) ?: 2024

                        return Wine(
                            name = productName,
                            winery = brands,
                            year = year,
                            grapeVariety = grapeVariety,
                            price = 14.99,
                            storageLocation = defaultLocation,
                            status = WineStatus.IN_STOCK,
                            notes = "Erfolgreich geladen via Online-Datenbank (EAN: $barcode)",
                            qrCode = barcode,
                            wineType = wineType,
                            region = region,
                            servingTemperature = servingTemp,
                            foodPairing = foodPairing
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WineLookup", "API error for $apiUrl: ${e.message}")
        }
        return null
    }

    private fun generateWineFromEanPrefix(barcode: String, defaultLocation: String): Wine {
        val prefix = if (barcode.length >= 3) barcode.substring(0, 3) else "300"
        val prefixInt = prefix.toIntOrNull() ?: 300

        val (winery, region, grape, wineType) = when (prefixInt) {
            in 300..379 -> Quad("Domaine de la Vallée", "Bordeaux, Frankreich", "Cabernet Sauvignon", "Rotwein")
            in 800..839 -> Quad("Tenuta dei Colli", "Toskana, Italien", "Sangiovese", "Rotwein")
            in 400..440 -> Quad("Weingut Reichsrat", "Mosel, Deutschland", "Riesling", "Weißwein")
            in 840..849 -> Quad("Bodegas Gran Reserva", "Rioja, Spanien", "Tempranillo", "Rotwein")
            else -> Quad("Château Prestige", "Côtes du Rhône, Frankreich", "Syrah", "Rotwein")
        }

        val servingTemp = getServingTemperature(wineType)
        val foodPairing = getFoodPairing(wineType)

        return Wine(
            name = "$wineType $grape Reserve",
            winery = winery,
            year = 2023,
            grapeVariety = grape,
            price = 16.50,
            storageLocation = defaultLocation,
            status = WineStatus.IN_STOCK,
            notes = "Über EAN-Länderkennung ($prefix) ermittelt. Bitte bei Bedarf anpassen.",
            qrCode = barcode,
            wineType = wineType,
            region = region,
            servingTemperature = servingTemp,
            foodPairing = foodPairing
        )
    }

    private data class Quad(val winery: String, val region: String, val grape: String, val wineType: String)

    private fun inferWineType(name: String, categories: String): String {
        val combined = "$name $categories".lowercase()
        return when {
            combined.contains("champagne") || combined.contains("schaumwein") || combined.contains("sekt") || combined.contains("prosecco") -> "Schaumwein"
            combined.contains("rosé") || combined.contains("rose") -> "Rosé"
            combined.contains("weißwein") || combined.contains("white wine") || combined.contains("riesling") || combined.contains("chardonnay") || combined.contains("sauvignon") -> "Weißwein"
            else -> "Rotwein"
        }
    }

    private fun extractGrapeFromCategories(categories: String): String {
        val lower = categories.lowercase()
        return when {
            lower.contains("riesling") -> "Riesling"
            lower.contains("chardonnay") -> "Chardonnay"
            lower.contains("cabernet") -> "Cabernet Sauvignon"
            lower.contains("merlot") -> "Merlot"
            lower.contains("pinot noir") || lower.contains("spätburgunder") -> "Spätburgunder"
            lower.contains("syrah") || lower.contains("shiraz") -> "Syrah"
            lower.contains("sauvignon") -> "Sauvignon Blanc"
            lower.contains("tempranillo") -> "Tempranillo"
            lower.contains("sangiovese") -> "Sangiovese"
            else -> "Cuvée / Verschiedene"
        }
    }

    private fun extractRegion(categories: String, countries: String): String {
        val lower = categories.lowercase()
        return when {
            lower.contains("bordeaux") -> "Bordeaux, Frankreich"
            lower.contains("burgund") || lower.contains("burgundy") -> "Burgund, Frankreich"
            lower.contains("toskana") || lower.contains("tuscany") -> "Toskana, Italien"
            lower.contains("rioja") -> "Rioja, Spanien"
            lower.contains("mosel") -> "Mosel, Deutschland"
            lower.contains("baden") -> "Baden, Deutschland"
            else -> countries
        }
    }

    private fun getServingTemperature(wineType: String): String {
        return when (wineType) {
            "Weißwein" -> "8-10 °C"
            "Rosé" -> "10-12 °C"
            "Schaumwein" -> "6-8 °C"
            else -> "16-18 °C"
        }
    }

    private fun getFoodPairing(wineType: String): String {
        return when (wineType) {
            "Weißwein" -> "Fisch, Meeresfrüchte, Geflügel, Spargel"
            "Rosé" -> "Salate, Grillgemüse, leichtere Sommergerichte"
            "Schaumwein" -> "Aperitif, Meeresfrüchte, Vorspeisen"
            else -> "Rotes Fleisch, Wild, kräftiger Käse, dunkle Schokolade"
        }
    }

    private fun extractYearFromText(text: String): Int? {
        val regex = Regex("\\b(19[5-9]\\d|20[0-2]\\d)\\b")
        val matches = regex.findAll(text)
        return matches.map { it.value.toInt() }.maxOrNull()
    }
}
