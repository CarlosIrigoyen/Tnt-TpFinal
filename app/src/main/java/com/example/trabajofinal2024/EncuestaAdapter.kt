package com.example.trabajofinal2024

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class EncuestaAdapter(
    private val onResumeClick: (Encuesta) -> Unit,
    private val onAbandonClick: (Encuesta) -> Unit,
    private val onReanudarClick: (Encuesta) -> Unit  // Nueva función para reanudar
) : ListAdapter<Encuesta, EncuestaAdapter.EncuestaVH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Encuesta>() {
            override fun areItemsTheSame(oldItem: Encuesta, newItem: Encuesta): Boolean =
                oldItem.firestoreId == newItem.firestoreId

            override fun areContentsTheSame(oldItem: Encuesta, newItem: Encuesta): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EncuestaVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_encuesta, parent, false)
        return EncuestaVH(view)
    }

    override fun onBindViewHolder(holder: EncuestaVH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class EncuestaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvEncuestaTitle)
        private val tvSub: TextView = itemView.findViewById(R.id.tvEncuestaSub)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        private val tvProgreso: TextView = itemView.findViewById(R.id.tvProgreso)
        private val btnReanudar: Button = itemView.findViewById(R.id.btnReanudar)
        private val btnAbandonar: Button = itemView.findViewById(R.id.btnAbandonar)
        private val btnVer: Button = itemView.findViewById(R.id.btnVer)

        fun bind(encuesta: Encuesta) {
            val totalAlimentos = FoodCatalog.ALL.size
            val progreso = encuesta.currentIndex.coerceAtMost(totalAlimentos)
            val porcentaje = if (totalAlimentos > 0) (progreso * 100 / totalAlimentos) else 0

            tvTitle.text = "Encuesta #${encuesta.firestoreId}"
            tvSub.text = "${encuesta.domicilio} — ${encuesta.ciudad}"

            // Mostrar estado
            val estado = when {
                encuesta.completa -> "COMPLETADA"
                !encuesta.activa -> "ABANDONADA"
                else -> "EN PROGRESO"
            }
            tvEstado.text = "Estado: $estado"
            tvProgreso.text = "Progreso: $progreso/$totalAlimentos alimentos ($porcentaje%)"

            // Configurar visibilidad de botones según estado
            if (encuesta.completa) {
                // Encuesta completada
                btnReanudar.visibility = View.GONE
                btnAbandonar.visibility = View.GONE
                btnVer.visibility = View.VISIBLE
                btnVer.text = "Ver Detalles"
                btnVer.setOnClickListener { onResumeClick(encuesta) }
            } else if (!encuesta.activa) {
                // Encuesta abandonada
                btnReanudar.visibility = View.VISIBLE
                btnReanudar.text = "Reanudar"
                btnAbandonar.visibility = View.GONE
                btnVer.visibility = View.GONE

                btnReanudar.setOnClickListener { onReanudarClick(encuesta) }
            } else {
                // Encuesta activa
                btnReanudar.visibility = View.VISIBLE
                btnReanudar.text = "Continuar"
                btnAbandonar.visibility = View.VISIBLE
                btnVer.visibility = View.GONE

                btnReanudar.setOnClickListener { onResumeClick(encuesta) }
                btnAbandonar.setOnClickListener { onAbandonClick(encuesta) }
            }
        }
    }
}