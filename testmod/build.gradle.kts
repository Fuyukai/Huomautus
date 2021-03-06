plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("fabric-loom") version("0.2.6-SNAPSHOT")
}

group = "green.sailor.mc"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(
        url = "http://maven.fabricmc.net/"
    )
}


// Required!
kapt {
    annotationProcessor("green.sailor.mc.huomautus.Processor")
    arguments {
        arg("sailor.huomautus.package", "green.sailor.mc.testmod.generated")
        arg("sailor.huomautus.prefix", "TestMod")
    }
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
        }
    }
}

dependencies {
    // Standard fabric stuff
    minecraft("com.mojang:minecraft:1.15.1")
    mappings("net.fabricmc:yarn:1.15.1+build.17:v2")
    modCompile("net.fabricmc:fabric-loader:0.7.3+build.176")
    modCompile("net.fabricmc.fabric-api:fabric-api:0.4.26+build.283-1.15")

    modImplementation(
        group = "net.fabricmc",
        name = "fabric-language-kotlin",
        version = "1.3.61+build.1"
    )

    implementation(kotlin("stdlib-jdk8"))

    // Includes annotations and enables annotation processing
    compileOnly(project(":huomautus"))
    kapt(project(":huomautus"))
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
