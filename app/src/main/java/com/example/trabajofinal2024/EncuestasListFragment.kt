package com.example.trabajofinal2024

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class EncuestasListFragment : Fragment() {

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        // Usamos ComposeView para renderizar la UI en Compose desde este Fragment.
        return ComposeView(requireContext()).apply {
            val gsi = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireContext(), gsi)
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val uid = currentUserUid
                        if (uid == null) {
                            findNavController().navigate(R.id.loginFragment)
                        } else {
                            val encuestas by encuestaViewModel.getEncuestasPorUsuario(uid)
                                .observeAsState(initial = emptyList())

                            EncuestasScreen(
                                encuestas = encuestas,
                                onNavigateToMapa = { findNavController().navigate(R.id.action_encuestasList_to_mapFragment) },
                                onNavigateToStats = { findNavController().navigate(R.id.action_encuestasList_to_statsFragment) },
                                onNuevaEncuesta = { findNavController().navigate(R.id.action_encuestasList_to_encuestaFragment) },
                                onCerrarSesion = {
                                    FirebaseAuth.getInstance().signOut()
                                    googleSignInClient.signOut().addOnCompleteListener{
                                        findNavController().navigate(
                                            R.id.loginFragment,
                                            null,
                                            NavOptions.Builder().setPopUpTo(R.id.main_navigation, true).build()
                                        )
                                    }
                                },
                                onResumeEncuesta = { encuesta ->
                                    val bundle = android.os.Bundle().apply { putInt("encuestaid", encuesta.encuestaId) }
                                    findNavController().navigate(R.id.action_encuestasList_to_foodFragment, bundle)
                                },
                                onReanudar = { encuesta ->
                                    encuestaViewModel.reanudarEncuesta(encuesta.encuestaId)
                                    Toast.makeText(requireContext(), "Encuesta #${encuesta.encuestaId} reanudada.", Toast.LENGTH_SHORT).show()
                                    val bundle = android.os.Bundle().apply { putInt("encuestaid", encuesta.encuestaId) }
                                    findNavController().navigate(R.id.action_encuestasList_to_foodFragment, bundle)
                                },
                                onAbandonar = { encuesta ->
                                    encuestaViewModel.abandonEncuesta(encuesta.encuestaId)
                                    Toast.makeText(requireContext(), "Encuesta #${encuesta.encuestaId} abandonada.", Toast.LENGTH_SHORT).show()
                                },
                                onVerDetalles = { encuesta ->
                                    val bundle = android.os.Bundle().apply { putInt("encuestaid", encuesta.encuestaId) }
                                    // Navegamos al fragmento de detalle
                                    findNavController().navigate(R.id.detalleEncuestaFragment, bundle)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncuestasScreen(
    encuestas: List<Encuesta>,
    onNavigateToMapa: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNuevaEncuesta: () -> Unit,
    onCerrarSesion: () -> Unit,
    onResumeEncuesta: (Encuesta) -> Unit,
    onReanudar: (Encuesta) -> Unit,
    onAbandonar: (Encuesta) -> Unit,
    onVerDetalles: (Encuesta) -> Unit
) {
    val ctx = LocalContext.current
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        val initials = remember { obtenerIniciales() }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                UserInitialsAvatar(initials)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Encuestas",
                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(onClick = onCerrarSesion) {
                Text(text = "Cerrar Sesión")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Botones: Mapa / Estadísticas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly) {
            Button(modifier = Modifier.weight(1f).padding(end = 8.dp), onClick = onNavigateToMapa) {
                Text("Mapa")
            }
            Button(modifier = Modifier.weight(1f).padding(start = 8.dp), onClick = onNavigateToStats) {
                Text("Estadísticas")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de encuestas
        if (encuestas.isEmpty()) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No hay encuestas cargadas")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(encuestas) { encuesta ->
                    EncuestaItem(
                        encuesta = encuesta,
                        onResume = { onResumeEncuesta(encuesta) },
                        onReanudar = { onReanudar(encuesta) },
                        onAbandonar = { onAbandonar(encuesta) },
                        onVerDetalles = { onVerDetalles(encuesta) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón inferior "Iniciar nueva encuesta"
        Button(onClick = onNuevaEncuesta, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Iniciar nueva encuesta")
        }
    }
}

fun obtenerIniciales(): String {
    val user = FirebaseAuth.getInstance().currentUser
    val nombre = user?.displayName
        ?: user?.email
        ?: "U"

    val partes = nombre.trim().split(" ")

    return if (partes.size >= 2) {
        "${partes[0].first()}${partes[1].first()}".uppercase()
    } else {
        nombre.take(2).uppercase()
    }
}

@Composable
fun UserInitialsAvatar(initials: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colors.primary)
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EncuestaItem(
    encuesta: Encuesta,
    onResume: () -> Unit,
    onReanudar: () -> Unit,
    onAbandonar: () -> Unit,
    onVerDetalles: () -> Unit
) {
    val totalAlimentos = try {
        FoodCatalog.ALL.size
    } catch (t: Throwable) {
        0
    }
    val progreso = encuesta.currentIndex.coerceAtMost(totalAlimentos)
    val porcentaje = if (totalAlimentos > 0) (progreso * 100 / totalAlimentos) else 0

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                Text(text = "Encuesta #${encuesta.encuestaId}", style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
                val estado = when {
                    encuesta.completa -> "COMPLETADA"
                    !encuesta.activa -> "ABANDONADA"
                    else -> "EN PROGRESO"
                }
                Text(text = estado, style = MaterialTheme.typography.caption)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${encuesta.domicilio} — ${encuesta.ciudad}", style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Progreso: $progreso/$totalAlimentos alimentos ($porcentaje%)", style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly) {
                if (encuesta.completa) {
                    Button(onClick = onVerDetalles, modifier = Modifier.weight(1f)) {
                        Text("Ver Detalles")
                    }
                } else if (!encuesta.activa) {
                    Button(onClick = onReanudar, modifier = Modifier.weight(1f)) {
                        Text("Reanudar")
                    }
                } else {
                    Button(onClick = onResume, modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                        Text("Continuar")
                    }
                    Button(onClick = onAbandonar, modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text("Abandonar")
                    }
                }
            }
        }
    }
}