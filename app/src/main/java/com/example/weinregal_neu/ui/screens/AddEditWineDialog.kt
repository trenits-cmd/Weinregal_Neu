package com.example.weinregal_neu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.weinregal_neu.data.PriceLookup
import com.example.weinregal_neu.data.PriceLookupService
import com.example.weinregal_neu.model.GrapeLibrary
import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.StorageKind
import com.example.weinregal_neu.model.Wine
import kotlinx.coroutines.launch
import java.util.Locale

private val wineTypes = listOf("Rotwein", "Weißwein", "Rosé", "Schaumwein")

@Composable
fun AddEditWineDialog(
    initialWine: Wine? = null,
    isNew: Boolean = initialWine == null,
    defaultLocation: String = RackSlots.DEFAULT,
    onDismiss: () -> Unit,
    onLookupPrice: suspend (Wine) -> PriceLookup,
    onOpenUrl: (String) -> Unit,
    onSave: (Wine) -> Unit
) {
    val startLocation = initialWine?.storageLocation ?: defaultLocation
    var name by remember { mutableStateOf(initialWine?.name.orEmpty()) }
    var winery by remember { mutableStateOf(initialWine?.winery.orEmpty()) }
    var yearStr by remember {
        mutableStateOf(initialWine?.year?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var grapeVariety by remember { mutableStateOf(initialWine?.grapeVariety.orEmpty()) }
    var priceStr by remember {
        mutableStateOf(
            initialWine?.takeIf { it.price > 0 }
                ?.let { String.format(Locale.GERMANY, "%.2f", it.price) }
                ?: ""
        )
    }
    var quantity by remember { mutableStateOf(initialWine?.quantity ?: 1) }
    var wineType by remember { mutableStateOf(initialWine?.wineType ?: "Rotwein") }
    var region by remember { mutableStateOf(initialWine?.region.orEmpty()) }
    var servingTemperature by remember {
        mutableStateOf(initialWine?.servingTemperature?.takeIf { it.isNotBlank() } ?: "16-18 °C")
    }
    var foodPairing by remember { mutableStateOf(initialWine?.foodPairing.orEmpty()) }
    var notes by remember { mutableStateOf(initialWine?.notes.orEmpty()) }
    var rating by remember { mutableStateOf(initialWine?.rating ?: 0) }

    var locationKind by remember { mutableStateOf(RackSlots.kind(startLocation)) }
    var compartment by remember {
        mutableStateOf(RackSlots.compartmentNumber(startLocation) ?: 1)
    }
    var freeLocation by remember {
        mutableStateOf(
            if (RackSlots.kind(startLocation) == StorageKind.FREE) startLocation else ""
        )
    }

    var error by remember { mutableStateOf<String?>(null) }
    var lookupRunning by remember { mutableStateOf(false) }
    var lookupResult by remember { mutableStateOf<PriceLookup?>(null) }
    var lookupMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val currentLocation = when (locationKind) {
        StorageKind.RACK -> RackSlots.compartment(compartment)
        StorageKind.FRIDGE -> RackSlots.FRIDGE
        StorageKind.PANTRY -> RackSlots.PANTRY
        StorageKind.FREE -> freeLocation.ifBlank { "Lagerplatz" }
    }

    val grapeProfile = GrapeLibrary.find(grapeVariety)
    val draftWine = (initialWine ?: Wine(name = name.ifBlank { "Neuer Wein" })).copy(
        name = name,
        winery = winery,
        year = yearStr.toIntOrNull() ?: 0,
        grapeVariety = grapeVariety,
        price = priceStr.toDoubleOrNull() ?: priceStr.replace(',', '.').toDoubleOrNull() ?: 0.0,
        storageLocation = currentLocation,
        wineType = wineType,
        region = region
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isNew) "Wein hinzufügen" else "Wein bearbeiten",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Weinname *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null && name.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = winery,
                    onValueChange = { winery = it },
                    label = { Text("Winzer / Weingut") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Weinart",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wineTypes.forEach { type ->
                        FilterChip(
                            selected = wineType == type,
                            onClick = { wineType = type },
                            label = { Text(type, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = grapeVariety,
                    onValueChange = { grapeVariety = it },
                    label = { Text("Rebsorte") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = grapeProfile?.let {
                        { Text("${it.character} · Säure ${it.acidity} · Tannin ${it.tannin}") }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it.filter { char -> char.isDigit() }.take(4) },
                        label = { Text("Jahrgang") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it.filter { char -> char.isDigit() || char == ',' || char == '.' } },
                        label = { Text("Preis (€)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        lookupRunning = true
                        lookupMessage = null
                        lookupResult = null
                        scope.launch {
                            val result = runCatching { onLookupPrice(draftWine) }.getOrNull()
                            lookupRunning = false
                            lookupResult = result
                            lookupMessage = when {
                                result == null -> "Preisabfrage fehlgeschlagen. Bitte Internetverbindung prüfen."
                                result.hasOffers -> null
                                else -> "Kein Preis gefunden. Die Shops haben keinen passenden Treffer geliefert."
                            }
                        }
                    },
                    enabled = !lookupRunning && name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (lookupRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preis online suchen")
                }

                lookupMessage?.let { message ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                lookupResult?.offers?.takeIf { it.isNotEmpty() }?.let { offers ->
                    Spacer(modifier = Modifier.height(8.dp))
                    offers.forEach { offer ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    priceStr = String.format(Locale.GERMANY, "%.2f", offer.price)
                                    lookupResult = null
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = offer.merchant,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = offer.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = String.format(Locale.GERMANY, "%.2f €", offer.price),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Text(
                        text = "Tippe einen Preis an, um ihn zu übernehmen.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (lookupResult != null && lookupResult?.hasOffers == false) {
                    TextButton(onClick = { onOpenUrl(PriceLookupService.webSearchUrl(draftWine)) }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Im Browser weitersuchen")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Flaschen im Bestand",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { quantity = (quantity - 1).coerceAtLeast(0) },
                            enabled = quantity > 0
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Eine Flasche weniger")
                        }
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalIconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Eine Flasche mehr")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Lagerort",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LocationChip(
                        label = "Regal",
                        selected = locationKind == StorageKind.RACK,
                        onClick = { locationKind = StorageKind.RACK }
                    )
                    LocationChip(
                        label = "Lagerplatz",
                        selected = locationKind == StorageKind.FREE,
                        onClick = { locationKind = StorageKind.FREE }
                    )
                    LocationChip(
                        label = RackSlots.FRIDGE,
                        selected = locationKind == StorageKind.FRIDGE,
                        onClick = { locationKind = StorageKind.FRIDGE }
                    )
                    LocationChip(
                        label = RackSlots.PANTRY,
                        selected = locationKind == StorageKind.PANTRY,
                        onClick = { locationKind = StorageKind.PANTRY }
                    )
                }

                when (locationKind) {
                    StorageKind.RACK -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                compartment = if (compartment <= 1) RackSlots.COMPARTMENT_COUNT else compartment - 1
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Vorheriges Fach")
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = RackSlots.compartment(compartment),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fach $compartment von ${RackSlots.COMPARTMENT_COUNT}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                compartment = if (compartment >= RackSlots.COMPARTMENT_COUNT) 1 else compartment + 1
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Nächstes Fach")
                            }
                        }
                    }

                    StorageKind.FREE -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = freeLocation,
                            onValueChange = { freeLocation = it },
                            label = { Text("Lagerplatz beschreiben") },
                            placeholder = { Text("z. B. Kellerregal links") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    else -> Unit
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
                        value = foodPairing,
                        onValueChange = { foodPairing = it },
                        label = { Text("Passt zu") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen & Geschmack") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bewertung",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$star Sterne",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { rating = if (rating == star) star - 1 else star }
                        )
                    }
                }

                error?.let { message ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message,
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
                            val year = yearStr.toIntOrNull()
                            val price = priceStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                            error = when {
                                name.isBlank() -> "Bitte mindestens den Weinnamen eingeben."
                                yearStr.isNotBlank() && (year == null || year !in 1900..2100) ->
                                    "Bitte einen gültigen Jahrgang angeben oder das Feld leer lassen."
                                price < 0 -> "Der Preis muss positiv sein."
                                locationKind == StorageKind.RACK -> null
                                locationKind == StorageKind.FREE && freeLocation.isBlank() ->
                                    "Bitte den Lagerplatz beschreiben."
                                else -> null
                            }

                            if (error == null) {
                                val base = initialWine ?: Wine(name = name)
                                val wine = base.copy(
                                    name = name.trim(),
                                    winery = winery.trim(),
                                    year = year ?: 0,
                                    grapeVariety = grapeVariety.trim(),
                                    price = price,
                                    quantity = quantity,
                                    storageLocation = currentLocation,
                                    notes = notes.trim(),
                                    status = if (quantity > 0) {
                                        com.example.weinregal_neu.model.WineStatus.IN_STOCK
                                    } else {
                                        com.example.weinregal_neu.model.WineStatus.OUT_OF_STOCK
                                    },
                                    wineType = wineType,
                                    region = region.trim(),
                                    servingTemperature = servingTemperature.trim(),
                                    foodPairing = foodPairing.trim(),
                                    rating = rating,
                                    qrCode = base.qrCode.ifBlank { newQrCode() }
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

@Composable
private fun LocationChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium
            )
        }
    )
}

private fun newQrCode(): String = "WINE-${(10000..99999).random()}"
