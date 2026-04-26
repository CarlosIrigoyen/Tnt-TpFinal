package com.example.trabajofinal2024

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EncuestasListFragment : Fragment() {

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        uid?.let {
            encuestaViewModel.startListeningFirestore(it)
        }
    }

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val uid = currentUserUid
                    if (uid == null) {
                        findNavController().navigate(R.id.loginFragment)
                    } else {
                        val encuestas by encuestaViewModel.getEncuestasPorUsuario(uid)
                            .observeAsState(initial = null)

                        EncuestasContent(
                            encuestas = encuestas,
                            onNuevaEncuesta = {
                                findNavController().navigate(R.id.action_encuestasList_to_encuestaFragment)
                            },
                            onResumeEncuesta = { encuesta ->
                                val bundle =
                                    Bundle().apply { putString("encuestaid", encuesta.firestoreId) }
                                findNavController().navigate(
                                    R.id.action_encuestasList_to_foodFragment,
                                    bundle
                                )
                            },
                            onReanudar = { encuesta ->
                                lifecycleScope.launch {
                                    val encuestaId = encuesta.firestoreId ?: return@launch
                                    encuestaViewModel.reanudarEncuestaFirebase(uid, encuestaId)
                                    Toast.makeText(requireContext(), "Encuesta #${encuesta.encuestaId} reanudada.", Toast.LENGTH_SHORT).show()
                                    val bundle = Bundle().apply { putString("encuestaid", encuesta.firestoreId) }
                                    findNavController().navigate(R.id.action_encuestasList_to_foodFragment, bundle)
                                }
                            },
                            onAbandonar = { encuesta ->
                                lifecycleScope.launch {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                                    val encuestaId = encuesta.firestoreId ?: return@launch
                                    encuestaViewModel.abandonEncuestaFirebase(uid, encuestaId)
                                    Toast.makeText(requireContext(), "Encuesta #${encuesta.encuestaId} abandonada.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onVerDetalles = { encuesta, index ->
                                val bundle = Bundle().apply { putString("encuestaid", encuesta.firestoreId); putInt("encuestaNumero", index + 1)}
                                findNavController().navigate(R.id.detalleEncuestaFragment, bundle)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncuestasContent(
    encuestas: List<Encuesta>?,
    onNuevaEncuesta: () -> Unit,
    onResumeEncuesta: (Encuesta) -> Unit,
    onReanudar: (Encuesta) -> Unit,
    onAbandonar: (Encuesta) -> Unit,
    onVerDetalles: (Encuesta, Int) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevaEncuesta,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva encuesta", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                encuestas == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                encuestas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay encuestas cargadas")
                    }
                }

                else -> {
                    LazyColumn {
                        itemsIndexed(encuestas) { index, encuesta ->
                            EncuestaItem(
                                encuesta = encuesta,
                                index = index,
                                onResume = { onResumeEncuesta(encuesta) },
                                onReanudar = { onReanudar(encuesta) },
                                onAbandonar = { onAbandonar(encuesta) },
                                onVerDetalles = { onVerDetalles(encuesta, index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncuestaItem(
    encuesta: Encuesta,
    index: Int,
    onResume: () -> Unit,
    onReanudar: () -> Unit,
    onAbandonar: () -> Unit,
    onVerDetalles: (Int) -> Unit
) {
    val totalAlimentos = FoodCatalog.ALL.size
    val progreso = (encuesta.currentIndex + 1).coerceAtMost(totalAlimentos)
    val porcentaje = if (totalAlimentos > 0) (progreso * 100 / totalAlimentos) else 0

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp), elevation = 6.dp) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Encuesta #${index + 1}", style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold))
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                when {
                    encuesta.completa -> {
                        Button(onClick = { onVerDetalles(index) }, modifier = Modifier.weight(1f)) {
                            Text("Ver Detalles")
                        }
                    }
                    !encuesta.activa -> {
                        Button(onClick = onReanudar, modifier = Modifier.weight(1f)) {
                            Text("Reanudar")
                        }
                    }
                    else -> {
                        Button(onClick = onResume, modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)) {
                            Text("Continuar")
                        }
                        Button(onClick = onAbandonar, modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)) {
                            Text("Abandonar")
                        }
                    }
                }
            }
        }
    }
}
