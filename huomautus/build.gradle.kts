plugins {
    kotlin("jvm")
}

group = "green.sailor.mc"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // == DEPENDENCIES == //

    // Kotlin code generation
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.4.+")
    // Java code generation
    implementation(group = "com.squareup", name = "javapoet", version = "1.11.+")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }
}
