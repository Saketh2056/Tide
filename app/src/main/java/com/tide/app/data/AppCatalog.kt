package com.tide.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String
)

/**
 * Resolves launchable apps, labels, and icons via [PackageManager], with in-memory caches so
 * list screens stay snappy and the Guardian service never blocks on icon loads.
 */
class AppCatalog(private val context: Context) {

    private val pm: PackageManager get() = context.packageManager
    private val iconCache = LruCache<String, ImageBitmap>(160)
    private val labelCache = HashMap<String, String>()
    private var cachedApps: List<InstalledApp>? = null
    private val mutex = Mutex()

    suspend fun launchableApps(): List<InstalledApp> = mutex.withLock {
        cachedApps?.let { return it }
        withContext(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolved = pm.queryIntentActivities(intent, 0)
            val apps = resolved
                .asSequence()
                .map { it.activityInfo.packageName }
                .distinct()
                .filter { it != context.packageName }
                .map { pkg -> InstalledApp(pkg, labelOf(pkg)) }
                .sortedBy { it.label.lowercase() }
                .toList()
            cachedApps = apps
            apps
        }
    }

    fun labelOf(packageName: String): String {
        labelCache[packageName]?.let { return it }
        val label = runCatching {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() })
        labelCache[packageName] = label
        return label
    }

    suspend fun iconOf(packageName: String): ImageBitmap? {
        iconCache.get(packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val drawable = pm.getApplicationIcon(packageName)
                val size = 144
                val bitmap = if (drawable.intrinsicWidth in 1..size && drawable.intrinsicHeight in 1..size) {
                    drawable.toBitmap()
                } else {
                    val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(b)
                    drawable.setBounds(0, 0, size, size)
                    drawable.draw(canvas)
                    b
                }
                bitmap.asImageBitmap().also { iconCache.put(packageName, it) }
            }.getOrNull()
        }
    }

    fun isInstalled(packageName: String): Boolean =
        runCatching { pm.getApplicationInfo(packageName, 0) }.isSuccess
}
