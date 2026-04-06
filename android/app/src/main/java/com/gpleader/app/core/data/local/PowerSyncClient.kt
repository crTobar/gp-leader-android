package com.gpleader.app.core.data.local

import android.content.Context
import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncDatabase
import com.powersync.db.schema.Column
import com.powersync.db.schema.Schema
import com.powersync.db.schema.Table

val AppSchema = Schema(
    listOf(
        Table(
            name = "grupos",
            columns = listOf(
                Column.text("nombre"),
                Column.text("descripcion"),
                Column.text("lugar_reunion"),
                Column.text("canto_favorito"),
                Column.text("versiculo"),
                Column.text("personaje_biblico"),
                Column.text("dia_semana"),
                Column.text("hora_inicio"),
                Column.text("hora_fin"),
                Column.text("iglesia_id"),
                Column.text("distrito_id"),
                Column.text("campo_id"),
            ),
        ),
        Table(
            name = "miembros",
            columns = listOf(
                Column.text("grupo_id"),
                Column.text("primer_nombre"),
                Column.text("segundo_nombre"),
                Column.text("primer_apellido"),
                Column.text("segundo_apellido"),
                Column.text("telefono"),
                Column.text("correo"),
                Column.text("direccion"),
                Column.text("estado"),
                Column.text("fecha_ingreso"),
            ),
        ),
        Table(
            name = "reuniones",
            columns = listOf(
                Column.text("grupo_id"),
                Column.text("fecha"),
                Column.integer("no_hubo_reunion"),
                Column.text("estado"),
                Column.integer("creada_por_suplente"),
                Column.text("suplente_nombre"),
                Column.integer("aprobada_por_lider"),
                Column.integer("sincronizada"),
            ),
        ),
        Table(
            name = "asistencias",
            columns = listOf(
                Column.text("reunion_id"),
                Column.text("miembro_id"),
                Column.text("nombre_visita"),
                Column.integer("es_visita"),
                Column.text("estado"),
            ),
        ),
        Table(
            name = "actividades",
            columns = listOf(
                Column.text("nombre"),
                Column.text("nivel"),
                Column.text("tipo"),
                Column.text("unidad"),
                Column.integer("es_oficial"),
                Column.text("solo_editable"),
            ),
        ),
        Table(
            name = "registro_actividades",
            columns = listOf(
                Column.text("reunion_id"),
                Column.text("actividad_id"),
                Column.integer("cantidad"),
                Column.text("notas"),
            ),
        ),
        Table(
            name = "codigos_suplente",
            columns = listOf(
                Column.text("grupo_id"),
                Column.text("codigo"),
                Column.text("vigencia_hasta"),
                Column.integer("usado"),
                Column.text("creado_en"),
            ),
        ),
    ),
)

fun createGpDatabase(context: Context): PowerSyncDatabase = PowerSyncDatabase(
    factory = DatabaseDriverFactory(context),
    schema = AppSchema,
    dbFilename = "gpleader.db",
)
