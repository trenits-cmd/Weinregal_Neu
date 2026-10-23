package com.example.weinregal_neu.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class WineStatus(val displayName: String) {
    IN_STOCK("im Bestand"),
    OUT_OF_STOCK("nicht lagernd")
}

/**
 * Ein Wein im Archiv. [quantity] ist die Anzahl der Flaschen, die aktuell im Bestand sind.
 * Ein Wein kann hoechstens eine Flasche in einem Regal-Fach haben (siehe [RackSlots]).
 */
@Serializable
data class Wine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val winery: String = "",
    val year: Int = 0,
    val grapeVariety: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val storageLocation: String = RackSlots.DEFAULT,
    val status: WineStatus = WineStatus.IN_STOCK,
    val notes: String = "",
    val qrCode: String = "",
    val addedDate: Long = System.currentTimeMillis(),
    /** Rotwein, Weißwein, Rosé oder Schaumwein */
    val wineType: String = "Rotwein",
    val region: String = "",
    val servingTemperature: String = "16-18 °C",
    val foodPairing: String = "",
    /** 0 = keine Bewertung, 1..5 Sterne */
    val rating: Int = 0
) {
    val inStock: Boolean get() = quantity > 0

    /** Jahrgang oder "o. J." für Weine ohne Jahrgangsangabe. */
    val yearLabel: String get() = if (year > 0) year.toString() else "o. J."

    val compartment: Int? get() = RackSlots.compartmentNumber(storageLocation)

    val storageKind: StorageKind get() = RackSlots.kind(storageLocation)

    val locationLabel: String get() = RackSlots.label(storageLocation)

    /** Flaschen, die nicht im sichtbaren Regal-Fach stehen (Kuehlschrank, Kammer, Rest). */
    val bottlesOutsideRack: Int
        get() = (quantity - if (compartment != null) 1 else 0).coerceAtLeast(0)

    fun withQuantity(newQuantity: Int): Wine {
        val safeQuantity = newQuantity.coerceAtLeast(0)
        return copy(
            quantity = safeQuantity,
            status = if (safeQuantity > 0) WineStatus.IN_STOCK else WineStatus.OUT_OF_STOCK
        )
    }
}
