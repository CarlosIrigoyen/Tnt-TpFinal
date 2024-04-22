package com.example.tnt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Obtener las referencias de los EditText y del botón
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)

        // Agregar un listener al botón de inicio de sesión
        buttonLogin.setOnClickListener {
            // Obtener el texto ingresado por el usuario en los EditText
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            // Verificar si el nombre de usuario y la contraseña son correctos
            if (username == "admin" && password == "tnt2024") {

                // Crear un Intent para iniciar la nueva actividad (ListadoActivity)
                val intent = Intent(this, ListadoActivity::class.java)
                startActivity(intent) // Iniciar la nueva actividad
            } else {
                // Si no son correctos, mostrar un mensaje de error
                Toast.makeText(this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
