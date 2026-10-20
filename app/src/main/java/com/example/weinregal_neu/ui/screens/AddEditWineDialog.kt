package com.example.weinregal_neu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus

@Composable
fun AddEditWineDialog(
    initialWine: Wine? = null,
    defaultLocation: String = "Regal Fach 1",
    onDismiss: () -> Unit,
    onSave: (Wine) -> Unit
) {
    var name by remember { mutableStateOf(initialWine?.name ?: "") }
    var winery by remember { mutableStateOf(initialWine?.winery ?: "") }
    var yearStr by remember { mutableStateOf(initialWine?.year?.toString() ?: "2020") }
    var grapeVariety by remember { mutableStateOf(initialWine?.grapeVariety ?: "") }
    var priceStr by remember { mutableStateOf(initialWine?.price?.toString() ?: "15.00") }
    var storageLocation by remember { mutableStateOf(initialWine?.storageLocation ?: defaultLocation) }
    var wineType by remember { mutableStateOf(initialWine?.wineType ?: "Rotwein") }
    var region by remember { mutableStateOf(initialWine?.region ?: "") }
    var servingTemperature by remember { mutableStateOf(initialWine?.servingTemperature ?: "16-18 °C") }
    var foodPairing by remember { mutableStateOf(initialWine?.foodPairing ?: "") }
    var notes by remember { mutableStateOf(initialWine?.notes ?: "") }
    var status by remember { mutableStateOf(initialWine?.status ?: WineStatus.IN_STOCK) }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (initialWine == null) "Wein hinzufügen" else "Wein bearbeiten",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Weinname *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = winery,
                    onValueChange = { winery = it },
                    label = { Text("Winzer / Weingut") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it },
                        label = { Text("Jahrgang") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Preis (€)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wineType,
                        onValueChange = { wineType = it },
                        label = { Text("Art (Rot/Weiß/Rosé)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = grapeVariety,
                        onValueChange = { grapeVariety = it },
                        label = { Text("Rebsorte") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Region & Herkunft") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = servingTemperature,
                        onValueChange = { servingTemperature = it },
                        label = { Text("Trinktemperatur") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = storageLocation,
                        onValueChange = { storageLocation = it },
                        label = { Text("Lagerort") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = foodPairing,
                    onValueChange = { foodPairing = it },
                    label = { Text("Passt zu Essen (Food Pairing)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen & Geschmack") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bitte mindestens den Weinnamen eingeben.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                showError = true
                            } else {
                                val year = yearStr.toIntOrNull() ?: 2020
                                val price = priceStr.replace(',', '.').toDoubleOrNull() ?: 10.0
                                val wine = (initialWine ?: Wine(name = name, winery = winery, year = year, grapeVariety = grapeVariety, price = price, storageLocation = storageLocation, status = status, notes = notes)).copy(
                                    name = name,
                                    winery = winery,
                                    year = year,
                                    grapeVariety = grapeVariety,
                                    price = price,
                                    storageLocation = storageLocation,
                                    notes = notes,
                                    status = status,
                                    wineType = wineType,
                                    region = region,
                                    servingTemperature = servingTemperature,
                                    foodPairing = foodPairing
                                )
                                onSave(wine)
                            }
                        }
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}
