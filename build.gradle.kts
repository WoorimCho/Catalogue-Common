plugins {
    `java-library`
}

group = "com.example"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Versions come from the Spring Boot BOM so they line up with the two
    // catalogue services that consume this module (both on Boot 4.1.1).
    val bootBom = "org.springframework.boot:spring-boot-dependencies:4.1.1"
    api(platform(bootBom))
    compileOnly(platform(bootBom))
    annotationProcessor(platform(bootBom))
    testImplementation(platform(bootBom))

    // --- API surface (leaks into consumers' compile classpath) ---
    api("org.springframework.data:spring-data-jpa")           // Tag, TagRepository, Page/Pageable
    api("org.springframework:spring-web")                     // ProblemDetail, @RestControllerAdvice
    api("org.springframework:spring-context")                 // @Service, @Configuration, @Component
    api("org.springframework:spring-tx")                      // @Transactional
    api("jakarta.persistence:jakarta.persistence-api")        // @Entity on Tag
    api("jakarta.validation:jakarta.validation-api")          // constraints on the request records
    api("io.zipkin.reporter2:zipkin-reporter")                // BytesMessageSender
    api("io.zipkin.reporter2:zipkin-sender-urlconnection")    // URLConnectionSender

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
