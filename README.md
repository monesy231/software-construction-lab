# 软件构造实验

将已有的 Java Windows 桌面版《飞机大战》迁移到 Android，并逐步完成界面、音效、本地排行榜、云端排行榜和双人得分对战。本仓库保存《软件构造实践》课程项目的 Android 源码、实验文档和分阶段提交记录。

**实验一至四已完成。** 从主菜单可选择难度、设置音乐与音效或打开本地排行榜。

## 项目要完成什么

最终交付一款可以在 Android 手机或模拟器上运行的飞机大战游戏，完成以下功能：

1. **迁移原有玩法**：保留英雄机、敌机、子弹、碰撞计分、道具及不同射击策略，将桌面绘制与鼠标操作改为 Android 绘制与触屏操作。
2. **完善单机体验**：提供简单、普通、困难三种难度选择，完成开始页面、游戏页面、结束流程、背景音乐和战斗音效。
3. **保存游戏成绩**：按难度记录玩家昵称、分数和时间，支持本地排行榜展示与记录管理。
4. **增加两项网络功能**：实现云端排行榜，以及双人同房间开局、实时交换得分和生命状态、最终比较成绩的对战模式。
5. **完成课程交付**：保留分阶段 Git 记录，整理任务书、AI 使用日志、运行截图和结题报告。

## 实验安排与交付内容

下表依据项目任务书和技术方案组织后续开发。

| 阶段 | 要做的事情 | 交付内容 | 进度 |
| --- | --- | --- | --- |
| 实验一：项目规划 | 分析 Windows 源码的可复用部分和 Android 适配部分，设计至少两项网络功能，明确分工与预期成果 | 开题任务书 | 已完成 |
| 实验二：代码迁移 | 搭建 Android 工程，迁移图片和游戏模型，使用 `SurfaceView` 绘制，接入触屏控制及完整游戏循环 | 可运行源码、四阶段迁移提交、AI 使用日志 | 已完成 |
| 实验三：界面与音效 | 选择三种难度，切换音乐与音效，进入对应场景并查看本局结算 | 难度菜单、游戏页面、原版配乐与音效 | 已完成 |
| 实验四：本地排行榜 | 录入昵称并保存成绩，按难度查询，支持添加、删除和重启后读取 | `ListView`、自定义 `BaseAdapter` 与 SQLite DAO | 已完成 |
| 实验五：网络功能 | 开发云端排行榜服务及客户端，完成双人房间、同步开局、得分与生命状态交换、胜负结算 | 服务端、Android 网络模块、两项联网功能演示 | 待开发 |
| 实验六：扩展完善 | 整合高级敌机、冰冻道具、强化弹道等已有机制，完善扩展玩法与交互体验 | 扩展功能演示 | 待开发 |
| 实验七：联调与结题 | 完成系统联调，整理操作说明、运行截图、核心设计和提交记录 | 最终工程与结题报告 | 待开发 |

## 当前版本能做什么

从难度菜单进入单局游戏后，可以体验以下功能。

- 滚动背景与游戏画面绘制，显示当前难度、得分和生命值。
- 手指按下或拖动屏幕控制英雄机，移动到画面边缘时保持整架飞机可见。
- 自动射击、敌机生成和移动、碰撞判定、击落计分及出界对象清理。
- 道具掉落与效果处理，保留原项目的敌机工厂、射击策略和道具观察者等结构。
- 生命值归零后显示本局难度和得分。输入昵称保存本局成绩后，会打开对应难度的排行榜。也可以直接再玩一局或返回菜单。
- 切到后台或锁屏时暂停游戏与音频，返回前台可继续本局。
- 窗口尺寸变化时，背景随画布调整，英雄机、敌机、子弹和道具按比例换算位置，本局得分和血量保留。Boss 贴图宽于窗口时居中显示。

实验三按讲义和两份课程示例优化实现。`MainActivity` 从 XML 加载难度菜单，用控件监听器响应操作，并将音频开关持久化至 `SharedPreferences`，通过 `Intent` 传递难度和音频设置。参考 `HandlerDemo(1).rar`，游戏子线程结束时由回调通过 `Message.obtain()` 发送 `Message`（`what` 标识结束事件、`arg1` 携带得分），`GameActivity` 通过静态弱引用 `GameHandler` 重写 `handleMessage()` 在主线程处理结算弹窗；若界面已暂停则暂存并在恢复前台时显示，销毁时主动关闭弹窗避免窗口泄漏。

