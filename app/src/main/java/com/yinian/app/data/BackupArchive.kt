package com.yinian.app.data

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** What a backup bundle contains, before it is turned back into models. */
data class BackupPayload(
    val ideas: List<Idea>,
    val groups: List<Group>,
    val appearance: Appearance,
    /** file name (no directory) -> bytes, everything under files/media. */
    val media: Map<String, ByteArray>,
)

/**
 * One zip holds the three JSON documents plus every image the notes point at.
 * The manifest is written last-ish but read first: if it is missing or newer
 * than this build understands, the caller must refuse instead of guessing.
 */
object BackupArchive {

    private const val MANIFEST = "manifest.json"
    private const val IDEAS = "ideas.json"
    private const val GROUPS = "groups.json"
    private const val APPEARANCE = "appearance.json"
    private const val MEDIA_PREFIX = "media/"
    private const val FORMAT = 1

    fun write(
        context: Context,
        ideas: List<Idea>,
        groups: List<Group>,
        appearance: Appearance,
        out: OutputStream,
    ) {
        val mediaDir = File(context.filesDir, "media")
        val files = mediaDir.listFiles()?.filter { it.isFile } ?: emptyList()

        val manifest = JSONObject().apply {
            put("format", FORMAT)
            put("exportedAt", System.currentTimeMillis())
            put("ideas", ideas.size)
            put("groups", groups.size)
            put("media", files.size)
        }

        ZipOutputStream(out).use { zip ->
            zip.setLevel(6)
            addText(zip, MANIFEST, manifest.toString())
            addText(zip, IDEAS, JournalJson.encodeIdeas(ideas))
            addText(zip, GROUPS, JournalJson.encodeGroups(groups))
            addText(zip, APPEARANCE, JournalJson.encodeAppearance(appearance))
            // Images are already compressed; storing them keeps export fast.
            files.forEach { file ->
                val entry = ZipEntry(MEDIA_PREFIX + file.name).apply {
                    method = ZipEntry.STORED
                    size = file.length()
                    compressedSize = file.length()
                    crc = crcOf(file)
                }
                zip.putNextEntry(entry)
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /** Throws [IllegalStateException] with a user-facing message when unusable. */
    fun read(input: InputStream): BackupPayload {
        var manifest: JSONObject? = null
        var ideasRaw: String? = null
        var groupsRaw: String? = null
        var appearanceRaw: String? = null
        val media = LinkedHashMap<String, ByteArray>()

        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                when {
                    entry.name == MANIFEST -> manifest = JSONObject(zip.readBytes().decodeToString())
                    entry.name == IDEAS -> ideasRaw = zip.readBytes().decodeToString()
                    entry.name == GROUPS -> groupsRaw = zip.readBytes().decodeToString()
                    entry.name == APPEARANCE -> appearanceRaw = zip.readBytes().decodeToString()
                    entry.name.startsWith(MEDIA_PREFIX) -> {
                        val name = entry.name.removePrefix(MEDIA_PREFIX)
                        if (name.isNotBlank() && !name.contains('/') && !name.contains('\\')) {
                            media[name] = zip.readBytes()
                        }
                    }
                }
                zip.closeEntry()
            }
        }

        val info = manifest ?: throw IllegalStateException("这个压缩包不是一念的备份")
        val format = info.optInt("format", 0)
        if (format > FORMAT) throw IllegalStateException("备份来自更新版本的一念，请先升级 App")
        if (ideasRaw == null || groupsRaw == null) throw IllegalStateException("备份文件不完整")

        val ideas = JournalJson.decodeIdeasOrNull(ideasRaw)
            ?: throw IllegalStateException("想法数据读不出来，备份可能损坏")
        val groups = JournalJson.decodeGroups(groupsRaw)
        return BackupPayload(
            ideas = ideas,
            groups = groups,
            appearance = appearanceRaw?.let { JournalJson.decodeAppearance(it) } ?: Appearance(),
            media = media,
        )
    }

    private fun addText(zip: ZipOutputStream, name: String, text: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun crcOf(file: File): Long {
        val crc = java.util.zip.CRC32()
        file.inputStream().use { stream ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                crc.update(buffer, 0, read)
            }
        }
        return crc.value
    }
}
