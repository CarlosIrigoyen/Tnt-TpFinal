package com.example.trabajofinal2024

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlin.math.roundToInt

class MarcadorView(context: Context) :
    MarkerView(context, R.layout.marcador) {

    private val tvMarker: TextView = findViewById(R.id.tvMarker)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        if (highlight != null && e != null) {

            val encuestaNumero = highlight.x.roundToInt()

            tvMarker.text = "Encuesta $encuestaNumero\n${e.y}"
        }

        super.refreshContent(e, highlight)
    }


    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}
