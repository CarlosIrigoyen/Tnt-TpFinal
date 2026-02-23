package com.example.trabajofinal2024

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.concurrent.ConcurrentHashMap

class MapaFragment : Fragment(R.layout.fragment_mapa) {

    private lateinit var mapView: MapView
    private lateinit var btnVolver: ImageButton
    private lateinit var tvLeyendaTitulo: TextView
    private lateinit var tvCompletadasValue: TextView
    private lateinit var tvPausadasValue: TextView

    // LIMITES FIJOS DE TRELEW (aprox.)
    private val TRELEW_NORTH = -43.2150
    private val TRELEW_SOUTH = -43.2900
    private val TRELEW_EAST = -65.2500
    private val TRELEW_WEST = -65.3400

    // pequeños parámetros para posicionar labels mejor
    private val TRELEW_LAT_SPAN = TRELEW_NORTH - TRELEW_SOUTH
    private val TRELEW_LON_SPAN = TRELEW_EAST - TRELEW_WEST

    private val centerFraction = 0.30

    // colores
    private val completeColor = 0xFF4CAF50.toInt()
    private val pausedColor = 0xFFFF5722.toInt()
    private val zoneFill0 = 0x334CAF50.toInt()
    private val zoneFill1 = 0x33FF5722.toInt()
    private val zoneFill2 = 0x338BC34A.toInt()
    private val zoneFill3 = 0x334CAFEF.toInt()
    private val zoneFill4 = 0x33F06292.toInt()

    // caches / estructuras
    private val geocodeCache = ConcurrentHashMap<String, Pair<Double, Double>>()
    private val zonePolygons = mutableMapOf<Int, Polygon>()
    private val zoneOriginalColors = mutableMapOf<Int, Int>()
    private val markersByZone = mutableMapOf<Int, MutableList<Marker>>()
    private val labelMarkers = mutableMapOf<Int, Marker>()

    // contadores
    private val zoneTotals = IntArray(5)
    private val zoneCompletes = IntArray(5)
    private val zonePaused = IntArray(5)

    private var selectedZoneIndex = -1

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        mapView = view.findViewById(R.id.map)
        mapView.setMultiTouchControls(true)

        btnVolver = view.findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener { findNavController().popBackStack() }

        tvLeyendaTitulo = view.findViewById(R.id.tvLeyendaTitulo)
        tvCompletadasValue = view.findViewById(R.id.tvCompletadasValue)
        tvPausadasValue = view.findViewById(R.id.tvPausadasValue)

        // centrar en Trelew y limitar scroll al área
        val center = GeoPoint(-43.2496, -65.3000)
        mapView.controller.setCenter(center)
        mapView.controller.setZoom(13.5)
        val bounds = BoundingBox(TRELEW_NORTH, TRELEW_EAST, TRELEW_SOUTH, TRELEW_WEST)
        mapView.setScrollableAreaLimitDouble(bounds)

        // dibujar cuadrantes fijos y labels
        drawZones()

        encuestaViewModel.cargarEncuestasCompletasFirebase()

        // observar encuestas y plotear marcadores/contadores
        encuestaViewModel.encuestasFirebase.observe(viewLifecycleOwner) { encs ->
            lifecycleScope.launch {
                plotEncuestasAndCount(encs)
            }
        }

