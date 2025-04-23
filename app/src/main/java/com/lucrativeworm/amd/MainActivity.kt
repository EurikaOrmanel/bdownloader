package com.lucrativeworm.amd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucrativeworm.amd.ui.theme.AMDTheme
import com.lucrativeworm.bdownloader.AllDownloaderManager
import com.lucrativeworm.bdownloader.internal.DownloadTask
import com.lucrativeworm.bdownloader.models.DownloadRequest
import com.lucrativeworm.bdownloader.models.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AMDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val downloader = AllDownloaderManager(context)
    var id: Long? by remember { mutableStateOf(null) }
    var progress by remember { mutableFloatStateOf(0F) }
    var paused by remember { mutableStateOf(false) }
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val listener = object : DownloadTask.Listener {
        override fun onPause() {
            CoroutineScope(Dispatchers.Main).launch {
                paused = true
            }
            Log.e(
                "DL",
                "Download has paused...."
            )
        }

        override fun onCompleted() {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    "Download is completed",
                    Toast.LENGTH_LONG
                ).show()
            }
            Log.i(
                "DL",
                "Download has been completed"
            )
        }

        override fun onProgress(value: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                progress = (value * 0.01).toFloat()
            }
        }

        override fun onStart() {
            paused = false

            Log.i(
                "DL",
                "Download has been started"
            )
        }

        override fun onError(error: String) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    error,
                    Toast.LENGTH_LONG
                ).show()
            }
            Log.e(
                "DL",
                "An error occured whiles trying to download: $error"
            )
        }
    }
    Column {
        Spacer(Modifier.size(10.dp, 60.dp))

        Text(
            text = "Uh, is this thing working on, $name!",
            modifier = modifier
        )
        Spacer(Modifier.size(10.dp, 10.dp))
        Row {
            LinearProgressIndicator(progress = { progress })
            Spacer(Modifier.size(60.dp, 10.dp))
            IconButton({
                println("the iddd of the paused.... $id")
                id?.let { iddd ->
                    if (paused) {
                        downloader.resume(iddd, listener)
                    } else {
                        downloader.pause(iddd)
                    }
                }

            }) {
                Icon(
                    when (paused) {
                        true -> Icons.Default.PlayArrow
                        false -> Icons.Filled.Refresh
                    }, ""
                )
            }
        }
        Spacer(Modifier.size(10.dp, 60.dp))

        OutlinedButton(
            onClick = {
                val urlPdf = "https://www.rd.usda.gov/sites/default/files/pdf-sample_0.pdf"
                val urlMp3 =
                    "https://www.crateshub.com/images/Music/Single/DJ-Maxi-The-Amapiano-Mixtape-Vol-4-www.crateshub.com.mp3"
                val urlZip =
                    "https://pub-821312cfd07a4061bf7b99c1f23ed29b.r2.dev/3dicons-png-dynamic-1.0.0.zip"
                coroutineScope.launch {
                    val addedId = downloader.enqueue(
                        DownloadRequest(
                            filePath = context.filesDir.absolutePath + "9-${System.currentTimeMillis()}.mp3",
                            url = urlMp3,
                            downloadedBytes = 0,
                            totalBytes = 0,
                            status = Status.UNKNOWN,
                            updatedAt = 0
                        ),
                        listener
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        if (addedId != null) {
                            println("added id: $addedId -------------------------------")
                            id = addedId
                        }
                    }
                }

            }
        ) {
            Text("Start download....")
        }


    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AMDTheme {
        Greeting("Android")
    }
}