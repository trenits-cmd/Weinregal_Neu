package com.example.weinregal_neu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.weinregal_neu.data.CsvService
import com.example.weinregal_neu.data.OpenFoodFactsService
import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var detailWineId by remember { mutableStateOf<String?>(null) }
    var compartmentContextForDetail by remember { mutableStateOf<String?>(null) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var addEditTarget by remember { mutableStateOf<Wine?>(null) }
    var addEditPrefill by remember { mutableStateOf<Wine?>(null) }
    var defaultAddLocation by remember { mutableStateOf(RackSlots.DEFAULT) }

    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showLabelPhotoDialog by remember { mutableStateOf(false) }
    var pendingImportText by remember { mutableStateOf<String?>(null) }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun openNewWineDialog(location: String, prefill: Wine? = null) {
        addEditTarget = null
        addEditPrefill = prefill
        defaultAddLocation = location
        showAddEditDialog = true
    }

    fun openUrl(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            showMessage("Kein Browser gefunden, um den Link zu öffnen.")
        }
    }

    // ---------------------------------------------------------------- CSV

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val csv = viewModel.exportCsv()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.writer(Charsets.UTF_8).use { it.write(csv) }
            }
            showMessage("CSV-Sicherung gespeichert (${wines.size} Weine).")
        } catch (e: Exception) {
            showMessage("Speichern fehlgeschlagen: ${e.message ?: "unbekannter Fehler"}")
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.reader(Charsets.UTF_8).use { it.readText() }
            }
            if (text.isNullOrBlank()) {
                showMessage("Die gewählte Datei ist leer.")
            } else {
                pendingImportText = text
            }
        } catch (e: Exception) {
            showMessage("Lesen fehlgeschlagen: ${e.message ?: "unbekannter Fehler"}")
        }
    }

    // ---------------------------------------------------------------- Meldungen

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val detailWine = detailWineId?.let { id -> wines.find { it.id == id } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            detailWineId = storedWine.id
                            compartmentContextForDetail = compartmentName
                        } else {
                            openNewWineDialog(location = compartmentName)
                        }
                    }
                )

                1 -> ArchiveScreen(
                    wines = wines,
                    onWineClick = { wine ->
                        detailWineId = wine.id
                        compartmentContextForDetail = null
                    }
                )

                else -> InventoryScreen(
                    wines = wines,
                    onAddWineClick = { openNewWineDialog(location = RackSlots.DEFAULT) },
                    onScanQrClick = { showQrScannerDialog = true },
                    onExportCsvClick = { exportLauncher.launch(CsvService.suggestedFileName()) },
                    onImportCsvClick = { importLauncher.launch(arrayOf("*/*")) },
                    onWineClick = { wine ->
                        detailWineId = wine.id
                        compartmentContextForDetail = null
                    },
                    onEditWine = { wine ->
                        addEditTarget = wine
                        addEditPrefill = null
                        defaultAddLocation = wine.storageLocation
                        showAddEditDialog = true
                    },
                    onDeleteWine = { wine -> viewModel.deleteWine(wine.id) },
                    onRemoveBottle = { wine -> viewModel.removeBottle(wine.id) },
                    onAddBottle = { wine -> viewModel.addBottle(wine.id) }
                )
            }
        }
    }

    // ---------------------------------------------------------------- Dialoge

    if (detailWine != null) {
        WineDetailDialog(
            wine = detailWine,
            compartmentContext = compartmentContextForDetail,
            onDismiss = { detailWineId = null },
            onEdit = {
                addEditTarget = detailWine
                addEditPrefill = null
                defaultAddLocation = detailWine.storageLocation
                detailWineId = null
                showAddEditDialog = true
            },
            onDelete = {
                viewModel.deleteWine(detailWine.id)
                detailWineId = null
            },
            onLookupPrice = { wine -> viewModel.lookupPrices(wine) },
            onOpenUrl = ::openUrl,
            onApplyPrice = { price ->
                viewModel.setPrice(detailWine.id, price)
                showMessage("Preis übernommen: ${String.format("%.2f", price)} €")
            },
            onAssignToCompartment = { location ->
                val number = RackSlots.compartmentNumber(location)
                if (number != null) {
                    viewModel.assignCompartment(detailWine.id, number)
                } else {
                    viewModel.setStorage(detailWine.id, location)
                }
                detailWineId = null
            },
            onRemoveBottle = { viewModel.removeBottle(detailWine.id) },
            onAddBottle = { viewModel.addBottle(detailWine.id) }
        )
    }

    if (showAddEditDialog) {
        val target = addEditTarget
        AddEditWineDialog(
            initialWine = target ?: addEditPrefill,
            isNew = target == null,
            defaultLocation = defaultAddLocation,
            onDismiss = { showAddEditDialog = false },
            onLookupPrice = { wine -> viewModel.lookupPrices(wine) },
            onOpenUrl = ::openUrl,
            onSave = { wine ->
                if (target == null) {
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
                detailWineId = scannedWine.id
                compartmentContextForDetail = null
            },
            onNewBarcodeScanned = { barcode ->
                showQrScannerDialog = false
                scope.launch {
                    val fetched = OpenFoodFactsService.fetchWineByEan(barcode, RackSlots.DEFAULT)
                    if (fetched != null) {
                        openNewWineDialog(location = RackSlots.DEFAULT, prefill = fetched)
                        showMessage("Produktdaten geladen. Bitte Angaben prüfen.")
                    } else {
                        openNewWineDialog(
                            location = RackSlots.DEFAULT,
                            prefill = Wine(name = "", qrCode = barcode)
                        )
                        showMessage("Kein Produkt gefunden – bitte Daten selbst ergänzen.")
                    }
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
                openNewWineDialog(location = defaultAddLocation, prefill = recognizedWine)
            }
        )
    }

    pendingImportText?.let { importText ->
        AlertDialog(
            onDismissRequest = { pendingImportText = null },
            title = { Text("CSV-Sicherung einlesen?") },
            text = {
                Text(
                    "Die Weine aus der Datei werden übernommen. Weine mit gleicher ID " +
                        "werden aktualisiert, alle anderen bleiben erhalten."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val result = viewModel.importCsv(importText)
                        pendingImportText = null
                        showMessage(result)
                    }
                ) {
                    Text("Importieren")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportText = null }) { Text("Abbrechen") }
            }
        )
    }
}
