import org.hypertrace.gradle.publishing.License.APACHE_2_0

plugins {
  id("org.hypertrace.publish-plugin") version "1.1.1"
  id("org.hypertrace.ci-utils-plugin") version "0.3.2"
  `java-gradle-plugin`
}

group = "org.hypertrace.gradle.dependency"

java {
  targetCompatibility = JavaVersion.VERSION_11
  sourceCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
  plugins {
    create("gradlePlugin") {
      id = "org.hypertrace.dependency-settings"
      implementationClass = "org.hypertrace.gradle.dependency.HypertraceDependencySettingsPlugin"
    }
  }
}


dependencies {
  api(gradleApi())
}

hypertracePublish {
  license.set(APACHE_2_0)
}
