pluginManagement {
    repositories {
        jcenter()
        maven {
            name = "Fabric"
            url = java.net.URI("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "mc-annotations"
include("huomautus")
include("testmod")
