# TODO 日志

## 2026-05-09

- [x] 确认当前目录为空项目。
- [x] 确认本机存在 Android SDK。
- [x] 确认本机存在 JDK 21。
- [x] 确认本机存在可用 Gradle 9.3.1 缓存。
- [x] 写入工作文档、Task 文档和 TODO 日志。
- [x] 使用 imagegen 生成第一版 UI 设计图并保存到 `docs/ui/v1-ui-design-board.png`。
- [x] 创建 Android 工程骨架。
- [x] 实现数据、配置、翻译引擎和模型下载模块。
- [x] 实现 Compose UI。
- [x] 构建 Debug APK。
- [x] 补充验证结果。

### 验证结果

- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`。
- [x] 已安装到真机设备 `1fd9b66`。
- [x] 已启动 `com.mxwis.aitranslate/.MainActivity`，应用进程存在且主页面处于前台。
- [ ] 暂未执行逐页点击和离线推理验证。

## 2026-05-10

- [x] 收到反馈：模型下载完成后离线翻译仍不可用。
- [x] 确认当前代码中 `OfflineTranslationEngine` 仍是占位实现。
- [x] 确认本机 Android SDK 未安装 NDK/CMake，不优先走本地编译 llama.cpp 路线。
- [x] 接入 Llamatik Android 依赖。
- [x] 替换离线翻译实现。
- [x] 构建 Debug APK。
- [x] 安装到真机并启动验证。
- [x] 确认手机内模型文件完整：`600534880` 字节。
- [x] 确认通用 Llamatik/llama.cpp 仍无法加载 Hy-MT 2bit GGUF。
- [x] 修正 App 内错误提示，说明是公开内核不支持，不再误导为模型文件损坏。
- [x] 补充验证结果。

## 2026-05-10：API 模型列表获取

- [x] 明确完成标准：能通过 Base URL + API Key 调用 `/v1/models` 并回填模型名称。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成设置页获取模型列表设计图并保存到 `docs/ui/settings-fetch-models-design.png`。
- [x] 实现云端模型列表请求与解析。
- [x] 实现设置页 UI 与状态展示。
- [x] 增加单元测试。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。

## 2026-05-10：切换 Hy-MT 1.25bit 离线路线

- [x] 确认 1.25bit GGUF 文件名：`Hy-MT1.5-1.8B-1.25bit.gguf`。
- [x] 确认文件大小：`461861216` 字节，约 440MB。
- [x] 确认该 GGUF 依赖 AngelSlim STQ1_0 kernel。
- [x] 更新 `docs/task文档.md`。
- [x] 更新模型下载目标和 UI 文案。
- [x] 更新离线加载失败提示。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 确认旧 2bit 文件仍在手机 App 私有目录中，但当前版本不会再识别它。
- [x] 用户确认后已删除旧 2bit 文件。

## 2026-05-10：切换标准 Q4_K_M 离线模型

- [x] 确认当前 APK 内 `libllama.so` 包含 `hunyuan` 和 `Q4_K` 支持，但不包含 `STQ`。
- [x] 确认腾讯官方 Q4_K_M GGUF 文件大小：`1133080512` 字节，约 1.13GB。
- [x] 确认手机 `/data` 可用空间约 81GB。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成模型页 Q4_K_M 设计图并保存到 `docs/ui/model-q4km-design.png`。
- [x] 更新模型下载目标和模型页文案。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已下载 Q4_K_M 模型到电脑并推送到手机 App 私有目录。
- [x] 真机离线翻译验证通过：`hi` -> `嗨`。
- [x] 用户确认后已删除旧 1.25bit 文件，仅保留 Q4_K_M 模型。

## 2026-05-10：API 模型下拉搜索与手动添加

- [x] 明确完成标准：下拉选择、搜索、手动添加和持久化。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成设置页模型下拉设计图并保存到 `docs/ui/settings-model-dropdown-design.png`。
- [x] 实现自定义模型持久化。
- [x] 实现模型下拉搜索与添加 UI。
- [x] 增加单元测试。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已打开设置页确认模型下拉入口和获取模型按钮可见。

## 2026-05-10：模块化设置页重新设计

- [x] 检索当前手机打开的软件：`com.github.lingyan000.fluxdo`。
- [x] 进入 FluxDo 的 `AI 模型服务` 设置。
- [x] 记录参考结构：`供应商`、`模型配置`、`聊天记录`、`快捷词管理`、`高级设置`。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成模块化设置页设计图并保存到 `docs/ui/settings-modular-ai-service-design.png`。

## 2026-05-10：实现模块化设置页

- [x] 明确完成标准：模块化结构、供应商配置面板、模型选择面板、构建与真机验证。
- [x] 更新 `docs/task文档.md`。
- [x] 实现模块化设置页 Compose。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [ ] 真机设置页 UI dump 被系统锁屏/遮罩挡住，继续在后续任务中补充点击验证。

## 2026-05-10：多供应商与模型图标美化

- [x] 明确完成标准：多供应商列表、供应商配置、模型图标徽章、构建与真机验证。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成新的设置页设计图并保存到 `docs/ui/settings-multi-provider-icons-design.png`。
- [x] 实现多供应商数据结构与持久化。
- [x] 实现设置页多供应商 UI 与模型图标。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已通过真机 UI dump 确认供应商配置面板、多个供应商和品牌徽章可见。

## 2026-05-10：收敛设置页供应商入口

- [x] 明确完成标准：外层只显示概览与入口，供应商列表只放进配置面板。
- [x] 更新 `docs/task文档.md`。
- [x] 使用 imagegen 生成新的设置页设计图并保存到 `docs/ui/settings-provider-contained-design.png`。
- [x] 修改设置页 Compose，移除外层供应商列表。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已确认主设置页不再直接显示 `DeepSeek` / `OpenRouter`，供应商配置面板内仍可见多个供应商。

## 2026-05-10：接入 Lobe Icons 大模型图标

- [x] 明确完成标准：使用 Lobe Icons 本地图标替换字母徽章，并完成构建、测试、真机验证。
- [x] 更新 `docs/task文档.md`。
- [x] 下载常用模型/供应商图标到 Android 资源目录。
- [x] 替换 Compose 字母徽章为 Lobe Icons 图片组件。
- [x] 补充第三方图标来源记录：`docs/Lobe Icons 图标来源.md`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] 已安装到真机设备 `1fd9b66` 并启动应用。
- [x] 已确认设置页和供应商配置面板的图标不再以 `O` / `DS` / `OR` 文本显示，而是使用图片资源。

## 2026-05-10：设置页收敛 AI 模型服务卡片并上浮反馈

- [x] 明确问题：`AI 模型服务` 卡信息重复、层级混乱、反馈 Banner 嵌在卡内。
- [x] 更新 `docs/task文档.md`，新增 Task 012。
- [x] `CurrentProviderSummary` 合并供应商状态 + 当前翻译模型，设置页只保留这张概览卡。
- [x] `AI 模型服务` 卡只保留 `供应商配置`、`翻译模型` 两个 Action。
- [x] `获取模型` 迁到 `ModelPickerSheet` 标题行，`添加自定义模型` 取消顶层冗余入口。
- [x] 删除 `高级参数` 底部面板，改为 `网络与性能` `SettingsModule`。
- [x] `MessageBanner` 上浮到设置页标题下方，`ModelPickerSheet` 内也显示。
- [x] 删除不再使用的 `SettingsSummaryItem` 和 `AdvancedSettingsSheet`。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest --no-daemon`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug --no-daemon`。
- [x] 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用。

## 2026-05-10：历史记录折叠预览 + 详情面板

- [x] 明确需求：历史列表改单行预览，点击后弹出详情面板显示完整内容。
- [x] 更新 `docs/task文档.md`，新增 Task 013。
- [x] `TranslateUiState` 增加 `selectedHistory`；`TranslateViewModel` 增加 `openHistoryDetail` / `closeHistoryDetail`，并在 `deleteHistory` 时自动清空选中。
- [x] `HistoryItem` 改为单行原文 + 单行译文 + 语言/模式/时间的紧凑可点击卡片。
- [x] 新增 `HistoryDetailSheet`：展示完整原文、完整译文、语言方向、模式徽章、时间，提供复制译文与删除按钮。
- [x] `AiTranslateContent` 顶层挂 `HistoryDetailSheet`，与语言/模型选择面板同级。
- [x] 单元测试 + Debug APK 构建通过：`.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon`。
- [x] 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用。
- [ ] 真机 UI 点击验证（折叠预览、点击展开、复制、删除后面板自动关闭）待补充。

## 2026-05-10：系统划词 / 分享翻译入口 + App 内迷你翻译面板

- [x] 明确完成标准：系统划词、文本分享、迷你面板承接、构建与测试通过。
- [x] 更新 `docs/task文档.md`，新增 Task 014。
- [x] 将暂不做的功能整理进后续规划文档。
- [x] 使用 imagegen 生成迷你翻译面板设计图并保存到 `docs/ui/mini-translate-panel-design.png`。
- [x] 注册 Android 系统划词与分享入口。
- [x] 实现外部文本 Intent 解析。
- [x] 实现 App 内迷你翻译面板。
- [x] 新增 `ExternalTextInputTest` 覆盖划词、分享、非文本和空白文本。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug`。
- [x] 已检查 debug merged manifest，确认系统入口已打入构建产物。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机系统菜单点击和 Intent 启动验证待补充。

