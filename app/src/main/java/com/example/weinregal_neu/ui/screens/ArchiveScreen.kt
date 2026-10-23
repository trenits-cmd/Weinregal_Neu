package com.example.weinregal_neu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.weinregal_neu.model.GrapeLibrary
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import java.util.Locale

/**
 * Weinarchiv: Bibliothek aller Weine, die im Regal stehen oder dort standen,
 * inklusive Status und Rebsorten-Informationen.
 */
@Composable
fun ArchiveScreen(
    wines: List<Wine>,
    onWineClick: (Wine) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<WineStatus?>(null) }

    val filteredWines = wines
        .filter { wine ->
            val query = searchQuery.trim()
            val matchesSearch = query.isEmpty() ||
                wine.name.contains(query, ignoreCase = true) ||
                wine.winery.contains(query, ignoreCase = true) ||
                wine.grapeVariety.contains(query, ignoreCase = true) ||
                wine.region.contains(query, ignoreCase = true) ||
                wine.storageLocation.contains(query, ignoreCase = true)

            val matchesFilter = selectedFilter == null || wine.status == selectedFilter
            matchesSearch && matchesFilter
        }
        .sortedWith(
            compareByDescending<Wine> { it.status == WineStatus.IN_STOCK }
                .thenByDescending { it.addedDate }
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Weinarchiv",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${wines.size} Weine · ${wines.count { it.status == WineStatus.IN_STOCK }} im Bestand",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Name, Winzer, Rebsorte, Region …") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    TextButton(onClick = { searchQuery = "" }) { Text("X") }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("Alle (${wines.size})") }
            )
            FilterChip(
                selected = selectedFilter == WineStatus.IN_STOCK,
                onClick = { selectedFilter = WineStatus.IN_STOCK },
                label = { Text("Im Bestand (${wines.count { it.status == WineStatus.IN_STOCK }})") }
            )
            FilterChip(
                selected = selectedFilter == WineStatus.OUT_OF_STOCK,
                onClick = { selectedFilter = WineStatus.OUT_OF_STOCK },
                label = { Text("Nicht lagernd (${wines.count { it.status == WineStatus.OUT_OF_STOCK }})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredWines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (wines.isEmpty()) "Noch keine Weine im Archiv." else "Keine Weine gefunden.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredWines, key = { it.id }) { wine ->
                    WineArchiveCard(wine = wine, onClick = { onWineClick(wine) })
                }
            }
        }
    }
}

@Composable
fun WineArchiveCard(
    wine: Wine,
    onClick: () -> Unit
) {
    val isInStock = wine.status == WineStatus.IN_STOCK
    val badgeColor = if (isInStock) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val badgeTextColor = if (isInStock) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = listOfNotNull(
                            wine.winery.takeIf { it.isNotBlank() },
                            wine.yearLabel,
                            wine.grapeVariety.takeIf { it.isNotBlank() }
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(shape = RoundedCornerShape(8.dp), color = badgeColor) {
                    Text(
                        text = wine.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }

            GrapeLibrary.find(wine.grapeVariety)?.let { grape ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${grape.name}: ${grape.character} · ${grape.aromas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lagerort: ${wine.locationLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${wine.quantity} Flasche(n) · ${wine.wineType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (wine.price > 0) {
                        Text(
                            text = String.format(Locale.GERMANY, "%.2f €", wine.price),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (wine.rating > 0) {
                        Row {
                            repeat(wine.rating) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
