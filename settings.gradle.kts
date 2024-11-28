pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
buildCache {
    local {
        directory = file("${rootDir}/build-cache")
    }
}

rootProject.name = "mumu-intellij-plugin"
