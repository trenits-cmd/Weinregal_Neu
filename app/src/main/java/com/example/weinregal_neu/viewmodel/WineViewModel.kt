package com.example.weinregal_neu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.weinregal_neu.data.WineStorage
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WineViewModel(application: Application) : AndroidViewModel(application) {

    private val _wines = MutableStateFlow<List<Wine>>(emptyList())
    val wines: StateFlow<List<Wine>> = _wines.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

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
        val updated = _wines.value + wine
        _wines.value = updated
        WineStorage.saveWines(getApplication(), updated)
    }

    fun updateWine(updatedWine: Wine) {
        val updated = _wines.value.map { if (it.id == updatedWine.id) updatedWine else it }
        _wines.value = updated
        WineStorage.saveWines(getApplication(), updated)
    }

    fun deleteWine(wineId: String) {
        val updated = _wines.value.filter { it.id != wineId }
        _wines.value = updated
        WineStorage.saveWines(getApplication(), updated)
    }

    fun changeStatus(wineId: String, status: WineStatus) {
        val updated = _wines.value.map { if (it.id == wineId) it.copy(status = status) else it }
        _wines.value = updated
        WineStorage.saveWines(getApplication(), updated)
    }

    fun assignLocation(wineId: String, location: String) {
        val updated = _wines.value.map { if (it.id == wineId) it.copy(storageLocation = location, status = WineStatus.IN_STOCK) else it }
        _wines.value = updated
        WineStorage.saveWines(getApplication(), updated)
    }

    fun estimatePriceOnline(wineId: String) {
        val wine = _wines.value.find { it.id == wineId } ?: return
        // Simulate online price check / estimation (+/- 10% variation or valuation boost based on age)
        val ageFactor = (2026 - wine.year).coerceAtLeast(1)
        val estimatedPrice = Math.round((wine.price * (1.0 + (ageFactor * 0.02)))) .toDouble()
        updateWine(wine.copy(price = estimatedPrice))
    }

    fun scanQrCode(qrCode: String): Wine? {
        return _wines.value.find { it.qrCode.equals(qrCode.trim(), ignoreCase = true) }
    }
}
