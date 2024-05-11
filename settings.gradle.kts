pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://github.com/jitsi/jitsi-maven-repository/raw/master/releases")
        }
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" ) }
        maven { url = uri("https://storage.zego.im/maven") }
    }
}

rootProject.name = "meno"
include(":app")
 