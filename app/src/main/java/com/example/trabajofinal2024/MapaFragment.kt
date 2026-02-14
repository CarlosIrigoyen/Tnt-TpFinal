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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class MapaFragment : Fragment(R.layout.fragment_mapa) {

    private lateinit var mapView: MapView
    private lateinit var btnVolver: ImageButton

    // Leyenda views
    private lateinit var tvLeyendaTitulo: TextView
    private lateinit var tvCompletadasValue: TextView
    private lateinit var tvPausadasValue: TextView

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    private val httpClient = OkHttpClient()
    private val geocodeCache = ConcurrentHashMap<String, Pair<Double, Double>>()

    // fracción del centro
    private val centerFraction = 0.30

    // colores y fills
    private val completeColor = 0xFF4CAF50.toInt()
    private val pausedColor = 0xFFFF5722.toInt()
    private val zoneFill0 = 0x334caf50.toInt()
    private val zoneFill1 = 0x33ff5722.toInt()
    private val zoneFill2 = 0x338bc34a.toInt()
    private val zoneFill3 = 0x334cafef.toInt()
    private val zoneFill4 = 0x33f06292.toInt()

    private val cityName = "Trelew, Chubut, Argentina"

    // estructuras
    private val zonePolygons = mutableMapOf<Int, Polygon>()
    private val zoneOriginalColors = mutableMapOf<Int, Int>()
    private val markersByZone = mutableMapOf<Int, MutableList<Marker>>()
    private val encuestasByZone = mutableMapOf<Int, MutableList<Encuesta>>()
    private val labelMarkers = mutableMapOf<Int, Marker>()

    private var selectedZoneIndex: Int = -1

    // contadores por zona
    private val zoneTotals = IntArray(5) { 0 }
    private val zoneCompletes = IntArray(5) { 0 }
    private val zonePaused = IntArray(5) { 0 }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // prefs osmdroid
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        mapView = view.findViewById(R.id.map)
        mapView.setMultiTouchControls(true)

        btnVolver = view.findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener { findNavController().popBackStack() }

        // leyenda views (ids en el XML que te pasé)
        tvLeyendaTitulo = view.findViewById(R.id.tvLeyendaTitulo)
        tvCompletadasValue = view.findViewById(R.id.tvCompletadasValue)
        tvPausadasValue = view.findViewById(R.id.tvPausadasValue)

        // centrar en Trelew
        mapView.controller.setCenter(GeoPoint(-43.2496, -65.3000))
        mapView.controller.setZoom(13.0)

        // dibujar zonas tras tener bounding box válido
        mapView.post { drawZones() }

        // observar encuestas usando tu método existente en el ViewModel
        encuestaViewModel.getEncuestas().observe(viewLifecycleOwner) { encs ->
            lifecycleScope.launch {
                plotEncuestasAndCount(encs)
            }
        }

        resetLeyenda()
    }

    private fun resetLeyenda() {
        tvLeyendaTitulo.text = "Estado de Encuestas"
        tvCompletadasValue.text = ""
        tvPausadasValue.text = ""
    }

    // ---------------- DIBUJAR ZONAS ----------------
    private fun drawZones() {
        // limpiamos overlays y estructuras (se volverán a agregar marcadores y labels)
        mapView.overlays.clear()
        zonePolygons.clear()
        zoneOriginalColors.clear()
        labelMarkers.clear()

        val bbox = mapView.boundingBox
        val north = bbox.latNorth
        val south = bbox.latSouth
        val east = bbox.lonEast
        val west = bbox.lonWest

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

        // CENTRO
        registerZone(0, createRectPolygon(centerNorth, centerEast, centerSouth, centerWest, zoneFill0))
        // NORTE
        registerZone(1, createRectPolygon(north, east, centerNorth, west, zoneFill1))
        // SUR
        registerZone(2, createRectPolygon(centerSouth, east, south, west, zoneFill2))
        // ESTE
        registerZone(3, createRectPolygon(centerNorth, east, centerSouth, centerEast, zoneFill3))
        // OESTE
        registerZone(4, createRectPolygon(centerNorth, centerWest, centerSouth, west, zoneFill4))

        // etiquetas iniciales: SOLO el nombre de la zona (sin números ni leyendas)
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
        markersByZone.getOrPut(index) { mutableListOf() }
        encuestasByZone.getOrPut(index) { mutableListOf() }
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
        polygon.strokeWidth = 2.0f
        return polygon
    }

    private fun addOrUpdateLabel(index: Int, lat: Double, lon: Double, text: String) {
        val existing = labelMarkers[index]
        if (existing != null) {
            existing.position = GeoPoint(lat, lon)
            existing.icon = createTextDrawable(text, 220, 70)
        } else {
            val m = Marker(mapView)
            m.position = GeoPoint(lat, lon)
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            m.icon = createTextDrawable(text, 220, 70)
            m.title = null
            mapView.overlays.add(m)
            labelMarkers[index] = m
        }
    }

    // ---------------- PLOTEAR ENCUESTAS Y CONTAR ----------------

    private suspend fun plotEncuestasAndCount(encuestas: List<Encuesta>) = withContext(Dispatchers.IO) {
        // resetear contadores y listas
        for (i in 0 until 5) {
            zoneTotals[i] = 0
            zoneCompletes[i] = 0
            zonePaused[i] = 0
            markersByZone[i] = mutableListOf()
            encuestasByZone[i] = mutableListOf()
        }

        // aseguramos polígonos dibujados antes en UI thread
        withContext(Dispatchers.Main) { drawZones() }

        for (enc in encuestas) {
            val coords = if (enc.lan != 0.0 && enc.lon != 0.0) {
                Pair(enc.lan, enc.lon)
            } else {
                val dom = enc.domicilio.ifBlank { enc.ciudad.ifBlank { null } }
                if (dom == null) null else cachedOrGeocodeApprox(dom)
            }

            coords?.let { (lat, lon) ->
                val zoneIndex = determineZoneForLatLon(lat, lon)
                if (zoneIndex in 0..4) {
                    zoneTotals[zoneIndex] = zoneTotals[zoneIndex] + 1
                    if (enc.completa) zoneCompletes[zoneIndex] = zoneCompletes[zoneIndex] + 1
                    else zonePaused[zoneIndex] = zonePaused[zoneIndex] + 1

                    // crear marcador con color según estado
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(lat, lon)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = null
                    val color = if (enc.completa) completeColor else pausedColor
                    marker.icon = createCircleDrawable(color, 44)

                    marker.setOnMarkerClickListener { m, mv ->
                        mv.controller.animateTo(m.position)
                        mv.controller.setZoom(16.0)
                        true
                    }

                    markersByZone.getOrPut(zoneIndex) { mutableListOf() }.add(marker)
                    encuestasByZone.getOrPut(zoneIndex) { mutableListOf() }.add(enc)

                    withContext(Dispatchers.Main) {
                        mapView.overlays.add(marker)
                    }
                }
            }
        }

        // NOTA: no cambiamos las etiquetas de los cuadrantes (siguen mostrando solo el NOMBRE)
    }

    private fun onZoneClicked(index: Int) {
        // si vuelven a pulsar la misma zona -> deseleccionar
        if (selectedZoneIndex == index) {
            restoreAllZoneColors()
            selectedZoneIndex = -1
            resetLeyenda()
            return
        }

        // resaltar visualmente
        zonePolygons.forEach { (i, poly) ->
            val orig = zoneOriginalColors[i] ?: 0x30CCCCCC.toInt()
            poly.fillColor = if (i == index) orig else 0x20CCCCCC.toInt()
        }
        selectedZoneIndex = index

        // actualizar tarjeta (sin tocar ViewModel)
        val total = zoneTotals.getOrNull(index) ?: 0
        if (total == 0) {
            tvLeyendaTitulo.text = "No hay encuestas en este cuadrante"
            tvCompletadasValue.text = "-"
            tvPausadasValue.text = "-"
        } else {
            tvLeyendaTitulo.text = "Estado de Encuestas"
            tvCompletadasValue.text = (zoneCompletes.getOrNull(index) ?: 0).toString()
            tvPausadasValue.text = (zonePaused.getOrNull(index) ?: 0).toString()
        }

        mapView.invalidate()
    }

    private fun restoreAllZoneColors() {
        zonePolygons.forEach { (i, poly) ->
            val orig = zoneOriginalColors[i] ?: 0x30CCCCCC.toInt()
            poly.fillColor = orig
        }
        mapView.invalidate()
    }

    private fun zoneName(index: Int): String = when (index) {
        0 -> "CENTRO"
        1 -> "NORTE"
        2 -> "SUR"
        3 -> "ESTE"
        4 -> "OESTE"
        else -> "DESCONOCIDO"
    }

    // ---------------- DRAW HELPERS (BITMAPS) ----------------

    private fun createCircleDrawable(color: Int, size: Int = 44): BitmapDrawable {
        val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
        return BitmapDrawable(resources, bmp)
    }

    private fun createTextDrawable(text: String, width: Int = 220, height: Int = 70): BitmapDrawable {
        val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xCCFFFFFF.toInt() }
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 14f, 14f, bg)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            isFakeBoldText = true
            color = 0xFF000000.toInt()
            textAlign = Paint.Align.CENTER
        }

        val lines = text.split("\n")
        val fm = paint.fontMetrics
        val lineHeight = (fm.bottom - fm.top)
        val totalTextHeight = lineHeight * lines.size
        var y = (height - totalTextHeight) / 2f - fm.top

        for (line in lines) {
            canvas.drawText(line, width / 2f, y, paint)
            y += lineHeight
        }

        return BitmapDrawable(resources, bmp)
    }

    // ---------------- GEOCODING (cacheado) ----------------

    private suspend fun cachedOrGeocodeApprox(dom: String): Pair<Double, Double>? {
        geocodeCache[dom]?.let { return it }

        val separators = listOf(" y ", " Y ", "&", " and ")
        for (sep in separators) {
            if (dom.contains(sep)) {
                val parts = dom.split(sep).map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size >= 2) {
                    val s1 = parts[0]
                    val s2 = parts[1]
                    val res = geocodeIntersection(s1, s2, cityName)
                    if (res != null) {
                        geocodeCache[dom] = res
                        return res
                    }
                }
            }
        }

        val q = "$dom, $cityName"
        val res = geocode(q)
        if (res != null) geocodeCache[dom] = res
        return res
    }

    private suspend fun geocodeIntersection(st1: String, st2: String, city: String?): Pair<Double, Double>? {
        val c1 = "$st1 y $st2${if (!city.isNullOrBlank()) ", $city" else ""}"
        val c2 = "$st1 & $st2${if (!city.isNullOrBlank()) ", $city" else ""}"
        return geocode(c1) ?: geocode(c2)
    }

    private suspend fun geocode(query: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "TntTpFinal/1.0 (tu_email@dominio.com)")
                .header("Accept-Language", Locale.getDefault().language)
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val arr = JSONArray(body)
                if (arr.length() == 0) return@withContext null
                val item = arr.getJSONObject(0)
                val lat = item.optDouble("lat")
                val lon = item.optDouble("lon")
                return@withContext Pair(lat, lon)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // ---------------- DETERMINAR ZONA ----------------

    private fun determineZoneForLatLon(lat: Double, lon: Double): Int {
        val bbox = mapView.boundingBox
        val north = bbox.latNorth
        val south = bbox.latSouth
        val east = bbox.lonEast
        val west = bbox.lonWest

        if (lat > north || lat < south || lon > east || lon < west) return -1

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

        return when {
            lat >= centerSouth && lat <= centerNorth && lon >= centerWest && lon <= centerEast -> 0
            lat > centerNorth -> 1
            lat < centerSouth -> 2
            lon > centerEast -> 3
            lon < centerWest -> 4
            else -> -1
        }
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
