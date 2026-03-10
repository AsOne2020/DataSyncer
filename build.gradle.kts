import xyz.jpenilla.runpaper.task.RunServer
import java.util.Calendar

plugins {

    `java-library`

    id("com.gradleup.shadow") version "9.0.0-beta16"

    // https://github.com/jpenilla/run-task
    id("xyz.jpenilla.run-paper") version "3.0.2"

    id("com.github.hierynomus.license") version "0.16.1"

    // https://github.com/PaperMC/paperweight
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" apply false
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
    api(project(":v1_21_11", io.papermc.paperweight.util.constants.REOBF_CONFIG))
}

runPaper.folia.registerTask()

val mcVersion = "1.21.11"
val jvavVersion = JavaLanguageVersion.of(21)
val jvmArgsExternal = listOf(
    "-Dcom.mojang.eula.agree=true",
    "-XX:+AllowEnhancedClassRedefinition",
    "-D${project.properties["plugin_name"]}.DEV_MODE=true"
)

val paperPlugins = runPaper.downloadPluginsSpec {

    //https://www.zrips.net/CMILib
    url("https://www.zrips.net/CMILib/CMILib1.5.8.7.jar")

    // https://zrips.net/Residence/
    url("https://zrips.net/Residence/files/Residence6.0.1.6.jar")

    // https://github.com/LunaDeerMC/Dominion/releases
    github("LunaDeerMC", "Dominion", "v4.7.5-release", "Dominion-4.7.5-release-full.jar")

    // https://ci.lucko.me/job/LuckPerms-Folia/lastBuild/
    url("https://ci.lucko.me/job/LuckPerms-Folia/lastBuild/artifact/bukkit/loader/build/libs/LuckPerms-Bukkit-5.5.29.jar")

    // https://github.com/Test-Account666/PlugManX
    github("Test-Account666", "PlugManX", "v3.0.4-dev1", "PlugManX-3.0.4-dev1.jar")

    // https://github.com/dmulloy2/ProtocolLib/releases/tag/dev-build
    github("dmulloy2", "ProtocolLib", "dev-build", "ProtocolLib.jar")

    // https://modrinth.com/plugin/viaversion/versions
    modrinth("ViaVersion", "5.7.3-SNAPSHOT+927")

    // https://modrinth.com/plugin/viabackwards/versions
    modrinth("ViaBackwards", "5.7.2")
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