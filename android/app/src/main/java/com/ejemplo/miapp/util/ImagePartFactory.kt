package com.ejemplo.miapp.util

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object ImagePartFactory {
    private const val MAX_SIZE_BYTES = 5L * 1024 * 1024

    fun fromUri(contentResolver: ContentResolver, uri: Uri): MultipartBody.Part {
        val declaredType = contentResolver.getType(uri)
        val extension = declaredType?.let {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
        } ?: uri.lastPathSegment
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }

        val mimeType = declaredType?.takeIf {
            it.startsWith("image/", ignoreCase = true)
        } ?: extension?.let {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
        }?.takeIf {
            it.startsWith("image/", ignoreCase = true)
        } ?: throw IllegalArgumentException("El archivo debe ser una imagen")

        val bytes = readBytes(contentResolver, uri)
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            name = "file",
            filename = "producto.${extension ?: "jpg"}",
            body = body
        )
    }

    private fun readBytes(contentResolver: ContentResolver, uri: Uri): ByteArray {
        val stream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        return stream.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var total = 0L
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                total += read
                if (total > MAX_SIZE_BYTES) {
                    throw IllegalArgumentException(
                        "La imagen supera el tamaño máximo de 5MB"
                    )
                }
                output.write(buffer, 0, read)
            }
            val bytes = output.toByteArray()
            if (bytes.isEmpty()) {
                throw IllegalArgumentException("El archivo está vacío")
            }
            bytes
        }
    }
}
