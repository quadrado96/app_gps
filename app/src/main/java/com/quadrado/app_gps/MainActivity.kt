package com.quadrado.app_gps

import android.content.Intent
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
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

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
            if (locationList.isEmpty()) {
                Toast.makeText(this, "Nenhum dado coletado!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gpx = gerarGpx()
            val file = salvarArquivoGpx(gpx)
            if (file != null) {
                tvStatus.text = "Status: Download realizado"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.azul))
                compartilharArquivo(file)
            }
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

    private fun gerarGpx(): String {
        val gpxBuilder = StringBuilder()
        gpxBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        gpxBuilder.append("<gpx version=\"1.1\" creator=\"AppGPS\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        gpxBuilder.append("<trk>\n<trkseg>\n")

        for (location in locationList) {
            val time = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date(location.time))

            gpxBuilder.append(
                "<trkpt lat=\"${location.latitude}\" lon=\"${location.longitude}\">\n" +
                        "<time>$time</time>\n" +
                        "</trkpt>\n"
            )
        }

        gpxBuilder.append("</trkseg>\n</trk>\n</gpx>")

        return gpxBuilder.toString()
    }

    private fun salvarArquivoGpx(gpxContent: String): File? {
        return try {
            val nomeArquivo = "track_${System.currentTimeMillis()}.gpx"
            val file = File(getExternalFilesDir(null), nomeArquivo)
            file.writeText(gpxContent)
            file
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar o arquivo.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun compartilharArquivo(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Compartilhar arquivo GPX"))
    }

}

