package com.example.weinregal_neu.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RackSlotsTest {

    @Test
    fun rackHas18CompartmentsIn6By3() {
        assertEquals(18, RackSlots.COMPARTMENT_COUNT)
        assertEquals(6, RackSlots.COLUMNS)
        assertEquals(3, RackSlots.ROWS)
        assertEquals(18, RackSlots.allCompartments().size)
        assertEquals("Regal Fach 1", RackSlots.allCompartments().first())
        assertEquals("Regal Fach 18", RackSlots.allCompartments().last())
    }

    @Test
    fun compartmentNumberIsParsed() {
        assertEquals(1, RackSlots.compartmentNumber("Regal Fach 1"))
        assertEquals(18, RackSlots.compartmentNumber("Regal Fach 18"))
        assertEquals(7, RackSlots.compartmentNumber("  regal   fach 7 "))
        assertNull(RackSlots.compartmentNumber("Regal Fach 19"))
        assertNull(RackSlots.compartmentNumber("Regal Fach 0"))
        assertNull(RackSlots.compartmentNumber("Regal Fach X"))
        assertNull(RackSlots.compartmentNumber("Kühlschrank"))
        assertNull(RackSlots.compartmentNumber(""))
    }

    @Test
    fun storageKindIsDetected() {
        assertEquals(StorageKind.RACK, RackSlots.kind("Regal Fach 3"))
        assertEquals(StorageKind.FRIDGE, RackSlots.kind("Kühlschrank"))
        assertEquals(StorageKind.FRIDGE, RackSlots.kind("kühlschrank"))
        assertEquals(StorageKind.PANTRY, RackSlots.kind("Speisekammer"))
        assertEquals(StorageKind.FREE, RackSlots.kind("Kellerregal links"))
        assertEquals(StorageKind.FREE, RackSlots.kind(""))
    }

    @Test
    fun labelsAreNeverBlank() {
        assertEquals("Regal Fach 5", RackSlots.label("Regal Fach 5"))
        assertEquals("Kühlschrank", RackSlots.label("Kühlschrank"))
        assertEquals("Speisekammer", RackSlots.label("Speisekammer"))
        assertEquals("Kellerregal links", RackSlots.label("Kellerregal links"))
        assertEquals("Ohne Lagerort", RackSlots.label("   "))
    }

    @Test
    fun compartmentNamesAreClampedToValidRange() {
        assertEquals("Regal Fach 1", RackSlots.compartment(0))
        assertEquals("Regal Fach 18", RackSlots.compartment(99))
    }

    @Test
    fun allCompartmentNamesAreUnique() {
        val names = RackSlots.allCompartments()
        assertTrue(names.size == names.toSet().size)
    }
}
