plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "net.playexile"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "net.playexile.recap"

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
                }
            }
        }
    }

    addonInfo {
        namespace = "exilerecap"
        displayName = "EXILE Recap"
        author = "EXILE"
        description = "Match results and game statistics for the EXILE Minecraft network."
        minecraftVersion = "*"
        version = rootProject.version.toString()
    }
}

subprojects {
    if (name == "protocol-tests") {
        plugins.apply("java")
    } else {
        plugins.apply("net.labymod.labygradle")
        plugins.apply("net.labymod.labygradle.addon")
    }

    group = rootProject.group
    version = rootProject.version

    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
