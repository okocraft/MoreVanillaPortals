plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.15"
}

group = "net.okocraft.morevanillaportals"
version = "1.7"

val mcVersion = "1.20.4"
val fullVersion = "${version}-mc${mcVersion}"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("$mcVersion-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    reobfJar {
        outputJar.set(
            project.layout.buildDirectory
                .file("libs/MoreVanillaPortals-${fullVersion}.jar")
        )
    }

    build {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand("projectVersion" to version)
        }
    }
}
