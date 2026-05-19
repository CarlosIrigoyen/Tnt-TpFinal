package com.example.trabajofinal2024

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt
import kotlin.math.max
import com.example.trabajofinal2024.setStatText

class StatsFragment : Fragment(R.layout.fragment_stats) {

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val statsViewModel: StatsViewModel by viewModels {
        StatsViewModel.Factory(
            (requireActivity().application as App).alimentoRepositorio
        )
    }

    private lateinit var tvNumEncuestas: TextView
    private lateinit var tvKcal: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvProteinas: TextView
    private lateinit var tvColesterol: TextView
    private lateinit var tvFibra: TextView
    private lateinit var tvGrasas: TextView
    private lateinit var tvGramosTotales: TextView
    private lateinit var tvAlcohol: TextView
    private lateinit var btnBack: Button
    private lateinit var scatterChart: ScatterChart
    private lateinit var chipGroupNutrientes: ChipGroup

    private lateinit var progressBar: ProgressBar
    private lateinit var contenidoStats: LinearLayout


    private var currentList: List<AlimentoDAO.DailySurveyStats> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        contenidoStats = view.findViewById(R.id.stats_container)

        tvNumEncuestas = view.findViewById(R.id.tvNumEncuestas)
        tvKcal = view.findViewById(R.id.tvAvgKcal)
        tvCarbs = view.findViewById(R.id.tvAvgCarbs)
        tvProteinas = view.findViewById(R.id.tvAvgProteinas)
        tvColesterol = view.findViewById(R.id.tvAvgColesterol)
        tvFibra = view.findViewById(R.id.tvAvgFibra)
        tvGrasas = view.findViewById(R.id.tvAvgGrasas)
        tvGramosTotales = view.findViewById(R.id.tvAvgGramos)
        tvAlcohol = view.findViewById(R.id.tvAvgAlcohol)
        btnBack = view.findViewById(R.id.backButton)
        scatterChart = view.findViewById(R.id.scatterChartProteinas)
        chipGroupNutrientes = view.findViewById(R.id.chipGroupNutrientes)

        try {
            scatterChart.isNestedScrollingEnabled = true
        } catch (_: Throwable) {
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        statsViewModel.getAveragesForUserFirebase(uid).observe(viewLifecycleOwner) { stats ->
            if (stats == null) {
                tvKcal.setStatText(tvKcal.text.toString(), "0", "kcal")
                tvCarbs.setStatText(tvCarbs.text.toString(), "0", "g")
                tvProteinas.setStatText(tvProteinas.text.toString(), "0", "g")
                tvColesterol.setStatText(tvColesterol.text.toString(), "0", "mg")
                tvFibra.setStatText(tvFibra.text.toString(), "0", "g")
                tvGrasas.setStatText(tvGrasas.text.toString(), "0", "g")
                tvGramosTotales.setStatText(tvGramosTotales.text.toString(), "0", "g")
                tvAlcohol.setStatText(tvAlcohol.text.toString(), "0", "g")
            } else {
                tvKcal.setStatText(
                    tvKcal.text.toString(),
                    stats.avg_kcal?.let { String.format("%.0f", it) } ?: "0",
                    "kcal")
                tvCarbs.setStatText(
                    tvCarbs.text.toString(),
                    stats.avg_carbohidratos?.let { String.format("%.1f", it) } ?: "0",
                    "g")
                tvProteinas.setStatText(
                    tvProteinas.text.toString(),
                    stats.avg_proteinas?.let { String.format("%.1f", it) } ?: "0",
                    "g")
                tvColesterol.setStatText(
                    tvColesterol.text.toString(),
                    stats.avg_colesterol?.let { String.format("%.1f", it) } ?: "0",
                    "mg")
                tvFibra.setStatText(
                    tvFibra.text.toString(),
                    stats.avg_fibra?.let { String.format("%.1f", it) } ?: "0",
                    "g")
                tvGrasas.setStatText(
                    tvGrasas.text.toString(),
                    stats.avg_grasas?.let { String.format("%.1f", it) } ?: "0",
                    "g")
                tvGramosTotales.setStatText(
                    tvGramosTotales.text.toString(),
                    stats.avg_gramos?.let { String.format("%.1f", it) } ?: "0",
                    "g")
                tvAlcohol.setStatText(
                    tvAlcohol.text.toString(),
                    stats.avg_alcohol?.let { String.format("%.1f", it) } ?: "0",
                    "g")
            }
        }

        statsViewModel.getDailyTotalsByUserFirebase(uid).observe(viewLifecycleOwner) { dailyList ->

            progressBar.visibility = View.GONE
            contenidoStats.visibility = View.VISIBLE

            if (dailyList.isNullOrEmpty()) {
                tvNumEncuestas.text = "Encuestas completadas: 0"
                scatterChart.clear()
                scatterChart.invalidate()
                currentList = emptyList()
                return@observe
            }

            currentList = dailyList
            tvNumEncuestas.text = "Encuestas completadas: ${currentList.size}"

        }

        configurarGrafico(
                currentList,
                { it.total_proteinas },
                "Proteínas (g)",
                150f,
                Color.parseColor("#1E88E5"),
                50f,
                120f
        )



        chipGroupNutrientes.setOnCheckedStateChangeListener { _, checkedIds ->

            if (currentList.isEmpty()) return@setOnCheckedStateChangeListener

            when (checkedIds.firstOrNull()) {

                R.id.chipProteinas ->
                    configurarGrafico(currentList, { it.total_proteinas }, "Proteínas (g)", 150f,
                        Color.parseColor("#1E88E5"), 50f, 120f)

                R.id.chipCarbs ->
                    configurarGrafico(currentList, { it.total_carbohidratos }, "Carbohidratos (g)", 400f,
                        Color.parseColor("#43A047"), 200f, 350f)

                R.id.chipFibra ->
                    configurarGrafico(currentList, { it.total_fibra }, "Fibra (g)", 60f,
                        Color.parseColor("#FB8C00"), 25f, 38f)

                R.id.chipKcal ->
                    configurarGrafico(currentList, { it.total_kcal }, "Kcal", 3000f,
                        Color.parseColor("#8E24AA"), 1800f, 2500f)

                R.id.chipColesterol ->
                    configurarGrafico(currentList, { it.total_colesterol }, "Colesterol (mg)", 400f,
                        Color.parseColor("#E53935"), 0f, 300f)

                R.id.chipAlcohol ->
                    configurarGrafico(currentList, { it.total_alcohol }, "Alcohol (g)", 100f,
                        Color.parseColor("#6D4C41"), 0f, 20f)

                R.id.chipGrasas ->
                    configurarGrafico(currentList, { it.total_grasas }, "Grasas totales (g)", 100f,
                        Color.parseColor("#FDD835"), 0f, 20f)

                R.id.chipGramosTotales ->
                    configurarGrafico(currentList, { it.total_gramos }, "Gramos totales (g)", 100f,
                        Color.parseColor("#00ACC1"), 0f, 20f)
            }
        }
    }