按实验三讲义，`public class MyMediaPlayer` 封装普通和 Boss 循环配乐（`playBgm` / `playBossBgm` / `stopBossBgm`），Boss 出场时暂停普通配乐，退场后从暂停处继续。`public class MySoundPool` 参考 `Actvity_SoundPool.zip`，使用 `AudioAttributes` 与 `SoundPool.Builder`（10 路并发）预加载 `res/raw` 音效并通过 `soundPoolMap` 映射样本 ID，加载完成后按优先级播放子弹命中、道具拾取、炸弹爆炸和游戏结束音（对未就绪关键音效提供加载完成补播）。`MusicManager` 显式维护开关、暂停与释放状态，在主线程 `Handler` 上顺序分派请求并在退出时释放资源。

针对 `SurfaceView` 与跨平台稳定性，`ImageManager` 缓存原始背景图与当前难度，避免同分辨率反复解码或二次缩放；`AbstractFlyingObject` 对坐标与存活标志使用 `volatile` 保证跨线程可见性；`Game` 开启抗锯齿画笔、在 `draw()` 中对 `SurfaceHolder` 加锁并结合 `WindowInsets` 避让状态栏与刘海区域，同时阻止游戏结束后因窗口变化或生命周期恢复误重启 `GameThread` 或恢复配乐（仅在画布就绪时重绘结束画面）。

## 实验四本地排行榜

- 在主菜单打开**本地排行榜**时先查看简单榜，可切换到普通或困难榜。列表显示名次、昵称、得分和记录时间，并高亮第一名。
- 在排行榜内可使用**添加记录**手动录入昵称和得分，或通过行末按钮确认删除指定记录。每档按得分降序排列，同分时较新的记录优先。
- `ScoreBoardActivity` 使用 `ListView` 和自定义 `RankAdapter` 复用行视图，记录变更后调用 `notifyDataSetChanged()` 刷新列表。两种存储实现共用 `ScoreDao` 接口，应用使用 `RankDbHelper` 管理私有 SQLite 数据库 `rank.db`。存储操作在后台执行，成绩在应用重启后仍可读取。

## 两项网络功能怎么做

| 功能 | 玩家能做什么 | 实现思路 | 完成时要验证什么 |
| --- | --- | --- | --- |
| 云端排行榜 | 游戏结束后上传昵称、分数、难度和时间，在排行榜页面查看相应难度的云端成绩 | 使用 HTTP + JSON；服务端提供成绩提交和排行榜查询接口，Android 在后台线程请求并回到主线程更新界面 | 一台设备上传的成绩可以在另一台设备查询，并按对应难度正确排序 |
| 双人实时得分对战 | 两名玩家进入同一房间并同步开局，查看对手得分和生命状态，双方结束后比较分数 | 使用 TCP Socket 连接房间服务端；约每 500ms 交换分数、生命值和结束状态，由服务端协调开局及结算 | 两台设备成功配对开局，能看到对手状态变化，最终结算结果一致 |

## 如何运行

### 开发环境

| 项目 | 当前配置 |
| --- | --- |
| Android 工程名 / 应用包名 | `AircraftWar2026` / `edu.hitsz` |
| 设备最低版本 | Android 11（API 30） |
| 编译 / 目标 SDK | API 37 / API 37 |
| SDK Build Tools | `36.0.0` |
| Java 源码级别 | Java 17 |
| Android Gradle Plugin / Gradle | `9.1.1` / `9.3.1` |
| 已验证的构建 JDK | Android Studio 内置 JBR 25 |

### 在 Android Studio 中运行

1. 将仓库克隆到英文路径，例如：

   ```powershell
   git clone https://gitee.com/zhu-jinpeng/software-construction-lab.git C:\Projects\AircraftWar2026
   ```

2. 在 Android Studio 中打开 `C:\Projects\AircraftWar2026`，选择包含 `settings.gradle` 的工程根目录。
3. 安装上表中的 SDK 和 Build Tools，使用 Android Studio 内置 JBR 25 同步 Gradle。SDK 路径由本机的 `local.properties` 保存。
4. 启动 API 30 或以上的模拟器，或连接已开启 USB 调试的 Android 手机。
5. 选择 `app` 运行配置并点击 **Run**。在菜单中设置音频并选择难度，进入游戏后拖动屏幕控制英雄机。射击自动进行。

### 命令行构建

在工程根目录执行：

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

