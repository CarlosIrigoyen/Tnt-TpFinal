package com.example.trabajofinal2024

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.Locale

class MapBottomSheet: BottomSheetDialogFragment() {

    companion object {
        fun newInstance(
            latInicial: Double = -43.2489,
            lonInicial: Double = -65.3039,
            domicilioInicial: String = ""
        ): MapBottomSheet {
            return MapBottomSheet().apply {
                arguments = Bundle().apply {
                    putDouble("lat", latInicial)
                    putDouble("lon", lonInicial)
                    putString("domicilio", domicilioInicial)
                }
            }
        }
    }

    var onUbicacionConfirmada: ((domicilio: String, lat: Double, lon: Double) -> Unit)? = null


    private lateinit var mapa: MapView
    private lateinit var marcador: Marker
    private var latActual = -43.2489
    private var lonActual = -65.3039
    private lateinit var busquedaInput: EditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            latActual = savedInstanceState.getDouble("lat")
            lonActual = savedInstanceState.getDouble("lon")
        } else {
            latActual = arguments?.getDouble("lat") ?: -43.2489
            lonActual = arguments?.getDouble("lon") ?: -65.3039
        }

        val domicilioInicial = savedInstanceState?.getString("domicilio")
            ?: arguments?.getString("domicilio") ?: ""

        Configuration.getInstance().userAgentValue = requireContext().packageName

        mapa = view.findViewById(R.id.mapaView)
        mapa.setTileSource(TileSourceFactory.MAPNIK)
        mapa.setMultiTouchControls(true)

        val mapController = mapa.controller
        mapController.setZoom(17.0)
        mapController.setCenter(GeoPoint(latActual, lonActual))



        marcador = Marker(mapa)
        marcador.position = GeoPoint(latActual, lonActual)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = "Ubicación seleccionada"
        marcador.isDraggable = true
        mapa.overlays.add(marcador)

        if (domicilioInicial.isNotBlank()) {
            busquedaInput.setText(domicilioInicial)
        }

        val tapOverlay = object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint

                latActual = geoPoint.latitude
                lonActual = geoPoint.longitude

                marcador.position = geoPoint
                mapView.invalidate()

                actualizarDireccionDesdeCoords(latActual, lonActual, view)
                return true
            }
        }

        mapa.overlays.add(0, tapOverlay)
        mapa.invalidate()

        busquedaInput = view.findViewById(R.id.busquedaInput)

        val buscarBtn: Button = view.findViewById(R.id.buscarBtn)

        buscarBtn.setOnClickListener {
            val texto = busquedaInput.text.toString().trim()
            if (texto.isNotBlank()) {
                buscarDireccion("$texto, Trelew, Chubut, Argentina", view)
            }
        }

        busquedaInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscarBtn.performClick()
                true
            } else false
        }

        view.findViewById<Button>(R.id.confirmarUbicacionBtn).setOnClickListener {
            val domicilio = busquedaInput.text.toString().trim()
            if (domicilio.isBlank()) {
                Toast.makeText(requireContext(), "Ingresa una dirección", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onUbicacionConfirmada?.invoke(domicilio, latActual, lonActual)
            dismiss()
        }
    }

    private fun buscarDireccion(direccion: String, view: View) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val resultados = geocoder.getFromLocationName(direccion, 1)
            if (!resultados.isNullOrEmpty()) {
                val loc = resultados[0]
                latActual = loc.latitude
                lonActual = loc.longitude

                val punto = GeoPoint(latActual, lonActual)
                mapa.controller.animateTo(punto)
                mapa.controller.setZoom(17.0)
                marcador.position = punto
                mapa.invalidate()
            } else {
                Toast.makeText(requireContext(), "No se encontró la dirección", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error buscando dirección", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarDireccionDesdeCoords(lat: Double, lon: Double, view: View) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val resultados = geocoder.getFromLocation(lat, lon, 1)
            if (!resultados.isNullOrEmpty()) {
                val direccion = resultados[0].thoroughfare ?: ""
                val numero = resultados[0].subThoroughfare ?: ""
                val texto = if (numero.isNotBlank()) "$direccion $numero" else direccion
                view.findViewById<EditText>(R.id.busquedaInput).setText(texto)
            }
        } catch (e: Exception) {
            Log.e("Mapa", "Error geocodificación inversa", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("lat", latActual)
        outState.putDouble("lon", lonActual)
        outState.putString("domicilio", busquedaInput.text.toString())
    }

    override fun onResume() {
        super.onResume()
        mapa.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapa.onPause()
    }
}