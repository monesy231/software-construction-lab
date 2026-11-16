# AircraftWar2026

《软件构造实践》实验一、实验二。项目成员，，，单人组。

## 实验内容

- 实验一：项目概述、原功能迁移分析、两项网络功能规划、分工及预期成果。
- 实验二：Android Java工程、SurfaceView绘制、触屏控制、自动射击、碰撞计分、道具逻辑和游戏结束显示。
- 游戏默认进入简单模式。拖动屏幕控制英雄机；切到后台暂停，返回后继续本局。

## 开发与构建

工程名和包名分别为 `AircraftWar2026`、`edu.hitsz`，最低支持 Android 11（API 30）。Java源码级别为17，使用AGP 9.1.1、Gradle 9.3.1、compileSdk/targetSdk 37和Build Tools 36.0.0。本次构建使用Android Studio内置JBR 25。

在无中文路径打开工程，例如 `C:/Projects/AircraftWar2026`，在Android Studio同步Gradle并运行 `app`。

命令行构建：

```powershell
.\gradlew.bat assembleDebug lintDebug
```

本机SDK路径写入 `local.properties`。调试APK生成于 `app/build/outputs/apk/debug/app-debug.apk`。

## 文档

- [实验一任务书](docs/实验一任务书.md)
- [实验二AI使用日志](docs/实验二AI使用日志.md)
- [技术方案](docs/技术方案.md)

AI日志记录所列模拟器场景与JVM检查结果。实验三至实验七继续实现页面、音效、本地排行榜及网络功能。

## Git记录

保留四个迁移阶段提交：空壳工程、资源与模型、SurfaceView渲染、触屏与完整逻辑。文档修订单独提交。

课程仓库：[Gitee](https://gitee.com/zhu-jinpeng/ruanjiangouzaoshijian)。
