# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 语言与沟通

- 所有回复、文档、注释一律使用简体中文。
- 面向用户先给结论，再给细节。

## 构建与测试

- 构建 Debug APK：`./gradlew.bat assembleDebug`（Windows）或 `./gradlew assembleDebug`。
- 运行单元测试：`./gradlew.bat testDebugUnitTest`。
- 运行单个测试类：`./gradlew.bat :app:testDebugUnitTest --tests "com.mxwis.aitranslate.data.translation.OfflineTranslationEngineTest"`。
- 运行单个测试方法：在 `--tests` 后追加 `.方法名`，反引号包裹的中文方法名同样适用。
- Lint：`./gradlew.bat lintDebug`。
- 工程要求 JDK 21（`compileOptions` 与 `jvmTarget` 均为 21）。

## 工程结构

单模块 Gradle 工程，只有 `:app`。包根 `com.mxwis.aitranslate`，分层：

- `domain/`：`LanguageOption`、`TranslationMode`、`TranslateRequest/Output` 等纯数据类型。
- `data/settings/`：`SettingsStore` 基于 DataStore Preferences 持久化 Base URL、API Key、模型名、自定义模型列表、默认翻译模式。
- `data/history/`：Room 数据库 `AppDatabase` + `TranslationHistoryDao`，`ai_translate.db`，Schema 导出到 `app/schemas/`（KSP 参数）。
- `data/model/`：`HyMtModelManager` 负责离线 GGUF 模型的下载、校验、删除、路径，状态以 `StateFlow<ModelState>` 暴露。
- `data/translation/`：`CloudTranslationEngine`（OpenAI Chat Completions 兼容）、`OfflineTranslationEngine`（走 `com.llamatik.library` 的 `LlamaBridge`）、`TranslationRepository`（统一入口，承担 AUTO 回退与历史记录写入）。
- `di/AppContainer.kt`：手写依赖容器，单例 `OkHttpClient`、Room、SettingsStore、ModelManager、Repository；在 `AiTranslateApplication.onCreate` 构造，`MainActivity` 通过 `viewModelFactory` 注入 `TranslateViewModel`。
- `ui/`：`AiTranslateApp` 为唯一 Compose 根，`TranslateViewModel` 用一个 `TranslateUiState` 驱动四个 Tab（翻译/历史/模型/设置）。主题在 `ui/theme/AppTheme.kt`。

## 架构要点（需读多文件才能理解的"大图"）

- **MVVM + 单一 UiState**：整个 App 只有一个 `TranslateViewModel` 持有一个 `TranslateUiState`，`collect` 三条 `Flow`（settings、history、modelState）合并进状态；所有页面切换、翻译、下载、模型选择均通过该 ViewModel 的方法触发，UI 层无独立 ViewModel。
- **三种翻译模式**（`TranslationMode`：`CLOUD` / `OFFLINE` / `AUTO`）统一走 `TranslationRepository.translate`，`AUTO` 先试云端，失败再判断 `HyMtModelManager.isModelAvailable()` 后回退离线；翻译成功后**无条件**写入一条 Room 历史记录。
- **云端端点解析**：`CloudTranslationEngine` 的 `resolveEndpoint` / `resolveModelsEndpoint` 会根据 Base URL 结尾补齐 `/v1/chat/completions` 或 `/v1/models`，兼容用户填入 `https://api.openai.com`、`.../v1`、`.../chat/completions` 三种写法。修改相关逻辑时需同步更新这两处。
- **离线推理生命周期**：`OfflineTranslationEngine` 使用 `Mutex` 守护模型加载，`loadedModelPath` 幂等；首次调用时 `LlamaBridge.updateGenerateParams(...)` + `initGenerateModel(modelPath)`，之后复用。`cleanOutput` 负责剥离 `<start_of_turn>assistant` / `<|im_end|>` 等模板标记并 `trim('"')`。
- **模型文件合约**：`HyMtModelManager` 下载到 `filesDir/models/HY-MT1.5-1.8B-Q4_K_M.gguf`，下载时写 `.part` 临时文件并在完成后 rename。校验阈值 `MIN_VALID_MODEL_BYTES = 1_000 MB`，期望大小 `EXPECTED_MODEL_BYTES = 1_133_080_512`；小于阈值视为失败。**离线模型 ~1.13GB，不打包进 APK**，用户首次启用离线模式时下载。
- **Settings 持久化**：自定义模型列表以换行分隔的字符串存储（`SettingsStore.encodeCustomModelNames` / `decodeCustomModelNames`），新增字段时要同时更新 `AppSettings`、`Keys`、`settings` Flow 的组装。

## 依赖约束

- 目标平台 `minSdk = 26`，`compileSdk = targetSdk = 36`。
- 只声明了 `android.permission.INTERNET` 权限。
- 离线推理依赖 `com.llamatik:library-android:1.3.0`；`LlamaBridge` 为 native 封装，调用可能抛 `UnsatisfiedLinkError`（已在引擎中翻译为"当前 APK 缺少可用 native 推理库，或设备 ABI 不受支持"）。
- Room 使用 KSP（不要切回 KAPT）；新增 Entity 后需要升级 `AppDatabase` 的 `version` 并处理迁移。

## 开发流程（来自 AGENTS.md）

1. 新增功能或优化前，先更新 `docs/task文档.md` 和 `docs/todo日志.md`。
2. UI 工作必须先用 imagegen 产出设计图，保存到 `docs/ui/`，再进入 Compose 实现。
3. 代码完成后优先跑构建或测试，验证结果回写到 TODO / Task 文档。
4. 删除文件、批量移动、依赖替换、调用生产 API 等高风险操作前，先确认再执行。
5. 悬浮窗、划词翻译属于后续规划（见 `docs/悬浮窗划词翻译规划.md`），不要进入第一版代码实现。

## 其它

- `README.md` 说明第一版能力与构建命令。
- `docs/工作文档.md` 记录架构决策与 Hy-MT 验证结论（含 SHA256、真机验证结果），遇到离线推理问题可先翻这里。
- `tmp/` 下的 `apk_inspect` 为历史排查产物，不要作为运行时参考。
