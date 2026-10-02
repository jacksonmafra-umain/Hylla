package com.umain.hylla.fleet

/** Shelf tags are `HYL-` and three digits. People type them loosely; the scanner does not. */
object ShelfTag {
    private val pattern = Regex("""^(?:HYL-?)?0*(\d{1,3})$""")

    /** `HYL-002`, `hyl-2`, `HYL2` and `2` all mean `HYL-002`. Anything else is null. */
    fun parse(text: String): DeviceId? {
        val digits = pattern.matchEntire(text.trim().uppercase())?.groupValues?.get(1) ?: return null
        return DeviceId("HYL-%03d".format(digits.toInt()))
    }
}
