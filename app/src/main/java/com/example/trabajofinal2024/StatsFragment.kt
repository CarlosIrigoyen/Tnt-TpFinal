package com.example.trabajofinal2024

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

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
    private lateinit var btnBack: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vínculos con la UI
        tvNumEncuestas = view.findViewById(R.id.tvNumEncuestas)
        tvKcal = view.findViewById(R.id.tvAvgKcal)
        tvCarbs = view.findViewById(R.id.tvAvgCarbs)
        tvProteinas = view.findViewById(R.id.tvAvgProteinas)
        tvColesterol = view.findViewById(R.id.tvAvgColesterol)
        tvFibra = view.findViewById(R.id.tvAvgFibra)
        btnBack = view.findViewById(R.id.backButton)

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
            return
        }

        statsViewModel.getAveragesForUser(uid).observe(viewLifecycleOwner) { stats ->
            if (stats == null) {
                tvNumEncuestas.text = "Encuestas completadas: 0"
                tvKcal.text = "Kcal promedio: 0"
                tvCarbs.text = "Carbohidratos promedio: 0"
                tvProteinas.text = "Proteínas promedio: 0"
                tvColesterol.text = "Colesterol promedio: 0"
                tvFibra.text = "Fibras promedio: 0"
            } else {
                tvNumEncuestas.text = "Encuestas completadas: ✔"

                tvKcal.text = "Kcal promedio: ${stats.avg_kcal?.let { String.format("%.0f", it) } ?: "0"}"
                tvCarbs.text = "Carbohidratos promedio: ${stats.avg_carbohidratos?.let { String.format("%.1f", it) } ?: "0"} g"
                tvProteinas.text = "Proteínas promedio: ${stats.avg_proteinas?.let { String.format("%.1f", it) } ?: "0"} g"
                tvColesterol.text = "Colesterol promedio: ${stats.avg_colesterol?.let { String.format("%.1f", it) } ?: "0"} mg"
                tvFibra.text = "Fibras promedio: ${stats.avg_fibra?.let { String.format("%.1f", it) } ?: "0"} g"
            }
        }
    }
}
