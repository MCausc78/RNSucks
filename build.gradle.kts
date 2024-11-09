import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        // Aliucords Maven repo which contains our tools and dependencies
        maven("https://maven.aliucord.com/snapshots")
        // Shitpack which still contains some Aliucord dependencies for now. TODO: Remove
        maven("https://jitpack.io")

        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        // Aliucord gradle plugin which makes everything work and builds plugins
        classpath("com.aliucord:gradle:main-SNAPSHOT")
        // Kotlin support. Remove if you want to use Java
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        // Shadow dependencies
        classpath("com.gradleup.shadow:shadow-gradle-plugin:8.3.5")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/snapshots")
    }
}

fun Project.aliucord(configuration: AliucordExtension.() -> Unit) =
    extensions.getByName<AliucordExtension>("aliucord").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")
    // Remove if using Java
    apply(plugin = "kotlin-android")

    // Fill out with your info
    aliucord {
        author("gatewaydisc.rdgg", 1073325901825187841L)
        updateUrl.set("https://raw.githubusercontent.com/MCausc78/RNSucks/builds/updater.json")
        buildUrl.set("https://raw.githubusercontent.com/MCausc78/RNSucks/builds/%s.zip")
    }

    android {
        compileSdkVersion(31)

        defaultConfig {
            minSdk = 24
            //noinspection ExpiredTargetSdkVersion
            targetSdk = 31
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11" // Required
                // Disables some unnecessary features
                freeCompilerArgs = freeCompilerArgs +
                        "-Xno-call-assertions" +
                        "-Xno-param-assertions" +
                        "-Xno-receiver-assertions"
            }
        }
    }

    dependencies {
        val discord by configurations
        val implementation by configurations

        // Stubs for all Discord classes
        discord("com.discord:discord:aliucord-SNAPSHOT")
        implementation("com.aliucord:Aliucord:main-SNAPSHOT")

        implementation("androidx.appcompat:appcompat:1.4.0")
        implementation("com.google.android.material:material:1.4.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

//task("deployWithAdb") {
//    group = "aliucord"
//    dependsOn("package")
//
//    val patchesPath = buildDir.resolve("patches.zip").absolutePath
//    val remotePatchesDir = "/storage/emulated/0/Android/data/com.aliucord.manager/cache/patches"
//
//    doLast {
//        val android = project(":Aliucord").extensions
//            .getByName<LibraryExtension>("android")
//
//        exec { commandLine(android.adbExecutable, "shell", "mkdir", "-p", remotePatchesDir) }
//        exec { commandLine(android.adbExecutable, "push", patchesPath, "$remotePatchesDir/$version.custom.zip") }
//    }
//}