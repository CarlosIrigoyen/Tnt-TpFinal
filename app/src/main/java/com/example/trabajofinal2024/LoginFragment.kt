package com.example.trabajofinal2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {

            val rootView = inflater.inflate(R.layout.fragment_login, container, false)

            // Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Views (los IDs vienen de TU XML)
            emailEditText = rootView.findViewById(R.id.usuariotext)
            passwordEditText = rootView.findViewById(R.id.contraseñaeditText)
            loginButton = rootView.findViewById(R.id.ingresarid)

            loginButton.setOnClickListener {
                loginUser()
            }

            return rootView
        }

        private fun loginUser() {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Ingrese usuario y contraseña", Toast.LENGTH_LONG).show()
                return
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_LONG).show()

                        // Navegar con Navigation Component
                        findNavController()
                            .navigate(R.id.action_loginFragment_to_encuestasListFragment)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
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
