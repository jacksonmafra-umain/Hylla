package com.umain.hylla.fleet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScannedCodeTest {
    @Test
    fun `a tag's QR code holds its device link`() {
        assertEquals(DeviceId("HYL-003"), ScannedCode.parse("hylla://device/HYL-003"))
    }

    @Test
    fun `a bare tag still scans`() {
        assertEquals(DeviceId("HYL-003"), ScannedCode.parse("HYL-003"))
    }

    @Test
    fun `other codes are ignored`() {
        assertNull(ScannedCode.parse("https://example.com/HYL-003"))
        assertNull(ScannedCode.parse("4006381333931"))
    }
}
