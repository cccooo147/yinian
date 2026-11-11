package com.yinian.app.data

/**
 * What the user framed in the cropper: a zoom plus a pan, measured in the
 * preview frame's pixels. Keeping it a plain value object lets the view model
 * crop off the main thread without knowing anything about Compose.
 */
data class CropSpec(
    val aspect: Float,
    val viewWidth: Int,
    val viewHeight: Int,
    val zoom: Float,
    val panX: Float,
    val panY: Float,
)
