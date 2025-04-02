/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.busschedule.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.busschedule.R
import com.example.busschedule.data.BusSchedule
import com.example.busschedule.ui.theme.BusScheduleTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class BusScheduleScreens {
    FullSchedule,
    RouteSchedule
}

enum class SortOrder {
    BY_NAME_ASC,
    BY_NAME_DESC,
    BY_TIME_ASC,
    BY_TIME_DESC
}

@Composable
fun BusScheduleApp(
    viewModel: BusScheduleViewModel = viewModel(factory = BusScheduleViewModel.factory)
) {
    val navController = rememberNavController()
    val fullScheduleTitle = stringResource(R.string.full_schedule)

    var topAppBarTitle by remember { mutableStateOf("Full Schedule") }

    var currentSortOrder by remember { mutableStateOf(SortOrder.BY_TIME_ASC) }

    // Aquí, re-calculamos el flujo con base en el estado de orden
    val routeScheduleFlow = remember(currentSortOrder) {
        when (currentSortOrder) {
            SortOrder.BY_NAME_ASC -> viewModel.getScheduleForByNameAsc()
            SortOrder.BY_NAME_DESC -> viewModel.getScheduleForByNameDesc()
            SortOrder.BY_TIME_ASC -> viewModel.getFullSchedule()
            SortOrder.BY_TIME_DESC -> viewModel.getFullScheduleAlter()
        }
    }

    // Ahora el 'collectAsState' está dentro de un Composable, por lo que no habrá error
    val busSchedules by routeScheduleFlow.collectAsState(emptyList()) // Obtener datos del flujo actual

    val onBackHandler = {
        topAppBarTitle = fullScheduleTitle
        navController.navigateUp()
    }

    Scaffold(
        topBar = {
            BusScheduleTopAppBar(
                title = "$topAppBarTitle",  // Mostrar la hora en el título
                canNavigateBack = navController.previousBackStackEntry != null,
                onBackClick = { onBackHandler() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BusScheduleScreens.FullSchedule.name
        ) {
            composable(BusScheduleScreens.FullSchedule.name) {
                FullScheduleScreen(
                    busSchedules = busSchedules,
                    contentPadding = innerPadding,
                    onScheduleClick = { busStopName ->
                        navController.navigate(
                            "${BusScheduleScreens.RouteSchedule.name}/$busStopName"
                        )
                        topAppBarTitle = busStopName
                    },
                    onHeaderNameClick = {
                        // Cambiar el orden de nombre cuando se hace clic en el encabezado
                        currentSortOrder = if (currentSortOrder == SortOrder.BY_NAME_ASC) SortOrder.BY_NAME_DESC else SortOrder.BY_NAME_ASC
                    },
                    onHeaderTimeClick = {
                        // Cambiar el orden de tiempo cuando se hace clic en el encabezado
                        currentSortOrder = if (currentSortOrder == SortOrder.BY_TIME_ASC) SortOrder.BY_TIME_DESC else SortOrder.BY_TIME_ASC
                    }
                )
            }
            val busRouteArgument = "busRoute"
            composable(
                route = BusScheduleScreens.RouteSchedule.name + "/{$busRouteArgument}",
                arguments = listOf(navArgument(busRouteArgument) { type = NavType.StringType })
            ) { backStackEntry ->
                val stopName = backStackEntry.arguments?.getString(busRouteArgument)
                    ?: error("busRouteArgument cannot be null")

                RouteScheduleScreen(
                    stopName = stopName,
                    busSchedules = busSchedules,
                    contentPadding = innerPadding,
                    onBack = { onBackHandler() },
                    onHeaderNameClick = {
                        // Cambiar el orden de nombre cuando se hace clic en el encabezado
                        currentSortOrder = if (currentSortOrder == SortOrder.BY_NAME_ASC) SortOrder.BY_NAME_DESC else SortOrder.BY_NAME_ASC
                    },
                    onHeaderTimeClick = {
                        // Cambiar el orden de tiempo cuando se hace clic en el encabezado
                        currentSortOrder = if (currentSortOrder == SortOrder.BY_TIME_ASC) SortOrder.BY_TIME_DESC else SortOrder.BY_TIME_ASC
                    }
                )
            }
        }
    }
}


@Composable
fun FullScheduleScreen(
    busSchedules: List<BusSchedule>,
    onScheduleClick: (String) -> Unit,
    onHeaderNameClick: () -> Unit,
    onHeaderTimeClick: () -> Unit, // Nuevo parámetro
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    BusScheduleScreen(
        busSchedules = busSchedules,
        onScheduleClick = onScheduleClick,
        contentPadding = contentPadding,
        modifier = modifier,
        onHeaderNameClick = onHeaderNameClick,
        onHeaderTimeClick = onHeaderTimeClick // Se pasa la función
    )
}

@Composable
fun RouteScheduleScreen(
    stopName: String,
    busSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onBack: () -> Unit = {},
    onHeaderNameClick: () -> Unit,
    onHeaderTimeClick: () -> Unit // Nuevo parámetro
) {
    BackHandler { onBack() }
    // Filtrar los horarios solo para la parada seleccionada
    val filteredSchedules = busSchedules.filter { it.stopName == stopName }

    BusScheduleScreen(
        busSchedules = filteredSchedules,  // Pasar solo los horarios filtrados
        modifier = modifier,
        contentPadding = contentPadding,
        stopName = stopName,
        onHeaderNameClick = onHeaderNameClick,
        onHeaderTimeClick = onHeaderTimeClick // Se pasa la función
    )
}

@Composable
fun BusScheduleScreen(
    busSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    stopName: String? = null,
    onScheduleClick: ((String) -> Unit)? = null,
    onHeaderNameClick: () -> Unit,
    onHeaderTimeClick: () -> Unit // Nuevo parámetro para cambiar la lista
) {
    val stopNameText = if (stopName == null) {
        stringResource(R.string.stop_name)
    } else {
        "$stopName ${stringResource(R.string.route_stop_name)}"
    }
    val layoutDirection = LocalLayoutDirection.current

    Column(
        modifier = modifier.padding(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = dimensionResource(R.dimen.padding_medium),
                    start = dimensionResource(R.dimen.padding_medium),
                    end = dimensionResource(R.dimen.padding_medium),
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stopNameText, modifier = Modifier.clickable { onHeaderNameClick() })
            Text(text = stringResource(R.string.arrival_time), modifier = Modifier.clickable { onHeaderTimeClick() })
        }
        Divider()
        BusScheduleDetails(
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding()
            ),
            busSchedules = busSchedules,
            onScheduleClick = onScheduleClick
        )
    }
}

