package com.plants

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.plants.navigation.PlantNavHost
import com.plants.ui.QrScannerMlKit

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val cameraPermissionGranted = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                var hasPermission by remember { mutableStateOf(cameraPermissionGranted) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted -> hasPermission = granted }
                )

                LaunchedEffect(Unit) {
                    if (!hasPermission) {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    if (hasPermission) {
                        // スキャナ領域
                        QrScannerMlKit(modifier = Modifier.weight(1f)) { value ->
                            // 読み取り時の処理：ここで保存する（例：リストに追加）
                            Toast.makeText(this@MainActivity, "Scanned: $value", Toast.LENGTH_SHORT)
                                .show()

                        }
                    } else {
                        Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                            Text("カメラ権限を許可する") // TODO: string resource
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { PlantNavHost() }
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
        // Used to load the 'plants' library on application startup.
        init {
            System.loadLibrary("plants")
        }
    }
}
