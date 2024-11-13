package com.example.cmarayubicacin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MyApp(fusedLocationClient)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyApp(fusedLocationClient: FusedLocationProviderClient) {
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(TakePicturePreview()) { bitmap ->
        bitmap?.let {
            imageBitmap = it
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    // Obtener el contexto fuera del listener
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if (permissionsState.allPermissionsGranted) {
                cameraLauncher.launch()
            } else {
                permissionsState.launchMultiplePermissionRequest()
            }
        }) {
            Text("Capturar Foto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (permissionsState.allPermissionsGranted) {
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    currentLocation = location
                }.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Error al obtener ubicación actual",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                permissionsState.launchMultiplePermissionRequest()
            }
        }) {
            Text("Obtener Ubicación")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageBitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Imagen capturada",
                modifier = Modifier.size(200.dp)
            )
        }

        currentLocation?.let { location ->
            Text("Latitud: ${location.latitude}, Longitud: ${location.longitude}")
        }
    }
}
