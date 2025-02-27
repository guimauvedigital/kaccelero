plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven)
    alias(libs.plugins.npm)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("core")
        description.set("Core of kaccelero.")
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
    watchosDeviceArm64()

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
        useEsModules()
        generateTypeScriptDefinitions()
        binaries.library()
        nodejs()
        browser()
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }
        val nativeMain by getting {}
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.tests.mockk)
            }
        }
        val desktopAndJsMain by creating {
            dependsOn(commonMain)
        }
        val jsMain by getting {
            dependsOn(desktopAndJsMain)
            dependencies {
                api(libs.kotlin.js)
            }
        }
        val desktopMain by creating {
            dependsOn(nativeMain)
            dependsOn(desktopAndJsMain)
        }
        val mingwMain by getting {
            dependsOn(desktopMain)
        }
        val linuxMain by getting {
            dependsOn(desktopMain)
        }
    }
}

npmPublish {
    readme.set(file("README.md"))
    registries {
        register("npmjs") {
            uri.set("https://registry.npmjs.org")
        }
    }
}
