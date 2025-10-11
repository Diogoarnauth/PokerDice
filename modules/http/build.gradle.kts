plugins {
    id("org.springframework.boot") version "3.3.4"
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

    api(project(":modules:services"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")


    implementation("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.security:spring-security-core:6.5.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // To use Spring MVC and the Servlet API
    implementation("org.springframework:spring-webmvc:6.2.10")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // To use SLF4J
    implementation("org.slf4j:slf4j-api:2.0.16")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}

tasks.test { useJUnitPlatform() }
