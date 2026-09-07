package com.newcamera.app.overlay

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.newcamera.app.R

enum class PoseCategory(@StringRes val labelRes: Int) {
    STUDIO(R.string.pose_category_studio),
    BEACH(R.string.pose_category_beach),
    MOUNTAIN(R.string.pose_category_mountain),
    GROUP(R.string.pose_category_group),
    FASHION(R.string.pose_category_fashion),
    URBAN(R.string.pose_category_urban),
    TRAVEL(R.string.pose_category_travel),
    CAFE(R.string.pose_category_cafe)
}

data class PoseGuide(
    val id: String,
    val displayName: String,
    val category: PoseCategory,
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
            PoseGuide("full_body", "Full Body", PoseCategory.STUDIO, R.drawable.pose_full_body),
            PoseGuide("portrait", "Portrait", PoseCategory.STUDIO, R.drawable.pose_portrait),
            PoseGuide("sitting", "Sitting", PoseCategory.STUDIO, R.drawable.pose_sitting),
            PoseGuide("action", "Action", PoseCategory.STUDIO, R.drawable.pose_action),
            PoseGuide("yoga", "Yoga", PoseCategory.STUDIO, R.drawable.pose_yoga),
            PoseGuide("beach_candid_sit", "Beach Sit", PoseCategory.BEACH, R.drawable.pose_beach_candid_sit),
            PoseGuide("tropical_lookback", "Lookback", PoseCategory.BEACH, R.drawable.pose_tropical_lookback),
            PoseGuide("sunset_silhouette", "Sunset", PoseCategory.BEACH, R.drawable.pose_sunset_silhouette),
            PoseGuide("summit_overlook", "Summit", PoseCategory.MOUNTAIN, R.drawable.pose_summit_overlook),
            PoseGuide("trail_sit", "Trail Sit", PoseCategory.MOUNTAIN, R.drawable.pose_trail_sit),
            PoseGuide("couple", "Couple", PoseCategory.GROUP, R.drawable.pose_couple),
            PoseGuide("group_selfie", "Group Selfie", PoseCategory.GROUP, R.drawable.pose_group_selfie),
            PoseGuide("street_walk", "Street Walk", PoseCategory.FASHION, R.drawable.pose_street_walk),
            PoseGuide("wall_lean", "Wall Lean", PoseCategory.FASHION, R.drawable.pose_wall_lean),
            PoseGuide("railing_lean", "Railing Lean", PoseCategory.URBAN, R.drawable.pose_railing_lean),
            PoseGuide("phone_candid", "Phone Call", PoseCategory.URBAN, R.drawable.pose_phone_candid),
            PoseGuide("point_at_view", "Point at View", PoseCategory.TRAVEL, R.drawable.pose_point_at_view),
            PoseGuide("overhead_jump", "Jump Shot", PoseCategory.TRAVEL, R.drawable.pose_overhead_jump),
            PoseGuide("coffee_sip", "Coffee Sip", PoseCategory.CAFE, R.drawable.pose_coffee_sip),
            PoseGuide("book_read", "Book Read", PoseCategory.CAFE, R.drawable.pose_book_read)
        )
    }

    fun findById(id: String?): PoseGuide? = poses.firstOrNull { it.id == id }

    fun byCategory(category: PoseCategory?): List<PoseGuide> =
        if (category == null) poses else poses.filter { it.category == category }
}
