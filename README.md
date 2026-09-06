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
- **Pose guide overlay**: 6 bundled minimalist stick-figure silhouettes (Full Body, Portrait,
  Sitting, Couple, Action, Yoga) plus "None", picked from a horizontally scrollable thumbnail
  strip above the shutter. The guide can be dragged and pinch-zoomed to align with the subject,
  and its opacity is adjustable via a slider. The overlay is drawn purely on a Compose canvas
  layer above `PreviewView` — it is never part of the `ImageCapture` pipeline, so it can never
  end up baked into a saved photo.
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
│   ├── CameraViewModel.kt       Owns CameraX use cases, flash/lens/capture state
│   └── FlashMode.kt             OFF/ON/AUTO enum + mapping to ImageCapture constants
├── overlay/
│   ├── PoseGuide.kt             Pose data + lazily-initialized PoseRepository
│   ├── PoseOverlayState.kt      Pure, immutable pan/zoom/opacity transform state
│   └── PoseOverlay.kt           Gesture-driven Compose overlay (pan + pinch-zoom)
├── ui/
│   ├── PermissionGate.kt        Runtime permission request + rationale screen
│   ├── CameraScreen.kt          Wires PreviewView + PoseOverlay + controls together
│   ├── CameraControls.kt        Shutter/flash/switch buttons, opacity slider, pose selector
│   └── theme/                   Material 3 dark theme
└── util/
    ├── FileNaming.kt            Pure JPEG filename formatting (unit-testable)
    ├── MediaStoreUtils.kt       MediaStore ContentValues for DCIM/Camera
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
  `PoseRepositoryTest` (pose bundle integrity), `FileNamingTest` (filename format),
  `PermissionUtilsTest` (pre/post API 29 permission set).
- **Instrumented tests** (`app/src/androidTest`, needs a device/emulator):
  `CameraScreenUiTest` (shutter/switch controls exist, pose selector shows entries, selecting
  a pose reveals the opacity slider), `MediaStoreUtilsInstrumentedTest` (ContentValues are
  built correctly), `ExampleInstrumentedTest` (sanity check on the app package name).

## Known limitations / next steps

- Pinch-to-zoom on the overlay guide is intentionally not wired to the camera's own optical/
  digital zoom — they're independent by design per the spec (overlay alignment vs. camera zoom).
  Wiring a second gesture region for camera zoom when no pose is selected would be a natural
  follow-up.
- No unit/instrumented test run has actually been executed against these files in this session
  (see the build note above) — please treat this as reviewed-but-unverified code and run the
  test suite as the first step.
