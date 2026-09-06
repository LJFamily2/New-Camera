package com.newcamera.app.overlay

import androidx.annotation.DrawableRes
import com.newcamera.app.R

data class PoseGuide(
    val id: String,
    val displayName: String,
    @DrawableRes val iconRes: Int
)

/**
 * Poses are exposed through a lazily-initialized list so the vector assets are only
 * resolved on first access (e.g. when the pose selector row first composes) rather than
 * at class-load time, keeping camera preview start-up free of avoidable work.
 */
object PoseRepository {
    val poses: List<PoseGuide> by lazy {
        listOf(
            PoseGuide("full_body", "Full Body", R.drawable.pose_full_body),
            PoseGuide("portrait", "Portrait", R.drawable.pose_portrait),
            PoseGuide("sitting", "Sitting", R.drawable.pose_sitting),
            PoseGuide("couple", "Couple", R.drawable.pose_couple),
            PoseGuide("action", "Action", R.drawable.pose_action),
            PoseGuide("yoga", "Yoga", R.drawable.pose_yoga)
        )
    }

    fun findById(id: String?): PoseGuide? = poses.firstOrNull { it.id == id }
}
