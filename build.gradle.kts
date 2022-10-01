import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}


allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")

	group = "com.saintbeller96"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}

	dependencies {
		implementation("org.apache.kafka:kafka-streams:3.2.3")
		implementation("org.slf4j:slf4j-api:2.0.1")
		implementation("org.slf4j:slf4j-simple:2.0.1")
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
