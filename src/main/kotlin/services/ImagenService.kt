package org.example.services

import io.ktor.server.plugins.*
import java.io.File
import java.util.*

class ImagenService(private val uploadDirBase: String) {

    init {
        val baseDir = File(uploadDirBase)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
            println("Directorio de subida creado: ${baseDir.absolutePath}")
        }
    }

    fun saveImageAndGetRelativePath(
        fileBytes: ByteArray,
        originalFileName: String,
        mimeType: String?,
        tipoAsociacion: TipoEntidad
    ): String {
        validateFile(originalFileName, fileBytes, mimeType)
        val uniqueFileName = generateUniqueFileName(originalFileName)
        val subfolder = getSubfolderForEntityType(tipoAsociacion)
        val relativePath = buildRelativeImagePath(uniqueFileName, subfolder)
        val absolutePath = buildAbsoluteImagePath(relativePath)

        val targetFile = File(absolutePath)
        targetFile.parentFile?.mkdirs()

        targetFile.writeBytes(fileBytes)
        return relativePath
    }

    fun deleteImageByRelativePath(relativePath: String?): Boolean {
        if (relativePath.isNullOrBlank()) {
            return true
        }

        val normalizedPath = File(relativePath).normalize().toString()
        if (normalizedPath.contains("..") || normalizedPath.startsWith("/")) {
            throw IllegalArgumentException("Ruta de archivo inválida para eliminación: $relativePath")
        }

        val absolutePath = buildAbsoluteImagePath(normalizedPath)
        val fileToDelete = File(absolutePath)

        return if (fileToDelete.exists() && fileToDelete.isFile) {
            fileToDelete.delete()
        } else {
            true
        }
    }

    // Función para construir la ruta absoluta a partir de una ruta relativa
    private fun buildAbsoluteImagePath(relativePath: String): String {
        return File(uploadDirBase, relativePath).absolutePath
    }

    // Función para construir la ruta relativa a partir del nombre de archivo y la subcarpeta
    private fun buildRelativeImagePath(fileName: String, subfolder: String = ""): String {
        return if (subfolder.isNotEmpty()) {
            "$subfolder/$fileName"
        } else {
            fileName
        }
    }

    private fun validateFile(filename: String, fileBytes: ByteArray, mimeType: String?) {
        val extension = filename.substringAfterLast('.', "").lowercase()
        val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "webp")
        if (extension !in allowedExtensions) {
            throw BadRequestException("Tipo de archivo no permitido: $extension. Extensiones permitidas: ${allowedExtensions.joinToString(", ")}")
        }

        val maxSizeInBytes = 5 * 1024 * 1024L // 5 MB
        if (fileBytes.size > maxSizeInBytes) {
            throw BadRequestException("El archivo es demasiado grande. Máximo permitido: ${maxSizeInBytes / (1024 * 1024)} MB")
        }

        val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
        if (mimeType != null && mimeType !in allowedMimeTypes) {
            throw BadRequestException("Tipo MIME no permitido: $mimeType")
        }
    }

    private fun generateUniqueFileName(originalFilename: String): String {
        val extension = originalFilename.substringAfterLast('.', "")
        return "${UUID.randomUUID()}.$extension"
    }

    private fun getSubfolderForEntityType(tipo: TipoEntidad): String {
        return when (tipo) {
            TipoEntidad.USUARIO -> "usuarios"
            TipoEntidad.POST -> "posts"
            TipoEntidad.COMENTARIO -> "comentarios"
            TipoEntidad.SECCION -> "secciones"
            TipoEntidad.ARTICULO -> "articulos"
        }
    }
}

enum class TipoEntidad {
    USUARIO, POST, COMENTARIO, SECCION, ARTICULO
}