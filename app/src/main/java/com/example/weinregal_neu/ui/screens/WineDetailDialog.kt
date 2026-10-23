package com.example.weinregal_neu.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.weinregal_neu.data.PriceLookup
import com.example.weinregal_neu.data.PriceLookupService
import com.example.weinregal_neu.model.GrapeLibrary
import com.example.weinregal_neu.model.Wine
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun WineDetailDialog(
    wine: Wine,
    compartmentContext: String? = null,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLookupPrice: suspend (Wine) -> PriceLookup,
    onOpenUrl: (String) -> Unit,
    onApplyPrice: (Double) -> Unit,
    onAssignToCompartment: (String) -> Unit,
    onRemoveBottle: () -> Unit,
    onAddBottle: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var lookupRunning by remember { mutableStateOf(false) }
    var lookupResult by remember { mutableStateOf<PriceLookup?>(null) }
    var lookupMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val grape = GrapeLibrary.find(wine.grapeVariety)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (wine.inStock) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Text(
                            text = wine.status.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (wine.inStock) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }

                if (wine.rating > 0) {
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= wine.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Weinart", wine.wineType)
                DetailRow("Winzer", wine.winery.ifBlank { "Unbekannt" })
                DetailRow("Jahrgang", wine.yearLabel)
                DetailRow("Rebsorte", wine.grapeVariety.ifBlank { "Unbekannt" })
                DetailRow("Region", wine.region.ifBlank { "Unbekannt" })
                DetailRow("Trinktemperatur", wine.servingTemperature.ifBlank { "16-18 °C" })
                DetailRow("Passt zu Essen", wine.foodPairing.ifBlank { "Allrounder" })
                DetailRow("Preis", if (wine.price > 0) String.format(Locale.GERMANY, "%.2f €", wine.price) else "kein Preis")
                DetailRow("Lagerort", wine.locationLabel)
                DetailRow("Bestand", "${wine.quantity} Flasche(n)")
                DetailRow("QR / Barcode", wine.qrCode.ifBlank { "-" })

                if (wine.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notizen & Geschmack",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = wine.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (grape != null) {
                    SuggestionCard(
                        title = "Rebsorte: ${grape.name} (${grape.color})",
                        lines = listOf(
                            "Charakter: ${grape.character}",
                            "Aromen: ${grape.aromas}",
                            "Säure: ${grape.acidity} · Tannin: ${grape.tannin}",
                            "Anbau: ${grape.regions}",
                            "Empfehlung: ${grape.servingTemperature} · ${grape.foodPairing}"
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onRemoveBottle,
                        enabled = wine.inStock,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Entnehmen")
                    }
                    FilledTonalButton(
                        onClick = onAddBottle,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hinzufügen")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        lookupRunning = true
                        lookupMessage = null
                        lookupResult = null
                        scope.launch {
                            val result = runCatching { onLookupPrice(wine) }.getOrNull()
                            lookupRunning = false
                            lookupResult = result
                            lookupMessage = when {
                                result == null -> "Preisabfrage fehlgeschlagen. Bitte Internetverbindung prüfen."
                                result.hasOffers -> null
                                else -> "Kein Online-Preis gefunden – die Shops hatten keinen passenden Treffer."
                            }
                        }
                    },
                    enabled = !lookupRunning,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (lookupRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Online-Preis suchen")
                }

                lookupMessage?.let { message ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { onOpenUrl(PriceLookupService.webSearchUrl(wine)) }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Im Browser suchen")
                    }
                }

                lookupResult?.offers?.takeIf { it.isNotEmpty() }?.let { offers ->
                    Spacer(modifier = Modifier.height(6.dp))
                    offers.forEach { offer ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    onApplyPrice(offer.price)
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
                                IconButton(
                                    onClick = { onOpenUrl(offer.url) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = "Im Shop öffnen",
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Preis antippen, um ihn zu übernehmen. Stand: jetzt.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (compartmentContext != null && wine.storageLocation != compartmentContext) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onAssignToCompartment(compartmentContext) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hierher verlegen ($compartmentContext)")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Schließen")
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Wein löschen?") },
            text = { Text("„${wine.name}“ wird dauerhaft aus dem Archiv entfernt.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun SuggestionCard(title: String, lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