        resetLeyenda()
    }

    // ---------------- DRAW ZONES (FIJOS EN TRELEW) ----------------

    private fun drawZones() {
        // limpiar polígonos previos (mantener marcadores porque plot los re-agregará)
        mapView.overlays.removeAll(zonePolygons.values)
        zonePolygons.clear()
        zoneOriginalColors.clear()
        // limpiar labels previos
        labelMarkers.values.forEach { mapView.overlays.remove(it) }
        labelMarkers.clear()

        val north = TRELEW_NORTH
        val south = TRELEW_SOUTH
        val east = TRELEW_EAST
        val west = TRELEW_WEST

        val latSpan = north - south
        val lonSpan = east - west

        val halfCenterLatSpan = (latSpan * centerFraction) / 2.0
        val halfCenterLonSpan = (lonSpan * centerFraction) / 2.0

        val centerLat = (north + south) / 2.0
        val centerLon = (east + west) / 2.0

        val centerNorth = centerLat + halfCenterLatSpan
        val centerSouth = centerLat - halfCenterLatSpan
        val centerWest = centerLon - halfCenterLonSpan
        val centerEast = centerLon + halfCenterLonSpan

        // crear polígonos
        registerZone(0, createRectPolygon(centerNorth, centerEast, centerSouth, centerWest, zoneFill0))
        registerZone(1, createRectPolygon(north, east, centerNorth, west, zoneFill1))
        registerZone(2, createRectPolygon(centerSouth, east, south, west, zoneFill2))
        registerZone(3, createRectPolygon(centerNorth, east, centerSouth, centerEast, zoneFill3))
        registerZone(4, createRectPolygon(centerNorth, centerWest, centerSouth, west, zoneFill4))

        // añadir labels (posicionados con offsets para evitar solapamientos)
        addOrUpdateLabel(0, (centerNorth + centerSouth) / 2.0, (centerWest + centerEast) / 2.0, zoneName(0))
        addOrUpdateLabel(1, (north + centerNorth) / 2.0, (west + east) / 2.0, zoneName(1))
        addOrUpdateLabel(2, (centerSouth + south) / 2.0, (west + east) / 2.0, zoneName(2))
        addOrUpdateLabel(3, centerLat, (centerEast + east) / 2.0, zoneName(3))
        addOrUpdateLabel(4, centerLat, (west + centerWest) / 2.0, zoneName(4))

        mapView.invalidate()
    }

    private fun registerZone(index: Int, polygon: Polygon) {
        polygon.setOnClickListener { _, _, _ ->
            onZoneClicked(index)
            true
        }
        zonePolygons[index] = polygon
        zoneOriginalColors[index] = polygon.fillColor
        mapView.overlays.add(polygon)
    }

    private fun createRectPolygon(north: Double, east: Double, south: Double, west: Double, fillColor: Int): Polygon {
        val polygon = Polygon(mapView)
        polygon.points = listOf(
            GeoPoint(north, west),
            GeoPoint(north, east),
            GeoPoint(south, east),
            GeoPoint(south, west)
        )
        polygon.fillColor = fillColor
        polygon.strokeColor = 0x80000000.toInt()
        polygon.strokeWidth = 2f
        return polygon
    }

    // ---------------- LABELS MEJORADAS ----------------

    private fun addOrUpdateLabel(index: Int, baseLat: Double, baseLon: Double, text: String) {
        // offsets relativos al tamaño de Trelew para evitar solapamientos
        val latOffset = TRELEW_LAT_SPAN * 0.03  // ~3% del alto del área
        val lonOffset = TRELEW_LON_SPAN * 0.03  // ~3% del ancho del área

        val (lat, lon) = when (index) {
            1 -> Pair(baseLat + latOffset, baseLon) // NORTE -> subir un poco
            2 -> Pair(baseLat - latOffset, baseLon) // SUR -> bajar un poco
            3 -> Pair(baseLat, baseLon + lonOffset) // ESTE -> mover a la derecha
            4 -> Pair(baseLat, baseLon - lonOffset) // OESTE -> mover a la izquierda
            else -> Pair(baseLat + latOffset * 0.4, baseLon) // CENTRO -> un poco arriba
        }

        val existing = labelMarkers[index]
        val drawable = createTextDrawable(text, width = 280, height = 110, textSize = 20f)
        if (existing != null) {
            existing.position = GeoPoint(lat, lon)
            existing.icon = drawable
        } else {
            val m = Marker(mapView)
            m.position = GeoPoint(lat, lon)
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            m.icon = drawable
            m.title = null
            mapView.overlays.add(m)
            labelMarkers[index] = m
        }
    }

    // ---------------- PLOTEAR ENCUESTAS Y CONTAR ----------------

    private suspend fun plotEncuestasAndCount(encuestas: List<EncuestaFirestore>) = withContext(Dispatchers.Main) {
        // resetear contadores y remover marcadores previos
        for (i in 0 until 5) {
            zoneTotals[i] = 0
            zoneCompletes[i] = 0
            zonePaused[i] = 0
            markersByZone[i]?.forEach { mapView.overlays.remove(it) }
            markersByZone[i] = mutableListOf()
        }

        // procesar encuestas con coords
        for (enc in encuestas) {
            val lat = enc.lan
            val lon = enc.lon
            if (lat == 0.0 && lon == 0.0) continue

            val zoneIndex = determineZoneForLatLon(lat, lon)
            if (zoneIndex !in 0..4) continue

            zoneTotals[zoneIndex]++
            if (enc.completa) zoneCompletes[zoneIndex]++ else zonePaused[zoneIndex]++

            val marker = Marker(mapView)
            marker.position = GeoPoint(lat, lon)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = createCircleDrawable(if (enc.completa) completeColor else pausedColor)
            marker.setOnMarkerClickListener { m, mv ->
                mv.controller.animateTo(m.position)
                mv.controller.setZoom(16.0)
                true
            }

            markersByZone.getOrPut(zoneIndex) { mutableListOf() }.add(marker)
            mapView.overlays.add(marker)
        }

        // si hay zona seleccionada, actualizar la leyenda con la zona y conteos
        if (selectedZoneIndex in 0..4) {
            tvLeyendaTitulo.text = "Estado de Encuesta: ${zoneName(selectedZoneIndex)}"
            tvCompletadasValue.text = zoneCompletes[selectedZoneIndex].toString()
            tvPausadasValue.text = zonePaused[selectedZoneIndex].toString()
        } else {
            resetLeyenda()
        }

        mapView.invalidate()
    }

    // ---------------- SELECCIÓN / LEYENDA ----------------

    private fun onZoneClicked(index: Int) {
        // resaltar polígonos
        zonePolygons.forEach { (i, poly) ->
            val orig = zoneOriginalColors[i] ?: 0x30CCCCCC.toInt()
            poly.fillColor = if (i == index) orig else 0x20CCCCCC.toInt()
        }
        selectedZoneIndex = index

        // actualizar la tarjeta para mostrar la zona
        tvLeyendaTitulo.text = "Estado de Encuesta: ${zoneName(index)}"
        tvCompletadasValue.text = zoneCompletes[index].toString()
        tvPausadasValue.text = zonePaused[index].toString()

        mapView.invalidate()
    }

    private fun resetLeyenda() {
        tvLeyendaTitulo.text = "Estado de Encuestas"
        tvCompletadasValue.text = "-"
        tvPausadasValue.text = "-"
    }

    // ---------------- DRAW HELPERS (ICONOS Y TEXTOS) ----------------

    private fun createCircleDrawable(color: Int, size: Int = 44): BitmapDrawable {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
        return BitmapDrawable(resources, bmp)
    }

    private fun createTextDrawable(text: String, width: Int = 220, height: Int = 80, textSize: Float = 18f): BitmapDrawable {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // fondo blanco semi-opaco con bordes redondeados
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xEFFFFF.toInt() } // blanco semitransparente
        // nota: usar 0xEFFFFF no es un color ARGB perfecto — si preferís: 0xCCFFFFFF.toInt()
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 14f, 14f, bg)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            isFakeBoldText = true
            color = 0xFF000000.toInt()
            textAlign = Paint.Align.CENTER
        }

        val lines = text.split("\n")
        val fm = paint.fontMetrics
        val lineHeight = (fm.bottom - fm.top)
        var y = (height - lineHeight * lines.size) / 2f - fm.top

        for (line in lines) {
            canvas.drawText(line, width / 2f, y, paint)
            y += lineHeight
        }

        return BitmapDrawable(resources, bmp)
    }

    // ---------------- DETERMINAR ZONA (FIJA EN TRELEW) ----------------

    private fun determineZoneForLatLon(lat: Double, lon: Double): Int {
        if (lat > TRELEW_NORTH || lat < TRELEW_SOUTH || lon > TRELEW_EAST || lon < TRELEW_WEST) return -1

        val latSpan = TRELEW_NORTH - TRELEW_SOUTH
        val lonSpan = TRELEW_EAST - TRELEW_WEST
        val halfCenterLatSpan = (latSpan * centerFraction) / 2.0
        val halfCenterLonSpan = (lonSpan * centerFraction) / 2.0
        val centerLat = (TRELEW_NORTH + TRELEW_SOUTH) / 2.0
        val centerLon = (TRELEW_EAST + TRELEW_WEST) / 2.0
        val centerNorth = centerLat + halfCenterLatSpan
        val centerSouth = centerLat - halfCenterLatSpan
        val centerWest = centerLon - halfCenterLonSpan
        val centerEast = centerLon + halfCenterLonSpan

        return when {
            lat >= centerSouth && lat <= centerNorth && lon >= centerWest && lon <= centerEast -> 0
            lat > centerNorth -> 1
            lat < centerSouth -> 2
            lon > centerEast -> 3
            lon < centerWest -> 4
            else -> -1
        }
    }

    private fun zoneName(index: Int): String = when (index) {
        0 -> "CENTRO"
        1 -> "NORTE"
        2 -> "SUR"
        3 -> "ESTE"
        4 -> "OESTE"
        else -> "DESCONOCIDO"
    }

    // lifecycle
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }
}
