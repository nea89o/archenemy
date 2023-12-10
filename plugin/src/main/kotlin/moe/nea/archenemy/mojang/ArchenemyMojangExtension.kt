package moe.nea.archenemy.mojang

import moe.nea.archenemy.MCSide
import net.minecraftforge.artifactural.gradle.GradleRepositoryAdapter
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import java.io.File
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
        GradleRepositoryAdapter.add(
            project.repositories,
            "Minecraft Mapped Provider",
            getLocalCacheDirectory().resolve("minecraft-transformation-provider"),
            mappedRepositoryProvider
        )
        project.repositories.maven {
            it.name = "Minecraft Libraries"
            it.url = URI("https://libraries.minecraft.net/")
        }
    }

    private val mappedRepositoryProvider = MappedRepositoryProvider(this)


    fun officialMappings(version: String, side: MCSide): MappingDependency {
        _registerMinecraftProvider
        val dependency by lazy {
            project.dependencies.create(
                sharedExtension.minecraftProvider.getMappingsDependencyCoordinate(
                    MinecraftProvider.MinecraftCoordinate(
                        version,
                        side
                    )
                )
            )
        }
        return OfficialMappingDependency(side, version, project.providers.provider { dependency })
    }

    fun mapJar(
        dependency: ModuleDependency,
        mappings: MappingDependency,
        sourceNamespace: String,
        destinationNamespace: String
    ): Dependency {
        _registerMinecraftProvider
        return project.dependencies.create(
            mappedRepositoryProvider.getDependencyCoordiante(
                MappedRepositoryProvider.MappedCoordinates(
                    dependency, mappings, sourceNamespace, destinationNamespace
                )
            )
        )
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

    fun getLocalCacheDirectory(): File {
        return sharedExtension.getLocalCacheDirectory().resolve("projectspecific")
            .resolve(if (project == project.rootProject) "__root" else project.path.replace(":", "_"))
    }

}