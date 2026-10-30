plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":api")) {
        isTransitive = false
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
