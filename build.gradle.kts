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

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // R2DBC
    // implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // WebFlux 의존성
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // 데이터베이스 드라이버
    runtimeOnly("com.mysql:mysql-connector-j")      // MySQL JDBC 드라이버 (JPA용)
    //runtimeOnly("io.asyncer:r2dbc-mysql:1.1.2")     // MySQL R2DBC 드라이버 (WebFlux용)
     runtimeOnly("com.h2database:h2")                // H2 JDBC 드라이버
    //runtimeOnly("io.r2dbc:r2dbc-h2") // H2 R2DBC 드라이버

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    implementation("org.springframework.boot:spring-boot-starter-security")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // open api
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0") // springdoc-openapi
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("com.epages:restdocs-api-spec-webtestclient:0.18.2")
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
