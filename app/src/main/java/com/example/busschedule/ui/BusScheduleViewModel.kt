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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.busschedule.BusScheduleApplication
import com.example.busschedule.data.BusSchedule
import com.example.busschedule.data.BusScheduleDao
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * ViewModel para horarios de autobuses.
 * contiene métodos para acceder a Room DB a través de [busScheduleDao]
 */
class BusScheduleViewModel(private val busScheduleDao: BusScheduleDao): ViewModel() {
    var topAppBarTitle by mutableStateOf("Full Schedule")
        private set

    fun updateTitle(newTitle: String) {
        topAppBarTitle = newTitle
    }

    // Obtiene el horario completo de autobuses desde Room DB
    fun getFullSchedule(): Flow<List<BusSchedule>> = busScheduleDao.getAll()

    fun getFullScheduleAlter(): Flow<List<BusSchedule>> = busScheduleDao.getAllAlter()

    fun getScheduleForByNameAsc(): Flow<List<BusSchedule>> = busScheduleDao.getByStopNameAsc()

    fun getScheduleForByNameDesc(): Flow<List<BusSchedule>> = busScheduleDao.getByStopNameDesc()

    fun searchBusSchedules(query: String): Flow<List<BusSchedule>> {
        return busScheduleDao.searchByStopName(query)
    }

    companion object {
        val factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BusScheduleApplication)
                BusScheduleViewModel(application.database.busScheduleDao())
            }
        }
    }
}
