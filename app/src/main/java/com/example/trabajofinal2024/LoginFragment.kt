package com.example.trabajofinal2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavHost
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño para este fragmento
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)

        // Configurar los bordes del sistema
        enableEdgeToEdge(rootView)

        // Encontrar y configurar el botón de ingreso
        val ingresarDatos = rootView.findViewById<Button>(R.id.ingresarid)
        ingresarDatos.setOnClickListener {
            val editusuario = rootView.findViewById<EditText>(R.id.usuariotext)
            val editpassword = rootView.findViewById<EditText>(R.id.contraseñaeditText)
            val usuario = editusuario.text.toString()
            val password = editpassword.text.toString()
            if (usuario == "admin" && password == "tnt2024") {
                val bundle = Bundle().apply {
                    putString("userExtra", usuario)
                }
                findNavController().navigate(R.id.action_loginFragment_to_welcomeLogin,bundle)
            } else {
                mostrarMensajeError(rootView, "El usuario y/o contraseña no son correctos")
            }
        }
        return rootView
    }

    private fun enableEdgeToEdge(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mostrarMensajeError(view: View, mensaje: String) {
        val snack = Snackbar.make(view, mensaje, Snackbar.LENGTH_SHORT)
        snack.show()
    }
}