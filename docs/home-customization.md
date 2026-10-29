# Home customization preview

Branch: `feature/home-customization`, based on the tested song-loader build.

- Home heading; For you and Recents tabs keep independent scroll positions.
- Speed Dial uses recent songs first, then unique playable recommendations. Random chooses from visible blocks, avoiding the current song where possible. Long-press actions remain available.
- Customize home is a full-width footer. Choose 2/3 columns and 1–3 rows, hide Speed Dial, reorder/hide feed sections, or reset. Preferences survive restart and participate in existing data export/import.
- More recommendations loads another page explicitly, allowing the footer to remain reachable.
- Controls use the app's existing lightweightLiquidGlass material, including its supported-device and theme fallbacks. No player or navigation files are changed.
- Settings → Playback → Song loader: 0–30 seconds, default 7. The delay applies after playback starts/resumes or the preload request changes; 0 starts immediately. Selecting after-current disables advance downloads and disables the delay slider while remembering its value. Quality/source selection remains unchanged.

Build: `./gradlew :app:assembleDevDebug -PhomePreview=true`
Tests: `./gradlew :app:testDevDebugUnitTest --tests 'com.music.bitchord.PreloadPolicyTest' --tests 'com.music.bitchord.HomeLayoutTest' -PhomePreview=true`

The CI APK is named **BitChord Home** (`com.dev.bitchord.home`) and installs alongside **BitChord Dev**. The previous CI signing key is unavailable, so this avoids uninstalling the working app. If desired, use Settings → Export data in the previous app and Import data in this one; sign-in may need to be repeated. CI caches the new preview signing key for later builds. If that cache is deleted, use a securely stored signing key for future updates.
