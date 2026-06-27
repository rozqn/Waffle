pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

if (!file(".git").exists()) {
    val errorText = """

        =====================[ ERROR ]=====================
         The Waffle project directory is not a properly cloned Git repository.

         In order to build Waffle from source you must clone
         the Waffle repository using Git, not download a code
         zip from GitHub.

         Built Waffle jars are available for download from the
         GitHub releases at https://github.com/rozqn/Waffle/releases

         See CONTRIBUTING.md for further information on building
         and modifying Waffle.
        ===================================================
    """.trimIndent()
    error(errorText)
}

rootProject.name = "waffle"

for (name in listOf("waffle-api", "waffle-server")) {
    include(name)
    file(name).mkdirs()
}
