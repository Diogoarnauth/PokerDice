plugins {
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

    implementation(project(":modules:repository"))

    // for JDBI
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.4"))

    // For Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    api("org.springframework.security:spring-security-core:6.5.5")

    // para testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))

    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    testImplementation(kotlin("test"))
}

//falta meter coisas dos "testes" aqui

tasks.test { useJUnitPlatform() }
