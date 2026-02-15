package com.example.trabajofinal2024

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

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
    private var currentList: List<AlimentoDAO.DailySurveyStats> = emptyList()




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vínculos con la UI
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



        // Navegar atrás con Navigation Component
        btnBack.setOnClickListener {
            // vuelve al fragment anterior en el nav graph (encuestas list en tu diseño)
            findNavController().popBackStack()
        }

        val uid = currentUserUid
        if (uid == null) {
            tvNumEncuestas.text = "Encuestas completadas: 0 (logueate)"
            tvKcal.text = "Kcal promedio: —"
            tvCarbs.text = "Carbohidratos promedio: —"
            tvProteinas.text = "Proteínas promedio: —"
            tvColesterol.text = "Colesterol promedio: —"
            tvFibra.text = "Fibras promedio: —"
            tvGrasas.text = "Grasas totales promedio: —"
            tvGramosTotales.text = "Gramos totales promedio: —"
            tvAlcohol.text = "Alcohol promedio: —"
            return
        }


        statsViewModel.getAveragesForUser(uid).observe(viewLifecycleOwner) { stats ->
            if (stats == null) {
                tvKcal.text = "Kcal promedio: 0"
                tvCarbs.text = "Carbohidratos promedio: 0"
                tvProteinas.text = "Proteínas promedio: 0"
                tvColesterol.text = "Colesterol promedio: 0"
                tvFibra.text = "Fibras promedio: 0"
                tvGrasas.text = "Grasas totales promedio: 0"
                tvGramosTotales.text = "Gramos totales promedio: 0"
                tvAlcohol.text = "Alcohol promedio: 0"

            } else {

                tvKcal.text = "Kcal promedio: ${stats.avg_kcal?.let { String.format("%.0f", it) } ?: "0"}"
                tvCarbs.text = "Carbohidratos promedio: ${stats.avg_carbohidratos?.let { String.format("%.1f", it) } ?: "0"} g"
                tvProteinas.text = "Proteínas promedio: ${stats.avg_proteinas?.let { String.format("%.1f", it) } ?: "0"} g"
                tvColesterol.text = "Colesterol promedio: ${stats.avg_colesterol?.let { String.format("%.1f", it) } ?: "0"} mg"
                tvFibra.text = "Fibras promedio: ${stats.avg_fibra?.let { String.format("%.1f", it) } ?: "0"} g"
                tvGrasas.text = "Grasas totales promedio: ${stats.avg_grasas?.let { String.format("%.1f", it) } ?: "0"} g"
                tvGramosTotales.text = "Gramos totales promedio: ${stats.avg_gramos?.let { String.format("%.1f", it) } ?: "0"} g"
                tvAlcohol.text = "Alcohol promedio: ${stats.avg_alcohol?.let { String.format("%.1f", it) } ?: "0"} g"
            }
        }

        statsViewModel.getDailyTotals(uid)
            .observe(viewLifecycleOwner) { dailyList ->

                if (dailyList.isNullOrEmpty()) {
                    tvNumEncuestas.text = "Encuestas completadas: 0"
                    return@observe
                }

                currentList = dailyList ?: emptyList()

                tvNumEncuestas.text = "Encuestas completadas: ${currentList.size}"


                configurarGrafico(
                    currentList,
                    { it.total_proteinas },
                    "Proteínas (g)",
                    150f,
                    Color.parseColor("#1E88E5"),
                    50f,
                    120f
                )

            }

        chipGroupNutrientes.setOnCheckedStateChangeListener { _, checkedIds ->

            when (checkedIds.firstOrNull()) {

                R.id.chipProteinas -> {
                    configurarGrafico(
                        currentList,
                        { it.total_proteinas },
                        "Proteínas (g)",
                        150f,
                        Color.parseColor("#1E88E5"), // azul
                        50f,
                        120f
                    )
                }

                R.id.chipCarbs -> {
                    configurarGrafico(
                        currentList,
                        { it.total_carbohidratos },
                        "Carbohidratos (g)",
                        400f,
                        Color.parseColor("#43A047"), // verde
                        200f,
                        350f
                    )
                }

                R.id.chipFibra -> {
                    configurarGrafico(
                        currentList,
                        { it.total_fibra },
                        "Fibra (g)",
                        60f,
                        Color.parseColor("#FB8C00"), // naranja
                        25f,
                        38f
                    )
                }

                R.id.chipKcal -> {
                    configurarGrafico(
                        currentList,
                        { it.total_kcal },
                        "Kcal",
                        3000f,
                        Color.parseColor("#8E24AA"), // violeta
                        1800f,
                        2500f
                    )
                }

                R.id.chipColesterol -> {
                    configurarGrafico(
                        currentList,
                        { it.total_colesterol },
                        "Colesterol (mg)",
                        400f,
                        Color.parseColor("#E53935"), // rojo
                        0f,
                        300f
                    )
                }

                R.id.chipAlcohol -> {
                    configurarGrafico(
                        currentList,
                        { it.total_alcohol },
                        "Alcohol (g)",
                        100f,
                        Color.parseColor("#6D4C41"), // marrón
                        0f,
                        20f
                    )
                }

                R.id.chipGrasas -> {
                    configurarGrafico(
                        currentList,
                        { it.total_grasas },
                        "Grasas totales (g)",
                        100f,
                        Color.parseColor("#FDD835"), // Mostaza
                        0f,
                        20f
                    )
                }

                R.id.chipGramosTotales -> {
                    configurarGrafico(
                        currentList,
                        { it.total_gramos },
                        "Gramos totales (g)",
                        100f,
                        Color.parseColor("#00ACC1"), // Turquesa
                        0f,
                        20f
                    )
                }
            }
        }

    }

    private fun configurarGrafico(
        dailyList: List<AlimentoDAO.DailySurveyStats>,
        valorSelector: (AlimentoDAO.DailySurveyStats) -> Double?,
        label: String,
        yMax: Float,
        color: Int,
        rangoMin: Float?,
        rangoMax: Float?
    ) {

        val entries = dailyList.mapIndexed { index, stat ->
            Entry(
                (index + 1).toFloat(),
                (valorSelector(stat) ?: 0.0).toFloat()
            )
        }

        val dataSet = ScatterDataSet(entries, label)
        dataSet.scatterShapeSize = 14f
        dataSet.color = color

        val scatterData = ScatterData(dataSet)
        scatterChart.data = scatterData

        // --- EJE Y ---
        val leftAxis = scatterChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = yMax
        leftAxis.removeAllLimitLines()




        // INTERACCIONES
        scatterChart.setTouchEnabled(true)
        scatterChart.isDragEnabled = true
        scatterChart.setScaleEnabled(true)
        scatterChart.setPinchZoom(true)
        scatterChart.isScaleXEnabled = true
        scatterChart.isScaleYEnabled = true
        scatterChart.setVisibleXRangeMaximum(5f)

        // Mover la vista al inicio
        scatterChart.moveViewToX(1f)

        scatterChart.setExtraOffsets(10f, 10f, 10f, 20f)
        scatterChart.isHighlightPerDragEnabled = true

        // EJE X
        val xAxis = scatterChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = 1f
        xAxis.axisMaximum = dailyList.size.toFloat()
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.roundToInt().toString()
            }
        }


        // Marcador
        val marker = MarcadorView(requireContext())
        marker.chartView = scatterChart
        scatterChart.marker = marker


        // 🔵 Rango mínimo ideal
        rangoMin?.let {
            val minLine = LimitLine(it, "Mín ideal")
            minLine.lineWidth = 2f
            minLine.lineColor = Color.GRAY
            leftAxis.addLimitLine(minLine)
        }

        // 🔵 Rango máximo ideal
        rangoMax?.let {
            val maxLine = LimitLine(it, "Máx ideal")
            maxLine.lineWidth = 2f
            maxLine.lineColor = Color.GRAY
            leftAxis.addLimitLine(maxLine)
        }

        scatterChart.axisRight.isEnabled = false

        // Animación suave
        scatterChart.animateY(600)

        scatterChart.description.text = label
        scatterChart.invalidate()
    }




}
