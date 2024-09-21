plugins {
    alias(libs.plugins.multiplatform) apply false
}

allprojects {
    group = "dev.kaccelero"
    version = "0.2.1"
    project.ext.set("url", "https://github.com/guimauvesoftware/kaccelero")
    project.ext.set("license.name", "GPL-3.0")
    project.ext.set("license.url", "https://opensource.org/licenses/GPL-3.0")
    project.ext.set("developer.id", "nathanfallet")
    project.ext.set("developer.name", "Nathan Fallet")
    project.ext.set("developer.email", "contact@nathanfallet.me")
    project.ext.set("developer.url", "https://www.nathanfallet.me")
    project.ext.set("scm.url", "https://github.com/guimauvesoftware/kaccelero.git")

    repositories {
        mavenCentral()
    }
}
