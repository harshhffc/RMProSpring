import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	war
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "com.homefirstindia"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	runtimeOnly("com.mysql:mysql-connector-j")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

	implementation("org.json:json:20220924")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("commons-codec:commons-codec:1.15")
	implementation("commons-io:commons-io:2.11.0")
	implementation("com.squareup.okhttp3:okhttp:4.10.0")
	implementation("org.apache.tika:tika-core:2.6.0")
	implementation("org.apache.httpcomponents:httpclient:4.5.13")
	implementation("com.fasterxml.jackson.core:jackson-core:2.14.0")
	implementation("com.google.code.gson:gson:2.10")
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.454")
	implementation ("com.itextpdf:itext7-core:7.1.18")
	implementation ("com.itextpdf:html2pdf:3.0.0")
	implementation("org.apache.pdfbox:pdfbox:1.8.17")

	implementation("org.springframework.security:spring-security-jwt:1.1.1.RELEASE")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

	implementation ("com.google.maps:google-maps-services:0.15.0")
	implementation("com.opencsv:opencsv:4.0")
	implementation("org.springframework.boot:spring-boot-starter-mail:2.7.5")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
