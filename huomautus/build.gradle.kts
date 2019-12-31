plugins {
    kotlin("jvm")
}

group = "green.sailor.mc"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // == DEPENDENCIES == //

    // Mixin itself, for annotations
    compileOnly(group = "org.spongepowered", name = "mixin", version = "0.8-SNAPSHOT")

    // Kotlin code generation
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.4.+")
    // Java code generation
    implementation(group = "com.squareup", name = "javapoet", version = "1.11.+")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
