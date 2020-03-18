group = "io.intrepid"
version = "2.0.0-alpha1"

val pluginId = "io.intrepid.static-analysis"

plugins {
    java
    kotlin("jvm") version "1.3.11"
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.9.7"
    `maven-publish`
    `java-gradle-plugin`
}

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.gradle.api.plugins:gradle-nexus-plugin:0.3")
    }
}

repositories {
    google()
    jcenter()
    mavenLocal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.3.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.11")
}

// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
gradlePlugin {
    plugins {
        create("staticAnalysis") {
            id = pluginId
            implementationClass = "io.intrepid.analysis.StaticAnalysis"
        }
    }
}

// Publishes to the public gradle plugins repo. ONLY RUN THIS WHEN RELEASING `./gradlew publishPlugins`
pluginBundle {
    website = "https://github.com/IntrepidPursuits/static-analysis-gradle-plugin"
    vcsUrl = "https://github.com/IntrepidPursuits/static-analysis-gradle-plugin"
    description = "Adds gradle tasks for running PMD and FindBugs, and includes a default configuration that is common to most Intrepid projects."
    tags = listOf("lint", "pmd", "findbugs", "analysis", "inspection", "android")

    (plugins) {
        create("staticAnalysis") {
            id = pluginId
            displayName = "Android Static Analysis Plugin"
        }
    }
}

// mainly for local publishing for development/testing. i.e `./gradlew publishToMavenLocal`
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            artifactId = project.name
            version = rootProject.version as String

            from(components["java"])
        }
    }
}
