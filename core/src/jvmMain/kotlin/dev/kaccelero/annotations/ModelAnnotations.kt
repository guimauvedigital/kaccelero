package dev.kaccelero.annotations

import dev.kaccelero.extensions.toUUIDOrNull
import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.UUID
import kotlinx.datetime.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

object ModelAnnotations {

    private inline fun <reified O : Annotation> getAnnotatedMembersSorted(kClass: KClass<*>): List<Pair<KCallable<*>, O>> =
        kClass.members.mapNotNull { member ->
            val annotation = member.annotations.firstNotNullOfOrNull {
                it as? O
            } ?: return@mapNotNull null
            Pair(member, annotation)
        }.sortedBy { (member, _) ->
            kClass.constructors.firstOrNull {
                it.parameters.any { parameter ->
                    parameter.name == member.name
                }
            }?.parameters?.indexOfFirst { it.name == member.name }
        }

    @JvmStatic
    fun <Model : IChildModel<*, *, *, *>> modelKeys(modelClass: KClass<Model>): List<ModelKey> =
        getAnnotatedMembersSorted<ModelProperty>(modelClass).map { (member, annotation) ->
            ModelKey(member.name, annotation.type, annotation.style)
        }

    @JvmStatic
    fun <Model : IChildModel<*, *, UpdatePayload, *>, UpdatePayload : Any> updatePayloadKeys(
        modelClass: KClass<Model>,
        updatePayloadClass: KClass<UpdatePayload>,
    ): List<PayloadKey> =
        getAnnotatedMembersSorted<ModelProperty>(modelClass).mapNotNull { (member, a) ->
            val annotation = a.takeIf { it.visibleOnUpdate } ?: return@mapNotNull null
            PayloadKey(member.name, annotation.type, annotation.style, false)
        } + getAnnotatedMembersSorted<PayloadProperty>(updatePayloadClass).map { (member, annotation) ->
            PayloadKey(member.name, annotation.type, annotation.style, true)
        }

    @JvmStatic
    fun <Model : IChildModel<*, CreatePayload, *, *>, CreatePayload : Any> createPayloadKeys(
        modelClass: KClass<Model>,
        createPayloadClass: KClass<CreatePayload>,
    ): List<PayloadKey> =
        getAnnotatedMembersSorted<PayloadProperty>(createPayloadClass).map { (member, annotation) ->
            PayloadKey(member.name, annotation.type, annotation.style, true)
        }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <Output : Any> constructPrimitiveFromString(type: KType, value: String?): Output? =
        when (type) {
            typeOf<Byte>(), typeOf<Byte?>() -> value?.toByteOrNull()
            typeOf<UByte>(), typeOf<UByte?>() -> value?.toUByteOrNull()
            typeOf<Short>(), typeOf<Short?>() -> value?.toShortOrNull()
            typeOf<UShort>(), typeOf<UShort?>() -> value?.toUShortOrNull()
            typeOf<Int>(), typeOf<Int?>() -> value?.toIntOrNull()
            typeOf<UInt>(), typeOf<UInt?>() -> value?.toUIntOrNull()
            typeOf<Long>(), typeOf<Long?>() -> value?.toLongOrNull()
            typeOf<ULong>(), typeOf<ULong?>() -> value?.toULongOrNull()
            typeOf<Char>(), typeOf<Char?>() -> value?.singleOrNull()
            typeOf<Float>(), typeOf<Float?>() -> value?.toFloatOrNull()
            typeOf<Double>(), typeOf<Double?>() -> value?.toDoubleOrNull()
            typeOf<Boolean>() -> !listOf("false", null).contains(value)
            typeOf<Boolean?>() -> value?.let { b -> b != "false" }
            typeOf<String>(), typeOf<String?>() -> value
            typeOf<UUID>(), typeOf<UUID?>() -> value?.toUUIDOrNull()
            typeOf<Instant>(), typeOf<Instant?>() -> value?.let(Instant::parse)
            typeOf<LocalDateTime>(), typeOf<LocalDateTime?>() -> value?.let(LocalDateTime::parse)
            typeOf<LocalDate>(), typeOf<LocalDate?>() -> value?.let(LocalDate::parse)
            typeOf<LocalTime>(), typeOf<LocalTime?>() -> value?.let(LocalTime::parse)
            typeOf<Month>(), typeOf<Month?>() -> value?.let { Month.valueOf(it) }
            typeOf<DayOfWeek>(), typeOf<DayOfWeek?>() -> value?.let { DayOfWeek.valueOf(it) }
            else -> null
        } as? Output?

    @JvmStatic
    fun <Output : Any> constructPayloadFromStringLists(
        type: KClass<Output>,
        stringValues: Map<String, List<String>>,
    ): Output? {
        val constructor = type.constructors.firstOrNull {
            it.parameters.all { parameter ->
                parameter.name in stringValues.keys
                        || parameter.isOptional
                        || parameter.type.isSubtypeOf(typeOf<Boolean>())
            }
        } ?: return null
        val params = constructor.parameters.associateWith {
            it.name?.let { name ->
                it.type.arguments.firstOrNull()?.type?.takeIf { _ ->
                    it.type.isSubtypeOf(typeOf<List<*>>())
                }?.let { subtype ->
                    stringValues[name]?.map { value ->
                        constructPrimitiveFromString<Any>(subtype, value)
                    }
                } ?: constructPrimitiveFromString<Any>(it.type, stringValues[name]?.singleOrNull())
            }
        }
        return constructor.callBy(params)
    }

    @JvmStatic
    fun <Payload : Any> constructPayloadFromStrings(
        type: KClass<Payload>,
        stringValues: Map<String, String>,
    ): Payload? = constructPayloadFromStringLists(type, stringValues.mapValues { listOf(it.value) })

    @JvmStatic
    fun <Model : IChildModel<Id, *, *, *>, Id> constructIdFromString(modelClass: KClass<Model>, id: String): Id {
        val idType = modelClass.members.first { it.name == "id" }.returnType
        return constructPrimitiveFromString(idType, id)
            ?: throw IllegalArgumentException("Unsupported id type: $idType")
    }

    @JvmStatic
    fun <Payload : Any> validatePayload(payload: Payload, type: KClass<Payload>) =
        type.declaredMemberProperties.forEach { member ->
            val value = member.call(payload) ?: return@forEach
            member.annotations.forEach { annotation ->
                PropertyValidator.validate(member.name, value, annotation)
            }
        }

}
