package org.example.services

// services/DynamicQueryService.kt

import org.example.models.*
import org.example.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq

import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq

import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction

class DynamicQueryService {

    // Definir listas blancas de campos permitidos por entidad
    private val allowedEntities = setOf("usuarios", "articulos", "categorias", "posts", "comentarios", "imagenes", "secciones", "seguimientos_secciones", "articulos_categorias")
    private val allowedFields = mapOf(
        "usuarios" to setOf("id_usuario", "nombre_usuario", "correo", "fecha_registro", "rol"),
        "articulos" to setOf("id_articulo", "titulo", "contenido", "fecha_creacion", "fecha_ultima_edicion", "estado"),
        "categorias" to setOf("id_categoria", "nombre_categoria", "descripcion"),
        "posts" to setOf("id_post", "titulo", "contenido", "fecha_creacion", "autor_id", "seccion_id"),
        "comentarios" to setOf("id_comentario", "contenido", "fecha_creacion", "autor_id", "post_id", "comentario_padre_id"),
        "imagenes" to setOf("id_imagen", "titulo", "descripcion", "url", "fecha_subida", "articulo_id"),
        "secciones" to setOf("id_seccion", "titulo", "descripcion", "fecha_creacion", "creador_id"),
        "seguimientos_secciones" to setOf("usuario_id", "seccion_id"),
        "articulos_categorias" to setOf("articulo_id", "categoria_id")
    )
    private val allowedOperators = setOf("eq", "neq", "gt", "lt", "gte", "lte", "in", "like")
    private val allowedDirections = setOf("asc", "desc")

    fun executeQuery(request: DynamicQueryRequest): List<Map<String, Any?>> {
        // 1. Validar entidad
        if (request.entity !in allowedEntities) {
            throw IllegalArgumentException("Entidad no permitida: ${request.entity}")
        }
        val table = getTableForEntity(request.entity)

        // 2. Validar campos de selección
        val selectedColumns = if (request.select != null && request.select.isNotEmpty()) {
            request.select.map { fieldName ->
                if (fieldName !in allowedFields[request.entity]!!) {
                    throw IllegalArgumentException("Campo no permitido para ${request.entity}: $fieldName")
                }
                getExposedColumnForField(table, fieldName)
            }
        } else {
            // Si no se especifica select, seleccionar todos los campos permitidos
            allowedFields[request.entity]!!.map { fieldName -> getExposedColumnForField(table, fieldName) }
        }

        return transaction {
            var query: Query = table.select(selectedColumns)

            // 3. Aplicar WHERE
            request.where?.forEach { filter ->
                if (filter.field !in allowedFields[request.entity]!!) {
                    throw IllegalArgumentException("Campo no permitido en filtro para ${request.entity}: ${filter.field}")
                }
                if (filter.operator !in allowedOperators) {
                    throw IllegalArgumentException("Operador no permitido: ${filter.operator}")
                }
                val column = getExposedColumnForField(table, filter.field)
                query = query.andWhere { buildFilterOp(column, filter.operator, filter.value) }
            }

            // 4. Aplicar ORDER BY
            request.orderBy?.let { order ->
                if (order.field !in allowedFields[request.entity]!!) {
                    throw IllegalArgumentException("Campo no permitido en orden para ${request.entity}: ${order.field}")
                }
                if (order.direction !in allowedDirections) {
                    throw IllegalArgumentException("Dirección de orden no permitida: ${order.direction}")
                }
                val column = getExposedColumnForField(table, order.field)
                val sortOrder = if (order.direction.lowercase() == "desc") SortOrder.DESC else SortOrder.ASC
                query = query.orderBy(column, sortOrder)
            }

            // 5. Aplicar LIMIT y OFFSET
            request.limit?.let { limit ->
                query = query.limit(limit, (request.offset ?: 0).toLong())
            }

            // 6. Ejecutar y mapear resultados
            query.map { row ->
                selectedColumns.associate { column ->
                    // Mapear cada columna seleccionada a su valor en la fila
                    // Exposed maneja la conversión de tipos (e.g., String, Int, LocalDateTime) automáticamente
                    column.name to row[column]
                }
            }
        }
    }

    private fun getTableForEntity(entity: String): Table = when(entity) {
        "usuarios" -> Usuarios
        "articulos" -> Articulos
        "categorias" -> Categorias
        "posts" -> Posts
        "comentarios" -> Comentarios
        "imagenes" -> Imagenes
        "secciones" -> Secciones
        "seguimientos_secciones" -> SeguimientosSecciones
        "articulos_categorias" -> ArticulosCategorias
        else -> throw IllegalArgumentException("Entidad no soportada: $entity")
    }

