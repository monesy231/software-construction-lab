# 实验二 AI 使用日志

- **课程**：软件构造实践
- **项目名称**：`AircraftWar2026`
- **姓名 / 学号**：项目成员 / 
- **日期**：2026-11-16

---

| 序号 | 所属阶段 | 原本想怎么做 | 向 AI 求助的问题 | 采纳的建议与关键原理 | 实际修改的代码与当前版本的验证结果 |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | Step 1 ~ Step 2 | 直接将 Windows 端 `src/images/` 和纯逻辑类搬进 Android 工程 | Android 编译提示资源名错误及 `BufferedImage`/`ImageIO` 不存在，咨询最小适配改法；同时排查 `HeroAircraft`、`AbstractFlyingObject` 与难度加速逻辑缺陷 | 1. Android `res/drawable` 仅允许 `a-z`、`0-9`、`_`，图片需改用 `BitmapFactory.decodeResource()`；<br>2. `HeroAircraft` 改懒汉式单例避免图片未解码时 NPE（且不加硬编码兜底，保证先 `initImage` 再调用），并在 `resetState()` 递增 `fireVersion` 隔离旧局火力恢复线程；<br>3. `AbstractFlyingObject` 连续冻结会覆盖原速为 `0`、边界冻结 (`speedX==0`) 会逐帧翻转 `baseSpeedX`，且 `Game.speedUpCurrentEnemies` 在减速期读取缩放后速度会拉低基础速度；需保存 `baseSpeedX/Y`、`effectRate`、`effectVersion`，提供 `getBaseSpeedY()` 并在 `forward()` 限定 `speedX != 0` 才触边反向。 | 重命名 `elite_plus.png`、`elite_pro.png`、`prop_bullet_plus.png`；`AbstractFlyingObject` 替换 `Bitmap` 并修复连续冻结、边界冻结翻转与减速期调速恢复逻辑；`Game.speedUpCurrentEnemies` 改为读取 `getBaseSpeedY()`；改写 `ImageManager` 与空壳 `MusicManager`；其余 36 个纯逻辑类原样保留。本版本已完成 `gradle clean assembleDebug lintDebug --offline`（Lint 0 错误、25 警告）及基于当前类字节码的临时 JVM 复现验证（减速期加 1 恢复为 6、左边界冻结 1 秒后以 `+3` 向右恢复移动）。 |
| 2 | Step 3 | 仅在 `MainActivity.onCreate` 调用 `setContentView(new EasyGame(this))`，且仅在 `surfaceDestroyed` 停止绘制线程 | 咨询 `SurfaceView` 与 `Activity` 生命周期差异，以及屏幕尺寸改变时英雄机坐标如何处理 | 1. 锁屏或半透明弹窗可能仅触发 `Activity.onPause()` 而不触发 `surfaceDestroyed()`，需由 `MainActivity` 的 `onResume`/`onPause` 与 `SurfaceHolder.Callback` 共同管理单一 `GameThread`；<br>2. 在 `AndroidManifest.xml` 配置 `android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"` 避免尺寸切换重建 `MainActivity`，并在 `surfaceChanged` 初次初始化、后续按新旧宽高比例换算并夹紧英雄机坐标。 | 在 `AndroidManifest.xml` 添加 `configChanges`；在 `MainActivity` 补充 `onResume()`/`onPause()`；在 `Game` 中以 `surfaceReady && activityResumed` 门控 `startLoopIfReady()`，在 `pauseGame()` 与 `surfaceDestroyed()` 统一 `stopLoop()` 并 `join()` 线程。在 `Medium_Phone_API_37.0` 模拟器实测：同一进程 PID `4417` 内动态缩小分辨率 `1080x2400 -> 720x1600`（`hero=(385,1106), score=15, hp=250`）、恢复 `1080x2400`、横屏旋转 `2400x1080`（`hero=(1284,747), score=65, hp=120`）及转回竖屏 `1080x2400` 均保留得分与生命；PID 用于核对进程连续性，Activity 未重建结合 `configChanges` 配置与非零游戏状态连续保留判断；锁屏、Home 键切换输出成对 `GameThread stopped` / `GameThread started`，无崩溃或残留日志。 |
| 3 | Step 4 | 在 `HeroController` 中仅检查触点坐标位于 `[0, WINDOW_WIDTH]` 范围内 | 实机/模拟器拖动到屏幕边缘时为何半架飞机被裁切，如何按精灵尺寸限位及安全结束线程 | `HeroAircraft` 的 `locationX/Y` 为图片中心点，需按 `halfW = heroAircraft.getWidth() / 2`、`halfH = heroAircraft.getHeight() / 2` 对触点做边界夹紧；游戏结束在 `GameThread` 先绘制 `GAME OVER` 再退出循环。 | 重写 `HeroController` 按半宽半高夹紧位置；在 `Game.run()` 串通生成、射击、移动、碰撞、清屏、结束判定与绘制。在 `Medium_Phone_API_37.0` 模拟器安装最终 `app-debug.apk`（`1,308,988` 字节）实测：全屏滚动背景与红字 HUD 正常显示；左右拖至屏幕边缘 `x=0` / `x=1080` 整机完整保留未裁切；自动射击击落敌机及加血道具掉落正常；撞击敌机后生命归零并绘制 `GAME OVER` 与 `SCORE: 135`（`Logcat`: `Game Over! Score = 135`）。 |

---

### 本次会话工作分工与验证说明
- **AI 参与内容**：辅助梳理跨平台迁移方案、定位原版冻结恢复与双生命周期边界问题、参与代码编写与调整，以及协助执行构建、JVM 复现和模拟器所列场景实测。
- **当前版本已验证项**：
  1. `gradle clean assembleDebug lintDebug --offline`：`BUILD SUCCESSFUL`，生成 `1,308,988` 字节 `app-debug.apk`，Lint 报告 0 错误、25 警告；
  2. 基于当前编译字节码的临时 JVM 复现验证：`EliteProEnemy` 减速期难度加 1 到期后恢复基础速度 `6`；边界 `locationX=0` 处冻结恢复后保持正向速度 `+3` 向右移动（`3, 6, 9`）；
  3. `Medium_Phone_API_37.0`（API 37）模拟器所列场景实测：冷启动安装运行、全屏画面与左右边缘夹紧（`02_left_edge`/`03_right_edge`）、自动射击计分与道具掉落、动态缩放（`720x1600`、`1080x2400`）与横竖屏旋转（`2400x1080`）保留得分与血量并按比例缩放夹紧坐标、锁屏/解锁及 Home 键启停线程（`GameThread started` / `GameThread stopped`）、撞击敌机结束并显示 `GAME OVER` / `SCORE: 135`；
  4. 四步 Git 提交历史（`stage-commit`、`stage-commit`、`stage-commit`、`stage-commit`）与构建目录（`C:/Projects\AircraftWar2026`）同步一致。
- **课程仓库**：[https://gitee.com/zhu-jinpeng/ruanjiangouzaoshijian](https://gitee.com/zhu-jinpeng/ruanjiangouzaoshijian)，保留四步迁移提交，文档修订单独提交。
- **后续工作**：实验三～七继续完成页面跳转、音效、本地存储与联网功能。
