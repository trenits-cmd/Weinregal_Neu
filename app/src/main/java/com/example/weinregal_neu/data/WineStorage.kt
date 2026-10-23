package com.example.weinregal_neu.data

import android.content.Context
import android.content.SharedPreferences
import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import kotlinx.serialization.json.Json

object WineStorage {
    private const val PREFS_NAME = "weinregal_prefs"
    private const val KEY_WINES = "key_wines"

    private val json = Json { ignoreUnknownKeys = true }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
            val migrated = json.decodeFromString<List<Wine>>(jsonString).map { it.migrated() }
            val usedCompartments = mutableSetOf<Int>()
            var changed = false
            val normalized = migrated.map { wine ->
                val compartment = wine.compartment
                if (compartment != null && wine.inStock && !usedCompartments.add(compartment)) {
                    changed = true
                    wine.copy(storageLocation = RackSlots.FREE_PLACE)
                } else {
                    wine
                }
            }
            if (changed) saveWines(context, normalized)
            normalized
        } catch (_: Exception) {
            getDefaultWines()
        }
    }

    /**
     * Alte Datensätze kannten nur "im Bestand"/"nicht lagernd".
     * "Nicht lagernd" bedeutet jetzt schlicht: keine Flasche mehr vorhanden.
     */
    private fun Wine.migrated(): Wine = when {
        status == WineStatus.OUT_OF_STOCK && quantity > 0 -> copy(quantity = 0)
        quantity > 0 && status == WineStatus.OUT_OF_STOCK -> copy(status = WineStatus.IN_STOCK)
        else -> this
    }

    private fun getDefaultWines(): List<Wine> = listOf(
        Wine(
            name = "Château Margaux Premier Grand Cru",
            winery = "Château Margaux",
            year = 2015,
            grapeVariety = "Cabernet Sauvignon",
            price = 650.0,
            quantity = 1,
            storageLocation = RackSlots.compartment(1),
            status = WineStatus.IN_STOCK,
            notes = "Hervorragender Jahrgang, perfekt zu rotem Fleisch.",
            qrCode = "WINE-10001",
            wineType = "Rotwein",
            region = "Bordeaux, Frankreich",
            servingTemperature = "17-19 °C",
            foodPairing = "Rind, Lamm, Wild",
            rating = 5
        ),
        Wine(
            name = "Barolo DOCG Riserva",
            winery = "Vietti",
            year = 2016,
            grapeVariety = "Nebbiolo",
            price = 120.0,
            quantity = 1,
            storageLocation = RackSlots.compartment(2),
            status = WineStatus.IN_STOCK,
            notes = "Tanninreich, langer Abgang.",
            qrCode = "WINE-10002",
            wineType = "Rotwein",
            region = "Piemont, Italien",
            servingTemperature = "17-19 °C",
            foodPairing = "Trüffel, geschmortes Rind",
            rating = 5
        ),
        Wine(
            name = "Riesling Spätlese Trocken",
            winery = "Dr. Loosen",
            year = 2020,
            grapeVariety = "Riesling",
            price = 24.5,
            quantity = 3,
            storageLocation = RackSlots.compartment(3),
            status = WineStatus.IN_STOCK,
            notes = "Fruchtig mit schöner Mineralität.",
            qrCode = "WINE-10003",
            wineType = "Weißwein",
            region = "Mosel, Deutschland",
            servingTemperature = "8-10 °C",
            foodPairing = "Fisch, asiatische Küche",
            rating = 4
        ),
        Wine(
            name = "Brunello di Montalcino",
            winery = "Casanova di Neri",
            year = 2017,
            grapeVariety = "Sangiovese",
            price = 85.0,
            quantity = 1,
            storageLocation = RackSlots.compartment(5),
            status = WineStatus.IN_STOCK,
            notes = "Klassischer toskanischer Rotwein.",
            qrCode = "WINE-10004",
            wineType = "Rotwein",
            region = "Toskana, Italien",
            servingTemperature = "17-18 °C",
            foodPairing = "Pasta, Pecorino, Rind",
            rating = 4
        ),
        Wine(
            name = "Chablis Grand Cru Les Clos",
            winery = "Domaine William Fèvre",
            year = 2019,
            grapeVariety = "Chardonnay",
            price = 95.0,
            quantity = 2,
            storageLocation = RackSlots.FRIDGE,
            status = WineStatus.IN_STOCK,
            notes = "Kühler Weißwein, ideal gekühlt trinken.",
            qrCode = "WINE-10005",
            wineType = "Weißwein",
            region = "Burgund, Frankreich",
            servingTemperature = "10-12 °C",
            foodPairing = "Meeresfrüchte, Geflügel",
            rating = 5
        ),
        Wine(
            name = "Rioja Gran Reserva",
            winery = "Marqués de Riscal",
            year = 2014,
            grapeVariety = "Tempranillo",
            price = 38.0,
            quantity = 2,
            storageLocation = RackSlots.PANTRY,
            status = WineStatus.IN_STOCK,
            notes = "Gut gereift, Noten von Vanille und Tabak.",
            qrCode = "WINE-10006",
            wineType = "Rotwein",
            region = "Rioja, Spanien",
            servingTemperature = "16-18 °C",
            foodPairing = "Lamm, Schinken, Tapas",
            rating = 4
        ),
        Wine(
            name = "Pinot Noir Estate",
            winery = "Bouchard Père & Fils",
            year = 2018,
            grapeVariety = "Spätburgunder",
            price = 45.0,
            quantity = 1,
            storageLocation = RackSlots.compartment(10),
            status = WineStatus.IN_STOCK,
            notes = "Elegant und samtig.",
            qrCode = "WINE-10007",
            wineType = "Rotwein",
            region = "Burgund, Frankreich",
            servingTemperature = "14-16 °C",
            foodPairing = "Ente, Pilze, milder Käse",
            rating = 4
        ),
        Wine(
            name = "Primitivo di Manduria",
            winery = "Poggio Le Volpi",
            year = 2021,
            grapeVariety = "Primitivo",
            price = 18.9,
            quantity = 1,
            storageLocation = RackSlots.compartment(12),
            status = WineStatus.IN_STOCK,
            notes = "Kraftvoll, dunkle Beeren.",
            qrCode = "WINE-10008",
            wineType = "Rotwein",
            region = "Apulien, Italien",
            servingTemperature = "16-18 °C",
            foodPairing = "Grillfleisch, BBQ",
            rating = 3
        ),
        Wine(
            name = "Champagne Brut Reserve",
            winery = "Billecart-Salmon",
            year = 2017,
            grapeVariety = "Chardonnay",
            price = 68.0,
            quantity = 3,
            storageLocation = RackSlots.FRIDGE,
            status = WineStatus.IN_STOCK,
            notes = "Feine Perlage für besondere Anlässe.",
            qrCode = "WINE-10009",
            wineType = "Schaumwein",
            region = "Champagne, Frankreich",
            servingTemperature = "6-8 °C",
            foodPairing = "Aperitif, Meeresfrüchte",
            rating = 5
        ),
        Wine(
            name = "Syrah Côte-Rôtie",
            winery = "E. Guigal",
            year = 2015,
            grapeVariety = "Syrah",
            price = 110.0,
            quantity = 0,
            storageLocation = RackSlots.compartment(18),
            status = WineStatus.OUT_OF_STOCK,
            notes = "Letzte Flasche letztes Jahr getrunken.",
            qrCode = "WINE-10010",
            wineType = "Rotwein",
            region = "Rhône, Frankreich",
            servingTemperature = "16-18 °C",
            foodPairing = "Wild, Lamm, Pfeffer",
            rating = 5
        )
    )
}
