package com.example.trabajofinal2024

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment

class EncuestaFragment : Fragment(R.layout.fragment_encuesta) {

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory((activity?.application as App).encuestaRepositorio)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val domicilioInput: EditText = view.findViewById(R.id.domicilioInput)
        val ciudadInput: EditText = view.findViewById(R.id.ciudadInput)
        val comenzarButton: Button = view.findViewById(R.id.comenzar)

        comenzarButton.setOnClickListener {
            val domicilio = domicilioInput.text.toString()
            val ciudad = ciudadInput.text.toString()
            try {
                encuestaViewModel.insert(Encuesta(domicilio = domicilio, ciudad = ciudad))
                Toast.makeText(context, "Creando encuesta...", Toast.LENGTH_SHORT).show()

                encuestaViewModel.encuestaId.observe(viewLifecycleOwner, Observer { encuestaid ->
                    if (encuestaid != null && encuestaid > 0) {
                        val bundle = Bundle().apply { putInt("encuestaid", encuestaid) }
                        NavHostFragment.findNavController(this).navigate(R.id.action_encuestaFragment_to_foodFragment, bundle)
                    }
                })
            } catch (e: Exception) {
                Log.e("EncuestaFragment", "Error insertando: ${e.message}")
            }
        }
    }
}
