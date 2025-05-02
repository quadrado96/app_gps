package com.quadrado.app_gps

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var locationManager: LocationManager
    private val locationList = mutableListOf<Location>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var status = "Não Iniciado"

        tvStatus = findViewById(R.id.tv_status)
        tvStatus.text = "Status: $status"

        val btnIniciar = findViewById<Button>(R.id.btn_iniciar)
        val btnEncerrar = findViewById<Button>(R.id.btn_encerrar)
        val btnDownload = findViewById<Button>(R.id.btn_download)

        btnIniciar.setOnClickListener {
            locationPermissionRequest.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }

        btnEncerrar.setOnClickListener {
            encerrarColeta()
        }

        btnDownload.setOnClickListener {
            status = "Download realizado."
            tvStatus.text = "Status: $status"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.azul))
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            iniciarColeta()
        } else {
            Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationList.add(location)
            Log.d("GPS", "Localização: ${location.latitude}, ${location.longitude}")
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    }

    private fun iniciarColeta() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )
            tvStatus.text = "Status: Iniciado"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.verde))
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permissão não concedida!", Toast.LENGTH_LONG).show()
        }
    }

    private fun encerrarColeta() {
        try {
            locationManager.removeUpdates(locationListener)
            tvStatus.text = "Status: Encerrado"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.vermelho))
            Toast.makeText(this, "Coleta encerrada com sucesso.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao encerrar a coleta.", Toast.LENGTH_SHORT).show()
        }
    }
}

