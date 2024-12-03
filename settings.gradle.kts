pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Plugins
            version("kotlin", "2.0.20")
            version("agp", "8.5.0")
            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("kover", "org.jetbrains.kotlinx.kover").version("0.8.3")
            plugin("ksp", "com.google.devtools.ksp").version("2.0.20-1.0.24")
            plugin("maven", "com.vanniktech.maven.publish").version("0.28.0")
            plugin("android-library", "com.android.library").versionRef("agp")

            // Kotlinx
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            library("kotlinx-datetime", "org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            library("kotlin-js", "org.jetbrains.kotlin-wrappers:kotlin-js:1.0.0-pre.812")

            // Tests
            library("tests-mockk", "io.mockk:mockk:1.13.12")
            library("tests-h2", "com.h2database:h2:2.3.232")

            // Exposed
            version("exposed", "0.54.0")
            library("exposed-core", "org.jetbrains.exposed", "exposed-core").versionRef("exposed")
            library("exposed-jdbc", "org.jetbrains.exposed", "exposed-jdbc").versionRef("exposed")
            library("exposed-dao", "org.jetbrains.exposed", "exposed-dao").versionRef("exposed")
            library("exposed-kotlin-datetime", "org.jetbrains.exposed", "exposed-kotlin-datetime").versionRef("exposed")
            library("exposed-json", "org.jetbrains.exposed", "exposed-json").versionRef("exposed")
            library("exposed-money", "org.jetbrains.exposed", "exposed-money").versionRef("exposed")

            bundle(
                "exposed",
                listOf(
                    "exposed-core",
                    "exposed-jdbc",
                    "exposed-dao",
                    "exposed-kotlin-datetime",
                    "exposed-json",
                    "exposed-money"
                )
            )

            // Ktor
            version("ktor", "2.3.12")
            library("ktor-serialization-kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("ktor-server-core", "io.ktor", "ktor-server-core").versionRef("ktor")
            library("ktor-server-test-host", "io.ktor", "ktor-server-test-host").versionRef("ktor")
            library("ktor-server-content-negotiation", "io.ktor", "ktor-server-content-negotiation").versionRef("ktor")
            library("ktor-server-sessions", "io.ktor", "ktor-server-sessions").versionRef("ktor")
            library("ktor-server-auth-jwt", "io.ktor", "ktor-server-auth-jwt").versionRef("ktor")
            library("ktor-server-freemarker", "io.ktor", "ktor-server-freemarker").versionRef("ktor")
            library("ktor-server-websockets", "io.ktor", "ktor-server-websockets").versionRef("ktor")
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-client-auth", "io.ktor", "ktor-client-auth").versionRef("ktor")
            library("ktor-client-mock", "io.ktor", "ktor-client-mock").versionRef("ktor")

            bundle(
                "ktor-server-api",
                listOf(
                    "ktor-server-core",
                    "ktor-server-content-negotiation",
                    "ktor-server-sessions",
                    "ktor-server-auth-jwt",
                    "ktor-server-websockets",
                    "ktor-serialization-kotlinx-json"
                )
            )
            bundle(
                "ktor-server-tests",
                listOf(
                    "ktor-server-test-host",
                    "ktor-client-core",
                    "ktor-client-content-negotiation"
                )
            )
            bundle(
                "ktor-server-freemarker",
                listOf(
                    "ktor-server-core",
                    "ktor-server-freemarker"
                )
            )
            bundle(
                "ktor-client-api",
                listOf(
                    "ktor-client-core",
                    "ktor-client-content-negotiation",
                    "ktor-serialization-kotlinx-json",
                    "ktor-client-auth"
                )
            )
            bundle(
                "ktor-client-tests",
                listOf(
                    "ktor-client-mock",
                )
            )

            // Swagger/OpenAPI
            library("swagger-codegen", "io.swagger.codegen.v3:swagger-codegen:3.0.57")
            library("swagger-codegen-generators", "io.swagger.codegen.v3:swagger-codegen-generators:1.0.50")
            library("swagger-core", "io.swagger.core.v3:swagger-core:2.2.22")
            bundle(
                "swagger",
                listOf(
                    "swagger-codegen",
                    "swagger-codegen-generators",
                    "swagger-core"
                )
            )

            // Others
            library("bcrypt", "at.favre.lib:bcrypt:0.9.0")
            library("sentry", "io.sentry:sentry-kotlin-extensions:7.10.0")
        }
    }
}

rootProject.name = "kaccelero"
include(":core")
include(":controllers")
include(":routers")
include(":routers-ktor")
include(":routers-client-ktor")
include(":i18n")
include(":i18n-ktor")
include(":i18n-ktor-freemarker")
include(":database-exposed")
include(":database-ktor-sessions")
include(":auth")
include(":auth-apple")
include(":health-ktor")
include(":health-sentry-ktor")
include(":analytics")
include(":settings")
