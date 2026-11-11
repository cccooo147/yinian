package com.yinian.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yinian.app.ui.theme.LocalAppColors

private val BLOCKED_SCHEMES = setOf("file", "javascript", "data", "content")

/**
 * One row of the redirect table: which hosts/paths belong to which app, and the
 * scheme that lands on the same content inside it.
 *
 * `scheme` may contain `$1`..`$9` for [pathRegex] groups and `{q}` for the
 * value of [queryKey]. Everything unknown simply falls back to the browser.
 */
private data class LinkRule(
    val hostSuffix: String,
    val pathRegex: Regex? = null,
    /** Null means "no scheme for this shape"; only the package/https steps run. */
    val scheme: String? = null,
    val queryKey: String? = null,
    val packageName: String? = null,
    /**
     * Some apps claim the https link (verified App Link) but ignore the path and
     * exit silently. For those, skip the app and hand the URL to the browser.
     */
    val forceBrowser: Boolean = false,
)

private val RULES = listOf(
    LinkRule(
        hostSuffix = "bilibili.com",
        pathRegex = Regex("/video/(BV[0-9A-Za-z]{10})"),
        scheme = "bilibili://video/\$1",
        packageName = "tv.danmaku.bili",
    ),
    LinkRule(
        hostSuffix = "bilibili.com",
        pathRegex = Regex("/space/(\\d+)"),
        // B 站 H5 页面用的就是这条；少了 ?frommodule=H5 会被静默丢弃。
        scheme = "bilibili://space/\$1?frommodule=H5",
        packageName = "tv.danmaku.bili",
    ),
    LinkRule(
        hostSuffix = "music.163.com",
        scheme = "orpheus://song/{q}",
        queryKey = "id",
        packageName = "com.netease.cloudmusic",
    ),
    LinkRule(
        hostSuffix = "youtube.com",
        scheme = "vnd.youtube:{q}",
        queryKey = "v",
        packageName = "com.google.android.youtube",
    ),
    LinkRule(
        hostSuffix = "youtu.be",
        pathRegex = Regex("/([A-Za-z0-9_-]{11})"),
        scheme = "vnd.youtube:\$1",
        packageName = "com.google.android.youtube",
    ),
)

private fun ruleFor(uri: Uri): LinkRule? {
    val host = uri.host?.lowercase() ?: return null
    if (uri.scheme?.lowercase() !in setOf("http", "https")) return null
    val path = uri.path ?: ""
    return RULES.firstOrNull { rule ->
        val hostMatches = host == rule.hostSuffix || host.endsWith(".${rule.hostSuffix}")
        hostMatches && (rule.pathRegex?.containsMatchIn(path) ?: true)
    }
}

private fun LinkRule.toScheme(uri: Uri): String? {
    var out = scheme ?: return null
    val regex = pathRegex
    if (regex != null) {
        val match = regex.find(uri.path ?: "") ?: return null
        out = out.replace("\$1", match.groupValues.getOrNull(1) ?: return null)
    }
    val key = queryKey
    if (key != null) {
        val value = uri.getQueryParameter(key) ?: return null
        out = out.replace("{q}", value)
    }
    return out.takeIf { !it.contains("\$1") && !it.contains("{q}") }
}

/**
 * Opens a stored link. Known shapes are sent straight to their app first; any
 * failure (app missing, filter absent) falls back to the plain https intent,
 * and the last resort is whatever the system picks — usually the browser.
 */
fun openExternalLink(context: Context, raw: String): Boolean {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return false
    val parsed = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return false
    val scheme = parsed.scheme?.lowercase()
    if (scheme != null && scheme in BLOCKED_SCHEMES) return false

    val target = if (scheme == null) Uri.parse("https://$trimmed") else parsed
    val rule = ruleFor(target)

    val intents = buildList {
        if (rule?.forceBrowser == true) {
            browserIntent(context, target)?.let { add(it) }
        } else {
            rule?.toScheme(target)?.let { add(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
            rule?.packageName?.let { pkg -> add(Intent(Intent.ACTION_VIEW, target).setPackage(pkg)) }
        }
        add(Intent(Intent.ACTION_VIEW, target))
    }

    for (intent in intents) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(intent) }.isSuccess) return true
    }
    return false
}

