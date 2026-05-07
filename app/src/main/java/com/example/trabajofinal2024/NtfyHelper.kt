package com.example.trabajofinal2024

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.text.SimpleDateFormat
import java.util.*

object NtfyHelper {

    fun enviarNotificacion(
        context: Context,
        uid: String,
        nombreVoluntario: String,
        titulo: String,
        mensaje: String,
        esAsignacion: Boolean = true
    ) {
        val topic = TopicHelper.generarTopic(uid, nombreVoluntario)
        Log.d("Ntfy", "Enviando a topic: $topic")
        val url = "https://ntfy.sh/$topic"
        val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val saludo = if (esAsignacion) "🎉 ¡Hola $nombreVoluntario!" else "😟 Hola $nombreVoluntario"

        val mensajePlano = """
            $saludo
            
            $mensaje
            
            📅 Enviado: $fechaActual
            
            ---
            Sistema de Gestión de Turnos
        """.trimIndent()

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response -> Log.d("Ntfy", "Notificación enviada: $response") },
            { error -> Log.e("Ntfy", "Error: ${error.message}") }
        ) {
            override fun getBody(): ByteArray = mensajePlano.toByteArray(Charsets.UTF_8)
            override fun getBodyContentType(): String = "text/plain; charset=utf-8"
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Title"] = titulo
                headers["Priority"] = "4"
                headers["Tags"] = if (esAsignacion) "white_check_mark" else "x"
                return headers
            }
        }
        Volley.newRequestQueue(context).add(request)
    }
}