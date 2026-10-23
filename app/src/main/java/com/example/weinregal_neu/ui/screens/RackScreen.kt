package com.example.weinregal_neu.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weinregal_neu.R
import com.example.weinregal_neu.model.RackSlots
import com.example.weinregal_neu.model.Wine

/**
 * Weinregal-Tab: Foto des Regals mit nummerierten Pfeilen auf alle 18 Faecher.
 * Ein Fach enthaelt genau eine Flasche, der Rest des Bestands steht im
 * Kuehlschrank, in der Speisekammer oder auf einem freien Lagerplatz.
 */
@Composable
fun RackScreen(
    wines: List<Wine>,
    onCompartmentClick: (compartmentName: String, wine: Wine?) -> Unit
) {
    val occupancy = remember(wines) {
        wines
            .filter { it.inStock && it.compartment != null }
            .associateBy { it.compartment!! }
    }
    val rackWines = (1..RackSlots.COMPARTMENT_COUNT).mapNotNull { occupancy[it] }
    val outsideBottles = wines
        .filter { it.inStock && it.compartment == null }
        .sumOf { it.quantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Mein Weinregal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "18 Fächer · 6 × 3 · tippe auf ein Fach",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            RackPhoto(
                occupancy = occupancy,
                onCompartmentClick = onCompartmentClick
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    label = "belegt: ${rackWines.size}/18"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    label = "frei: ${RackSlots.COMPARTMENT_COUNT - rackWines.size}"
                )
                if (outsideBottles > 0) {
                    Text(
                        text = "+$outsideBottles Flaschen außerhalb des Regals",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Welcher Wein steht wo?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (rackWines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Noch kein Fach belegt. Tippe im Foto auf ein Fach, um einen Wein einzutragen.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(rackWines, key = { it.id }) { wine ->
                RackWineRow(
                    wine = wine,
                    onClick = {
                        onCompartmentClick(RackSlots.compartment(wine.compartment ?: 1), wine)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun RackPhoto(
    occupancy: Map<Int, Wine>,
    onCompartmentClick: (String, Wine?) -> Unit
) {
    val aspect = 1195f / 896f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Image(
            painter = painterResource(id = R.drawable.rack_photo),
            contentDescription = "Foto meines Weinregals",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Dezenter Schleier, damit die weissen Nummern auf dem Foto lesbar bleiben.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.10f))
        )

        val width = maxWidth
        val height = maxHeight
        val insetX = 0.055f
        val insetY = 0.06f
        val cellWidth = width * (1f - 2f * insetX) / RackSlots.COLUMNS
        val cellHeight = height * (1f - 2f * insetY) / RackSlots.ROWS

        for (index in 1..RackSlots.COMPARTMENT_COUNT) {
            val column = (index - 1) % RackSlots.COLUMNS
            val row = (index - 1) / RackSlots.COLUMNS
            val left = width * insetX + cellWidth * column
            val top = height * insetY + cellHeight * row
            val wine = occupancy[index]

            // Klickfläche des gesamten Faches
            Box(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .size(width = cellWidth, height = cellHeight)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (wine != null) 2.dp else 1.dp,
                        color = if (wine != null) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        } else {
                            Color.White.copy(alpha = 0.35f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        onCompartmentClick(RackSlots.compartment(index), wine)
                    }
            )

            // Pfeil mit Fachnummer, zeigt von oben in das Fach
            CompartmentMarker(
                number = index,
                occupied = wine != null,
                modifier = Modifier
                    .offset(
                        x = left + cellWidth / 2 - 18.dp,
                        y = top + cellHeight / 2 - 30.dp
                    )
                    .size(width = 36.dp, height = 44.dp)
            )
        }
    }
}

@Composable
private fun CompartmentMarker(
    number: Int,
    occupied: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeColor = if (occupied) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    }
    val textColor = if (occupied) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(badgeColor)
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = if (occupied) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .size(22.dp)
                .offset(y = (-6).dp)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RackWineRow(wine: Wine, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Fach ${wine.compartment ?: "-"}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wine.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOfNotNull(
                        wine.winery.takeIf { it.isNotBlank() },
                        wine.yearLabel,
                        wine.grapeVariety.takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (wine.quantity > 1) {
                Icon(
                    imageVector = Icons.Default.WineBar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${wine.quantity}×",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
