package org.example.plugins



import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
object InstantIso8601Serializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // Convierte Instant a String en formato ISO
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString()) // Parsea String a Instant
    }
}
@OptIn(ExperimentalTime::class)
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
                serializersModule = SerializersModule {
                    contextual(Instant::class, InstantIso8601Serializer) // Registra Instant
                    // contextual(LocalDateTime::class, LocalDateTimeIso8601Serializer) // Registra otros si los usas
                    // Puedes registrar serializadores para otros tipos aquí si es necesario
                }
            }
        )
    }
}
