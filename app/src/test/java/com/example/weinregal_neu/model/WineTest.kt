package com.example.weinregal_neu.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WineTest {

    @Test
    fun newWineIsInStock() {
        val wine = Wine(name = "Test")
        assertTrue(wine.inStock)
        assertEquals(1, wine.quantity)
        assertEquals(WineStatus.IN_STOCK, wine.status)
    }

    @Test
    fun removingLastBottleMarksOutOfStock() {
        val wine = Wine(name = "Test", quantity = 1, status = WineStatus.IN_STOCK)
        val updated = wine.withQuantity(0)
        assertFalse(updated.inStock)
        assertEquals(WineStatus.OUT_OF_STOCK, updated.status)
    }

    @Test
    fun addingBottleSetsInStockAgain() {
        val wine = Wine(name = "Test", quantity = 0, status = WineStatus.OUT_OF_STOCK)
        val updated = wine.withQuantity(1)
        assertTrue(updated.inStock)
        assertEquals(WineStatus.IN_STOCK, updated.status)
    }

    @Test
    fun negativeQuantityIsClamped() {
        val wine = Wine(name = "Test", quantity = 2)
        val updated = wine.withQuantity(-5)
        assertEquals(0, updated.quantity)
        assertEquals(WineStatus.OUT_OF_STOCK, updated.status)
    }

    @Test
    fun compartmentIsDerivedFromStorageLocation() {
        val wine = Wine(name = "Test", storageLocation = "Regal Fach 12")
        assertEquals(12, wine.compartment)
        assertEquals(StorageKind.RACK, wine.storageKind)
        assertEquals("Regal Fach 12", wine.locationLabel)

        val fridgeWine = Wine(name = "Test", storageLocation = RackSlots.FRIDGE)
        assertNull(fridgeWine.compartment)
        assertEquals(StorageKind.FRIDGE, fridgeWine.storageKind)
    }

    @Test
    fun bottlesOutsideRackCountsRemainingBottles() {
        val inRack = Wine(name = "Test", quantity = 3, storageLocation = "Regal Fach 4")
        assertEquals(2, inRack.bottlesOutsideRack)

        val fridge = Wine(name = "Test", quantity = 3, storageLocation = RackSlots.FRIDGE)
        assertEquals(3, fridge.bottlesOutsideRack)

        val outOfStock = Wine(name = "Test", quantity = 0, storageLocation = "Regal Fach 4")
        assertEquals(0, outOfStock.bottlesOutsideRack)
    }

    @Test
    fun yearLabelHandlesMissingVintage() {
        assertEquals("2021", Wine(name = "Test", year = 2021).yearLabel)
        assertEquals("o. J.", Wine(name = "Test", year = 0).yearLabel)
    }

    @Test
    fun defaultStorageIsFirstCompartment() {
        assertEquals(RackSlots.DEFAULT, Wine(name = "Test").storageLocation)
    }
}
