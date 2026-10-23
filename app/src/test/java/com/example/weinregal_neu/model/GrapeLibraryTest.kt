package com.example.weinregal_neu.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GrapeLibraryTest {

    @Test
    fun findsExactName() {
        val riesling = GrapeLibrary.find("Riesling")
        assertNotNull(riesling)
        assertEquals("Riesling", riesling!!.name)
        assertTrue(riesling.aromas.isNotBlank())
        assertTrue(riesling.foodPairing.isNotBlank())
    }

    @Test
    fun findsAliases() {
        assertEquals("Spätburgunder", GrapeLibrary.find("Pinot Noir")?.name)
        assertEquals("Syrah", GrapeLibrary.find("Shiraz")?.name)
        assertEquals("Garnacha", GrapeLibrary.find("Grenache")?.name)
        assertEquals("Weißburgunder", GrapeLibrary.find("Pinot Blanc")?.name)
    }

    @Test
    fun ignoresCaseAndUmlauts() {
        assertEquals("Weißburgunder", GrapeLibrary.find("weissburgunder")?.name)
        assertEquals("Gewürztraminer", GrapeLibrary.find("gewurztraminer")?.name)
    }

    @Test
    fun findsGrapeInsideLongerText() {
        assertEquals("Cabernet Sauvignon", GrapeLibrary.find("Cabernet Sauvignon & Merlot")?.name)
        assertEquals("Riesling", GrapeLibrary.find("Riesling trocken 2021")?.name)
    }

    @Test
    fun returnsNullForUnknownGrape() {
        assertNull(GrapeLibrary.find(""))
        assertNull(GrapeLibrary.find("   "))
        assertNull(GrapeLibrary.find("Unbekannte Rebsorte"))
    }

    @Test
    fun allProfilesAreComplete() {
        assertTrue(GrapeLibrary.all().size >= 15)
        GrapeLibrary.all().forEach { profile ->
            assertTrue("Name fehlt", profile.name.isNotBlank())
            assertTrue("Farbe fehlt bei ${profile.name}", profile.color.isNotBlank())
            assertTrue("Charakter fehlt bei ${profile.name}", profile.character.isNotBlank())
            assertTrue("Aromen fehlen bei ${profile.name}", profile.aromas.isNotBlank())
            assertTrue("Regionen fehlen bei ${profile.name}", profile.regions.isNotBlank())
            assertTrue("Temperatur fehlt bei ${profile.name}", profile.servingTemperature.isNotBlank())
        }
    }

    @Test
    fun grapeNamesAreUnique() {
        val names = GrapeLibrary.all().map { it.name }
        assertEquals(names.size, names.toSet().size)
    }
}
