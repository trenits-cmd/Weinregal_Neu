package com.example.weinregal_neu.data

import android.content.Context
import android.content.SharedPreferences
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import kotlinx.serialization.json.Json

object WineStorage {
    private const val PREFS_NAME = "weinregal_prefs"
    private const val KEY_WINES = "key_wines"

    private val json = Json { ignoreUnknownKeys = true }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveWines(context: Context, wines: List<Wine>) {
        val jsonString = json.encodeToString(wines)
        getPrefs(context).edit().putString(KEY_WINES, jsonString).apply()
    }

    fun loadWines(context: Context): List<Wine> {
        val jsonString = getPrefs(context).getString(KEY_WINES, null)
        if (jsonString.isNullOrEmpty()) {
            val defaultWines = getDefaultWines()
            saveWines(context, defaultWines)
            return defaultWines
        }
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            getDefaultWines()
        }
    }

    private fun getDefaultWines(): List<Wine> {
        return listOf(
            Wine(
                name = "Château Margaux Premier Grand Cru",
                winery = "Château Margaux",
                year = 2015,
                grapeVariety = "Cabernet Sauvignon",
                price = 650.0,
                storageLocation = "Regal Fach 1",
                status = WineStatus.IN_STOCK,
                notes = "Hervorragender Jahrgang, perfekt zu rotem Fleisch.",
                qrCode = "WINE-10001"
            ),
            Wine(
                name = "Barolo DOCG Riserva",
                winery = "Vietti",
                year = 2016,
                grapeVariety = "Nebbiolo",
                price = 120.0,
                storageLocation = "Regal Fach 2",
                status = WineStatus.IN_STOCK,
                notes = "Tanninreich, langer Abgang.",
                qrCode = "WINE-10002"
            ),
            Wine(
                name = "Riesling Spätlese Trocken",
                winery = "Dr. Loosen",
                year = 2020,
                grapeVariety = "Riesling",
                price = 24.5,
                storageLocation = "Regal Fach 3",
                status = WineStatus.IN_STOCK,
                notes = "Fruchtig mit schöner Mineralität.",
                qrCode = "WINE-10003"
            ),
            Wine(
                name = "Brunello di Montalcino",
                winery = "Casanova di Neri",
                year = 2017,
                grapeVariety = "Sangiovese",
                price = 85.0,
                storageLocation = "Regal Fach 5",
                status = WineStatus.IN_STOCK,
                notes = "Klassischer toskanischer Rotwein.",
                qrCode = "WINE-10004"
            ),
            Wine(
                name = "Chablis Grand Cru Les Clos",
                winery = "Domaine William Fèvre",
                year = 2019,
                grapeVariety = "Chardonnay",
                price = 95.0,
                storageLocation = "Kühlschrank",
                status = WineStatus.IN_STOCK,
                notes = "Kühler Weißwein, ideal gekühlt trinken.",
                qrCode = "WINE-10005"
            ),
            Wine(
                name = "Rioja Gran Reserva",
                winery = "Marqués de Riscal",
                year = 2014,
                grapeVariety = "Tempranillo",
                price = 38.0,
                storageLocation = "Speisekammer",
                status = WineStatus.IN_STOCK,
                notes = "Gut gereift, Noten von Vanille und Tabak.",
                qrCode = "WINE-10006"
            ),
            Wine(
                name = "Pinot Noir Estate",
                winery = "Bouchard Père & Fils",
                year = 2018,
                grapeVariety = "Spätburgunder",
                price = 45.0,
                storageLocation = "Regal Fach 10",
                status = WineStatus.IN_STOCK,
                notes = "Elegant und samtig.",
                qrCode = "WINE-10007"
            ),
            Wine(
                name = "Primitivo di Manduria",
                winery = "Poggio Le Volpi",
                year = 2021,
                grapeVariety = "Primitivo",
                price = 18.9,
                storageLocation = "Regal Fach 12",
                status = WineStatus.IN_STOCK,
                notes = "Kraftvoll, dunkle Beeren.",
                qrCode = "WINE-10008"
            ),
            Wine(
                name = "Champagne Brut Reserve",
                winery = "Billecart-Salmon",
                year = 2017,
                grapeVariety = "Chardonnay / Pinot Noir",
                price = 68.0,
                storageLocation = "Kühlschrank",
                status = WineStatus.IN_STOCK,
                notes = "Feine Perlage für besondere Anlässe.",
                qrCode = "WINE-10009"
            ),
            Wine(
                name = "Syrah Cote-Rotie",
                winery = "E. Guigal",
                year = 2015,
                grapeVariety = "Syrah",
                price = 110.0,
                storageLocation = "Regal Fach 18",
                status = WineStatus.OUT_OF_STOCK,
                notes = "Letzte Flasche letztes Jahr getrunken.",
                qrCode = "WINE-10010"
            )
        )
    }
}
