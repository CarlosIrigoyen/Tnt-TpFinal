package com.example.trabajofinal2024

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

class EncuestaFragment : Fragment(R.layout.fragment_encuesta) {

    private val CIUDAD_FIJA = "Trelew"

    private var latConfirmada = 0.0
    private var lonConfirmada = 0.0
    private var domicilioConfirmado = ""

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("lat", latConfirmada)
        outState.putDouble("lon", lonConfirmada)
        outState.putString("domicilio", domicilioConfirmado)
    }


    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory((activity?.application as App).encuestaRepositorio)
    }

    private val turnoAdminViewModel: TurnoAdminViewModel by viewModels {
        TurnoAdminViewModel.TurnoAdminViewModelFactory((activity?.application as App).turnoRepository)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val turnoSpinner: Spinner = view.findViewById(R.id.turnoInput)
        val domicilioInput: EditText = view.findViewById(R.id.domicilioInput)
        val comenzarButton: Button = view.findViewById(R.id.comenzar)
        val volverButton: Button = view.findViewById(R.id.volver)

        savedInstanceState?.let {
            latConfirmada = it.getDouble("lat", 0.0)
            lonConfirmada = it.getDouble("lon", 0.0)
            domicilioConfirmado = it.getString("domicilio", "") ?: ""
            domicilioInput.setText(domicilioConfirmado)
        }



        val listaTurnos = mutableListOf<TurnoNombre>()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listaTurnos
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        turnoSpinner.adapter = adapter


        turnoAdminViewModel.buscarTurnosConfirmados()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                turnoAdminViewModel.turnosNombre.collect { turnos ->
                    listaTurnos.clear()
                    listaTurnos.addAll(turnos)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        domicilioInput.isFocusable = false
        domicilioInput.isFocusableInTouchMode = false

        domicilioInput.setOnClickListener {
            val bottomSheet = MapBottomSheet.newInstance(
                latInicial = if (latConfirmada != 0.0) latConfirmada else -43.2489,
                lonInicial = if (lonConfirmada != 0.0) lonConfirmada else -65.3039,
                domicilioInicial = domicilioConfirmado
            )
            bottomSheet.onUbicacionConfirmada = { domicilio, lat, lon ->
                domicilioConfirmado = domicilio
                latConfirmada = lat
                lonConfirmada = lon
                domicilioInput.setText(domicilio)
            }
            bottomSheet.show(parentFragmentManager, "MapaBottomSheet")
        }


        comenzarButton.setOnClickListener {
            val domicilio = domicilioInput.text.toString().trim()
            val ciudad = CIUDAD_FIJA

            val turnoSeleccionado = turnoSpinner.selectedItem as? TurnoNombre
            if (turnoSeleccionado == null) {
                Toast.makeText(requireContext(), "Seleccioná un turno", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (domicilio.isBlank()) {
                Toast.makeText(requireContext(), "Completá domicilio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (latConfirmada == 0.0 && lonConfirmada == 0.0) {
                Toast.makeText(requireContext(), "Falta la dirección en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            comenzarButton.isEnabled = false

            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserUid == null) {
                findNavController().navigate(R.id.loginFragment)
                return@setOnClickListener
            }

            try {
                val nuevaEncuesta = Encuesta(
                    domicilio = domicilio,
                    ciudad = ciudad,
                    lon = lonConfirmada,
                    lan = latConfirmada,
                    voluntarioid = turnoSeleccionado.uidVoluntario,
                    administradorid = currentUserUid,
                    currentIndex = 0,
                    activa = true,
                    completa = false,
                    updatedAt = System.currentTimeMillis(),
                    turnoId = turnoSeleccionado.id
                )

                encuestaViewModel.insertFirebase(nuevaEncuesta)
                Toast.makeText(context, "Creando encuesta...", Toast.LENGTH_SHORT).show()
                turnoAdminViewModel.asignarTurno(turnoSeleccionado.id)
                encuestaViewModel.firestoreId.observe(viewLifecycleOwner) { encuestaid ->
                    if (encuestaid != null && encuestaid != "") {
                        encuestaViewModel.encuestaId.removeObservers(viewLifecycleOwner)
                        val bundle = Bundle().apply { putString("encuestaid", encuestaid) }
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

        val bottomSheetExistente = parentFragmentManager.findFragmentByTag("MapaBottomSheet") as? MapBottomSheet
            bottomSheetExistente?.onUbicacionConfirmada = { domicilio, lat, lon ->
            domicilioConfirmado = domicilio
            latConfirmada = lat
            lonConfirmada = lon
            domicilioInput.setText(domicilio)
        }

        volverButton.setOnClickListener {
            findNavController().navigate(R.id.encuestasListFragment)
        }
    }

}