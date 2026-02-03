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

            comenzarButton.isEnabled = false

            try {
                val currentUserUid = "admin" // FUTURO: FirebaseAuth.getInstance().currentUser?.uid

                val nuevaEncuesta = Encuesta(
                    domicilio = domicilio,
                    ciudad = ciudad,
                    userUid = currentUserUid,
                    currentIndex = 0,
                    activa = true,
                    completa = false,
                    updatedAt = System.currentTimeMillis()
                )

                encuestaViewModel.insert(nuevaEncuesta)

                Toast.makeText(context, "Creando encuesta...", Toast.LENGTH_SHORT).show()

                encuestaViewModel.encuestaId.observe(viewLifecycleOwner) { encuestaid ->
                    if (encuestaid != null && encuestaid > 0) {
                        encuestaViewModel.encuestaId.removeObservers(viewLifecycleOwner)
                        val bundle = Bundle().apply { putInt("encuestaid", encuestaid) }
                        findNavController().navigate(R.id.action_encuestaFragment_to_foodFragment, bundle)
                    } else {
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
            findNavController().navigate(R.id.encuestasListFragment)

        }
    }
}
