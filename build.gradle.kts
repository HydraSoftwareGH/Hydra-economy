plugins {
    id("java")
}

group = "com.hydrasoftware"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

tasks.withType<JavaCompile> {
    options.source = "21"
    options.target = "21"
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }
}
