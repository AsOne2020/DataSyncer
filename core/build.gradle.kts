repositories {
    flatDir {
        dirs("libs")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly(files("libs/CMILib1.5.4.1.jar"))
    compileOnly(files("libs/Residence5.1.7.3.jar"))
    compileOnly("cn.lunadeer:DominionAPI:4.3")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "plugin_name" to project.properties["plugin_name"],
                "plugin_version" to project.properties["plugin_version"],
                "api_version" to project.properties["api_version"],
            )
        }
    }
}