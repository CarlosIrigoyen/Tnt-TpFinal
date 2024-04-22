package com.example.tnt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tnt.R

class EncuestasAdapter(private val itemCount: Int) : RecyclerView.Adapter<EncuestasAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_encuesta_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position + 1)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val encuestaNumber: TextView = itemView.findViewById(R.id.encuestaNumber)
        private val encuestaIcon: ImageView = itemView.findViewById(R.id.encuestaIcon)

        fun bind(number: Int) {
            // Concatenar "Encuesta" con el número y establecerlo en el TextView
            encuestaNumber.text = "Encuesta $number"
            // Establecer un tamaño específico al icono
            encuestaIcon.layoutParams.width = 48 // Ancho deseado en píxeles
            encuestaIcon.layoutParams.height = 48 // Altura deseada en píxeles
            encuestaIcon.requestLayout() // Actualizar el diseño
            // Aquí puedes establecer el icono relacionado a encuestas
            encuestaIcon.setImageResource(R.drawable.ic_encuesta)
        }
    }
}