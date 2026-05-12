# AI Translate

AI Translate 是一个 Android 原生大模型翻译 App，主线能力是“云端翻译 + 本地离线翻译 + 跨 App 快捷入口”。当前版本已经支持文本翻译、OpenAI 兼容 API、多供应商配置、Hy-MT Q4_K_M 离线模型、翻译历史、系统划词/分享入口、剪贴板快捷翻译提示和半自动悬浮球翻译。

## 当前能力

- 文本翻译首页：支持源语言自动检测、目标语言选择、复制译文和翻译状态提示。
- 三种翻译模式：云端、离线、自动；自动模式会优先尝试云端，失败后在模型可用时回退离线。
- OpenAI 兼容云端接口：支持 Base URL、API Key、模型名称配置。
- 多供应商管理：内置 OpenAI、DeepSeek、OpenRouter 和自定义兼容接口，可切换当前供应商。
- 模型列表获取：通过 `/v1/models` 拉取模型列表，支持搜索、选择和手动添加模型。
- 离线翻译：使用 HY-MT1.5-1.8B Q4_K_M GGUF，接入 Llamatik/llama.cpp Android 推理库。
- 离线模型管理：支持首次下载、状态展示、进度展示、删除和基础文件校验。
- 历史记录：使用 Room 保存翻译历史，列表折叠预览，点击后查看完整详情。
- 系统划词与分享：注册 `ACTION_PROCESS_TEXT` 和 `ACTION_SEND`，可从其他 App 把文本带入快速翻译面板。
- 剪贴板快捷翻译：App 打开或回到前台时检测剪贴板文本，用户确认后进入快速翻译。
- 悬浮球翻译：设置页可引导授权悬浮窗，开启悬浮球后点击读取剪贴板并显示悬浮迷你翻译窗。
- 本地图标：供应商和模型标识使用 Lobe Icons 静态 PNG 资源，未匹配模型保留文字兜底。

## 技术栈

- Kotlin + Jetpack Compose + Material 3
- Android ViewModel + Kotlin Coroutines + Flow
- DataStore Preferences：保存 API、供应商、模型和默认模式配置
- Room + KSP：保存翻译历史
- OkHttp：请求 OpenAI 兼容接口和下载模型
- Llamatik / llama.cpp Android：本地 GGUF 推理
- JUnit + kotlinx-coroutines-test：单元测试

## 环境要求

- JDK 21
- Android SDK，当前 `compileSdk = 36`，`targetSdk = 36`
- 最低支持 Android 8.0，`minSdk = 26`
- Gradle Wrapper 已随项目提交，优先使用根目录的 `gradlew.bat`

## 常用命令

```powershell
# 运行单元测试
.\gradlew.bat testDebugUnitTest --no-daemon

# 构建 Debug APK
.\gradlew.bat assembleDebug --no-daemon

# 同时测试并构建
.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon
```

构建成功后，Debug APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 离线模型说明

当前离线模型为腾讯 HY-MT 标准 Q4_K_M GGUF：

- 文件名：`HY-MT1.5-1.8B-Q4_K_M.gguf`
- 体积：约 1.13GB
- 下载源：Cloudflare R2 分片下载，原始模型来自 `tencent/HY-MT1.5-1.8B-GGUF`
- 真机验证 SHA256：`4383AC0C3C8E476DE98FF979C2A3F069F8C4FB385E7860CF2D28DA896CC477C7`

大模型文件不内置进 APK，采用首次下载到 App 私有目录的方案。当前实现会从 R2 下载 6 个分片，拼接为完整 GGUF 后校验文件大小和 SHA256，再保存到 App 私有目录的 `models/` 子目录。

## 权限与系统入口

App 当前声明以下关键权限和入口：

- `INTERNET`：云端翻译、模型列表获取、离线模型下载。
- `SYSTEM_ALERT_WINDOW`：用户授权后显示悬浮球和悬浮迷你翻译窗。
- `ACTION_PROCESS_TEXT`：接收系统划词文本。
- `ACTION_SEND` + `text/plain`：接收其他 App 分享的文本。

剪贴板能力只在 App 前台或用户点击悬浮球后读取，不做后台自动监听，也不会自动提交翻译。

## 项目结构

```text
app/
├── src/main/java/com/mxwis/aitranslate/
│   ├── data/          # 设置、模型、翻译、历史记录数据层
│   ├── di/            # AppContainer 依赖组装
│   ├── domain/        # 语言、翻译模式、外部文本解析
│   ├── overlay/       # 悬浮球与悬浮迷你翻译窗 Service
│   └── ui/            # Compose 页面、ViewModel、主题
├── src/main/res/      # 字符串、主题、图标和 Lobe Icons 资源
└── src/test/          # 单元测试

docs/
├── task文档.md        # 按任务记录目标、范围、完成标准和验证结果
├── todo日志.md        # 执行日志和验证清单
├── 工作文档.md        # 架构决策、模型资料和实现记录
├── 悬浮窗划词翻译规划.md
└── ui/                # 每次 UI 实现前生成的设计图
```

## 当前验证状态

- `testDebugUnitTest`：最近任务记录中通过。
- `assembleDebug`：最近任务记录中通过。
- Q4_K_M 离线翻译：已在真机验证，`hi` 可翻译为 `嗨`。
- 系统划词/分享、剪贴板提示、悬浮球：代码与构建产物已就绪；部分真机交互项在最近日志中仍标记为待设备连接后补充。

## 参考文档

- `docs/工作文档.md`
- `docs/task文档.md`
- `docs/todo日志.md`
- `docs/悬浮窗划词翻译规划.md`
- `docs/Lobe Icons 图标来源.md`
- `docs/ui/README.md`
