package com.example.trabajofinal2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MapFragment : Fragment(R.layout.fragment_map) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText: TextView = view.findViewById(R.id.titleText)
        titleText.text = "Mapa"

        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        descriptionText.text = "Pantalla de mapa - Funcionalidad pendiente"

        val backButton: Button = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}