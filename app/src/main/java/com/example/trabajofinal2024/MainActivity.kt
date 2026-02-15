package com.example.trabajofinal2024

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // edge-to-edge (mantenemos tu llamada)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // aplicar paddings de system bars a la root (tu código original)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ------------------------------
        // Elegir startDestination según sesión (compatible con navigation 2.7.7+)
        // ------------------------------
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        if (navHostFragment == null) {
            // Por seguridad: si no encuentra el NavHost, abortar sin crash.
            return
        }

        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.main_navigation)

        // Comprobar sesión Firebase
        val userLogged = FirebaseAuth.getInstance().currentUser != null

        // setStartDestination (método compatible)
        if (userLogged) {
            navGraph.setStartDestination(R.id.encuestasListFragment)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }

        navController.graph = navGraph
    }
}
