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
            getLocalCacheDirectory().resolve("minecraft-mapped-provider"),
            mappedRepositoryProvider
        )
        GradleRepositoryAdapter.add(
            project.repositories,
            "Minecraft Merged Provider",
            getLocalCacheDirectory().resolve("minecraft-merged-provider"),
            mergedRepositoryProvider
        )
        project.repositories.maven {
            it.name = "Minecraft Libraries"
            it.url = URI("https://libraries.minecraft.net/")
        }
    }

    private val mappedRepositoryProvider = MappedRepositoryProvider(this)
    private val mergedRepositoryProvider = MergedRepositoryProvider(this)

    fun yarnMappings(dependency: Dependency): MappingDependency {
        dependency as ModuleDependency
        return YarnMappingDependency(this, dependency)
    }

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

    fun intermediaryMappings(version: String): MappingDependency {
        return yarnMappings(project.dependencies.create("net.fabricmc:intermediary:$version:v2"))
    }

    fun mergeJar(
        base: Dependency,
        overlay: Dependency,
    ): Dependency {
        base as ModuleDependency
        overlay as ModuleDependency
        _registerMinecraftProvider
        return project.dependencies.create(
            mergedRepositoryProvider.getCoordinate(
                MergedRepositoryProvider.Coordinate(base, overlay)
            )
        )
    }

    fun mapJar(
        dependency: Dependency,
        mappings: MappingDependency,
        sourceNamespace: String,
        destinationNamespace: String
    ): Dependency {
        dependency as ModuleDependency
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