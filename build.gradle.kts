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
    // Location of the Checkstyle configuration file
    configFile = file("${rootDir}/config/google-check.xml")
    // Ennable violation reporting in terminal
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2") // CI 환경에서 사용
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0") // springdoc-openapi
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    outputs.dir(snippetsDir)
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
    title = "My API"
    description = "My API description"
    version = "0.1.0"
    format = "yaml"
}
