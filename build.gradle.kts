plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"
}

group = "com.tuum"
version = "0.0.1-SNAPSHOT"

val mybatisVersion = "3.0.3"
val mybatisPlusVersion = "3.5.7"
val springdocOpenApiVersion = "2.6.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    testCompileOnly {
        extendsFrom(configurations.testAnnotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.flywaydb:flyway-core")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisVersion")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:$mybatisPlusVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenApiVersion")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:$mybatisVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(":jacocoTestReport")
}

val skipJacoco: Boolean = false
val jacocoEnabled: Boolean = true
tasks.withType<JacocoReport> {
    isEnabled = jacocoEnabled
    if (skipJacoco) {
        enabled = false
    }
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

sonar {
    properties {
        property("sonar.projectKey", "ktenman_core-banking")
        property("sonar.organization", "ktenman")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.exclusions", "src/test/**")
    }
}
