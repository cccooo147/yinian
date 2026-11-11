package com.yinian.app.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import java.io.File

/**
 * The only image hook the UI needs: a relative path in, an app-private File
 * out. Importing/cropping lives in [com.yinian.app.data.ImageStore].
 */
val LocalImageFile = staticCompositionLocalOf<(String?) -> File?> { { null } }
