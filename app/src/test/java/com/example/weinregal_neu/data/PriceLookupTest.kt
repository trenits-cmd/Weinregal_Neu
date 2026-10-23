package com.example.weinregal_neu.data

import com.example.weinregal_neu.model.Wine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceLookupTest {

    @Test
    fun normalizesUmlautsAndPunctuation() {
        assertEquals("weissburgunder", PriceLookupService.normalizeForMatch("Weißburgunder"))
        assertEquals("gewuerztraminer 2021", PriceLookupService.normalizeForMatch("Gewürztraminer, 2021"))
        assertEquals("chateau margaux", PriceLookupService.normalizeForMatch("  Château   Margaux  "))
    }

    @Test
    fun acceptsProductWithMatchingNameAndWinery() {
        val wine = Wine(name = "Château Margaux 2015", winery = "Château Margaux")
        assertTrue(
            PriceLookupService.isRelevant(
                wine,
                "Château Margaux Premier Grand Cru Classé 2015 0,75 l"
            )
        )
    }

    @Test
    fun rejectsUnrelatedProduct() {
        val wine = Wine(name = "Riesling Kabinett", winery = "Weingut Müller", grapeVariety = "Riesling")
        assertFalse(
            PriceLookupService.isRelevant(
                wine,
                "Sancerre Sauvignon Blanc Loire 2022"
            )
        )
    }

    @Test
    fun acceptsProductMatchingOnlyTheGrape() {
        val wine = Wine(name = "", grapeVariety = "Riesling")
        assertTrue(
            PriceLookupService.isRelevant(
                wine,
                "Johannes Zillinger Riesling Nussberg 2022"
            )
        )
    }

    @Test
    fun wineWithoutDistinctiveWordsAcceptsEverything() {
        val wine = Wine(name = "Rot", winery = "", grapeVariety = "")
        assertTrue(PriceLookupService.isRelevant(wine, "Irgendein anderer Wein"))
    }

    @Test
    fun webSearchUrlSkipsMissingYear() {
        val wine = Wine(name = "Riesling", winery = "Weingut Müller", year = 0)
        val url = PriceLookupService.webSearchUrl(wine)
        assertTrue(url.startsWith("https://www.google.com/search?q="))
        assertFalse(url.contains("0+Preis"))
        assertTrue(url.contains("Riesling"))
    }
}
