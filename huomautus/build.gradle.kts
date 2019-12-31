plugins {
    kotlin("jvm")
}

group = "green.sailor.mc"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "http://maven.fabricmc.net/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // == DEPENDENCIES == //

    // Mixin itself, for annotations
    // implementation(group = "org.spongepowered", name = "mixin", version = "0.8-SNAPSHOT")
    implementation(group = "net.fabricmc", name = "sponge-mixin", version = "0.8+build.16")

    // Kotlin code generation
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.4.+")
    // Java code generation
    implementation(group = "com.squareup", name = "javapoet", version = "1.11.+")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
