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
    val storageLocation: String, // e.g. "Regal Fach 1" ... "Regal Fach 18", "Kühlschrank", "Speisekammer"
    val status: WineStatus = WineStatus.IN_STOCK,
    val notes: String = "",
    val qrCode: String = "WINE-${(10000..99999).random()}",
    val addedDate: Long = System.currentTimeMillis()
)
