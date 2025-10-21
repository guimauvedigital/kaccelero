plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.maven) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = "dev.kaccelero"
    version = "0.6.7"
    project.ext.set("url", "https://github.com/guimauvedigital/kaccelero")
    project.ext.set("license.name", "Apache 2.0")
    project.ext.set("license.url", "https://www.apache.org/licenses/LICENSE-2.0.txt")
    project.ext.set("developer.id", "nathanfallet")
    project.ext.set("developer.name", "Nathan Fallet")
    project.ext.set("developer.email", "contact@nathanfallet.me")
    project.ext.set("developer.url", "https://www.nathanfallet.me")
    project.ext.set("scm.url", "https://github.com/guimauvedigital/kaccelero.git")
    project.ext.set("npm.publish.organization", "kaccelero")

    repositories {
        google()
        mavenCentral()
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
