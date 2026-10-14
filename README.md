# ADARA — Actually Decent and Reasonable Assistant

Fresh native Android starter. The prior APKs/browser prototype are not used as source. This establishes the app/services and a CI build; it is **not a finished or device-tested APK**.

## Build
- Open in Android Studio with JDK 17 and Android SDK Platform 35, or run `gradle :app:assembleDebug` with Gradle 8.11.1.
- GitHub Actions builds `app/build/outputs/apk/debug/app-debug.apk` on push, pull request, or manual run. Debug signing only; a release requires a protected keystore and separate release workflow.
- Application ID: `com.adara.assistant` (new identity; upgrade compatibility with the earlier APK depends on its package/signature).

## Privacy
- No `INTERNET` permission, analytics SDK, account sync, or background upload.
- Speech input uses Android's on-device recognizer API only (Android 12/API 31+); if the device/language pack does not support it, ADARA refuses to fall back to a network recognizer.
- Speech output uses an installed Android TTS voice marked non-network; if none exists, it refuses to speak.
- ADARA does not persist transcripts, notification content, call content, or usage history. Android/OEM services and apps opened for calls/maps have their own behavior and privacy policies; ADARA cannot vouch for those.

## Implemented in this starter
Black/red terminal UI (system monospace for now; Red Hat Mono asset still needs bundling); assistant/dialer role request; explicit commands for `call <number>` (opens the system dialer, or if ADARA is the default dialer asks for confirmation and the system call permission before placing), `navigate <place>`, `play/pause/next/previous`, exact visible-text tap with confirmation, scroll, answer/reject active cellular call, read notifications (including an audible, confirmed mode), and clear only clearable notifications with confirmation. Accessibility, notification listener, overlay, and dialer features remain opt-in through Android system consent/settings.

## Not done / limits
- No “Adara on” always-on hotword yet. That needs a bundled local keyword model, foreground microphone service, and power/thermal testing; this build does not listen in the background.
- No bundled Piper/Vosk/sherpa model yet. First version uses only the phone's offline speech services; availability varies by device.
- WhatsApp/custom-app automation is best-effort Accessibility UI action by exact visible text; requires user confirmation and can fail after app/OS changes. It is not a WhatsApp call API.
- Navigation and calls hand off to external apps/carrier services.
- Workspace lacks JDK/Gradle/Android SDK/emulator, so source is not compiled or installed here. Build in Android Studio/CI and test on target devices before relying on assistant, call, or accessibility behavior.
