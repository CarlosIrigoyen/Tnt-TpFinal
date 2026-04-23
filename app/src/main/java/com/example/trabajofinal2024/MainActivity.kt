package com.example.trabajofinal2024

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var navController: androidx.navigation.NavController



    override fun onCreate(savedInstanceState: Bundle?) {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)



        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        // Configurar el toggle manualmente (esto hace que el ícono abra el drawer)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,   // puedes crear estos strings
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Escuchar cambios de destino para actualizar título y bloquear drawer en login
        navController.addOnDestinationChangedListener { _, destination, _ ->

            val user = FirebaseAuth.getInstance().currentUser

            if (user == null && destination.id != R.id.loginFragment) {
                navController.navigate(R.id.loginFragment)
            }
            when (destination.id) {
                R.id.loginFragment -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    title = "Iniciar Sesión"
                }
                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    title = when (destination.id) {
                        R.id.encuestasListFragment -> "Encuestas"
                        R.id.mapaFragment -> "Mapa"
                        R.id.statsFragment -> "Estadísticas"
                        R.id.encuestaFragment -> "Nueva Encuesta"
                        R.id.foodFragment -> "Registro de Alimentos"
                        R.id.detalleEncuestaFragment -> "Detalle de Encuesta"
                        else -> "Encuestas"
                    }
                }
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_encuestas -> {
                    navController.navigate(R.id.encuestasListFragment, null, NavOptions.Builder()
                        .setPopUpTo(R.id.encuestasListFragment, true)
                        .build())
                }
                R.id.nav_mapa -> {
                    navController.navigate(R.id.mapaFragment)
                }
                R.id.nav_estadisticas -> {
                    navController.navigate(R.id.statsFragment)
                }
                R.id.nav_cerrar_sesion -> {
                    FirebaseAuth.getInstance().signOut()
                    googleSignInClient.signOut().addOnCompleteListener {
                        navController.navigate(
                            R.id.loginFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.loginFragment, true)
                                .build()
                        )
                    }
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            navController.navigate(
                R.id.encuestasListFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()
            )
        }
    }

    // Este método es llamado cuando se presiona el ícono de la barra
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Si el toggle maneja el evento (abrir/cerrar drawer), lo usamos
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}