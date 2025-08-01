plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":core"))
    paperweight.paperDevBundle(property("paperweight_version").toString())
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

tasks.assemble {
    dependsOn(tasks.reobfJar)
}