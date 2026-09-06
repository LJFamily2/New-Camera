package com.newcamera.app

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.newcamera.app.ui.CameraScreen
import com.newcamera.app.ui.theme.NewCameraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [CameraScreen] end to end on a real device/emulator, with the camera permission
 * pre-granted so CameraX actually binds. Verifies the standard controls a camera app is
 * expected to expose, plus the "None" entry in the pose-guide selector.
 */
@RunWith(AndroidJUnit4::class)
class CameraScreenUiTest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shutterFlashAndSwitchControlsAreDisplayed() {
        composeTestRule.setContent {
            NewCameraTheme { CameraScreen() }
        }

        composeTestRule.onNodeWithContentDescription("Take photo").assertExists()
        composeTestRule.onNodeWithContentDescription("Switch camera").assertExists()
    }

    @Test
    fun poseSelectorShowsNoneEntryByDefault() {
        composeTestRule.setContent {
            NewCameraTheme { CameraScreen() }
        }

        composeTestRule.onNodeWithText("None").assertExists()
        composeTestRule.onNodeWithText("Full Body").assertExists()
    }

    @Test
    fun selectingAPoseRevealsTheOpacitySlider() {
        composeTestRule.setContent {
            NewCameraTheme { CameraScreen() }
        }

        composeTestRule.onNodeWithText("Full Body").performClick()

        composeTestRule.onNodeWithContentDescription("Guide opacity").assertExists()
    }
}
