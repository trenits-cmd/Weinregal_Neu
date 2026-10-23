package com.example.weinregal_neu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.weinregal_neu.data.CsvService
import com.example.weinregal_neu.data.PriceLookup
import com.example.weinregal_neu.data.PriceLookupService
import com.example.weinregal_neu.data.WineStorage
import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class WineViewModel(application: Application) : AndroidViewModel(application) {

    private val _wines = MutableStateFlow<List<Wine>>(emptyList())
    val wines: StateFlow<List<Wine>> = _wines.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadWines()
    }

    fun loadWines() {
        _wines.value = WineStorage.loadWines(getApplication())
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun addWine(wine: Wine) {
        val (prepared, updatedList) = resolveCompartment(wine, _wines.value)
        persist(updatedList + prepared)
        _messages.tryEmit("„${prepared.name}“ wurde hinzugefügt.")
    }

    fun updateWine(updatedWine: Wine) {
        val (prepared, updatedList) = resolveCompartment(updatedWine, _wines.value, exceptId = updatedWine.id)
        persist(updatedList.map { if (it.id == prepared.id) prepared else it })
    }

    fun deleteWine(wineId: String) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        persist(_wines.value.filterNot { it.id == wineId })
        _messages.tryEmit("„${wine.name}“ wurde aus dem Archiv gelöscht.")
    }

    /** Eine Flasche entnehmen: Bestand sinkt um eins, bei 0 ist der Wein nicht mehr lagernd. */
    fun removeBottle(wineId: String) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        if (wine.quantity <= 0) {
            _messages.tryEmit("„${wine.name}“ ist bereits nicht lagernd.")
            return
        }
        val updated = wine.withQuantity(wine.quantity - 1)
        persist(_wines.value.map { if (it.id == wineId) updated else it })
        val text = if (updated.quantity == 0) {
            "Letzte Flasche entnommen – „${wine.name}“ ist nicht mehr lagernd."
        } else {
            "Flasche entnommen. „${wine.name}“: noch ${updated.quantity} im Bestand."
        }
        _messages.tryEmit(text)
    }

    /** Eine weitere Flasche des gleichen Weins hinzufügen. */
    fun addBottle(wineId: String) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        val updated = wine.withQuantity(wine.quantity + 1)
        persist(_wines.value.map { if (it.id == wineId) updated else it })
        _messages.tryEmit("„${wine.name}“: ${updated.quantity} Flaschen im Bestand.")
    }

    fun changeStatus(wineId: String, status: WineStatus) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        val updated = if (status == WineStatus.IN_STOCK) {
            wine.withQuantity(wine.quantity.coerceAtLeast(1))
        } else {
            wine.withQuantity(0)
        }
        persist(_wines.value.map { if (it.id == wineId) updated else it })
    }

    /**
     * Verlegt die im Regal stehende Flasche in ein anderes Fach.
     * Das Fach muss frei sein – sonst wird der dort stehende Wein auf einen
     * freien Lagerplatz ausserhalb des Regals verschoben.
     */
    fun assignCompartment(wineId: String, compartment: Int) {
        val location = RackSlots.compartment(compartment)
        val wine = _wines.value.find { it.id == wineId } ?: return
        var current = _wines.value

        val occupant = current.find {
            it.id != wineId && it.compartment == compartment && it.inStock
        }
        if (occupant != null) {
            current = current.map {
                if (it.id == occupant.id) it.copy(storageLocation = RackSlots.FREE_PLACE) else it
            }
            _messages.tryEmit("„${occupant.name}“ stand in $location und wurde nach „${RackSlots.FREE_PLACE}“ verschoben.")
        }

        val moved = wine.copy(
            storageLocation = location,
            quantity = wine.quantity.coerceAtLeast(1),
            status = WineStatus.IN_STOCK
        )
        persist(current.map { if (it.id == wineId) moved else it })
    }

    fun setStorage(wineId: String, location: String) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        persist(_wines.value.map { if (it.id == wineId) wine.copy(storageLocation = location.trim()) else it })
    }

    fun setPrice(wineId: String, price: Double) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        persist(_wines.value.map { if (it.id == wineId) wine.copy(price = price.coerceAtLeast(0.0)) else it })
    }

    suspend fun lookupPrices(wine: Wine): PriceLookup = PriceLookupService.lookup(wine)

    fun exportCsv(): String = CsvService.export(_wines.value)

    /** Zusammenführen: gleiche ID wird aktualisiert, neue Weine werden ergänzt. */
    fun importCsv(text: String): String {
        val result = CsvService.import(text)
        if (result.wines.isEmpty()) {
            return result.messages.firstOrNull() ?: "Keine Weine in der Datei gefunden."
        }

        val merged = _wines.value.associateBy { it.id }.toMutableMap()
        var updated = 0
        var added = 0
        result.wines.forEach { imported ->
            if (merged.containsKey(imported.id)) updated++ else added++
            merged[imported.id] = imported
        }
        persist(merged.values.sortedByDescending { it.addedDate })

        val skipped = if (result.skippedRows > 0) ", ${result.skippedRows} Zeile(n) übersprungen" else ""
        return "Import: $added neu, $updated aktualisiert$skipped."
    }

    fun scanQrCode(qrCode: String): Wine? {
        val code = qrCode.trim()
        if (code.isEmpty()) return null
        return _wines.value.find {
            it.qrCode.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true)
        }
    }

    /**
     * Ein Regal-Fach darf nur von einer Flasche belegt sein. Steht dort schon ein
     * anderer Wein, wird dieser auf einen freien Lagerplatz ausserhalb des Regals
     * verschoben - die neue Auswahl des Nutzers bleibt erhalten.
     */
    private fun resolveCompartment(
        wine: Wine,
        wines: List<Wine>,
        exceptId: String? = null
    ): Pair<Wine, List<Wine>> {
        val compartment = wine.compartment ?: return wine to wines
        val occupant = wines.find {
            it.id != (exceptId ?: wine.id) && it.compartment == compartment && it.inStock
        } ?: return wine to wines

        val movedList = wines.map {
            if (it.id == occupant.id) it.copy(storageLocation = RackSlots.FREE_PLACE) else it
        }
        _messages.tryEmit(
            "„${occupant.name}“ stand bereits in ${RackSlots.compartment(compartment)} " +
                "und wurde nach „${RackSlots.FREE_PLACE}“ verschoben."
        )
        return wine to movedList
    }

    private fun persist(wines: List<Wine>) {
        _wines.value = wines
        WineStorage.saveWines(getApplication(), wines)
    }
}
