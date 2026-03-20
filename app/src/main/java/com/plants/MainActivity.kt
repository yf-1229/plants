package com.plants

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.plants.data.AppContainer
import com.plants.data.AppDataContainer
import com.plants.data.Plant
import com.plants.data.PlantsDatabase
import com.plants.navigation.PlantNavHost
import com.plants.ui.QrScannerMlKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val plantDao by lazy {
        PlantsDatabase.getDatabase(applicationContext).plantDao()
    }

    lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppDataContainer(this)
        setContent {
            MaterialTheme {
                val cameraPermissionGranted = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                var hasPermission by remember { mutableStateOf(cameraPermissionGranted) }
                var isScannerOpen by remember { mutableStateOf(true) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted -> hasPermission = granted }
                )

                LaunchedEffect(Unit) {
                    if (!hasPermission) {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (hasPermission && isScannerOpen) {
                        QrScannerMlKit(modifier = Modifier.fillMaxSize()) { value ->
                            val scannedValue = value.trim()
                            val parameterValue = scannedValue.toIntOrNull()
                            if (parameterValue != null) {
                                lifecycleScope.launch {
                                    withContext(Dispatchers.IO) {
                                        plantDao.insert(
                                            Plant(
                                                name = scannedValue,
                                                description = "Scanned QR code: $scannedValue",
                                                parameter = parameterValue
                                            )
                                        )
                                    }
                                    isScannerOpen = false
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Scanned: $scannedValue",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please scan an integer value",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else if (hasPermission) {
                        PlantNavHost() // <- go to main screen
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                                Text("カメラ権限を許可する") // TODO: string resource
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart Called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy Called")
    }

    /**
     * A native method that is implemented by the 'plants' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        val container: Any

        // Used to load the 'plants' library on application startup.
        init {
            System.loadLibrary("plants")
        }
    }
}
