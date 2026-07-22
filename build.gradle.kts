import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("kapt") version "2.3.21"
    kotlin("plugin.allopen") version "2.3.21"
    id("com.google.devtools.ksp") version "2.3.10"
    id("io.micronaut.application") version "4.6.2"
    id("io.micronaut.aot") version "4.6.2"
    id("com.gradleup.shadow") version "9.6.1"
    id("com.google.protobuf") version "0.10.0"
}

version = "0.1"
group = "net.trueog.staffauth"

val kotlinVersion = project.property("kotlinVersion")

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.security:micronaut-security-annotations")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    ksp("io.micronaut.validation:micronaut-validation-processor")
    implementation("com.google.protobuf:protobuf-kotlin:4.35.1")
    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.grpc:grpc-kotlin-stub:1.5.0")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    implementation("io.micronaut.r2dbc:micronaut-r2dbc-core")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.security:micronaut-security-oauth2")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("org.springframework.security:spring-security-crypto:7.1.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.84")
    implementation("org.slf4j:jcl-over-slf4j")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("dev.samstevens.totp:totp:1.7.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.4")
    implementation("sh.ory.hydra:hydra-client:26.2.0")
    implementation("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:12.10.0")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut:micronaut-http-client")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
}

application {
    mainClass = "net.trueog.staffauth.ApplicationKt"
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

graalvmNative.toolchainDetection = false

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.35.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.82.2"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.5.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("net.trueog.staffauth.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }

}








