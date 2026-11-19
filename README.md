# My Little Launcher — Soft Mint Luxury

A Kotlin + Jetpack Compose Android launcher starter, tailored for POCO F3 / HyperOS.

## Included in v0.2.0
- HOME launcher and normal app drawer launcher intents.
- Android package visibility query for launcher activities.
- Photo-first home screen using the supplied baby photo as the bundled default wallpaper.
- User-selectable wallpaper via Android document picker.
- Live clock and Indonesian date.
- App drawer with search and installed app launching.
- Floating dock using installed app icons.
- In-app lock-screen visual preview (not a replacement for Android's secure Keyguard).
- Soft Mint / Sage / Ivory visual palette.
- GitHub Actions Debug APK build.

## Build
JDK 17 and Gradle 8.13:
```bash
gradle --no-daemon :app:assembleDebug
```

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
Push to `main` or `develop`; download `my-little-launcher-soft-mint-debug` from Actions artifacts.

## Important Android platform boundaries
A third-party launcher cannot silently replace Android/HyperOS Keyguard, Quick Settings, or the system notification shade. The lock screen in this version is an in-app visual preview only. Secure authentication remains owned by Android. Widget hosting, drag-and-drop workspace editing, folders, custom icon mapping, and notification listener integration are planned next steps and are not represented as completed features.
