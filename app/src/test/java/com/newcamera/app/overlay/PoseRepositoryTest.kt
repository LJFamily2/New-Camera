package com.newcamera.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseRepositoryTest {

    @Test
    fun `bundles at least five distinct poses`() {
        val poses = PoseRepository.poses
        assertTrue("expected 5-7 poses, found ${poses.size}", poses.size in 5..7)
    }

    @Test
    fun `every pose has a unique non-blank id`() {
        val ids = PoseRepository.poses.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test
    fun `every pose has a valid drawable resource id`() {
        assertTrue(PoseRepository.poses.all { it.iconRes != 0 })
    }

    @Test
    fun `findById returns the matching pose or null`() {
        val first = PoseRepository.poses.first()
        assertEquals(first, PoseRepository.findById(first.id))
        assertNull(PoseRepository.findById("does_not_exist"))
        assertNotNull(PoseRepository.findById(first.id))
    }
}
