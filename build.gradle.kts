import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    `maven-publish`
    `java-library`
    signing
}

group = "konichiva.geom"
version = "0.8"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "geom-konichiva-interpreter"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("Geometry Checker")
                description.set("Interpreter and DSL for geometry tasks")
                url.set("https://github.com/konichiva-geom/GeometryChecker")
                properties.set(mapOf(
                    "myProp" to "value",
                    "prop.with.dots" to "anotherValue"
                ))
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("llesha")
                        name.set("Alexey Kononov")
                        email.set("kononal@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/konichiva-geom/GeometryChecker.git")
                    developerConnection.set("scm:git:ssh://github.com/konichiva-geom/GeometryChecker.git")
                    url.set("https://github.com/konichiva-geom/GeometryChecker")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}


signing {
    sign(publishing.publications["mavenJava"])
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation(kotlin("reflect"))
    implementation("org.postgresql:postgresql:42.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}