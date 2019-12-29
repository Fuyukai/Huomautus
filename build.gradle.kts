plugins {
    kotlin("jvm") version "1.3.61"
    id("com.diffplug.gradle.spotless") version "3.26.0"
}


group = "green.sailor.mc"
version = "0.1.0"

allprojects {
    apply(plugin = "com.diffplug.gradle.spotless")

    spotless {
        kotlin {
            targetExclude("build/generated/**")
            ktlint().userData(
                mapOf(
                    "disabled_rules" to "no-wildcard-imports",
                    "max_line_length" to "100"
                )
            )
            @Suppress("INACCESSIBLE_TYPE")  // this works fine?
            licenseHeaderFile("$rootDir/gradle/LICENCE-HEADER")

        }
    }
}
