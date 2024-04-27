plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.6.0"
}

group = "net.okocraft.morevanillaportals"
version = "1.7"

val mcVersion = "1.20.5"
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

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
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

    jar {
        archiveFileName = "MoreVanillaPortals-${fullVersion}.jar"
    }
}
