package moe.nea.archenemy.util

import java.io.InputStream
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

// TODO: Figure out license shit with unimined cause i copied some of these utils?
fun Path.zipFs(): FileSystem {
    if (!exists()) {
        parent.createDirectories()
        ZipOutputStream(this.outputStream()).use { }
    }
    return FileSystems.newFileSystem(URI.create("jar:${toUri()}"), mapOf("create" to true), null)
}

fun Path.readZipFs(reader: (String, InputStream) -> Unit) {
    ZipInputStream(this.inputStream()).use { stream ->
        for (entry in generateSequence { stream.nextEntry }) {
            if (entry.isDirectory) {
                continue
            }
            reader(entry.name, stream)
        }
    }
}
