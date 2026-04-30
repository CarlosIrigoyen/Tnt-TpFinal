package com.example.trabajofinal2024

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
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
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupHeader()

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
                        R.id.turnosAdminFragment -> " Turnos "
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
                R.id.nav_turnos -> {
                    navController.navigate(R.id.turnosAdminFragment)
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

    private fun setupHeader() {
        val headerView = navView.getHeaderView(0)
        val tvInitial = headerView.findViewById<TextView>(R.id.tvInitial)
        val tvName = headerView.findViewById<TextView>(R.id.tvName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvEmail)



        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val name = it.displayName ?: "Usuario"
            val email = it.email ?: ""
            tvName.text = name
            tvEmail.text = email
            val initial = name.firstOrNull()?.uppercase() ?: "U"
            tvInitial.text = initial
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}