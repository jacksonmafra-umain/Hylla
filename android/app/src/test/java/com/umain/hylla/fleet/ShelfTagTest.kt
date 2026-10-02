package com.umain.hylla.fleet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShelfTagTest {
    @Test
    fun `loose spellings resolve to the canonical tag`() {
        for (text in listOf("HYL-002", "hyl-2", "HYL2", "2", " 002 ")) {
            assertEquals(text, DeviceId("HYL-002"), ShelfTag.parse(text))
        }
    }

    @Test
    fun `anything that is not a tag is rejected`() {
        for (text in listOf("", "HYL-", "ABC-002", "HYL-1234", "Fold7")) {
            assertNull(text, ShelfTag.parse(text))
        }
    }
}