/** The user's default browser, found by probing a generic https link. */
private fun browserIntent(context: Context, uri: Uri): Intent? {
    val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
    val pkg = runCatching {
        context.packageManager
            .resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }.getOrNull()
    if (pkg.isNullOrBlank() || pkg == "android") return null
    return Intent(Intent.ACTION_VIEW, uri).setPackage(pkg)
}

/**
 * Pulls the first URL out of a share blurb, e.g.
 * "【标题-哔哩哔哩】 https://b23.tv/uWPEPHh" → "https://b23.tv/uWPEPHh".
 */
fun extractLink(text: String): String? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    val raw = SCHEME_URL.find(trimmed)?.value
        ?: WEB_URL.find(trimmed)?.value?.let { if (it.startsWith("www.", true)) "https://$it" else it }
        ?: return null
    val cleaned = raw.trimEnd(*TRAILING_PUNCTUATION).trim()
    return cleaned.takeIf { it.length > 8 && it.contains('.') }
}

private val SCHEME_URL = Regex("[a-zA-Z][a-zA-Z0-9+.-]*://[^\\s<>\"']+")
private val WEB_URL = Regex("(?:https?://|www\\.)[^\\s<>\"']+", RegexOption.IGNORE_CASE)
private val TRAILING_PUNCTUATION = charArrayOf(
    '】', '）', ')', '，', ',', '。', '.', '！', '!', '？', '?',
    '、', '"', '\'', ';', '；', '》', '>', '…',
)

/** Short, readable label for the bar — never the whole URL. */
fun linkLabel(raw: String): String {
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return raw
    val scheme = uri.scheme?.lowercase()
    if (scheme == "http" || scheme == "https") {
        val host = uri.host
        if (!host.isNullOrBlank()) return host.removePrefix("www.")
    }
    val known = scheme?.let { APP_NAMES[it] }
    if (known != null) return known
    if (!scheme.isNullOrBlank()) return scheme
    return raw
}

private val APP_NAMES = mapOf(
    "bilibili" to "哔哩哔哩",
    "orpheus" to "网易云音乐",
    "vnd.youtube" to "YouTube",
)

/** Escape hatch: always shows the system chooser, even if the app path no-ops. */
fun openWithChooser(context: Context, raw: String) {
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching {
        context.startActivity(
            Intent.createChooser(intent, "打开方式").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

fun copyLink(context: Context, raw: String): Boolean {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return false
    manager.setPrimaryClip(ClipData.newPlainText("链接", raw))
    return true
}

/** The "打开" bar shown under a step that has a link. Long press = escape hatch. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepLinkBar(
    uri: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.accentSoft)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinkGlyph(Modifier.size(13.dp), colors.accent)
        Spacer(Modifier.width(7.dp))
        Text(
            text = linkLabel(uri),
            color = colors.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
        )
        Text(
            text = "打开 ›",
            color = colors.accent,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
        )
    }
}

@Composable
fun LinkGlyph(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val tint = if (color == Color.Unspecified) Color.White else color
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val box = Path().apply {
            moveTo(w * 0.16f, h * 0.36f)
            lineTo(w * 0.16f, h * 0.84f)
            lineTo(w * 0.64f, h * 0.84f)
            lineTo(w * 0.64f, h * 0.62f)
        }
        drawPath(box, tint, style = stroke)
        val arrow = Path().apply {
            moveTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.84f, h * 0.18f)
            lineTo(w * 0.84f, h * 0.52f)
            moveTo(w * 0.84f, h * 0.18f)
            lineTo(w * 0.44f, h * 0.58f)
        }
        drawPath(arrow, tint, style = stroke)
    }
}
