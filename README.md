package com.example.dctracks

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AudioConverterApp()
            }
        }
    }
}

@Composable
private fun AudioConverterApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var outputFormat by remember { mutableStateOf("mp3") }
    var status by remember { mutableStateOf("Select an audio file to convert.") }
    var convertedFilePath by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        convertedFilePath = null
        status = uri?.let { "Selected: ${it.lastPathSegment ?: "audio file"}" } ?: "No file selected."
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Audio Converter") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null)
                    Text(
                        text = "Audio conversion studio",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Button(
                onClick = { launcher.launch("audio/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Text("Choose audio file")
            }

            if (selectedUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Selected file", style = MaterialTheme.typography.titleMedium)
                        Text(selectedUri.toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedTextField(
                value = outputFormat,
                onValueChange = { outputFormat = it.trim().lowercase() },
                label = { Text("Output format") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            )

            Button(
                onClick = {
                    val uri = selectedUri ?: run {
                        status = "Please choose an audio file first."
                        return@Button
                    }

                    if (isBusy) return@Button

                    scope.launch {
                        isBusy = true
                        status = "Converting..."
                        try {
                            val inputFile = File(context.cacheDir, "input_${System.currentTimeMillis()}.tmp")
                            val outputFile = File(
                                context.cacheDir,
                                "converted_${System.currentTimeMillis()}.${normalizeExtension(outputFormat)}"
                            )

                            copyUriToFile(context, uri, inputFile)
                            val command = buildConversionCommand(inputFile.absolutePath, outputFile.absolutePath, outputFormat)
                            val session = withContext(Dispatchers.IO) { FFmpegKit.execute(command) }

                            if (session.returnCode.isSuccess) {
                                convertedFilePath = outputFile.absolutePath
                                status = "Conversion complete. Output saved to ${outputFile.absolutePath}"
                            } else {
                                status = "Conversion failed. Please try a different file or format. Return code: ${session.returnCode.value}"
                            }
                        } catch (e: Exception) {
                            status = "Error: ${e.message ?: "Unknown error"}"
                        } finally {
                            isBusy = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Text(if (isBusy) "Converting..." else "Convert")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Status", style = MaterialTheme.typography.titleMedium)
                    Text(status)
                    convertedFilePath?.let {
                        Text("Output: $it")
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    selectedUri = null
                    outputFormat = "mp3"
                    convertedFilePath = null
                    status = "Reset complete. Select a new file."
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Text("Reset")
            }
        }
    }
}

private fun normalizeExtension(format: String): String = when (format.lowercase()) {
    "wav" -> "wav"
    "mp3" -> "mp3"
    "aac" -> "aac"
    "flac" -> "flac"
    else -> "mp3"
}

private fun buildConversionCommand(inputPath: String, outputPath: String, format: String): String {
    val safeInput = shellQuote(inputPath)
    val safeOutput = shellQuote(outputPath)
    return when (normalizeExtension(format)) {
        "wav" -> "-y -i $safeInput -vn $safeOutput"
        "aac" -> "-y -i $safeInput -vn -c:a aac $safeOutput"
        "flac" -> "-y -i $safeInput -vn -c:a flac $safeOutput"
        else -> "-y -i $safeInput -vn -c:a libmp3lame -q:a 2 $safeOutput"
    }
}

private fun shellQuote(path: String): String = "'${path.replace("'", "'\\''")}'"

private fun copyUriToFile(context: android.content.Context, uri: Uri, outFile: File) {
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(outFile).use { output ->
            input.copyTo(output)
        }
    } ?: throw IllegalStateException("Unable to read the selected file.")
}