@SuppressLint("NewApi")
@Composable
fun BusScheduleDetails(
    busSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onScheduleClick: ((String) -> Unit)? = null
) {
    // Estado para la parada más cercana
    var currentTime = LocalTime.now() // Hora actual sin fecha
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault() // Asegura que usa la zona horaria del sistema
    val currentFormattedTime = sdf.format(Date())
    var selectedStop by remember { mutableStateOf<String?>(null) }
    var selectedStopSchedules by remember { mutableStateOf<List<BusSchedule>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<BusSchedule?>(null) }
    // Estado mutable para la parada más cercana
    var closestSchedule by remember { mutableStateOf<BusSchedule?>(null) }

    // Función que calcula la parada más cercana
    fun updateClosestSchedule() {
        val futureSchedules = busSchedules.filter { schedule ->
            val arrivalTimeMillis = schedule.arrivalTimeInMillis * 1000L // Aseguramos que sea en milisegundos
            val arrivalDate = Date(arrivalTimeMillis)
            val arrivalCalendar = Calendar.getInstance().apply { time = arrivalDate }
            val arrivalHour = arrivalCalendar.get(Calendar.HOUR_OF_DAY) // Hora en 24h
            val arrivalMinute = arrivalCalendar.get(Calendar.MINUTE) // Minutos

            val arrivalTime = LocalTime.of(arrivalHour, arrivalMinute) // Crear LocalTime con hora y minuto
            arrivalTime.isAfter(currentTime) // Solo consideramos las paradas en el futuro
        }

        // Encontrar la parada más cercana
        val newClosestSchedule = futureSchedules.minByOrNull { schedule ->
            val arrivalTimeMillis = schedule.arrivalTimeInMillis * 1000L // Aseguramos que sea en milisegundos
            val arrivalDate = Date(arrivalTimeMillis)
            val arrivalCalendar = Calendar.getInstance().apply { time = arrivalDate }
            val arrivalHour = arrivalCalendar.get(Calendar.HOUR_OF_DAY) // Hora en 24h
            val arrivalMinute = arrivalCalendar.get(Calendar.MINUTE) // Minutos

            LocalTime.of(arrivalHour, arrivalMinute) // Crear LocalTime con hora y minuto
        }

        // Actualizar el estado con la nueva parada más cercana
        closestSchedule = newClosestSchedule
    }

    // Actualización continua con un LaunchedEffect
    LaunchedEffect(currentTime) {
        while (true) {
            updateClosestSchedule()  // Actualiza la parada más cercana
            currentTime = LocalTime.now() // Actualiza la hora actual
            delay(1000)  // Cada 1 segundo
            Log.d("BusScheduuleApp", "Actualizando parada más cercana")
        }
    }


    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(
            items = busSchedules,
            key = { busSchedule -> busSchedule.id }
        ) { schedule ->
            val arrivalTimeMillis = schedule.arrivalTimeInMillis * 1000L // Aseguramos que sea en milisegundos
            val arrivalDate = Date(arrivalTimeMillis)
            val arrivalCalendar = Calendar.getInstance().apply {
                time = arrivalDate
            }

            val arrivalHour = arrivalCalendar.get(Calendar.HOUR_OF_DAY) // Hora en 24h
            val arrivalMinute = arrivalCalendar.get(Calendar.MINUTE) // Minutos

            val arrivalTime = LocalTime.of(arrivalHour, arrivalMinute) // Crear LocalTime con hora y minuto
            val arrivalFormattedTime = sdf.format(arrivalDate)

            // Comparar solo la hora y los minutos
            val isClosestSchedule = closestSchedule == schedule // Verifica si es la más cercana

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onScheduleClick != null) {
                        onScheduleClick?.invoke(schedule.stopName)
                    }
                    .padding(dimensionResource(R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onScheduleClick == null) {
                    Text(
                        text = schedule.stopName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = dimensionResource(R.dimen.font_large).value.sp,
                            fontWeight = FontWeight(300)
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                            .clickable {
                                selectedSchedule = schedule
                                showDialog = true
                            }
                    )
                } else {
                    Text(
                        text = schedule.stopName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = dimensionResource(R.dimen.font_large).value.sp,
                            fontWeight = FontWeight(300)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .border(2.dp, if (isClosestSchedule) Color.Red else Color.Transparent, shape = MaterialTheme.shapes.medium)
                        .padding(4.dp) // Opcional para darle más espacio al borde
                ) {
                    Text(
                        text = arrivalFormattedTime,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = dimensionResource(R.dimen.font_large).value.sp,
                            fontWeight = FontWeight(600),
                            color = if (isClosestSchedule) Color.Red else Color.Unspecified
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.wrapContentSize()
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            title = {},
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "${selectedSchedule?.stopName ?: "Desconocida"}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally), fontSize = 32.sp)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = selectedSchedule?.let {
                            val arrivalTimeMillis = it.arrivalTimeInMillis * 1000L // Convertimos segundos a milisegundos
                            val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(arrivalTimeMillis))
                            formattedTime
                        } ?: "--",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                        fontSize = 32.sp
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (canNavigateBack) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(
                            R.string.back
                        )
                    )
                }
            },
            modifier = modifier
        )
    } else {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullScheduleScreenPreview() {
    BusScheduleTheme {
        FullScheduleScreen(
            busSchedules = List(3) { index ->
                BusSchedule(
                    index,
                    "Main Street",
                    111111
                )
            },
            onScheduleClick = {},
            onHeaderNameClick = {},
            onHeaderTimeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RouteScheduleScreenPreview() {
    BusScheduleTheme {
        RouteScheduleScreen(
            stopName = "Main Street",
            busSchedules = List(3) { index ->
                BusSchedule(
                    index,
                    "Main Street",
                    111111
                )
            },
            onBack = {},
            onHeaderNameClick = {},
            onHeaderTimeClick = {}
        )
    }
}
