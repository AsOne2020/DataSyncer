import xyz.jpenilla.runpaper.task.RunServer
import java.util.Calendar

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-beta16"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.hierynomus.license") version "0.16.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
}

group = project.properties["maven_group"]!!
version = "v${project.properties["plugin_version"]}-Paper"

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.hierynomus.license")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io/")
        maven("https://oss.sonatype.org/content/groups/public/")

        //ProtocolLib
        maven("https://repo.dmulloy2.net/repository/public/")
    }

    dependencies {
        implementation("io.papermc:paperlib:1.0.8")
    }

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
            options.release = 21
        }
        javadoc {
            options.encoding = "UTF-8"
        }
    }

    license {
        // use "gradle licenseFormat" to apply license headers
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
        skipExistingHeaders = true

        headerDefinitions.create("SLASHSTAR_STYLE_NEWLINE") {
            firstLine = "/*"
            beforeEachLine = " * "
            endLine = " */" + System.lineSeparator()
            afterEachLine = ""
            skipLinePattern = null
            firstLineDetectionPattern = """(\s|\t)*/\*.*$"""
            lastLineDetectionPattern = """.*\*/(\s|\t)*$"""
            allowBlankLines = false
            isMultiline = true
            padLines = false
        }
        mapping("java", "SLASHSTAR_STYLE_NEWLINE")

        ext["name"] = project.properties["plugin_name"]
        ext["author"] = project.properties["author"]
        ext["year"] = Calendar.getInstance().get(Calendar.YEAR).toString()
    }
}

dependencies {
    api(project(":core"))
    api(project(":v1_21", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_4", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_5", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_6", io.papermc.paperweight.util.constants.REOBF_CONFIG))
}

runPaper.folia.registerTask()

val mcVersion = "1.21.6"
val jvavVersion = JavaLanguageVersion.of(21)
val jvmArgsExternal = listOf(
    "-Dcom.mojang.eula.agree=true",
    "-XX:+AllowEnhancedClassRedefinition",
    "-D${project.properties["plugin_name"]}.DEV_MODE=true"
)

val paperPlugins = runPaper.downloadPluginsSpec {
    url("https://www.zrips.net/CMILib/CMILib1.5.6.0.jar")
    url("https://zrips.net/Residence/files/Residence5.1.7.7.jar")
    github("LunaDeerMC", "Dominion", "v4.5.0-beta", "Dominion-4.5.0-beta-full.jar")
    url("https://ci.lucko.me/job/LuckPerms-Folia/9/artifact/bukkit/loader/build/libs/LuckPerms-Bukkit-5.5.11.jar")
    github("Test-Account666", "PlugManX", "2.4.1", "PlugManX-2.4.1.jar")
    url("https://ci.dmulloy2.net/job/ProtocolLib/755/artifact/build/libs/ProtocolLib.jar")
    modrinth("ViaVersion", "5.5.0-SNAPSHOT+793")
    modrinth("ViaBackwards", "5.4.3-SNAPSHOT+467")
}

tasks {
    withType<RunServer> {
        val isFolia = gradle.startParameter.taskNames.any { it.contains("Folia", ignoreCase = true) }
        minecraftVersion(mcVersion)
        runDirectory = rootDir.resolve("run/${if (isFolia) "folia" else "paper"}/$mcVersion")
        downloadPlugins.from(paperPlugins)
        jvmArgs = jvmArgsExternal
        javaLauncher = project.javaToolchains.launcherFor {
            @Suppress("UnstableApiUsage")
            vendor = JvmVendorSpec.JETBRAINS
            languageVersion = jvavVersion
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "${project.properties["maven_group"]}.bstats")
        minimize()
        manifest {
            attributes["paperweight-mappings-namespace"] = io.papermc.paperweight.util.constants.SPIGOT_NAMESPACE
        }
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${project.properties["plugin_name"]}" }
        }
    }

    build {
        dependsOn(shadowJar)
    }
}