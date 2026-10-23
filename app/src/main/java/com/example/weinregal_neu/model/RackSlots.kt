package com.example.weinregal_neu.model

/**
 * Zentrale Definition des Weinregals: 18 gleich grosse Faecher, 6 Spalten x 3 Reihen.
 * Ein Fach enthaelt genau eine Flasche. Neben dem Regal gibt es den Kuehlschrank,
 * die Speisekammer und freie Lagerplaetze.
 */
enum class StorageKind {
    RACK,
    FRIDGE,
    PANTRY,
    FREE
}

object RackSlots {
    const val COLUMNS = 6
    const val ROWS = 3
    const val COMPARTMENT_COUNT = COLUMNS * ROWS

    const val FRIDGE = "Kühlschrank"
    const val PANTRY = "Speisekammer"
    const val DEFAULT = "Regal Fach 1"
    const val FREE_PLACE = "Lagerplatz (freie Fläche)"

    private val compartmentPattern = Regex("""^Regal\s*Fach\s*(\d{1,2})$""", RegexOption.IGNORE_CASE)

    /** Liefert den Fachnamen zu einer Fachnummer (1..18). */
    fun compartment(number: Int): String = "Regal Fach ${number.coerceIn(1, COMPARTMENT_COUNT)}"

    /** Liest die Fachnummer aus einem Lagerort wie "Regal Fach 7". */
    fun compartmentNumber(location: String): Int? {
        val match = compartmentPattern.find(location.trim()) ?: return null
        return match.groupValues[1].toIntOrNull()?.takeIf { it in 1..COMPARTMENT_COUNT }
    }

    fun kind(location: String): StorageKind {
        val trimmed = location.trim()
        return when {
            compartmentNumber(trimmed) != null -> StorageKind.RACK
            trimmed.equals(FRIDGE, ignoreCase = true) -> StorageKind.FRIDGE
            trimmed.equals(PANTRY, ignoreCase = true) -> StorageKind.PANTRY
            else -> StorageKind.FREE
        }
    }

    /** Anzeigename des Lagerorts, nie leer. */
    fun label(location: String): String = when (kind(location)) {
        StorageKind.RACK -> compartment(compartmentNumber(location) ?: 1)
        StorageKind.FRIDGE -> FRIDGE
        StorageKind.PANTRY -> PANTRY
        StorageKind.FREE -> location.trim().ifBlank { "Ohne Lagerort" }
    }

    /** Alle waehlbaren Regal-Faecher in Anzeigereihenfolge. */
    fun allCompartments(): List<String> = (1..COMPARTMENT_COUNT).map { compartment(it) }
}
