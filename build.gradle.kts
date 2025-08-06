buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.epages:restdocs-api-spec-gradle-plugin:0.18.2")
    }
}

plugins {
    java
    checkstyle
    id("com.diffplug.spotless") version "6.25.0"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.epages.restdocs-api-spec") version "0.18.2"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

checkstyle {
    toolVersion = "10.23.0"
    configFile = file("${rootDir}/config/google-check.xml")
    isShowViolations = true
}

spotless {
    java {
        googleJavaFormat("1.26.0")
        target("src/**/*.java")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }

dependencies {
    // WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // R2DBC
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.projectlombok:lombok:1.18.34")

    // R2DBC 드라이버
    runtimeOnly("io.asyncer:r2dbc-mysql:1.1.2")
    runtimeOnly("io.r2dbc:r2dbc-h2")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ========== 테스트 의존성 ==========
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // Reactive 테스트
    testImplementation("io.projectreactor:reactor-test")

    // WebFlux spring doc
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0")

    // REST Docs
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("com.epages:restdocs-api-spec-webtestclient:0.18.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    outputs.dir(snippetsDir)

    // R2DBC 테스트
    systemProperty("spring.r2dbc.url", "r2dbc:h2:mem:///testdb")
    systemProperty("spring.r2dbc.username", "sa")
    systemProperty("spring.r2dbc.password", "")
}

tasks.withType<org.asciidoctor.gradle.jvm.AsciidoctorTask> {
    inputs.dir(snippetsDir)
    dependsOn(tasks.withType<Test>())
    doFirst {
        delete(outputDir)
    }
}

tasks.bootJar {
    dependsOn(tasks.getByName<org.asciidoctor.gradle.jvm.AsciidoctorTask>("asciidoctor"))
    from("${tasks.getByName<org.asciidoctor.gradle.jvm.AsciidoctorTask>("asciidoctor").outputDir}/html5") {
        into("static/docs")
    }
}

openapi3 {
    title = "Hilite"
    description = "Reactive Spring Boot API with WebFlux and R2DBC"
    version = "0.1.0"
    format = "yaml"
}