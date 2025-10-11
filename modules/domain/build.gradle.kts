plugins {
   // id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"            // compatível com Spring Boot 3.3.x
    kotlin("plugin.spring") version "1.9.24"   // importante p/ anotações Spring
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // 17 ou 21
    }
}

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    api("org.springframework.security:spring-security-core:6.5.5")

    // para testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))


    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}

tasks.test { useJUnitPlatform() }

tasks.named<Jar>("jar") {
    enabled = true
}
