package moe.nea.archenemy.mojang

import moe.nea.archenemy.util.readZipFs
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ExtractNativesTask : DefaultTask() {

	@get:OutputDirectory
	abstract val nativeDirectory: DirectoryProperty

	@get:Input
	abstract val natives: ListProperty<MojangVersionMetadata.Library>

	@Internal
	fun getNativeDirectoryPath(): File {
		return nativeDirectory.get().asFile
	}

	@TaskAction
	fun extractNatives() {
		val nativeDirectory = nativeDirectory.get().asFile
		natives.get().forEach {
			val extract = it.extract ?: return@forEach
			val file = project.configurations
				.detachedConfiguration(project.dependencies.create(it.getArtifactCoordinate()))
				.files
				.single().toPath()
			file.readZipFs { path, inputStream ->
				if (extract.exclude.any { path.startsWith(it) }) return@readZipFs
				val targetFile = nativeDirectory.resolve(path)
				targetFile.parentFile.mkdirs()
				targetFile.outputStream().use { output ->
					inputStream.copyTo(output)
				}
			}
		}
	}
}