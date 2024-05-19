import dev.architectury.pack200.java.Pack200Adapter
import org.apache.commons.lang3.SystemUtils

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    idea
    java
}

val baseGroup: String by project
val mcVersion: String by project
val modVersion: String by project
project.version = modVersion
val modID: String by project
val modName: String by project

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            property("asmhelper.verbose", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            arg("--mixin", "mixins.${modID}.json")
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(Pack200Adapter())
        mixinConfig("mixins.$modID.json")
    }
    mixin.defaultRefmapName.set("mixins.$modID.refmap.json")
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

val shadowMe: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowMe("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
    shadowMe("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24")
    shadowMe("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    withType(Jar::class) {
        archiveBaseName.set(modID)
        manifest.attributes(
            "FMLCorePluginContainsFMLMod" to true,
            "FMLCorePlugin" to "${modID}.forge.DianaLoadingPlugin",
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixins.$modID.json",
            "ModSide" to "CLIENT",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "TweakOrder" to "0"
        )
    }

    processResources {
        inputs.property("version", version)
        inputs.property("mcversion", mcVersion)
        inputs.property("modID", modID)
        inputs.property("modName", modName)

        filesMatching(listOf("mcmod.info", "mixins.$modID.json")) {
            expand(inputs.properties)
        }

        rename("(.+_at.cfg)", "META-INF/$1")
    }


    val remapJar by named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
        archiveClassifier.set("")
        from(shadowJar)
        input.set(shadowJar.get().archiveFile)
    }

    jar {
        archiveClassifier.set("without-deps")
        destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    }

    shadowJar {
        destinationDirectory.set(layout.buildDirectory.dir("badjars"))
        archiveClassifier.set("all-dev")
        configurations = listOf(shadowMe)
        doLast {
            configurations.forEach {
                println("Copying jars into mod: ${it.files}")
            }
        }

        fun relocate(name: String) = relocate(name, "$baseGroup.deps.$name")
    }

    assemble.get().dependsOn(remapJar)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))