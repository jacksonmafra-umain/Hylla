package com.umain.hylla.fleet

/**
 * A shelf tag's QR code holds its device link, `hylla://device/HYL-003`, so the camera, a link and
 * a typed tag all arrive at the same id. Older tags printed with the bare tag still scan.
 */
object ScannedCode {
    fun parse(raw: String): DeviceId? = DeepLink.parse(raw) ?: ShelfTag.parse(raw)
}
