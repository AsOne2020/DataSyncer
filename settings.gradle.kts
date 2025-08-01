plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "DataSyncer"

include(":core")

listOf(
    "v1_21", "v1_21_2", "v1_21_4", "v1_21_5", "v1_21_6"
).forEach {
    include(":$it")
    val proj = project(":$it")
    proj.projectDir = file("versions/$it")
    proj.buildFileName = "../nms.gradle.kts"
}