    // Función helper para mapear un nombre de campo string a una Columna Exposed
    // Esta función debe manejar correctamente el mapeo para cada tabla y columna.
    // Es crítica para la seguridad y funcionalidad.
    private fun getExposedColumnForField(table: Table, fieldName: String): Column<*> {
        // Aquí necesitas un mapeo de fieldName -> Column<*> para cada tabla
        // Ejemplo para Usuarios:
        if (table is Usuarios) {
            return when (fieldName) {
                "id_usuario" -> Usuarios.id
                "nombre_usuario" -> Usuarios.nombreUsuario
                "correo" -> Usuarios.correo
                "fecha_registro" -> Usuarios.fechaRegistro
                "rol" -> Usuarios.rol
                else -> throw IllegalArgumentException("Campo no encontrado en Usuarios: $fieldName")
            }
        }
        if (table is Articulos) {
            return when (fieldName) {
                "id_articulo" -> Articulos.id
                "titulo" -> Articulos.titulo
                "contenido" -> Articulos.contenido
                "fecha_creacion" -> Articulos.fechaCreacion
                "fecha_ultima_edicion" -> Articulos.fechaUltimaEdicion
                "estado" -> Articulos.estado
                else -> throw IllegalArgumentException("Campo no encontrado en Articulos: $fieldName")
            }
        }
        // ... repetir para otras tablas (Categorias, Posts, etc.)
        // Este es un punto crítico y propenso a errores si hay muchas columnas.
        // Considera estrategias para automatizar o simplificar este mapeo.
        throw IllegalArgumentException("Mapeo de campo no encontrado para tabla ${table.tableName} y campo $fieldName")
    }

    // Función helper para construir la operación WHERE basada en el operador
    private fun buildFilterOp(column: Column<*>, operator: String, value: String): Op<Boolean> {
        // Asegurar que el valor se convierte al tipo correcto de la columna
        // Exposed puede ayudar con esto, pero debes manejar casos como 'in' que requieren listas.
        return when (operator) {
            "eq" -> {
                if (column.columnType is VarCharColumnType) {
                    column as Column<String>
                    column eq value
                } else if (column.columnType is IntegerColumnType) {
                    (column eq value.toIntOrNull())
                        ?: throw IllegalArgumentException("Valor no numérico para 'eq' en columna numérica: $value")
                } else if (column.columnType is TextColumnType) {
                    column as Column<String>
                    column eq value
                } else if (column.columnType is BooleanColumnType) {
                    column as Column<Boolean>
                    column eq (value.lowercase() == "true")
                } else {
                    // Manejar otros tipos según sea necesario
                    // Por simplicidad, asumimos string si no es numérico
                    column as Column<String>
                    column eq value
                }
            }
            "neq" -> {
                if (column.columnType is org.jetbrains.exposed.sql.VarCharColumnType) {
                    column as Column<String>
                    column neq value
                } else if (column.columnType is org.jetbrains.exposed.sql.IntegerColumnType) {
                    column as Column<Int>
                    column neq value.toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico para 'neq' en columna numérica: $value")
                } else {
                    column as Column<String>
                    column neq value
                }
            }
            "gt" -> {
                if (column.columnType is IntegerColumnType) {
                    column as Column<Int>
                    column greater value.toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico para 'gt' en columna numérica: $value")
                } else {
                    // Solo numéricos para gt
                    throw IllegalArgumentException("Operador 'gt' solo válido para columnas numéricas")
                }
            }
            "lt" -> {
                if (column.columnType is IntegerColumnType) {
                    column as Column<Int>
                    column less value.toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico para 'lt' en columna numérica: $value")
                } else {
                    throw IllegalArgumentException("Operador 'lt' solo válido para columnas numéricas")
                }
            }
            "gte" -> {
                if (column.columnType is org.jetbrains.exposed.sql.IntegerColumnType) {
                    column as Column<Int>
                    column greaterEq value.toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico para 'gte' en columna numérica: $value")
                } else {
                    throw IllegalArgumentException("Operador 'gte' solo válido para columnas numéricas")
                }
            }
            "lte" -> {
                if (column.columnType is org.jetbrains.exposed.sql.IntegerColumnType) {
                    column as Column<Int>
                    column lessEq value.toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico para 'lte' en columna numérica: $value")
                } else {
                    throw IllegalArgumentException("Operador 'lte' solo válido para columnas numéricas")
                }
            }
            "in" -> {
                if (column.columnType is org.jetbrains.exposed.sql.VarCharColumnType) {
                    column as Column<String>
                    val valuesList = value.split(",").map { it.trim() }
                    column inList valuesList
                } else if (column.columnType is org.jetbrains.exposed.sql.IntegerColumnType) {
                    column as Column<Int>
                    val valuesList = value.split(",").map { it.trim().toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico en lista para 'in': $it") }
                    column inList valuesList
                } else {
                    throw IllegalArgumentException("Operador 'in' solo válido para columnas numéricas o de texto")
                }
            }
            "like" -> {
                if (column.columnType is VarCharColumnType || column.columnType is TextColumnType) {
                    column as Column<String>
                    column like value
                } else {
                    throw IllegalArgumentException("Operador 'like' solo válido para columnas de texto")
                }
            }
            else -> throw IllegalArgumentException("Operador no soportado: $operator")
        }
    }
}