    private fun configurarGrafico(
        dailyList: List<AlimentoDAO.DailySurveyStats>,
        valorSelector: (AlimentoDAO.DailySurveyStats) -> Double?,
        label: String,
        yMaxSugerido: Float,
        color: Int,
        rangoMin: Float?,
        rangoMax: Float?
    ) {

        val entries = dailyList.mapIndexed { index, stat ->
            Entry((index + 1).toFloat(), (valorSelector(stat) ?: 0.0).toFloat())
        }

        val dataSet = ScatterDataSet(entries, label).apply {
            scatterShapeSize = 14f
            setColor(color)
            setDrawValues(true)
            valueTextSize = 10f
        }

        scatterChart.data = ScatterData(dataSet)

        val leftAxis = scatterChart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.axisMinimum = 0f

        val maxReal = entries.maxOfOrNull { it.y } ?: 0f

        val axisMaxFinal = if (maxReal <= 0f) {
            yMaxSugerido
        } else {
            max(yMaxSugerido, maxReal * 1.10f)
        }

        leftAxis.axisMaximum = axisMaxFinal
        leftAxis.spaceTop = 12f

        rangoMin?.let {
            val minLine = LimitLine(it, "Mín ideal")
            minLine.lineWidth = 2f
            minLine.lineColor = Color.GRAY
            leftAxis.addLimitLine(minLine)
        }

        rangoMax?.let {
            val maxLine = LimitLine(it, "Máx ideal")
            maxLine.lineWidth = 2f
            maxLine.lineColor = Color.GRAY
            leftAxis.addLimitLine(maxLine)
        }

        scatterChart.axisRight.isEnabled = false

        val xAxis = scatterChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = 1f
        xAxis.axisMaximum = dailyList.size.toFloat()

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.roundToInt().toString()
            }
        }

        try {
            val marker = MarcadorView(requireContext())
            marker.chartView = scatterChart
            scatterChart.marker = marker
        } catch (_: Throwable) { }

        scatterChart.setTouchEnabled(true)
        scatterChart.isDragEnabled = true
        scatterChart.setScaleEnabled(true)
        scatterChart.setPinchZoom(true)
        scatterChart.isScaleXEnabled = true
        scatterChart.isScaleYEnabled = true

        scatterChart.setVisibleXRangeMaximum(5f)
        scatterChart.moveViewToX(1f)

        scatterChart.setExtraOffsets(10f, 10f, 10f, 20f)
        scatterChart.isHighlightPerDragEnabled = true

        scatterChart.description.text = label
        scatterChart.invalidate()
        scatterChart.animateY(600)
    }
}