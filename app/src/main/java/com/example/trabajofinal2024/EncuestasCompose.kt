package com.example.trabajofinal2024

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EncuestasListComposeHost(
    encuestaViewModel: EncuestaViewModel,
    onNavigateToFood: (encuestaId: Int) -> Unit,
    onNavigateToEncuesta: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToStats: () -> Unit,
    onSignOut: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid == null) {
        onSignOut()
        return
    }

    val encuestas by encuestaViewModel
        .getEncuestasPorUsuario(uid)
        .observeAsState(initial = emptyList())

    EncuestasListScreen(
        encuestas = encuestas,
        onContinuar = { onNavigateToFood(it.encuestaId) },
        onAbandonar = { encuestaViewModel.abandonEncuesta(it.encuestaId) },
        onReanudar = {
            encuestaViewModel.reanudarEncuesta(it.encuestaId)
            onNavigateToFood(it.encuestaId)
        },
        onVer = { onNavigateToFood(it.encuestaId) },
        onNew = onNavigateToEncuesta,
        onMap = onNavigateToMap,
        onStats = onNavigateToStats,
        onSignOut = onSignOut
    )
}

@Composable
fun EncuestasListScreen(
    encuestas: List<Encuesta>,
    onContinuar: (Encuesta) -> Unit,
    onAbandonar: (Encuesta) -> Unit,
    onReanudar: (Encuesta) -> Unit,
    onVer: (Encuesta) -> Unit,
    onNew: () -> Unit,
    onMap: () -> Unit,
    onStats: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encuestas") },
                actions = {
                    TextButton(onClick = onMap) { Text("Mapa") }
                    TextButton(onClick = onStats) { Text("Estadísticas") }
                    TextButton(onClick = onSignOut) { Text("Cerrar sesión") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNew) {
                Text("+")
            }
        }
    ) { innerPadding ->

        if (encuestas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay encuestas cargadas")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onNew) {
                        Text("Iniciar nueva encuesta")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(encuestas, key = { it.encuestaId }) { encuesta ->
                    EncuestaItem(
                        encuesta = encuesta,
                        onContinuar = onContinuar,
                        onAbandonar = onAbandonar,
                        onReanudar = onReanudar,
                        onVer = onVer
                    )
                }
            }
        }
    }
}

@Composable
fun EncuestaItem(
    encuesta: Encuesta,
    onContinuar: (Encuesta) -> Unit,
    onAbandonar: (Encuesta) -> Unit,
    onReanudar: (Encuesta) -> Unit,
    onVer: (Encuesta) -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Encuesta #${encuesta.encuestaId}",
                    style = MaterialTheme.typography.h6
                )

                val estado = when {
                    encuesta.completa -> "COMPLETADA"
                    !encuesta.activa -> "ABANDONADA"
                    else -> "EN PROGRESO"
                }

                Text(
                    text = estado,
                    style = MaterialTheme.typography.caption
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${encuesta.domicilio} — ${encuesta.ciudad}",
                style = MaterialTheme.typography.body2
            )

            Spacer(modifier = Modifier.height(8.dp))

            val totalAlimentos = try {
                FoodCatalog.ALL.size
            } catch (e: Exception) {
                0
            }

            val progreso = encuesta.currentIndex.coerceAtMost(totalAlimentos)
            val porcentaje =
                if (totalAlimentos > 0) (progreso * 100 / totalAlimentos)
                else 0

            Text(
                text = "Progreso: $progreso/$totalAlimentos alimentos ($porcentaje%)",
                style = MaterialTheme.typography.caption
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                encuesta.completa -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { onVer(encuesta) }) {
                            Text("Ver detalles")
                        }
                    }
                }

                !encuesta.activa -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { onReanudar(encuesta) }) {
                            Text("Reanudar")
                        }
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { onContinuar(encuesta) }) {
                            Text("Continuar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(onClick = { onAbandonar(encuesta) }) {
                            Text("Abandonar")
                        }
                    }
                }
            }
        }
    }
}
