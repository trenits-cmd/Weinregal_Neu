package com.example.weinregal_neu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.weinregal_neu.data.OpenFoodFactsService
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import com.example.weinregal_neu.ui.screens.*
import com.example.weinregal_neu.ui.theme.Weinregal_NeuTheme
import com.example.weinregal_neu.viewmodel.WineViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: WineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Weinregal_NeuTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: WineViewModel) {
    val wines by viewModel.wines.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var activeDialogWine by remember { mutableStateOf<Wine?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editTargetWine by remember { mutableStateOf<Wine?>(null) }
    var defaultAddLocation by remember { mutableStateOf("Regal Fach 1") }
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showLabelPhotoDialog by remember { mutableStateOf(false) }
    var compartmentContextForDetail by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.WineBar, contentDescription = "Weinregal") },
                    label = { Text("Weinregal") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Archive, contentDescription = "Weinarchiv") },
                    label = { Text("Weinarchiv") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Weinbestand") },
                    label = { Text("Weinbestand") }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> RackScreen(
                    wines = wines,
                    onCompartmentClick = { compartmentName, storedWine ->
                        if (storedWine != null) {
                            activeDialogWine = storedWine
                            compartmentContextForDetail = compartmentName
                            showDetailDialog = true
                        } else {
                            defaultAddLocation = compartmentName
                            editTargetWine = null
                            showAddEditDialog = true
                        }
                    }
                )
                1 -> ArchiveScreen(
                    wines = wines,
                    onWineClick = { wine ->
                        activeDialogWine = wine
                        compartmentContextForDetail = null
                        showDetailDialog = true
                    }
                )
                2 -> InventoryScreen(
                    wines = wines,
                    onAddWineClick = {
                        editTargetWine = null
                        defaultAddLocation = "Regal Fach 1"
                        showAddEditDialog = true
                    },
                    onScanQrClick = {
                        showQrScannerDialog = true
                    },
                    onUpdateAllPricesClick = {
                        wines.forEach { wine ->
                            viewModel.estimatePriceOnline(wine.id)
                        }
                    },
                    onEditWine = { wine ->
                        editTargetWine = wine
                        showAddEditDialog = true
                    },
                    onDeleteWine = { wineId ->
                        viewModel.deleteWine(wineId)
                    },
                    onToggleStatus = { wineId, status ->
                        viewModel.changeStatus(wineId, status)
                    }
                )
            }
        }
    }

    // Dialogs
    if (showDetailDialog && activeDialogWine != null) {
        WineDetailDialog(
            wine = activeDialogWine!!,
            compartmentContext = compartmentContextForDetail,
            onDismiss = { showDetailDialog = false },
            onEdit = {
                showDetailDialog = false
                editTargetWine = activeDialogWine
                showAddEditDialog = true
            },
            onDelete = {
                viewModel.deleteWine(activeDialogWine!!.id)
                showDetailDialog = false
            },
            onEstimatePrice = {
                viewModel.estimatePriceOnline(activeDialogWine!!.id)
                activeDialogWine = viewModel.wines.value.find { it.id == activeDialogWine!!.id }
            },
            onAssignToCompartment = { location ->
                viewModel.assignLocation(activeDialogWine!!.id, location)
                showDetailDialog = false
            }
        )
    }

    if (showAddEditDialog) {
        AddEditWineDialog(
            initialWine = editTargetWine,
            defaultLocation = defaultAddLocation,
            onDismiss = { showAddEditDialog = false },
            onSave = { wine ->
                if (editTargetWine == null) {
                    viewModel.addWine(wine)
                } else {
                    viewModel.updateWine(wine)
                }
                showAddEditDialog = false
            }
        )
    }

    if (showQrScannerDialog) {
        RealQrScannerDialog(
            wines = wines,
            onDismiss = { showQrScannerDialog = false },
            onWineScanned = { scannedWine ->
                showQrScannerDialog = false
                activeDialogWine = scannedWine
                compartmentContextForDetail = null
                showDetailDialog = true
            },
            onNewBarcodeScanned = { barcode ->
                showQrScannerDialog = false
                coroutineScope.launch {
                    val fetchedWine = OpenFoodFactsService.fetchWineByEan(barcode, defaultAddLocation)
                    editTargetWine = fetchedWine
                    showAddEditDialog = true
                }
            },
            onTakePhotoClick = {
                showQrScannerDialog = false
                showLabelPhotoDialog = true
            }
        )
    }

    if (showLabelPhotoDialog) {
        LabelPhotoScannerDialog(
            defaultLocation = defaultAddLocation,
            onDismiss = { showLabelPhotoDialog = false },
            onWineRecognized = { recognizedWine ->
                showLabelPhotoDialog = false
                editTargetWine = recognizedWine
                showAddEditDialog = true
            }
        )
    }
}
