package com.quadrado.app_gps

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

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

        btnIniciar.setOnClickListener {
            status = "Iniciado"
            tvStatus.text = "Status: $status"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.verde))
        }

        btnEncerrar.setOnClickListener {
            status = "Encerrado"
            tvStatus.text = "Status: $status"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.vermelho))
        }

    }
}
