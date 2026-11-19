# Software Construction Lab

A course project that migrates an existing Java Windows aircraft shooting game to Android, then adds menus, audio, local score storage, cloud rankings, and two-player score competition.

**Labs 1 through 3 are complete.** The start menu offers Easy, Normal, and Hard modes and an audio switch.

## Project goals and roadmap

| Stage | Work and deliverables | Status |
| --- | --- | --- |
| Lab 1 | Migration analysis, project task book, and designs for two network features | Complete |
| Lab 2 | Android project, images and game models, SurfaceView rendering, touch control, full game loop, four migration commits, and AI usage log | Complete |
| Lab 3 | Difficulty menu, audio switch, game scenes, result dialog, music, and sound effects | Complete |
| Lab 4 | Player-name entry, per-difficulty local rankings, persistent score records, and record management | Planned |
| Lab 5 | Cloud leaderboard service and client, two-player rooms, synchronized start, live score and life status, and result comparison | Planned |
| Lab 6 | Integrate advanced enemy, supply, and shooting mechanics; improve extended gameplay and interaction | Planned |
| Lab 7 | Integration testing, usage instructions, screenshots, design notes, and final report | Planned |

## What works now

- Scrolling background, difficulty/score/life display, and touch-controlled movement with sprite-sized screen bounds.
- Automatic shooting, enemy generation and movement, collision scoring, supply effects, and off-screen object cleanup.
- A result dialog with the selected difficulty and final score, plus options to replay or return to the menu.
- Pause/resume across screen locking and app backgrounding, plus layout updates when the window size changes.

The menu passes the difficulty and audio setting through an `Intent`. `MediaPlayer` handles regular and Boss music. `SoundPool` plays hit, pickup, explosion, and game-over effects. Local rankings and networking follow in Labs 4 and 5.

## Planned network features

1. **Cloud leaderboard:** upload player nickname, score, difficulty, and time through HTTP + JSON, then query rankings by difficulty. Completion requires a score uploaded on one device to appear on another device in the correct ranking order.
2. **Two-player score competition:** join a shared room through TCP Socket, start together, exchange score/life/end status about every 500 ms, and compare final scores after both players finish. Completion requires matching opponent status and final results on both devices.

## Build and run

The project is `AircraftWar2026`, with application ID `edu.hitsz`. Its configuration uses Android 11 (API 30) as the minimum, compile/target SDK 37, Build Tools 36.0.0, Java source level 17, AGP 9.1.1, and Gradle 9.3.1. The recorded build uses Android Studio's bundled JBR 25.

1. Clone the repository into an English-only path, such as `C:\Projects\AircraftWar2026`.
2. Open the directory containing `settings.gradle` in Android Studio, install the configured SDK/Build Tools, select bundled JBR 25, and sync Gradle.
3. Run `app` on an API 30+ emulator or Android device with USB debugging enabled.
4. Set the audio switch and choose a difficulty. Drag on the screen to move the hero aircraft. Shooting is automatic.

From the project root, build the debug APK and run Android Lint:

```powershell
.\gradlew.bat assembleDebug lintDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. The Lint report is at `app/build/reports/lint-results-debug.html`.

## Documentation and validation

See [README.md](README.md) for the Chinese project guide, [project task book](docs/实验一任务书.md), [AI usage log](docs/实验二AI使用日志.md), and [technical plan](docs/技术方案.md).

Recorded Lab 2 checks cover build/Lint, API 37 emulator launch, touch boundaries, shooting and scoring, supplies, game over, pause/resume, resolution changes, and rotation. Git history preserves the four migration stages: empty Android project, resources and models, SurfaceView rendering, and touch control with the full game loop, followed by the Lab 3 commit adding the difficulty menu, `GameActivity` result dialog, and `MediaPlayer`/`SoundPool` audio playback.

Repositories: Gitee [Software Construction Lab](https://gitee.com/zhu-jinpeng/software-construction-lab) | GitHub [software-construction-lab](https://github.com/monesy231/software-construction-lab).
