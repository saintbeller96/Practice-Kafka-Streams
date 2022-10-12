import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	id("io.kotest.multiplatform") version "5.0.2"
}

allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")

	group = "com.saintbeller96"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}

	dependencies {
		implementation("org.apache.kafka:kafka-streams:3.3.1")
		implementation("org.slf4j:slf4j-api:2.0.3")
		implementation("org.slf4j:slf4j-simple:2.0.3")
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
		testImplementation("org.apache.kafka:kafka-streams-test-utils:3.3.1")
		testImplementation("io.kotest:kotest-runner-junit5:5.5.0")
		testImplementation("io.kotest:kotest-common:5.5.0")
		testImplementation("io.kotest:kotest-assertions-core:5.5.0")
		testImplementation("io.kotest:kotest-property:5.5.0")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict")
			jvmTarget = "17"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}


val springApplications = emptyList<Any?>()

configure(springApplications) {
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	dependencies {
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		implementation("org.springframework.boot:spring-boot-starter-webflux")
		implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
		implementation("org.springframework.kafka:spring-kafka")
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testImplementation("io.projectreactor:reactor-test")
		testImplementation("org.springframework.kafka:spring-kafka-test")
	}
}
