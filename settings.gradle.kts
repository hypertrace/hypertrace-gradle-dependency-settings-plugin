pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    maven {
      url = uri("https://hypertrace.jfrog.io/artifactory/maven")
    }
  }
}

plugins {
  id("org.hypertrace.version-settings") version "0.2.1"
}

rootProject.name = "hypertrace-gradle-dependency-settings-plugin"