## 2026-05-11：剪贴板快捷翻译提示

- [x] 明确完成标准：前台检测剪贴板、用户确认后进入快速翻译、忽略后不重复打扰。
- [x] 更新 `docs/task文档.md`，新增 Task 015。
- [x] 使用 imagegen 生成剪贴板快捷翻译提示设计图并保存到 `docs/ui/clipboard-quick-translate-design.png`。
- [x] 实现前台剪贴板文本读取。
- [x] 实现剪贴板确认提示 UI。
- [x] 确认后复用快速翻译面板。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug`。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机安装和前台剪贴板提示验证待补充。

## 2026-05-11：悬浮球半自动剪贴板翻译

- [x] 明确完成标准：授权悬浮窗、设置页开关、悬浮球、点击后悬浮迷你窗、测试和构建通过。
- [x] 更新 `docs/task文档.md`，新增 Task 016。
- [x] 使用 imagegen 生成悬浮球和悬浮迷你窗设计图并保存到 `docs/ui/floating-translate-design.png`。
- [x] 声明悬浮窗权限和悬浮服务。
- [x] 实现设置页悬浮翻译模块。
- [x] 实现悬浮球 Service。
- [x] 实现悬浮迷你翻译窗。
- [x] 单元测试通过：`.\gradlew.bat testDebugUnitTest`。
- [x] Debug APK 构建通过：`.\gradlew.bat assembleDebug`。
- [x] 已检查 debug merged manifest，确认权限和服务已打入构建产物。
- [x] 回写验证结果到 Task 文档。
- [ ] 当前 `adb devices` 无在线设备，真机悬浮窗授权、悬浮球拖拽和点击翻译验证待补充。

## 2026-05-12：更新项目 README

- [x] 明确完成标准：README 要覆盖当前能力、技术栈、构建测试命令、模型策略和文档入口。
- [x] 检索项目结构、Gradle 配置、Manifest、核心翻译模块和历史任务记录。
- [x] 更新 `docs/task文档.md`，新增 Task 017。
- [x] 更新根目录 README。
- [x] 检索 README 关键章节，确认内容已写入。
- [x] 回写验证结果到 Task 文档。
- [x] 本次仅修改文档，未运行 Android 构建。
