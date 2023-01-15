import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "dev.lyzev"
version = "1.0.2"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "18"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.materialIconsExtended)
                // https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
                implementation("org.seleniumhq.selenium:selenium-java:4.7.2")
                // https://mvnrepository.com/artifact/com.google.code.gson/gson
                implementation("com.google.code.gson:gson:2.10.1")
                // https://bonigarcia.dev/webdrivermanager/
                implementation("io.github.bonigarcia:webdrivermanager:5.3.1")
                // https://mvnrepository.com/artifact/com.microsoft.alm/auth-secure-storage
                implementation("com.microsoft.alm:auth-secure-storage:0.6.4")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.lyzev.typewriterbot.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TypewriterBot"
            packageVersion = version.toString()
            description = "A bot for typewriter (https://at4.typewriter.at/) that will automatically log in and do levels."
            windows {
                iconFile.set(projectDir.resolve("src/jvmMain/resources/icon.ico"))
                menu = true
            }
            linux {
                iconFile.set(projectDir.resolve("src/jvmMain/resources/icon.png"))
            }
        }
    }
}
