package com.umain.hylla.fleet

import java.net.URI

/**
 * `hylla://device/HYL-003` opens that device. The path is a shelf tag, read as loosely as a
 * typed one, so a link written by hand still works. Anything else is not a Hylla link.
 */
object DeepLink {
    const val SCHEME = "hylla"
    const val DEVICE_HOST = "device"

    fun device(id: DeviceId): String = "$SCHEME://$DEVICE_HOST/${id.value}"

    fun parse(link: String): DeviceId? {
        val uri = runCatching { URI(link.trim()) }.getOrNull() ?: return null
        if (!uri.scheme.equals(SCHEME, ignoreCase = true) || !uri.host.equals(DEVICE_HOST, ignoreCase = true)) return null
        val segments = uri.path.orEmpty().split('/').filter { it.isNotEmpty() }
        return segments.singleOrNull()?.let(ShelfTag::parse)
    }
}
