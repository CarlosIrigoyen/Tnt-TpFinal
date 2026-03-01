package com.example.trabajofinal2024

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class EncuestaFragment : Fragment(R.layout.fragment_encuesta) {

    private  val CIUDAD_FIJA = "Trelew"
    private  val PROVINCIA_FIJA = "Chubut"
    private  val PAIS_FIJO = "Argentina"

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
            val domicilioCompleto = "$domicilio, $CIUDAD_FIJA, $PROVINCIA_FIJA, $PAIS_FIJO"
            val coords = geocodificarDireccion(requireContext(), domicilioCompleto)
            if (coords == null) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo ubicar la dirección",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val(lan,lon) = coords

            val ciudad = ciudadInput.text.toString().trim()

            if (domicilio.isBlank() || ciudad.isBlank()) {
                Toast.makeText(requireContext(), "Completá domicilio y ciudad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            comenzarButton.isEnabled = false

            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserUid == null) {
                findNavController().navigate(R.id.loginFragment)
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("encuestas").document()


            try {
                val nuevaEncuesta = Encuesta(
                    firestoreId = docRef.id,
                    domicilio = domicilio,
                    ciudad = ciudad,
                    lon = lon,
                    lan = lan,
                    userUid = currentUserUid,
                    currentIndex = 0,
                    activa = true,
                    completa = false,
                    updatedAt = System.currentTimeMillis()
                )

                docRef.set(nuevaEncuesta)

                encuestaViewModel.insert(nuevaEncuesta)

                Toast.makeText(context, "Creando encuesta...", Toast.LENGTH_SHORT).show()

                val bundle = Bundle().apply {
                    putString("encuestaid", nuevaEncuesta.firestoreId)
                }

                findNavController().navigate(
                    R.id.action_encuestaFragment_to_foodFragment,
                    bundle
                )
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

    private fun geocodificarDireccion(
        context: Context,
        direccion: String
    ): Pair<Double, Double>? {

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(direccion, 1)

            if (!results.isNullOrEmpty()) {
                val location = results[0]
                Pair(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}
