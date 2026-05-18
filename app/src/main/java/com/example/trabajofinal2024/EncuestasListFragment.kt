package com.example.trabajofinal2024

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                                    Toast.makeText(requireContext(), "Encuesta reanudada.", Toast.LENGTH_SHORT).show()
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

enum class FiltroEncuesta { TODAS, EN_PROGRESO, COMPLETADAS, ABANDONADAS }

@Composable
private fun EncuestasContent(
    encuestas: List<Encuesta>?,
    onNuevaEncuesta: () -> Unit,
    onResumeEncuesta: (Encuesta) -> Unit,
    onReanudar: (Encuesta) -> Unit,
    onAbandonar: (Encuesta) -> Unit,
    onVerDetalles: (Encuesta, Int) -> Unit
) {
    var filtroSeleccionado by remember { mutableStateOf(FiltroEncuesta.TODAS) }

    val encuestasFiltradas: List<IndexedValue<Encuesta>>? = encuestas
        ?.withIndex()
        ?.filter { (_, encuesta) ->
            when (filtroSeleccionado) {
                FiltroEncuesta.TODAS        -> true
                FiltroEncuesta.EN_PROGRESO  -> encuesta.activa && !encuesta.completa
                FiltroEncuesta.COMPLETADAS  -> encuesta.completa
                FiltroEncuesta.ABANDONADAS  -> !encuesta.activa && !encuesta.completa
            }
        }

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
            FiltroSelector(
                seleccionado = filtroSeleccionado,
                onFiltroChange = { filtroSeleccionado = it }
            )

            Spacer(modifier = Modifier.height(12.dp))
            when {
                encuestasFiltradas == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                encuestasFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay encuestas cargadas")
                    }
                }

                else -> {
                    LazyColumn {
                        items(encuestasFiltradas) { indexedEncuesta ->
                            EncuestaItem(
                                encuesta = indexedEncuesta.value,
                                index = indexedEncuesta.index,
                                onResume = { onResumeEncuesta(indexedEncuesta.value) },
                                onReanudar = { onReanudar(indexedEncuesta.value) },
                                onAbandonar = { onAbandonar(indexedEncuesta.value) },
                                onVerDetalles = { onVerDetalles(indexedEncuesta.value, indexedEncuesta.index) }
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun FiltroSelector(
    seleccionado: FiltroEncuesta,
    onFiltroChange: (FiltroEncuesta) -> Unit
) {
    val opciones = listOf(
        FiltroEncuesta.TODAS       to "Todas",
        FiltroEncuesta.EN_PROGRESO to "En progreso",
        FiltroEncuesta.COMPLETADAS to "Completadas",
        FiltroEncuesta.ABANDONADAS to "Abandonadas"
    )

    ScrollableTabRow(
        selectedTabIndex = opciones.indexOfFirst { it.first == seleccionado },
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.primary,
        edgePadding = 0.dp
    ) {
        opciones.forEach { (filtro, label) ->
            Tab(
                selected = seleccionado == filtro,
                onClick = { onFiltroChange(filtro) },
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1
                    )
                }
            )
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
                val (estadoTexto, estadoColor) = when {
                    encuesta.completa  -> "COMPLETADA"  to Color(0xFF2E7D32) // verde oscuro
                    !encuesta.activa   -> "ABANDONADA"  to Color(0xFFC62828) // rojo oscuro
                    else               -> "EN PROGRESO" to Color(0xFFF9A825) // amarillo oscuro
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = estadoColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = estadoTexto,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.caption.copy(
                            color = estadoColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${encuesta.domicilio} — ${encuesta.ciudad}", style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progreso: $progreso/$totalAlimentos alimentos",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "$porcentaje%",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = porcentaje / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colors.primary,
                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
            )
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
