plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("ktor-routers-client")
        description.set("Client for APIs using ktor-routers.")
        url.set(project.ext.get("url")?.toString())
        licenses {
            license {
                name.set(project.ext.get("license.name")?.toString())
                url.set(project.ext.get("license.url")?.toString())
            }
        }
        developers {
            developer {
                id.set(project.ext.get("developer.id")?.toString())
                name.set(project.ext.get("developer.name")?.toString())
                email.set(project.ext.get("developer.email")?.toString())
                url.set(project.ext.get("developer.url")?.toString())
            }
        }
        scm {
            url.set(project.ext.get("scm.url")?.toString())
        }
    }
}

kotlin {
    // Tiers are in accordance with <https://kotlinlang.org/docs/native-target-support.html>
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier 3
    mingwX64()
    //watchosDeviceArm64() // Not supported by ktor

    // jvm & js
    jvmToolchain(21)
    jvm {
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        binaries.library()
        nodejs()
        browser()
        //generateTypeScriptDefinitions() // Not supported for now because of collections etc...
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                api(project(":core"))
                api(libs.bundles.ktor.client.api)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.bundles.ktor.client.tests)
            }
        }
    }
}
