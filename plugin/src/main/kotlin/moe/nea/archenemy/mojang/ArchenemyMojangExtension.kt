package moe.nea.archenemy.mojang

import moe.nea.archenemy.MCSide
import net.minecraftforge.artifactural.gradle.GradleRepositoryAdapter
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.net.URI

abstract class ArchenemyMojangExtension(val project: Project) {
    val sharedExtension = project.rootProject.extensions.getByType(ArchenemySharedExtension::class.java)

    private val _registerMinecraftProvider by lazy {
        GradleRepositoryAdapter.add(
            project.repositories,
            "Minecraft Provider",
            sharedExtension.getLocalCacheDirectory().resolve("minecraft-provider"),
            sharedExtension.minecraftProvider
        )
        project.repositories.maven {
            it.name = "Minecraft Libraries"
            it.url = URI("https://libraries.minecraft.net/")
        }
    }


    fun minecraft(version: String, side: MCSide): Dependency {
        _registerMinecraftProvider
        return project.dependencies.create(
            sharedExtension.minecraftProvider.getDependencyCoordinate(
                MinecraftProvider.MinecraftCoordinate(
                    version,
                    side,
                )
            )
        )
    }

}