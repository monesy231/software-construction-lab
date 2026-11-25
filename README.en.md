# Software Construction Lab

A course project that migrates an existing Java Windows aircraft shooting game to Android, then adds menus, audio, local score storage, cloud rankings, and two-player score competition.

**Labs 1 through 4 are complete.** The start menu offers three difficulty levels, an audio switch, and access to the local leaderboard.

## Project goals and roadmap

| Stage | Work and deliverables | Status |
| --- | --- | --- |
| Lab 1 | Migration analysis, project task book, and designs for two network features | Complete |
| Lab 2 | Android project, images and game models, SurfaceView rendering, touch control, full game loop, four migration commits, and AI usage log | Complete |
| Lab 3 | Difficulty menu, audio switch, game scenes, result dialog, music, and sound effects | Complete |
| Lab 4 | Player-name entry, per-difficulty rankings, add and delete actions, and SQLite persistence | Complete |
| Lab 5 | Cloud leaderboard service and client, two-player rooms, synchronized start, live score and life status, and result comparison | Planned |
| Lab 6 | Integrate advanced enemy, supply, and shooting mechanics; improve extended gameplay and interaction | Planned |
| Lab 7 | Integration testing, usage instructions, screenshots, design notes, and final report | Planned |

## What works now

- Scrolling background, difficulty/score/life display, and touch-controlled movement with sprite-sized screen bounds.
- Automatic shooting, enemy generation and movement, collision scoring, supply effects, and off-screen object cleanup.
- A result dialog shows the difficulty and final score. Save the score under a nickname to open the leaderboard for that difficulty, or replay or return to the menu without saving.
- Gameplay and audio pause when the screen locks or the app moves to the background, then resume on return.
- Window resizing adjusts the background and scales the positions of aircraft, bullets, and supplies while preserving score and health. A Boss sprite wider than the window stays centered.

`MainActivity` loads the difficulty menu from XML, registers listeners, persists the audio toggle in `SharedPreferences`, and passes the difficulty and audio setting in an `Intent`. Following `HandlerDemo(1).rar`, the game thread reports a game-over `Message` via `Message.obtain()` with the event type in `what` and the score in `arg1`. `GameActivity` uses a static `WeakReference` subclass `GameHandler` overriding `handleMessage()` to display the result dialog on the main thread, defers pending results until resumed, and dismisses any active dialog in `onDestroy()` to avoid window leaks.

Following the Lab 3 slides, `MyMediaPlayer` (`public class`) wraps regular and Boss looping tracks and pauses the regular track while Boss music plays, resuming from the paused position afterward. `MySoundPool` (`public class`) follows `Actvity_SoundPool.zip` with `AudioAttributes` (`USAGE_GAME`), 10 max concurrent streams, and a `soundPoolMap` sample registry to play hit, pickup, explosion, and game-over effects once loaded (with load-completion playback compensation for critical effects). `MusicManager` tracks explicit enabled/paused/released flags and dispatches requests in order on the main-thread `Handler`.

To align with `SurfaceViewDemo` and Lab 2 guidelines, `ImageManager` caches the raw background bitmap and loaded difficulty to avoid redundant decoding/scaling, `AbstractFlyingObject` marks coordinates and validity `volatile`, and `Game` uses an anti-aliased `Paint`, `synchronized (mSurfaceHolder)` around canvas drawing, `WindowInsets` safe margins for the HUD, and game-over guards so resizing or resuming while ended repaints the final frame without restarting `GameThread` or music.

## Lab 4 local leaderboard

- Open the local leaderboard from the menu to view Easy scores, then switch to Normal or Hard. Each list shows rank, nickname, score, and time, and highlights first place.
- Add a record by entering a nickname and score, or confirm a deletion using the button on its row. Each difficulty sorts scores from highest to lowest, with newer records first when scores tie.
- `ScoreBoardActivity` uses a `ListView` and custom `RankAdapter` to reuse row views. The adapter calls `notifyDataSetChanged()` after record changes. The file and SQLite implementations share the `ScoreDao` interface. `RankDbHelper` manages the private `rank.db` database that the app uses to retain scores across restarts. Storage operations run in the background.

## Planned network features

1. **Cloud leaderboard:** upload player nickname, score, difficulty, and time through HTTP + JSON, then query rankings by difficulty. Completion requires a score uploaded on one device to appear on another device in the correct ranking order.
2. **Two-player score competition:** join a shared room through TCP Socket, start together, exchange score/life/end status about every 500 ms, and compare final scores after both players finish. Completion requires matching opponent status and final results on both devices.

## Build and run

The project is `AircraftWar2026`, with application ID `edu.hitsz`. Its configuration uses Android 11 (API 30) as the minimum, compile/target SDK 37, Build Tools 36.0.0, Java source level 17, AGP 9.1.1, and Gradle 9.3.1. The recorded build uses Android Studio's bundled JBR 25.

1. Clone the repository into an English-only path, such as `C:\Projects\AircraftWar2026`.
2. Open the directory containing `settings.gradle` in Android Studio, install the configured SDK/Build Tools, select bundled JBR 25, and sync Gradle.
3. Run `app` on an API 30+ emulator or Android device with USB debugging enabled.
4. Set the audio switch and choose a difficulty. Drag on the screen to move the hero aircraft. Shooting is automatic.

From the project root, run the unit tests, build the debug APK, and run Android Lint:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

`gradle.properties` sets `android.overridePathCheck=true` and `-Dfile.encoding=COMPAT` alongside explicit UTF-8 `JavaCompile` options in `app/build.gradle`, so Android Studio Panda can sync, build, and run JUnit 4 unit tests directly even under a Chinese parent directory. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`, test results at `app/build/reports/tests/testDebugUnitTest/index.html`, and the Lint report at `app/build/reports/lint-results-debug.html`.

## Documentation and validation

See [README.md](README.md) for the Chinese project guide, [project task book](docs/实验一任务书.md), [AI usage log](docs/实验二AI使用日志.md), and [technical plan](docs/技术方案.md).

Recorded Lab 2 checks cover build/Lint, API 37 emulator launch, touch boundaries, shooting and scoring, supplies, game over, pause/resume, resolution changes, and rotation. Git history preserves the four migration stages: empty Android project, resources and models, SurfaceView rendering, and touch control with the full game loop, followed by the Lab 3 commits.

The latest Lab 1–3 optimization build passed `clean testDebugUnitTest assembleDebug lintDebug --offline --no-daemon` with **12/12 JUnit 4 unit tests passing** (`HeroAircraftTest`: 7, `GameCoreLogicTest`: 5; 0 failed, 0 skipped), produced a `10,692,823`-byte debug APK, and passed Lint with **0 errors and 26 expected warnings**. End-to-end checks on `emulator-5554` verified menu difficulty buttons, `SharedPreferences` persistence for `audio_switch`, active `MediaPlayer` and `SoundPool` (`USAGE_GAME`) playback in `HardGame`, touch boundary clamping, sleep/wake and Home/Recents `GameThread` pause/resume, proportional position scaling across resize (`1080×2400 -> 720×1600 -> 1080×2400`) and rotation (`2400×1080 -> 1080×2400`), no spurious thread/audio restart when resizing while the result dialog is open, replay (`recreate()`), return to menu (`finish()`), silent mute mode, and **0 `FATAL EXCEPTION`, `ANR`, or `WindowLeaked`** entries in Logcat.

Repositories: Gitee [Software Construction Lab](https://gitee.com/zhu-jinpeng/software-construction-lab) | GitHub [software-construction-lab](https://github.com/monesy231/software-construction-lab).
