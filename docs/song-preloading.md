# Song loader update

Based on `iteratehello-glitch/BitChord`, branch `main`, commit
`04a4a83889bb8fa5bd6ee6f6097f0c6608671d68`.

## Using it

Open the Android app's **Settings → Playback → Song loader**:

- **After this song ends:** disables the background queue preloader. Normal
  player gapless buffering, Crossfade and Automix can still prepare transitions.
- **Preload 1 song:** loads the next queued song while the current song plays.
  This is the default.
- **Preload 3 songs:** loads up to the next three queue entries, one at a time.

Preloading begins after a configurable delay from 0 to 30 seconds (default 7)
so the current song can buffer first. Set 0 for instant loading. Changing the setting takes effect immediately and is saved across
app restarts. Pause, stop, repeat-one, queue changes and skipping cancel outdated
background work. Repeat-all wraparound follows the player's next-item order.

The loader uses the existing streaming quality/source selection, including
configured lossless sources, rather than the separate offline-download quality.
It asks available higher-ranked sources before falling back to ordinary playback
resolution. It does not turn lossy audio into lossless or add a new music provider.
Actual quality depends on the configured sources and their available recordings.

Local/downloaded songs occupy their queue slots without being downloaded again.
Audio goes into the existing bounded song cache, not Downloads. Preloading uses
mobile data on a cellular connection. Slow networks, unavailable sources or a
small cache can prevent a full preload; this is not a guarantee of zero gaps or
permanent offline availability.

## Apply the updated-files ZIP

1. Download/extract the update ZIP.
2. Copy its `app` and `docs` folders into your BitChord checkout, merging folders
   and replacing the matching files. Keep their paths exactly as supplied.
3. Review the diff, build/test, then commit and push your checkout to GitHub.

For GitHub's web uploader, open the repository root, choose **Add file → Upload
files**, and drag the extracted `app` and `docs` folders into it. Confirm the
relative paths before committing. Upload the extracted source files, not the ZIP.

Alternatively, apply `BitChord-song-loader.patch` from the repository root with
`git apply --check BitChord-song-loader.patch`, then
`git apply BitChord-song-loader.patch`. Use either the ZIP or patch, not both.

The full-source ZIP is a separate complete source snapshot with these changes,
without Git history or build outputs. The current implementation is on the `feature/home-customization` branch.

## Build and verification

Use JDK 17 and the Android SDK/NDK/CMake required by the repository. The checked-out
`app/build.gradle.kts` requires **compileSdk 37**, even though CONTRIBUTING.md
currently mentions 36. No dependencies were added by this update.

On Windows, from the repository root:

```powershell
.\gradlew.bat :app:testDevDebugUnitTest --tests "com.music.bitchord.PreloadPolicyTest"
.\gradlew.bat :app:assembleDevDebug
```

On macOS/Linux, replace `.\gradlew.bat` with `./gradlew`.

Included JUnit coverage checks disabled loading, one/three queue slots, short
queues, repeat-all, repeat-one, shuffle traversal, invalid/cyclic queues, and
saved setting decoding. The Gradle test command was attempted during preparation
but stopped before compilation: Gradle 8.11.1 could not be downloaded because
network access was unavailable. Therefore neither the Android build nor these
JUnit tests have been verified as passing, and no APK is included.

Before release, run the commands above and check on a device:

1. Select each loader mode and restart the app to check persistence.
2. Queue four distinct songs. Check cache growth for the next one or three while
   the first plays; repeat with a configured lossless source and ordinary audio.
3. Pause, skip, reorder the queue and switch the loader off during a download.
   Check that obsolete downloads stop and playback remains usable.
4. Try repeat-one, repeat-all at the queue boundary, shuffle, downloaded/local
   songs and Music-only conversion of video tracks.
5. Test an unavailable source and a lost connection; the current song should not
   fail because of a background preload, and ordinary playback recovery remains
   responsible for any later failure when the queued song is reached.

## Implementation

`AppSettings` persists a `SongPreloadMode`; the Settings sheet exposes the loader
selector. `PreloadPolicy` selects upcoming entries in actual playback order.
`PlaybackService` observes the preference and passes full playback URIs to
`AudioCache`, preserving source/mix/recording metadata.

The cache writer reads the selected stream to its own end rather than using a
YouTube byte count for a potentially much larger FLAC/ALAC rendition. Downloads
run sequentially with interruptible cancellation and blocking cache locks to
avoid downloading uncached duplicate data behind an existing writer. Source-backed
tracks retain their chosen stream across partial-cache continuation reads; cache
invalidation releases that choice.
