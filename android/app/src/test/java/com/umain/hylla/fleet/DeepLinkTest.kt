package com.umain.hylla.fleet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkTest {
    @Test
    fun `a device link resolves to its shelf tag`() {
        assertEquals(DeviceId("HYL-003"), DeepLink.parse("hylla://device/HYL-003"))
    }

    @Test
    fun `hand-written links are read like typed tags`() {
        for (link in listOf("hylla://device/hyl-3", "HYLLA://Device/3", "hylla://device/HYL-003/")) {
            assertEquals(link, DeviceId("HYL-003"), DeepLink.parse(link))
        }
    }

    @Test
    fun `other schemes, hosts and shapes are not Hylla links`() {
        for (link in listOf("https://device/HYL-003", "hylla://fleet/HYL-003", "hylla://device/", "hylla://device/HYL-003/history", "not a link")) {
            assertNull(link, DeepLink.parse(link))
        }
    }

    @Test
    fun `links round-trip`() {
        assertEquals(DeviceId("HYL-017"), DeepLink.parse(DeepLink.device(DeviceId("HYL-017"))))
    }
}
