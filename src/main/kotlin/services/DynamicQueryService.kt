package org.example.services

// services/DynamicQueryService.kt

import org.example.models.*
import org.example.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq

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
                query = query.limit(limit).offset((request.offset ?: 0).toLong())
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

    // Función helper para construir la operación WHERE basada en el operador
    private fun buildFilterOp(column: Column<*>, operator: String, value: String): Op<Boolean> {
        // Asegurar que el valor se convierte al tipo correcto de la columna
        // Seleccionamos el tipo de columna para aplicar la operación correcta
        return when {
            // Operadores para cadenas (VARCHAR, TEXT)
            column.columnType is VarCharColumnType || column.columnType is TextColumnType -> {
                val typedColumn = column as Column<String> // Hacer cast seguro
                when (operator) {
                    "eq" -> typedColumn eq value
                    "neq" -> typedColumn neq value
                    "like" -> typedColumn like value
                    else -> throw IllegalArgumentException("Operador '$operator' no soportado para columnas de texto")
                }
            }
            // Operadores para enteros
            column.columnType is IntegerColumnType -> {
                val typedColumn = column as Column<Int> // Hacer cast seguro
                val typedValue = value.toIntOrNull() ?: throw IllegalArgumentException("Valor no numérico para columna Int: $value")
                when (operator) {
                    "eq" -> typedColumn eq typedValue
                    "neq" -> typedColumn neq typedValue
                    "gt" -> typedColumn greater typedValue
                    "lt" -> typedColumn less typedValue
                    "gte" -> typedColumn greaterEq typedValue
                    "lte" -> typedColumn lessEq typedValue
                    else -> throw IllegalArgumentException("Operador '$operator' no soportado para columnas numéricas")
                }
            }
            // Operadores para booleanos
            column.columnType is BooleanColumnType -> {
                val typedColumn = column as Column<Boolean> // Hacer cast seguro
                val typedValue = value.lowercase() == "true"
                when (operator) {
                    "eq" -> typedColumn eq typedValue
                    "neq" -> typedColumn neq typedValue
                    else -> throw IllegalArgumentException("Operador '$operator' no soportado para columnas booleanas")
                }
            }
            // Operadores para Long (similar a Int)
            column.columnType is LongColumnType -> {
                val typedColumn = column as Column<Long>
                val typedValue = value.toLongOrNull() ?: throw IllegalArgumentException("Valor no numérico para columna Long: $value")
                when (operator) {
                    "eq" -> typedColumn eq typedValue
                    "neq" -> typedColumn neq typedValue
                    "gt" -> typedColumn greater typedValue
                    "lt" -> typedColumn less typedValue
                    "gte" -> typedColumn greaterEq typedValue
                    "lte" -> typedColumn lessEq typedValue
                    else -> throw IllegalArgumentException("Operador '$operator' no soportado para columnas numéricas Long")
                }
            }
            // Operadores para Double (similar a Int)
            column.columnType is DoubleColumnType -> {
                val typedColumn = column as Column<Double>
                val typedValue = value.toDoubleOrNull() ?: throw IllegalArgumentException("Valor no numérico para columna Double: $value")
                when (operator) {
                    "eq" -> typedColumn eq typedValue
                    "neq" -> typedColumn neq typedValue
                    "gt" -> typedColumn greater typedValue
                    "lt" -> typedColumn less typedValue
                    "gte" -> typedColumn greaterEq typedValue
                    "lte" -> typedColumn lessEq typedValue
                    else -> throw IllegalArgumentException("Operador '$operator' no soportado para columnas numéricas Double")
                }
            }
            // Operadores para Float (similar a Int)
            column.columnType is FloatColumnType -> {
                val typedColumn = column as Column<Float>
                val typedValue = value.toFloatOrNull() ?: throw IllegalArgumentException("Valor no numérico para columna Float: $value")
                when (operator) {
                    "eq" -> typedColumn eq typedValue
                    "neq" -> typedColumn neq typedValue
                    "gt" -> typedColumn greater typedValue
                    "lt" -> typedColumn less typedValue
                    "gte" -> typedColumn greaterEq typedValue
                    "lte" -> typedColumn lessEq typedValue
                    else -> throw IllegalArgumentException("Operador '$operator' no soportado para columnas numéricas Float")
                }
            }
            // Puedes añadir más tipos según necesites (Date, Time, etc.)

            else -> {
                // Si el tipo de columna no está soportado por esta lógica, lanzar un error
                throw IllegalArgumentException("Tipo de columna no soportado para operaciones de filtro: ${column.columnType}")
            }
        }
    }

}