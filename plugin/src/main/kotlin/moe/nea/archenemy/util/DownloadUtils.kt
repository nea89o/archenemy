package moe.nea.archenemy.util

import java.io.File
import java.io.IOException
import java.net.URL
import java.security.MessageDigest

object DownloadUtils {
    fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }


    fun sha1Hash(file: File): String {
        if (!file.exists()) return ""
        val messageDigest = MessageDigest.getInstance("SHA-1")

        file.inputStream().use {
            val r = ByteArray(4096)
            while (true) {
                val d = it.read(r)
                if (d < 0) break
                messageDigest.update(r, 0, d)
            }
        }
        return bytesToHex(messageDigest.digest())
    }

    fun areHashesEqual(a: String, b: String): Boolean {
        return b.equals(a, ignoreCase = true)
    }

    fun downloadFile(source: URL, sha1: String, targetFile: File) {
        targetFile.parentFile.mkdirs()
        if (areHashesEqual(sha1Hash(targetFile), sha1)) return
        source.openStream().use { inp ->
            targetFile.outputStream().use { out ->
                inp.copyTo(out)
            }
        }
        if (!areHashesEqual(sha1Hash(targetFile), sha1))
            throw IOException("$targetFile should hash to $sha1, but does not")
    }
}