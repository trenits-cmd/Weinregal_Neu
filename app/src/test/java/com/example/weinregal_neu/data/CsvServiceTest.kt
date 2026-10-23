package com.example.weinregal_neu.data

import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvServiceTest {

    private fun sampleWine() = Wine(
        id = "id-1",
        name = "Riesling; \"Kabinett\"",
        winery = "Weingut Müller",
        year = 2021,
        grapeVariety = "Riesling",
        price = 12.5,
        quantity = 3,
        storageLocation = RackSlots.compartment(7),
        status = WineStatus.IN_STOCK,
        notes = "Zeile 1\nZeile 2 mit; Semikolon",
        qrCode = "WINE-1",
        addedDate = 1_700_000_000_000,
        wineType = "Weißwein",
        region = "Mosel, Deutschland",
        servingTemperature = "8-10 °C",
        foodPairing = "Fisch",
        rating = 4
    )

    @Test
    fun exportStartsWithBomAndHeader() {
        val csv = CsvService.export(listOf(sampleWine()))
        assertTrue(csv.startsWith("\uFEFF"))
        val firstLine = csv.removePrefix("\uFEFF").lineSequence().first()
        assertEquals(CsvService.HEADER.joinToString(";"), firstLine)
    }

    @Test
    fun roundTripKeepsAllFields() {
        val original = sampleWine()
        val csv = CsvService.export(listOf(original))
        val result = CsvService.import(csv)

        assertEquals(1, result.wines.size)
        val imported = result.wines.first()
        assertEquals(original.id, imported.id)
        assertEquals(original.name, imported.name)
        assertEquals(original.winery, imported.winery)
        assertEquals(original.year, imported.year)
        assertEquals(original.grapeVariety, imported.grapeVariety)
        assertEquals(original.price, imported.price, 0.001)
        assertEquals(original.quantity, imported.quantity)
        assertEquals(original.storageLocation, imported.storageLocation)
        assertEquals(original.notes, imported.notes)
        assertEquals(original.qrCode, imported.qrCode)
        assertEquals(original.addedDate, imported.addedDate)
        assertEquals(original.wineType, imported.wineType)
        assertEquals(original.rating, imported.rating)
    }

    @Test
    fun importAcceptsLegacyRowWithoutHeader() {
        val text = "alt-1;Alter Wein;Winzer;2018;Merlot;Rotwein;;9,50;1;Kühlschrank;IN_STOCK;16-18 °C;;;;;;3"
        val result = CsvService.import(text)

        assertEquals(1, result.wines.size)
        val wine = result.wines.first()
        assertEquals("Alter Wein", wine.name)
        assertEquals(9.5, wine.price, 0.001)
        assertEquals(RackSlots.FRIDGE, wine.storageLocation)
        assertEquals(WineStatus.IN_STOCK, wine.status)
    }

    @Test
    fun outOfStockRowBecomesZeroBottles() {
        val text = "id;name;status\nx;Leerer Wein;OUT_OF_STOCK"
        val wine = CsvService.import(text).wines.single()
        assertEquals(0, wine.quantity)
        assertEquals(WineStatus.OUT_OF_STOCK, wine.status)
    }

    @Test
    fun rowWithoutNameIsSkipped() {
        val text = "id;name\n1;\n2;Guter Wein"
        val result = CsvService.import(text)
        assertEquals(1, result.wines.size)
        assertEquals(1, result.skippedRows)
        assertEquals("Guter Wein", result.wines.single().name)
    }

    @Test
    fun emptyFileIsReported() {
        val result = CsvService.import("")
        assertTrue(result.wines.isEmpty())
        assertTrue(result.messages.isNotEmpty())
    }

    @Test
    fun quotedValuesWithDelimiterAndNewlineAreParsed() {
        val rows = CsvService.parseRows("a;\"b;c\";\"d\"\"e\"\n\"x\ny\";z;")
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b;c", "d\"e"), rows[0])
        assertEquals("x\ny", rows[1][0])
    }

    @Test
    fun suggestedFileNameContainsDateAndCsvSuffix() {
        val name = CsvService.suggestedFileName()
        assertTrue(name.startsWith("weinregal-sicherung-"))
        assertTrue(name.endsWith(".csv"))
    }

    @Test
    fun importsQuantityAndStatusConsistently() {
        val wine = Wine(name = "Test", quantity = 0, status = WineStatus.OUT_OF_STOCK)
        val csv = CsvService.export(listOf(wine))
        val imported = CsvService.import(csv).wines.single()
        assertEquals(0, imported.quantity)
        assertEquals(WineStatus.OUT_OF_STOCK, imported.status)
        assertNotNull(imported.id)
    }
}
