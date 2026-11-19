# My Little Launcher

Native Android launcher starter built with Kotlin and Jetpack Compose.

## Current starter features
- Declares itself as an Android HOME launcher.
- Lists installed apps with launcher activities.
- App drawer with live text filtering.
- Launches selected apps.
- Selects a photo from the document picker and persists its URI permission.
- Minimal clock/date presentation and photo-backed home screen.
- GitHub Actions builds a Debug APK on pushes to `main`/`develop`.

## Build locally
Requirements: JDK 17 and Gradle 8.13.

```bash
gradle :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
Push this repository to GitHub. Open **Actions** and download the artifact named
`my-little-launcher-debug` from the latest successful workflow run.

## Important next steps
This is an initial scaffold, not a finished production launcher. The clock currently
updates when Compose recomposes; replace it with a lifecycle-aware ticker. Add proper
app icon rendering/caching, drag-and-drop grid placement, folders, favorites persistence,
gesture handling, widget hosting, crash handling, and signed release builds before
daily use. Test on the target Android devices and OS versions.
