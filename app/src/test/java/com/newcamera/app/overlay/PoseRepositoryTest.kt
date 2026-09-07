package com.newcamera.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseRepositoryTest {

    @Test
    fun `bundles a healthy variety of distinct poses`() {
        val poses = PoseRepository.poses
        assertTrue("expected at least 10 poses, found ${poses.size}", poses.size >= 10)
    }

    @Test
    fun `every category has at least one pose`() {
        PoseCategory.entries.forEach { category ->
            val posesInCategory = PoseRepository.byCategory(category)
            assertTrue("expected at least one pose in $category", posesInCategory.isNotEmpty())
            assertTrue(posesInCategory.all { it.category == category })
        }
    }

    @Test
    fun `byCategory with null returns every pose`() {
        assertEquals(PoseRepository.poses.size, PoseRepository.byCategory(null).size)
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
