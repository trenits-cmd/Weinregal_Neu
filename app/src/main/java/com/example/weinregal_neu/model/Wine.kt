package com.example.weinregal_neu.model

import kotlinx.serialization.Serializable

@Serializable
enum class WineStatus(val displayName: String) {
    IN_STOCK("im Bestand"),
    OUT_OF_STOCK("nicht lagernd")
}

@Serializable
data class Wine(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val winery: String,
    val year: Int,
    val grapeVariety: String,
    val price: Double,
    val storageLocation: String = "Regal Fach 1",
    val status: WineStatus = WineStatus.IN_STOCK,
    val notes: String = "",
    val qrCode: String = "WINE-${(10000..99999).random()}",
    val addedDate: Long = System.currentTimeMillis(),
    val wineType: String = "Rotwein", // Rotwein, Weißwein, Rosé, Schaumwein
    val region: String = "Unbekannte Region",
    val servingTemperature: String = "16-18 °C",
    val foodPairing: String = "Fleisch, Käse, Pasta"
)
