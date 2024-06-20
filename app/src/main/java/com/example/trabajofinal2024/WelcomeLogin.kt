package com.example.trabajofinal2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class WelcomeLogin : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño para este fragmento
        val rootView = inflater.inflate(R.layout.fragment_welcome_login, container, false)


        // Configurar los bordes del sistema
        enableEdgeToEdge(rootView)


        // Configurar el botón de encuestas
        val encuestas = rootView.findViewById<Button>(R.id.encuestasid)
        encuestas.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeLogin_to_yogurtFragment)
        }

        return rootView
    }

    private fun enableEdgeToEdge(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}