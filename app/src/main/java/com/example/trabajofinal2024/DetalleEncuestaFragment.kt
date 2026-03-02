package com.example.trabajofinal2024

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DetalleEncuestaFragment : Fragment(R.layout.fragment_detalle_encuesta) {

    private val repositorio: RepositorioEncuestas
        get() = (requireActivity().application as App).encuestaRepositorio

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val encuestaId = arguments?.getInt("encuestaid", 0) ?: 0

        // TextViews totales
        val tvSub: TextView = view.findViewById(R.id.tvSubtitulo)
        val tvKcalTotal: TextView = view.findViewById(R.id.tvKcalTotal)
        val tvCarboTotal: TextView = view.findViewById(R.id.tvCarboTotal)
        val tvProteTotal: TextView = view.findViewById(R.id.tvProteTotal)
        val tvColesterolTotal: TextView = view.findViewById(R.id.tvColesterolTotal)
        val tvFibraTotal: TextView = view.findViewById(R.id.tvFibraTotal)
        val tvGrasasTotal: TextView = view.findViewById(R.id.tvGrasasTotal)
        val tvAlcoholTotal: TextView = view.findViewById(R.id.tvAlcoholTotal)
        val tvGramosTotal: TextView = view.findViewById(R.id.tvGramosTotal)

        // TextViews promedios
        val tvKcalProm: TextView = view.findViewById(R.id.tvKcalPromedio)
        val tvCarboProm: TextView = view.findViewById(R.id.tvCarboPromedio)
        val tvProteProm: TextView = view.findViewById(R.id.tvProtePromedio)
        val tvGrasasProm: TextView = view.findViewById(R.id.tvGrasasPromedio)
        val tvColesterolProm: TextView = view.findViewById(R.id.tvColesterolPromedio)
        val tvFibraProm: TextView = view.findViewById(R.id.tvFibraPromedio)

        val btnVolver: Button = view.findViewById(R.id.btnVolver)

        tvSub.text = "Encuesta #$encuestaId"

        // Cargar datos y calcular totales/promedios en coroutine
        lifecycleScope.launch {
            try {
                val alimentos = repositorio.obtenerAlimentosPorEncuesta(encuestaId)

                var totalKcal = 0.0
                var totalCarbo = 0.0
                var totalProte = 0.0
                var totalColesterol = 0.0
                var totalFibra = 0.0
                var totalGrasas = 0.0
                var totalAlcohol = 0.0
                var totalGramos = 0.0

                alimentos.forEach { a ->
                    totalKcal += a.kcal.toDouble()
                    totalCarbo += a.carbohidratos.toDouble()
                    totalProte += a.proteinas.toDouble()
                    totalColesterol += a.colesterol.toDouble()
                    totalFibra += a.fibra.toDouble()
                    totalGrasas += a.grasas.toDouble()
                    totalAlcohol += a.alcohol.toDouble()
                    totalGramos += a.gramos.toDouble()
                }

                val n = alimentos.size.coerceAtLeast(1)
                fun dbl(v: Double) = ((v * 10.0).roundToInt() / 10.0)

                // Totales
                tvKcalTotal.text = "Kcal total: ${dbl(totalKcal)}"
                tvCarboTotal.text = "Carbohidratos total: ${dbl(totalCarbo)} g"
                tvProteTotal.text = "Proteínas total: ${dbl(totalProte)} g"
                tvColesterolTotal.text = "Colesterol total: ${dbl(totalColesterol)} mg"
                tvFibraTotal.text = "Fibras total: ${dbl(totalFibra)} g"
                tvGrasasTotal.text = "Grasas totales: ${dbl(totalGrasas)} g"
                tvAlcoholTotal.text = "Alcohol total: ${dbl(totalAlcohol)} g"
                tvGramosTotal.text = "Gramos totales: ${dbl(totalGramos)} g"

                // Promedios por alimento
                tvKcalProm.text = "Kcal promedio por alimento: ${dbl(totalKcal / n)}"
                tvCarboProm.text = "Carbohidratos promedio por alimento: ${dbl(totalCarbo / n)} g"
                tvProteProm.text = "Proteínas promedio por alimento: ${dbl(totalProte / n)} g"
                tvGrasasProm.text = "Grasas promedio por alimento: ${dbl(totalGrasas / n)} g"
                tvColesterolProm.text = "Colesterol promedio por alimento: ${dbl(totalColesterol / n)} mg"
                tvFibraProm.text = "Fibra promedio por alimento: ${dbl(totalFibra / n)} g"

            } catch (e: Exception) {
                tvKcalTotal.text = "Error cargando datos: ${e.localizedMessage ?: e.message}"
            }
        }

        // Botón volver: vuelve a la pantalla anterior del NavController
        btnVolver.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}