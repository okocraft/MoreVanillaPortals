plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
}

group = "net.okocraft.morevanillaportals"
version = "1.1"

val mcVersion = "1.18"
val fullVersion = "${version}-mc${mcVersion}"

repositories {
    mavenCentral()
}

dependencies {
    paperDevBundle("$mcVersion-R0.1-SNAPSHOT")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    reobfJar {
        outputJar.convention(
            project.layout.buildDirectory
                .file("libs/MoreVanillaPortals-${fullVersion}.jar")
        )
    }

    build {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}

bukkit {
    name = "MoreVanillaPortals"
    main = "net.okocraft.morevanillaportals.MoreVanillaPortalsPlugin"
    version = fullVersion
    apiVersion = "1.18"
    author = "Siroshun09"
}
