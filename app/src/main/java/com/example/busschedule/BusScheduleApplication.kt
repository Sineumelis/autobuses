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
package com.example.busschedule

import android.app.Application
import com.example.busschedule.data.AppDatabase

/**
 * La clase Application es una clase base en Android que contiene el estado global de la aplicación.
 *
 * La palabra clave by lazy significa que la inicialización de database
 * se pospone hasta que se acceda a ella por primera vez
 *
 * La expresión { AppDatabase.getDatabase(this) } es una lambda que se ejecuta para inicializar database
 *
 * AppDatabase.getDatabase(this) es una llamada a un método estático getDatabase en la clase AppDatabase
 *
 * Este código define una aplicación que tiene una base de datos que se inicializa de manera
 * (lazy) cuando se accede por primera vez
 *
 * Esto es útil para mejorar el rendimiento y evitar
 * inicializaciones innecesarias hasta que realmente se necesiten.
 */


class BusScheduleApplication: Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
