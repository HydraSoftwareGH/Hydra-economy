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
    compileOnly("net.dv8tion:JDA:5.3.0")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-source", "21", "-target", "21"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }
}
