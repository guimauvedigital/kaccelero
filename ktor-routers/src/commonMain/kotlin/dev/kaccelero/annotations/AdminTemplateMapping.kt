package dev.kaccelero.annotations

@Target(AnnotationTarget.FUNCTION)
annotation class AdminTemplateMapping(
    val template: String = "",
)
