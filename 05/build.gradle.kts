plugins {
    `java-library`
    application
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api("org.postgresql:postgresql:42.7.3")
    api("com.yugabyte:jdbc-yugabytedb:42.3.3")
    api("org.eclipse.jetty:jetty-servlet:11.0.21")
    api("jakarta.servlet:jakarta.servlet-api:6.1.0-M2")
    api("ca.uhn.hapi.fhir:hapi-fhir-base:7.2.0")
    api("ca.uhn.hapi.fhir:hapi-fhir-server:7.2.0")
    api("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:7.2.0")
    api("org.slf4j:slf4j-simple:2.1.0-alpha1")
    api("org.eclipse.jetty:jetty-server:11.0.21")

    testImplementation("junit:junit:4.11")

    // Lombok
    compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
}

application {
    mainClass.set("com.medblocks.App")
}

group = "com.medblocks"
version = "1.0-SNAPSHOT"
description = "bootcamp-fhir-facade"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
