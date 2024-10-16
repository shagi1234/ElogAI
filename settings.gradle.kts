pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url  = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url  = uri("https://jitpack.io") }
        jcenter()
    }
}

rootProject.name = "ELogAI"
include(":app")
include(":EldBleLib-release")
 