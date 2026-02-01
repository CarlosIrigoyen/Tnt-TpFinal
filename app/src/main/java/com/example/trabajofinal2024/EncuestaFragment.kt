package com.example.trabajofinal2024

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class EncuestaFragment : Fragment(R.layout.fragment_encuesta) {

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory((activity?.application as App).encuestaRepositorio)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val domicilioInput: EditText = view.findViewById(R.id.domicilioInput)
        val ciudadInput: EditText = view.findViewById(R.id.ciudadInput)
        val comenzarButton: Button = view.findViewById(R.id.comenzar)
        val volverButton: Button = view.findViewById(R.id.volver)

        comenzarButton.setOnClickListener {
            val domicilio = domicilioInput.text.toString().trim()
            val ciudad = ciudadInput.text.toString().trim()

            if (domicilio.isBlank() || ciudad.isBlank()) {
                Toast.makeText(requireContext(), "Completá domicilio y ciudad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Evitar doble click
            comenzarButton.isEnabled = false

            try {
                // Insertar encuesta (se actualizará encuestaId en el ViewModel)
                encuestaViewModel.insert(
                    Encuesta(
                        domicilio = domicilio,
                        ciudad = ciudad
                    )
                )

                Toast.makeText(context, "Creando encuesta...", Toast.LENGTH_SHORT).show()

                // Observador: una vez que tengamos el id navegamos y removemos observers
                encuestaViewModel.encuestaId.observe(viewLifecycleOwner) { encuestaid ->
                    if (encuestaid != null && encuestaid > 0) {
                        // evitamos que se active de nuevo
                        encuestaViewModel.encuestaId.removeObservers(viewLifecycleOwner)

                        val bundle = Bundle().apply { putInt("encuestaid", encuestaid) }

                        // Navegar al único FoodFragment (no fragment por alimento)
                        findNavController().navigate(R.id.action_encuestaFragment_to_foodFragment, bundle)
                    } else {
                        // si id es 0 o nulo - reactivar botón para reintento
                        comenzarButton.isEnabled = true
                        Log.e("EncuestaFragment", "ID de encuesta inválida: $encuestaid")
                    }
                }
            } catch (e: Exception) {
                Log.e("EncuestaFragment", "Error insertando la encuesta: ${e.message}")
                Toast.makeText(context, "Error creando encuesta", Toast.LENGTH_SHORT).show()
                comenzarButton.isEnabled = true
            }
        }

        volverButton.setOnClickListener {
            findNavController().navigate(R.id.welcomeLogin)
        }
    }
}
