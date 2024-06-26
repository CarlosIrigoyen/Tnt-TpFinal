package com.example.trabajofinal2024

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.launch


class EncuestaFragment : Fragment() {

    private val encuestaViewModel: EncuestaViewModel by viewModels() {
        EncuestaViewModel.EncuestaViewModelFactory((activity?.application as App).encuestaRepositorio)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_encuesta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val domicilioInput: EditText = view.findViewById(R.id.domicilioInput)
        val ciudadInput: EditText = view.findViewById(R.id.ciudadInput)

        // Configurar el click listener para el botón
        val comenzarButton: Button = view.findViewById(R.id.comenzar)
        comenzarButton.setOnClickListener {
            val domicilio = domicilioInput.text.toString()
            val ciudad = ciudadInput.text.toString()


                try {
                    encuestaViewModel.insert(
                        Encuesta(domicilio = domicilio,
                            ciudad = ciudad
                        )
                    )
                    Toast.makeText(context, "Encuesta creada", Toast.LENGTH_SHORT).show()

                    encuestaViewModel.encuestaId.observe(viewLifecycleOwner) { encuestaid ->
                        if (encuestaid != null) {
                            val bundle = Bundle()
                            bundle.putInt("encuestaid", encuestaid)
                            NavHostFragment.findNavController(this)
                                .navigate(R.id.action_encuestaFragment_to_lechePolvoEntera, bundle)

                        }else {
                            Log.e("EncuestaFragment", "Error: ID de encuesta nula")
                        }
                    }
                }catch (e:Exception) {
                    Log.e("EncuestaFragment", "Error insertando la encuesta: ${e.message}")
                }

            }

        val volverButton: Button = view.findViewById(R.id.volver)
        volverButton.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_encuestaFragment_to_welcomeLogin)
        }
    }

}