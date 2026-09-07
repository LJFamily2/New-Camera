# New Camera

A lightweight, production-oriented custom camera app for Android with an interactive
**pose guide overlay** — a transparent, draggable/zoomable/opacity-adjustable silhouette
guide rendered on top of the live viewfinder to help frame shots, mimicking the posing
guides found in phone camera apps such as Xiaomi's.

## Stack

- Kotlin, Jetpack Compose (Material 3)
- CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`)
- Min SDK 26, Target/Compile SDK 34
- No third-party image/UI libraries beyond AndroidX — kept intentionally lightweight

## Features

- **Preview & capture**: `PreviewView` in `ImplementationMode.PERFORMANCE` (SurfaceView-backed)
  for low-latency, low-power rendering; `ImageCapture` in `CAPTURE_MODE_MINIMIZE_LATENCY`.
- **Standard controls**: large circular shutter, front/back camera switch, flash cycling
  (Off → On → Auto), tap-to-focus on the viewfinder — the same baseline controls found in
  stock camera apps (Xiaomi's Camera app included). The flash toggle hides itself on lenses
  without a flash unit (e.g. most front cameras), matching that behavior.
- **More camera options (Xiaomi-style)**: a top options bar with a rule-of-thirds **grid**
  toggle, a **self-timer** (Off/3s/10s) with an on-screen countdown, and an **aspect ratio**
  cycle (Full/4:3/1:1) that dims the viewfinder outside the frame that will actually be saved.
  CameraX has no native square-capture mode, so 1:1 captures at 4:3 and is center-cropped after
  saving (`ImageCropUtils`). **Zoom** is available via pinch-to-zoom directly on the viewfinder
  (a `ScaleGestureDetector` combined with the existing tap-to-focus listener) plus a 1x/2x pill
  row driven by `CameraInfo.zoomState`/`CameraControl.setZoomRatio`; the 2x pill only appears
  when the active lens actually supports it.
- **Pose guide overlay**: 20 bundled pose guides across eight categories — **Studio** (Full
  Body, Portrait, Sitting, Action, Yoga), **Beach** (Beach Sit, Lookback, Sunset), **Mountain**
  (Summit, Trail Sit), **Group** (Couple, Group Selfie), **Fashion** (Street Walk, Wall Lean),
  **Urban** (Railing Lean, Phone Call), **Travel** (Point at View, Jump Shot), and **Cafe**
  (Coffee Sip, Book Read) — filterable via a category chip row, picked from a horizontally
  scrollable thumbnail strip above the shutter. Each guide is a **filled silhouette** (a solid
  head, a solid tapered torso, thick rounded limbs) rather than a thin stick figure, since a
  body-shaped silhouette reads at a glance while wireframe lines don't; all of it is original
  vector art authored for this app, not photos — using real photos of real people as in-app
  assets would need a license this project doesn't have, and this app never fetches or embeds
  any web content into its asset bundle. The guide can be dragged and pinch-zoomed to align with
  the subject, and its opacity is adjustable via a slider. The overlay is drawn purely on a
  Compose canvas layer above `PreviewView` — it is never part of the `ImageCapture` pipeline, so
  it can never end up baked into a saved photo.
- **MediaStore saving**: JPEGs are written to `DCIM/Camera` via MediaStore, bridging the
  scoped-storage API (Q+) and the legacy `DATA`-column API (26–28), since minSdk is 26.
  CameraX handles JPEG EXIF orientation automatically; an `OrientationEventListener` keeps
  `ImageCapture.targetRotation` in sync with the physical device orientation so captures are
  upright regardless of how the phone was rotated when the shutter was pressed, even though
  the activity itself stays portrait-locked for a simpler, standard-camera-app UI.
- **Lifecycle-safe**: the camera is bound from a `LaunchedEffect` tied to the composition and
  released in `DisposableEffect`'s `onDispose` as well as `ViewModel.onCleared()`, so backing
  out of the screen or finishing the activity always unbinds CameraX cleanly.

## Architecture

```
app/src/main/java/com/newcamera/app/
├── MainActivity.kt              Compose host, edge-to-edge, applies the theme
├── camera/
│   ├── CameraViewModel.kt       Owns CameraX use cases, flash/lens/zoom/aspect-ratio state
│   ├── FlashMode.kt             OFF/ON/AUTO enum + mapping to ImageCapture constants
│   ├── CaptureTimer.kt          OFF/THREE/TEN self-timer enum
│   └── CaptureAspectRatio.kt    FULL/4:3/1:1 enum + CameraX mapping + crop-mask ratio
├── overlay/
│   ├── PoseGuide.kt             Pose + PoseCategory data, lazily-initialized PoseRepository
│   ├── PoseOverlayState.kt      Pure, immutable pan/zoom/opacity transform state
│   └── PoseOverlay.kt           Gesture-driven Compose overlay (pan + pinch-zoom)
├── ui/
│   ├── PermissionGate.kt        Runtime permission request + rationale screen
│   ├── CameraScreen.kt          Wires PreviewView + overlays + controls together
│   ├── CameraControls.kt        Shutter/flash/switch/zoom/top-options/pose-selector controls
│   ├── CameraOverlays.kt        Grid, aspect-ratio crop mask, timer countdown (visual only)
│   └── theme/                   Material 3 dark theme
└── util/
    ├── FileNaming.kt            Pure JPEG filename formatting (unit-testable)
    ├── MediaStoreUtils.kt       MediaStore ContentValues for DCIM/Camera
    ├── ImageCropUtils.kt        Center-square crop for the 1:1 aspect ratio option
    └── PermissionUtils.kt       Required-permissions logic (varies pre/post API 29)