工程已配置 `android.overridePathCheck=true`、`org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=COMPAT` 与 `JavaCompile` UTF-8 编码选项，可在 Android Studio Panda 及中文父目录下直接运行 JUnit 4 单元测试、构建与 Lint。调试安装包生成于 `app/build/outputs/apk/debug/app-debug.apk`，测试与 Lint 报告分别位于 `app/build/reports/tests/testDebugUnitTest/index.html` 与 `app/build/reports/lint-results-debug.html`。

## 代码与文档在哪里

```text
AircraftWar2026/
├── app/src/
│   ├── main/
│   │   ├── AndroidManifest.xml       # 应用入口与 Activity 配置
│   │   ├── java/edu/hitsz/
│   │   │   ├── MainActivity.java     # 难度选择与音频开关
│   │   │   ├── GameActivity.java     # 游戏入口、前后台切换、GameHandler 与结算
│   │   │   ├── ScoreBoardActivity.java # 分难度排行榜与记录管理
│   │   │   ├── application/          # 游戏循环、绘制、图片缓存、触控与难度规则
│   │   │   ├── aircraft/             # 飞机模型
│   │   │   ├── bullet/               # 子弹模型
│   │   │   ├── supply/               # 道具
│   │   │   ├── observer/             # 道具效果通知
│   │   │   ├── factory/              # 敌机与道具创建
│   │   │   ├── strategy/             # 射击策略
│   │   │   └── music/                # MusicManager、MyMediaPlayer 与 MySoundPool
│   │   └── res/                      # Android 图片、布局、音频与样式资源
│   └── test/java/edu/hitsz/          # JUnit 4 单元测试（HeroAircraftTest、GameCoreLogicTest）
├── docs/                             # 任务书、AI 日志与技术方案
└── gradle/                           # Gradle Wrapper
```

| 文档 | 用途 |
| --- | --- |
| [实验一任务书](docs/实验一任务书.md) | 查看项目目标、迁移分析、两项网络功能设计及预期成果 |
| [实验二 AI 使用日志](docs/实验二AI使用日志.md) | 查看迁移过程中使用 AI 的情况、实际修改和验证记录 |
| [技术方案](docs/技术方案.md) | 查看四步迁移路线、实验三和实验四与课程示例的对照，以及后续阶段实现计划 |

## 验证与提交记录

实验二已有构建和运行记录：构建与 Lint 检查通过；在 API 37 模拟器上验证了启动、触控边界、射击计分、道具掉落、游戏结束、锁屏与前后台切换，以及动态分辨率调整和横竖屏旋转。具体操作和结果见 [AI 使用日志](docs/实验二AI使用日志.md)。

2026-11-24 的实验一至三版本通过 `.\gradlew.bat clean testDebugUnitTest assembleDebug lintDebug --offline --no-daemon`：**12 项 JUnit 4 单元测试全部通过**（`HeroAircraftTest` 7 项、`GameCoreLogicTest` 5 项，0 失败、0 跳过），生成 `10,692,823` 字节调试 APK，Lint 为 **0 错误、26 条预期警告**（均来自原图资源目录与模板构造函数提示，`Activity` 与音频/游戏核心类 0 警告）。在 `emulator-5554` 模拟器完成全流程实测：三档难度按钮与音频开关 `SharedPreferences` 持久化、开启音频进入 `HardGame` 时 `MediaPlayer`（`looping=true`）与 `SoundPool`（`USAGE_GAME`）运行正常、边缘触屏夹紧、锁屏休眠与 Home/最近任务切换成对启停 `GameThread`、分辨率缩放（`1080×2400 -> 720×1600 -> 1080×2400`）与横竖屏旋转（`2400×1080 -> 1080×2400`）按比例换算坐标并保留得分血量、结算弹窗显示期间尺寸变化不误重启线程与音频、“再玩一局”与“返回菜单”正常流转、静音模式无活跃 `MediaPlayer`，全过程 Logcat **0 `FATAL EXCEPTION`、0 `ANR`、0 `WindowLeaked`**。

Git 历史保留四个迁移阶段：**空 Android 工程 → 图片资源与游戏模型 → SurfaceView 渲染 → 触屏与完整游戏循环**；实验三新增难度选择菜单、`GameActivity` 结算流程以及 `MediaPlayer`/`SoundPool` 音频系统提交。实验四新增 SQLite 本地排行榜。

仓库地址：Gitee [软件构造实验](https://gitee.com/zhu-jinpeng/software-construction-lab) ｜ GitHub [software-construction-lab](https://github.com/monesy231/software-construction-lab)。
