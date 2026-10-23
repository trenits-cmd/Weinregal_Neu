package com.example.weinregal_neu.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.weinregal_neu.model.Wine
import com.example.weinregal_neu.model.WineStatus
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.Executors

@Composable
fun LabelPhotoScannerDialog(
    defaultLocation: String,
    onDismiss: () -> Unit,
    onWineRecognized: (Wine) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var isProcessing by remember { mutableStateOf(false) }
    var recognizedTextPreview by remember { mutableStateOf<String?>(null) }
    var extractedWine by remember { mutableStateOf<Wine?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Etikett per Foto scannen (KI)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen")
                }
            }

            // Camera Preview or Confirmation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (extractedWine == null) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageCapture
                                    )
                                } catch (exc: Exception) {
                                    exc.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isProcessing) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Analysiere Weinetikett...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Capture Button overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 24.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    val photoFile = File(context.cacheDir, "label_${System.currentTimeMillis()}.jpg")
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                    imageCapture.takePicture(
                                        outputOptions,
                                        cameraExecutor,
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onError(exc: ImageCaptureException) {
                                                isProcessing = false
                                            }

                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                processLabelPhoto(context, photoFile, defaultLocation) { wine, text ->
                                                    isProcessing = false
                                                    recognizedTextPreview = text
                                                    extractedWine = wine
                                                }
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Foto aufnehmen",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                } else {
                    // Confirmation Card for Recognized Wine
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Wein erfolgreich erkannt! 🍷",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Name: ${extractedWine!!.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Winzer: ${extractedWine!!.winery}", style = MaterialTheme.typography.bodyMedium)
                            Text("Jahrgang: ${extractedWine!!.year}", style = MaterialTheme.typography.bodyMedium)
                            Text("Rebsorte: ${extractedWine!!.grapeVariety}", style = MaterialTheme.typography.bodyMedium)
                            Text("Region: ${extractedWine!!.region}", style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Extrahierter Text:\n$recognizedTextPreview",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(onClick = { extractedWine = null }) {
                                    Text("Erneut fotografieren")
                                }
                                Button(onClick = { onWineRecognized(extractedWine!!) }) {
                                    Text("Übernehmen & Speichern")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun processLabelPhoto(
    context: Context,
    file: File,
    defaultLocation: String,
    onResult: (Wine, String) -> Unit
) {
    try {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                val wine = parseWineFromOcrText(text, defaultLocation)
                onResult(wine, text)
            }
            .addOnFailureListener {
                val fallbackWine = Wine(
                    name = "Etikett-Wein",
                    winery = "",
                    year = 0,
                    grapeVariety = "",
                    price = 0.0,
                    storageLocation = defaultLocation,
                    notes = "Foto konnte nicht gelesen werden. Bitte Angaben ergänzen."
                )
                onResult(fallbackWine, "Kein Text erkannt")
            }
    } catch (e: Exception) {
        val fallbackWine = Wine(
            name = "Etikett-Wein",
            winery = "",
            year = 0,
            grapeVariety = "",
            price = 0.0,
            storageLocation = defaultLocation,
            notes = "Fehler bei der Bildverarbeitung. Bitte Angaben ergänzen."
        )
        onResult(fallbackWine, "Fehler bei Bildverarbeitung")
    }
}

private fun parseWineFromOcrText(ocrText: String, defaultLocation: String): Wine {
    val lines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }
    val name = lines.firstOrNull() ?: "Etikett-Wein"
    val winery = if (lines.size > 1) lines[1] else ""

    // Extract year
    val yearRegex = Regex("\\b(19[5-9]\\d|20[0-3]\\d)\\b")
    val yearMatch = yearRegex.find(ocrText)
    val year = yearMatch?.value?.toInt() ?: 0

    // Extract grape or type
    val lower = ocrText.lowercase()
    val grape = when {
        lower.contains("riesling") -> "Riesling"
        lower.contains("chardonnay") -> "Chardonnay"
        lower.contains("cabernet") -> "Cabernet Sauvignon"
        lower.contains("merlot") -> "Merlot"
        lower.contains("spätburgunder") || lower.contains("pinot noir") -> "Spätburgunder"
        lower.contains("syrah") || lower.contains("shiraz") -> "Syrah"
        lower.contains("tempranillo") -> "Tempranillo"
        lower.contains("sangiovese") -> "Sangiovese"
        else -> ""
    }

    val wineType = when {
        lower.contains("riesling") || lower.contains("chardonnay") || lower.contains("sauvignon") -> "Weißwein"
        lower.contains("rosé") || lower.contains("rose") -> "Rosé"
        lower.contains("champagne") || lower.contains("sekt") || lower.contains("prosecco") -> "Schaumwein"
        else -> "Rotwein"
    }

    return Wine(
        name = name,
        winery = winery,
        year = year,
        grapeVariety = grape,
        price = 0.0,
        storageLocation = defaultLocation,
        notes = "Automatisch per Etikett-Foto (OCR) erkannt. Bitte prüfen.",
        wineType = wineType,
        region = "",
        servingTemperature = if (wineType == "Weißwein") "8-10 °C" else "16-18 °C",
        foodPairing = if (wineType == "Weißwein") "Fisch & Meeresfrüchte" else "Dunkles Fleisch & Käse"
    )
}
