package com.example.weinregal_neu.data

import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

/**
 * CSV-Sicherung: eine Zeile pro Wein, Semikolon als Trennzeichen,
 * UTF-8 mit BOM, damit Excel Umlaute korrekt anzeigt.
 */
object CsvService {

    private const val DELIMITER = ';'
    private const val BOM = "\uFEFF"

    private val priceFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))

    val HEADER = listOf(
        "id", "name", "winery", "year", "grapeVariety", "wineType", "region",
        "price", "quantity", "storageLocation", "status", "servingTemperature",
        "foodPairing", "notes", "qrCode", "addedDate", "rating"
    )

    data class ImportResult(
        val wines: List<Wine>,
        val skippedRows: Int = 0,
        val messages: List<String> = emptyList()
    )

    // ---------------------------------------------------------------- Export

    fun export(wines: List<Wine>): String = buildString {
        append(BOM)
        appendLine(HEADER.joinToString(DELIMITER.toString()))
        wines.forEach { wine ->
            appendLine(
                listOf(
                    wine.id,
                    wine.name,
                    wine.winery,
                    wine.year.toString(),
                    wine.grapeVariety,
                    wine.wineType,
                    wine.region,
                    priceFormat.format(wine.price),
                    wine.quantity.toString(),
                    wine.storageLocation,
                    wine.status.name,
                    wine.servingTemperature,
                    wine.foodPairing,
                    wine.notes,
                    wine.qrCode,
                    wine.addedDate.toString(),
                    wine.rating.toString()
                ).joinToString(DELIMITER.toString()) { escape(it) }
            )
        }
    }

    private fun escape(value: String): String {
        val needsQuotes = value.contains(DELIMITER) ||
            value.contains('"') ||
            value.contains('\n') ||
            value.contains('\r')
        if (!needsQuotes) return value
        return '"' + value.replace("\"", "\"\"") + '"'
    }

    // ---------------------------------------------------------------- Import

    fun import(text: String): ImportResult {
        val cleaned = text.removePrefix(BOM).replace("\r\n", "\n").replace('\r', '\n')
        val rows = parseRows(cleaned)
        if (rows.isEmpty()) return ImportResult(emptyList(), 0, listOf("Die Datei ist leer."))

        val headerRow = rows.first().map { it.trim().lowercase() }
        val hasHeader = headerRow.contains("name")
        val columnIndex = if (hasHeader) {
            headerRow.withIndex().associate { (index, name) -> name to index }
        } else {
            HEADER.withIndex().associate { (index, name) -> name to index }
        }
        val dataRows = if (hasHeader) rows.drop(1) else rows

        val wines = mutableListOf<Wine>()
        val messages = mutableListOf<String>()
        var skipped = 0

        dataRows.forEachIndexed { rowNumber, row ->
            if (row.all { it.isBlank() }) return@forEachIndexed

            fun value(column: String): String {
                val index = columnIndex[column] ?: columnIndex[column.lowercase()] ?: return ""
                return row.getOrNull(index)?.trim().orEmpty()
            }

            val name = value("name")
            if (name.isBlank()) {
                skipped++
                messages += "Zeile ${rowNumber + if (hasHeader) 2 else 1}: kein Weinname, übersprungen."
                return@forEachIndexed
            }

            val quantity = value("quantity").toIntOrNull()?.coerceAtLeast(0)
            val legacyStatus = runCatching { WineStatus.valueOf(value("status").uppercase()) }.getOrNull()
            val resolvedQuantity = quantity ?: if (legacyStatus == WineStatus.OUT_OF_STOCK) 0 else 1

            val location = value("storageLocation").ifBlank { RackSlots.DEFAULT }

            wines += Wine(
                id = value("id").ifBlank { UUID.randomUUID().toString() },
                name = name,
                winery = value("winery"),
                year = value("year").toIntOrNull() ?: 2020,
                grapeVariety = value("grapeVariety"),
                wineType = value("wineType").ifBlank { "Rotwein" },
                region = value("region"),
                price = value("price").toPriceOrNull() ?: 0.0,
                quantity = resolvedQuantity,
                storageLocation = location,
                status = if (resolvedQuantity > 0) WineStatus.IN_STOCK else WineStatus.OUT_OF_STOCK,
                servingTemperature = value("servingTemperature").ifBlank { "16-18 °C" },
                foodPairing = value("foodPairing"),
                notes = value("notes"),
                qrCode = value("qrCode"),
                addedDate = value("addedDate").toLongOrNull() ?: System.currentTimeMillis(),
                rating = value("rating").toIntOrNull()?.coerceIn(0, 5) ?: 0
            )
        }

        if (wines.isEmpty()) messages += "Es konnten keine Weine gelesen werden."
        return ImportResult(wines = wines, skippedRows = skipped, messages = messages)
    }

    /** Zerlegt den CSV-Text in Zeilen und Felder und beachtet Anfuehrungszeichen. */
    internal fun parseRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0

        fun finishField() {
            fields += field.toString()
            field.setLength(0)
        }

        fun finishRow() {
            finishField()
            rows += fields
            fields = mutableListOf()
        }

        while (index < text.length) {
            val char = text[index]
            when {
                inQuotes && char == '"' && index + 1 < text.length && text[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }

                char == '"' -> inQuotes = !inQuotes

                !inQuotes && char == DELIMITER -> finishField()

                !inQuotes && char == '\n' -> finishRow()

                else -> field.append(char)
            }
            index++
        }

        if (field.isNotEmpty() || fields.isNotEmpty()) finishRow()
        return rows.filter { row -> row.any { it.isNotBlank() } }
    }

    private fun String.toPriceOrNull(): Double? = replace(" ", "")
        .replace("€", "")
        .replace(",", ".")
        .toDoubleOrNull()

    /** Dateiname fuer den System-Dialog zum Speichern. */
    fun suggestedFileName(): String {
        val stamp = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(java.util.Date())
        return "weinregal-sicherung-$stamp.csv"
    }
}
