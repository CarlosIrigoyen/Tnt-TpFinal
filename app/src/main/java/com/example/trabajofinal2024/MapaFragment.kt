package com.example.trabajofinal2024

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapaFragment : Fragment(R.layout.fragment_mapa) {

    private lateinit var mapView: MapView

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    private fun addMarker(point: GeoPoint, titulo: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = titulo
        mapView.overlays.add(marker)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración obligatoria
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        val btnVolver = view.findViewById<ImageButton>(R.id.btnVolver)

        btnVolver.setOnClickListener {
            findNavController().navigate(R.id.action_mapaFragment_to_encuestasListFragment)
        }

        mapView = view.findViewById(R.id.map)
        mapView.setMultiTouchControls(true)

        // Zoom y posición inicial
        val startPoint = GeoPoint(-43.2489, -65.3051) // Trelew
        mapView.controller.setZoom(14.5)
        mapView.controller.setCenter(startPoint)
        encuestaViewModel.getEncuestas().observe(viewLifecycleOwner) { encuestas ->
            mapView.overlays.clear()
            encuestas
                .filter { it.lan != null && it.lon != null }
                .forEach { encuesta ->
                    addMarker(
                        GeoPoint(encuesta.lan!!, encuesta.lon!!),
                        encuesta.domicilio
                    )
                }
            mapView.invalidate()
        }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