```

The pose overlay's transform state (`PoseOverlayState`) is hoisted in `CameraScreen` and kept
completely separate from `CameraUiState` (the ViewModel's camera/flash/lens state). Dragging or
zooming the guide only recomposes the overlay layer, never the camera binding, and switching
cameras never resets the guide's position — this is the isolation the performance requirement
asks for.

## Building

```
./gradlew assembleDebug   # build a debug APK
./gradlew test            # JVM unit tests
./gradlew connectedAndroidTest  # instrumented tests (needs a device/emulator)
```

Open the project root in Android Studio (Koala/2024.1+) and it will sync via the committed
Gradle wrapper (Gradle 8.7, AGP 8.5.2, Kotlin 2.0.21).

> **Note on this environment**: this project was built in a sandboxed session without access
> to Google's Maven repository (`dl.google.com` / `maven.google.com`), which is where the
> Android Gradle Plugin, AndroidX, CameraX and Compose artifacts are hosted — that host is
> blocked by this session's egress policy. Because of that, `./gradlew` could not actually
> download those dependencies here to run a full `assembleDebug`/`test` here, so an end-to-end
> build was not executed as part of this task. Every source file was written and manually
> reviewed against the real CameraX/Compose APIs (correct import paths, method signatures,
> and Gradle/version-catalog wiring), and the Gradle wrapper itself was verified to work and
> the build scripts were confirmed to parse correctly (the resolution attempt fails only at
> the network fetch of `com.android.application`, past all script/catalog parsing). Please run
> `./gradlew assembleDebug` and `./gradlew test` locally (or open in Android Studio) as the
> first thing you do — if anything doesn't compile, it's most likely a small typo rather than
> a structural issue, and I'm happy to fix it once you paste the error.

## Tests

- **Unit tests** (`app/src/test`, no device needed): `PoseOverlayStateTest` (pan/zoom/opacity
  math and clamping), `FlashModeTest` (cycling + mapping to `ImageCapture` constants),
  `CaptureTimerTest` (Off/3s/10s cycling), `CaptureAspectRatioTest` (Full/4:3/1:1 cycling,
  CameraX mapping, square-crop flag), `PoseRepositoryTest` (pose bundle integrity, all 8
  categories populated, category filtering), `FileNamingTest` (filename format),
  `PermissionUtilsTest` (pre/post API 29 permission set).
- **Instrumented tests** (`app/src/androidTest`, needs a device/emulator):
  `CameraScreenUiTest` (shutter/switch controls exist, pose selector shows entries, selecting
  a pose reveals the opacity slider), `MediaStoreUtilsInstrumentedTest` (ContentValues are
  built correctly), `ExampleInstrumentedTest` (sanity check on the app package name).

## Known limitations / next steps

- The overlay's own pinch-to-zoom (for aligning the pose guide) and the camera's pinch-to-zoom
  (on the bare viewfinder, via `ScaleGestureDetector`) are two independent gesture regions by
  design: when a pose is selected, the overlay's full-size gesture box takes pinch input for
  guide alignment; when no pose is selected, pinch reaches the viewfinder's zoom listener
  instead. Layering "zoom the camera while a guide is also visible" onto the same gesture would
  need an explicit mode switch to avoid the two interpretations conflicting.
- Zoom pills only offer 1x/2x (whatever the active lens' `maxZoomRatio` supports) since this
  app doesn't do multi-lens (ultra-wide) switching — that would need CameraX's extended lens
  selection APIs, a materially bigger addition.
- No unit/instrumented test run has actually been executed against these files in this session
  (see the build note above) — please treat this as reviewed-but-unverified code and run the
  test suite as the first step.
