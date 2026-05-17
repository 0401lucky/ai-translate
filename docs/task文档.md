# Task 文档

## Task 001：第一版文本翻译 App

### 目标

从空目录创建 Android App 第一版，实现文本翻译主流程、云端 OpenAI 兼容配置、离线模型下载状态、历史记录和基础设置。

### 范围

- 创建 Android 工程骨架。
- 生成并保存 UI 设计图。
- 实现 Compose 页面。
- 实现云端翻译接口。
- 实现离线模型下载、删除和状态管理。
- 实现历史记录保存、展示和清空。
- 写入悬浮窗后续规划文档。

### 不包含

- 第一版不实现悬浮窗。
- 第一版不实现跨 App 划词。
- 第一版不把 600MB 模型内置进 APK。
- 第一版不强行接入未验证稳定的 GGUF 推理内核。

### 完成标准

- Debug APK 能成功构建。
- UI 设计图已生成并保存到 `docs/ui/`。
- App 能打开首页、设置页、模型页和历史页。
- 云端配置缺失时给出明确提示。
- 空输入不会触发翻译请求。
- 离线模型未下载时阻止离线翻译并提示用户。
- 模型下载、删除和状态展示流程可用。
- 历史记录能保存、展示、删除和清空。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 当前未连接真机或模拟器，UI 点击验证和离线推理真机验证待设备可用后执行。
- AGP 9.1.0 + KSP 当前需要 `android.disallowKotlinSourceSets=false` 兼容开关，后续升级工具链时复查。

## Task 002：接入离线 GGUF 推理

### 目标

修复“模型已下载但离线翻译不可用”的问题，让 App 能调用已下载的 Hy-MT GGUF 模型执行本地翻译。

### 范围

- 接入 Android 可用的 llama.cpp 封装库。
- 替换 `OfflineTranslationEngine` 的占位错误。
- 使用 HY-MT 官方推荐 prompt 和推理参数。
- 保持现有模型下载路径不变，继续使用 App 私有目录中的 GGUF 文件。
- 增加基础测试，验证离线 prompt 和模型路径逻辑。

### 完成标准

- Debug APK 能成功构建。
- 离线模式不再直接提示“推理内核未接入”。
- 模型未下载时仍能明确提示先下载。
- 模型已下载时会尝试加载本地 GGUF 并生成译文。
- 真机安装启动后无明显崩溃。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66`。
- 已确认手机内模型文件完整，大小为 `600534880` 字节，与 Hugging Face `X-Linked-Size` 一致。
- 已确认新包包含 `libllama.so`、`libllama_jni.so` 等 native 推理库。
- 真机离线加载失败：通用 Llamatik/llama.cpp 无法加载 Hy-MT 2bit GGUF。
- 结论：当前公开推理内核暂不支持该模型的专用 2bit 量化；需等待腾讯/AngelSlim 发布专用 llama.cpp Android kernel 后继续接入。

## Task 003：API 配置页获取模型列表

### 目标

在设置页增加“通过 Base URL 和 API Key 获取实际模型”的能力，减少用户手动填写模型名称的出错概率。

### 范围

- 调用 OpenAI 兼容的 `/v1/models` 接口。
- 校验 Base URL 和 API Key 不能为空。
- 解析接口返回的模型 `id` 列表。
- 在设置页展示拉取中、成功、失败和空列表状态。
- 支持点击已获取模型后回填并保存到模型名称。
- UI 调整前生成新的设置页设计图并保存到 `docs/ui/`。

### 完成标准

- Debug APK 能成功构建。
- 单元测试覆盖模型接口地址解析和模型列表解析。
- 设置页可以点击“获取模型”，并能把返回模型写入当前配置。
- 请求失败时有明确错误提示。
- Base URL 或 API Key 缺失时不发起网络请求。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-fetch-models-design.png`。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已安装到真机设备 `1fd9b66` 并启动应用。

## Task 004：切换离线模型到 Hy-MT 1.25bit

### 目标

将离线模型下载目标从当前不可加载的 2bit GGUF 切换到 AngelSlim 已发布 STQ1_0 kernel 路线的 Hy-MT 1.25bit GGUF。

### 范围

- 更新模型文件名、下载地址、预期大小和基础校验阈值。
- 更新模型页文案，避免继续展示 2bit/601MB。
- 更新离线加载失败提示，明确 1.25bit 依赖 STQ1_0 kernel。
- 保留旧 2bit 文件，不在未经确认时自动删除用户手机上的模型文件。

### 完成标准

- Debug APK 能成功构建。
- 模型页显示 1.25bit、约 440MB。
- App 不再把旧 2bit 文件识别为当前可用离线模型。
- 如当前 APK 内核仍无法加载，提示需接入 STQ1_0 Android 内核。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已确认手机 App 私有目录仍存在旧 2bit 文件：`Hy-MT1.5-1.8B-2bit.gguf`，大小 `600534880` 字节。
- 当前版本使用新的 1.25bit 文件名，因此旧文件不会被识别为当前离线模型。
- 用户已确认删除旧模型，旧 2bit 文件已从手机 App 私有目录删除。

## Task 005：切换到标准 Q4_K_M 离线模型

### 目标

绕过 1.25bit STQ1_0 内核尚未集成的问题，切换到当前 Llamatik/llama.cpp 内核可识别的标准 Hy-MT Q4_K_M GGUF，让离线翻译优先恢复可运行。

### 范围

- 更新模型下载目标为腾讯官方 `HY-MT1.5-1.8B-Q4_K_M.gguf`。
- 更新模型页文案为 Q4_K_M、约 1.13GB。
- 更新离线加载错误提示，不再指向 STQ1_0。
- 保留已下载的 1.25bit 文件，不未经确认自动删除。
- 生成新的模型页 UI 设计图。

### 完成标准

- Debug APK 能成功构建。
- 模型页显示 Q4_K_M 和约 1.13GB。
- 当前 APK 不再把 1.25bit 文件识别为当前模型。
- 手机安装新版后可下载或放置 Q4_K_M 模型。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/model-q4km-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已将 Q4_K_M 模型放入手机 App 私有目录：`HY-MT1.5-1.8B-Q4_K_M.gguf`。
- 模型文件大小为 `1133080512` 字节，SHA256 为 `4383AC0C3C8E476DE98FF979C2A3F069F8C4FB385E7860CF2D28DA896CC477C7`。
- 真机离线翻译验证通过：输入 `hi`，离线模式输出 `嗨`，界面提示“已使用离线翻译”。
- 用户已确认删除旧 1.25bit 文件，手机 App 私有目录目前仅保留 Q4_K_M 模型。

## Task 006：API 模型下拉搜索与手动添加

### 目标

优化设置页 API 模型选择体验，将现有模型按钮列表升级为可搜索下拉列表，并支持手动添加适合翻译的模型。

### 范围

- 使用下拉选择组件展示模型候选，容纳更多模型。
- 支持在下拉列表内搜索模型。
- 支持手动添加模型名称。
- 手动添加的模型持久化保存到 DataStore。
- 获取到的模型与手动添加模型合并去重。
- 添加模型后自动选中并保存为当前模型。
- UI 调整前生成新的设置页设计图。

### 完成标准

- Debug APK 能成功构建。
- 单元测试覆盖自定义模型序列化和去重逻辑。
- 设置页可搜索模型并从下拉列表选中。
- 设置页可手动添加模型并自动选中。
- 重启 App 后手动添加模型仍保留。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-model-dropdown-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已打开设置页确认可见 `翻译模型`、`获取模型`、`已获取 0 个模型，已添加 0 个`。

## Task 007：模块化设置页重新设计

### 目标

参考当前手机中 FluxDo 的 `AI 模型服务` 设置结构，重新设计本 App 的设置页，将 API 配置升级为独立的“AI 模型服务”模块，并让设置页更适合后续扩展。

### 参考观察

- FluxDo 的 `AI 模型服务` 不是直接暴露一堆输入框，而是拆成 `供应商`、`模型配置`、`聊天记录`、`快捷词管理`、`高级设置`。
- `供应商` 页面展示供应商名称、协议类型和模型数量。
- `模型配置` 页面按用途配置默认模型，例如聊天模型、图像模型、标题生成模型等，并支持重置为自动推断。

### 范围

- 先生成新的设置页 UI 设计图。
- 设置页按模块组织：AI 模型服务、离线模型、翻译偏好、历史与数据、关于与许可。
- AI 模型服务模块展示服务状态、供应商数量、当前翻译模型、模型获取/搜索/添加入口。
- 暂不直接进入 Compose 实现，先完成设计图确认。

### 完成标准

- 新设计图保存到 `docs/ui/`。
- 设计图能清楚体现模块分组。
- API 配置不再表现为散落字段，而是归入 `AI 模型服务`。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-modular-ai-service-design.png`。
- 已对照 FluxDo 参考结构，把 API 配置改为 `AI 模型服务` 模块，并拆出供应商配置、翻译模型、获取模型、添加自定义模型和高级参数入口。

## Task 008：实现模块化设置页

### 目标

按 `docs/ui/settings-modular-ai-service-design.png` 实现设置页模块化 UI，让设置页从单列表单升级为可扩展的模块入口。

### 范围

- 设置页拆为 `AI 模型服务`、`离线模型`、`翻译偏好`、`历史与数据`、`关于与许可`。
- `AI 模型服务` 展示供应商、配置状态、当前翻译模型、模型数量。
- 供应商配置通过底部面板编辑 Base URL 和 API Key。
- 翻译模型、添加自定义模型复用现有可搜索模型选择器。
- 获取模型保留现有接口调用能力。
- 离线模型模块跳转到模型页。
- 历史与数据模块显示历史条数，并支持清空历史。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 真机安装后设置页显示模块化结构。
- 供应商配置底部面板可打开并编辑。
- 模型选择底部面板可打开。

### 验证记录

- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 真机 UI dump 一度被系统锁屏/遮罩挡住，模块化设置页继续在 Task 009 中补充验证。

## Task 009：多供应商与模型图标美化

### 目标

继续优化设置页的 `AI 模型服务` 模块，把供应商从单一 `OpenAI 兼容` 展示升级为多供应商列表，并为供应商和模型加入更清晰的品牌图标徽章。

### 参考观察

- 之前探索到 FluxDo 的 `AI 模型服务` 入口按 `供应商`、`模型配置`、`高级设置` 等模块组织。
- `供应商` 不是一个固定接口，而应支持多个服务商配置。
- 图标参考开源 AI/LLM 图标库路线：Lobe Icons 更贴合模型/供应商场景，Simple Icons 可作为品牌图标兜底。

### 范围

- 更新新的设置页设计图，体现多个供应商和模型图标。
- 数据层增加多供应商配置列表和当前选中供应商。
- 设置页 `AI 模型服务` 显示多个供应商卡片，可切换当前供应商。
- 供应商配置面板支持新增供应商、编辑供应商名称、Base URL 和 API Key。
- 模型选择列表增加模型品牌徽章。
- 保留现有 OpenAI 兼容翻译、获取模型、手动添加模型能力。

### 完成标准

- 新设计图保存到 `docs/ui/`。
- Debug APK 能成功构建。
- 单元测试通过。
- 设置页能看到多个供应商入口和当前选中供应商。
- 模型列表能显示模型品牌徽章。
- 真机安装后设置页可打开，供应商配置面板可进入。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-multi-provider-icons-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已通过真机 UI dump 确认供应商配置面板可打开，并能看到 `OpenAI`、`DeepSeek`、`OpenRouter`、`自定义兼容接口`。
- 已确认供应商/模型品牌徽章可见，例如 `O`、`DS`、`OR`。

## Task 010：收敛设置页供应商入口

### 目标

修正 `AI 模型服务` 模块中“外层供应商卡片”和“供应商配置入口”重复的问题。设置页外层只展示概览和入口，多供应商列表只放在供应商配置面板内。

### 范围

- 重新生成设置页设计图，体现外层精简、内层管理。
- 移除设置页外层的多供应商卡片列表。
- 外层保留当前供应商、配置状态、当前模型、模型数量和操作入口。
- 供应商配置面板继续支持多供应商切换、添加和编辑。
- 保留供应商与模型品牌徽章。

### 完成标准

- 新设计图保存到 `docs/ui/`。
- Debug APK 能成功构建。
- 单元测试通过。
- 真机设置页外层不再直接展示多个供应商卡片。
- 点击 `供应商配置` 后仍能看到多个供应商并切换。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/settings-provider-contained-design.png`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已通过真机 UI dump 确认设置页外层不再显示 `DeepSeek` 和 `OpenRouter` 供应商卡片。
- 已通过真机 UI dump 确认 `供应商配置` 面板内仍显示 `OpenAI`、`DeepSeek`、`OpenRouter`、`自定义兼容接口`。

## Task 011：接入 Lobe Icons 大模型图标

### 目标

将当前供应商和模型的字母徽章替换为 `lobehub/lobe-icons` 开源图标资源，让设置页与模型选择页的 AI 模型标识更专业。

### 参考资料

- `lobehub/lobe-icons`：AI/LLM 品牌 SVG/PNG/WebP 图标集合。
- 许可：MIT License。
- Android 方案：使用静态 PNG 本地资源，避免运行时网络依赖和前端包接入复杂度。

### 范围

- 下载常用图标：OpenAI、DeepSeek、OpenRouter、Claude、Gemini、Qwen，必要时补充 Hunyuan。
- 保存到 Android `drawable-nodpi` 资源目录。
- 设置页供应商图标优先使用 Lobe Icons 图片。
- 模型选择列表和当前模型摘要优先使用 Lobe Icons 图片。
- 未匹配模型仍保留简洁文字兜底徽章。
- 补充第三方图标来源记录。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 真机设置页能看到真实模型/供应商图标，不再只显示 O/DS/OR 字母徽章。
- 供应商配置面板仍可正常打开。

### 验证记录

- 已下载 Lobe Icons 静态 PNG 图标到 `app/src/main/res/drawable-nodpi/`。
- 已新增图标来源记录：`docs/Lobe Icons 图标来源.md`。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- 已安装到真机设备 `1fd9b66` 并启动应用。
- 已通过真机 UI dump 确认设置页图标由 `ImageView` 承载，不再以 `O` / `DS` / `OR` 文本显示。
- 已确认 `供应商配置` 面板可打开，`OpenAI`、`DeepSeek`、`OpenRouter` 仍正常显示。

## Task 012：设置页收敛 AI 模型服务卡片并上浮反馈

### 目标

用户反馈设置页信息重复、层级混乱，把 `AI 模型服务` 模块瘦身为"概览 + 两个入口"，并整理高级参数、反馈 Banner 与模型获取入口的归属。

### 问题盘点

- `AI 模型服务` 卡内部连续出现 3 个 `SummaryItem`、`CurrentProviderSummary`、当前模型行、模型计数文案，同一份信息重复三遍。
- `获取模型`、`添加自定义模型`、`高级参数` 与 `供应商配置`、`翻译模型` 平级，但实际不是同一层级。
- `MessageBanner` 嵌在 `AI 模型服务` 卡内部，反馈长度变化会把卡片撑乱，不够显眼。
- `SettingsSummaryItem` 固定 `width = 96.dp`，中文供应商名容易被截断。

### 范围

- 精简 `AI 模型服务` 卡：只保留 `CurrentProviderSummary`（合并供应商状态 + 当前模型）+ `供应商配置`、`翻译模型` 两个入口。
- `获取模型` 作为工具按钮迁入 `ModelPickerSheet` 顶部标题行；`添加自定义模型` 原本就在面板底部，取消设置页上的冗余入口。
- 删除 `高级参数` 底部面板，改为独立 `网络与性能` `SettingsModule`，与翻译偏好、历史与数据平级。
- 把 `modelFetchMessage` / `modelFetchError` Banner 上浮到设置页标题下方；`ModelPickerSheet` 内也显示该 Banner，保证获取模型操作能在面板内看到反馈。
- 删除 `SettingsSummaryItem`、`AdvancedSettingsSheet` 两个不再使用的组件。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 设置页 `AI 模型服务` 卡只有概览卡 + 2 个 Action 行。
- `网络与性能` 作为独立模块出现。
- `获取模型` 按钮出现在模型选择面板标题行。
- 模型获取反馈同时显示在设置页顶部和模型面板内。

### 验证记录

- `.\gradlew.bat testDebugUnitTest --no-daemon`：通过。
- `.\gradlew.bat assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 本次改动仅影响设置页 Compose 层，未改动数据层、ViewModel 的模型获取流程或网络行为，真机交互验证待设备就绪后补充。
- 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用。

## Task 013：历史记录折叠预览 + 详情面板

### 目标

用户反馈历史记录条目文本太长时整张卡被撑开。把列表改为单行预览，点击后用底部面板显示完整原文与译文。

### 范围

- `HistoryItem` 改为单行原文、单行译文的紧凑卡片，整卡可点击。
- 新增 `HistoryDetailSheet`：展示模式徽章、语言方向、时间、完整原文、完整译文，提供复制译文与删除入口。
- `TranslateUiState` 增加 `selectedHistory` 字段；`TranslateViewModel` 增加 `openHistoryDetail` / `closeHistoryDetail`，并在 `deleteHistory` 时清空选中。
- `AiTranslateContent` 顶层挂 `HistoryDetailSheet`（与语言/模型选择面板同级）。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 历史列表每条只显示一行原文和一行译文。
- 点击列表项弹出详情面板；面板内可复制译文、删除并关闭。
- 删除当前详情对应的条目时，面板自动关闭。

### 验证记录

- `.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已通过 adb 安装到真机设备 `1fd9b66` 并启动应用，真机点击验证待补充。

## Task 014：系统划词 / 分享翻译入口 + App 内迷你翻译面板

### 目标

先实现低权限、高频使用的跨 App 文本入口，并在 App 内提供迷你翻译面板承接外部文本，作为后续悬浮窗翻译的前置能力。

### 范围

- 支持 Android 系统 `ACTION_PROCESS_TEXT` 划词入口，读取外部选中文本并进入翻译流程。
- 支持 Android 系统 `ACTION_SEND` 文本分享入口，读取分享文本并进入翻译流程。
- 从外部入口进入时打开 App 内迷你翻译面板，而不是直接打断主翻译页结构。
- 迷你面板展示外部原文、当前语言方向、模式选择提示、翻译状态、译文、复制译文和转到完整翻译页入口。
- 迷你面板复用现有翻译仓库、设置、历史记录和云端/离线/自动模式逻辑。
- 其余暂不实现的增强能力写入后续规划文档。

### 不包含

- 本任务不申请悬浮窗权限。
- 本任务不实现无障碍划词。
- 本任务不做 OCR、语音朗读、术语表、历史收藏导出等增强能力。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- Manifest 注册系统划词与文本分享入口。
- App 能从 `ACTION_PROCESS_TEXT` 和 `ACTION_SEND` Intent 中提取文本。
- 外部文本进入后自动打开迷你翻译面板。
- 迷你面板可触发翻译、展示译文、复制译文，并可把文本带入完整翻译页。
- 空文本或非文本分享不会触发异常。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/mini-translate-panel-design.png`。
- 新增纯 Kotlin 单元测试 `ExternalTextInputTest`，覆盖系统划词、文本分享、非文本分享和空白文本。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已检查 debug merged manifest，确认包含 `android.intent.action.PROCESS_TEXT`、`android.intent.action.SEND` 和 `singleTop`。
- 已定位 adb 路径，但当前 `adb devices` 无在线设备，真机系统菜单点击和 Intent 启动验证待设备连接后补充。

## Task 015：剪贴板快捷翻译提示

### 目标

补足用户复制文本后进入 App 的快捷路径：当 App 打开或回到前台时，如果剪贴板中存在文本，先弹出确认提示，用户确认后再打开快速翻译面板。

### 范围

- App 前台时读取系统剪贴板文本。
- 文本为空、重复文本或当前已有快速翻译面板时不重复提示。
- 提示用户“检测到剪贴板文本，是否快速翻译？”。
- 用户点击确认后复用现有 App 内迷你翻译面板。
- 用户点击忽略后，本次剪贴板内容不再重复打扰。
- 继续遵守隐私边界：不后台读取剪贴板，不自动提交翻译。

### 不包含

- 不做后台剪贴板监听。
- 不做自动翻译。
- 不读取图片、链接预览或其他非文本内容。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 打开或回到 App 前台时能识别剪贴板文本。
- 识别到文本后弹出确认提示。
- 确认后打开快速翻译面板，忽略后不重复弹同一段文本。
- 系统划词 / 分享入口仍可正常打开快速翻译面板。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/clipboard-quick-translate-design.png`。
- 新增 `ExternalTextInput.extractClipboardText`，并补充剪贴板文本清理和空白过滤单元测试。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 当前 `adb devices` 无在线设备，未能安装到真机补充前台剪贴板提示验证。

## Task 016：悬浮球半自动剪贴板翻译

### 目标

实现低风险的跨 App 快捷翻译：用户复制文本后点击悬浮球，App 显示悬浮迷你翻译窗，并尝试读取剪贴板文本进行翻译。

### 范围

- 申请并引导用户开启 `SYSTEM_ALERT_WINDOW` 悬浮窗权限。
- 在设置页新增 `悬浮翻译` 模块，显示权限状态，并提供开启/关闭悬浮球入口。
- 实现悬浮球 Service，悬浮球可拖拽、可关闭。
- 点击悬浮球后展示悬浮迷你翻译窗。
- 悬浮迷你翻译窗读取剪贴板文本，展示原文、翻译状态、译文、复制译文和关闭按钮。
- 翻译逻辑复用现有 `TranslationRepository`，语言方向使用自动检测到简体中文，模式使用当前默认翻译模式。

### 不包含

- 不实现无障碍自动监听。
- 不后台自动读取剪贴板。
- 不自动弹窗打断用户，必须由用户点击悬浮球触发。
- 不做前台服务常驻通知，当前版本为用户手动开启后的普通悬浮服务，后续可按稳定性再升级。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- Manifest 声明悬浮窗权限和悬浮服务。
- 设置页可以引导用户授权悬浮窗。
- 授权后可以开启和关闭悬浮球。
- 点击悬浮球后能弹出悬浮迷你翻译窗。
- 剪贴板为空或系统限制读取时有明确提示。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/floating-translate-design.png`。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- 已检查 debug merged manifest，确认包含 `SYSTEM_ALERT_WINDOW` 权限和 `FloatingTranslateService`。
- 当前 `adb devices` 无在线设备，未能安装到真机补充悬浮窗授权、悬浮球拖拽和点击翻译验证。

## Task 017：更新项目 README

### 目标

检索当前项目结构、核心代码和历史任务记录，更新根目录 README，使其准确反映当前 App 能力、技术栈、构建方式和验证状态。

### 范围

- 梳理当前 Android 工程结构和关键模块。
- 汇总云端翻译、离线翻译、历史记录、系统划词/分享、剪贴板快捷翻译和悬浮球能力。
- 补充环境要求、构建命令、测试命令和文档入口。
- 明确离线模型不内置 APK，采用首次下载或放置到 App 私有目录的方案。

### 完成标准

- README 不再停留在第一版描述。
- README 能说明当前主要功能、技术栈、项目结构和常用命令。
- README 中的模型、权限、跨 App 入口信息与当前代码一致。
- 完成后至少运行一次文档内容检索，确认关键章节已写入。

### 验证记录

- 已更新根目录 `README.md`。
- 已检索 README 关键章节，确认包含当前能力、技术栈、常用命令、离线模型说明、权限与系统入口、项目结构和当前验证状态。
- 本次仅修改文档，未运行 Android 构建。

## Task 018：Cloudflare R2 模型与更新包分发

### 目标

使用 Cloudflare R2 承载 App 更新包和 Hy-MT 离线模型文件，降低用户下载模型时对 Hugging Face 和代理环境的依赖。

### 范围

- 使用 Wrangler 创建 R2 bucket。
- 上传当前 Q4_K_M GGUF 模型文件。
- 生成并上传模型 SHA256 校验文件和 `models.json` manifest。
- 开启公开访问，优先使用自定义域名；未确定域名时先使用 R2 dev URL 做临时验证。
- 将 App 离线模型下载地址切换到 R2 分发地址。
- 记录 R2 bucket、对象路径、公开访问地址和验证结果。

### 完成标准

- R2 bucket 创建成功。
- 模型文件、SHA256 文件和 manifest 上传成功。
- 公开 URL 可访问 manifest。
- App 中模型下载地址指向 R2 分发地址。
- 单元测试通过。

### 验证记录

- R2 bucket 已创建：`ai-translate-assets`。
- 已开启临时公开访问：`https://pub-e16b86eab02f4594aaa4fd358cf6151e.r2.dev`。
- 已上传 `models/models.json`、`models/HY-MT1.5-1.8B-Q4_K_M.gguf.sha256` 和 `releases/latest.json`。
- Wrangler 单文件上传远端 R2 上限为 300MiB，Q4_K_M 模型已改为 6 个分片上传到 `models/parts/`。
- 公开访问校验通过：manifest 返回 200，6 个分片 HEAD 返回 200，`part00` Range 请求返回 206。
- App 离线模型下载已切换到 R2 分片下载，拼接后校验总大小和 SHA256。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：命令超过 60 秒上限；测试结果 XML 已生成，4 个测试类共 18 个测试均为 0 failure / 0 error。
- 待绑定自定义域名后，将临时 R2 dev URL 替换为正式下载域名。

## Task 019：绑定 R2 正式下载域名

### 目标

将 R2 临时 `r2.dev` 下载地址切换为正式自定义域名 `download.204152.xyz`，用于 App 更新包和离线模型分片分发。

### 范围

- 查询 `204152.xyz` 在 Cloudflare 中的 Zone ID。
- 将 `download.204152.xyz` 绑定到 R2 bucket `ai-translate-assets`。
- 更新 App 里的模型分片基础地址。
- 更新 `docs/r2/models.json` 中的公开下载地址。
- 上传更新后的 `models.json` 到 R2。
- 验证自定义域名下 manifest、分片 HEAD 和 Range 请求可访问。

### 完成标准

- `download.204152.xyz` 绑定成功。
- App 模型下载地址不再使用临时 `r2.dev` 地址。
- R2 manifest 中模型分片 URL 指向正式域名。
- 公开访问验证通过。
- Kotlin 编译通过。

### 验证记录

- `204152.xyz` Zone ID：`fe23f737002048c62ab39874b2b03222`。
- 已绑定 R2 自定义域名：`https://download.204152.xyz`。
- R2 域名状态：`ownership_status = active`，`ssl_status = active`，`min_tls_version = 1.2`。
- 已将 App 模型下载基础地址改为 `https://download.204152.xyz`。
- 已更新并上传 R2 `models/models.json`，分片 URL 均指向正式域名。
- 正式域名访问验证通过：manifest GET 返回 200，6 个模型分片 HEAD 返回 200，`part00` Range 请求返回 206。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。

## Task 020：剪贴板中央浮层翻译与文本朗读

### 目标

优化剪贴板快捷翻译的使用手感，并为原文和译文补充系统朗读能力。剪贴板流程调整为前台检测后弹出中央确认卡片，用户确认后直接打开中央快速翻译卡片并自动翻译。

### 范围

- 生成新的中央浮层设计图并保存到 `docs/ui/`。
- 剪贴板确认提示由底部面板改为中央卡片。
- App 内快速翻译面板由底部面板改为中央浮层卡片。
- 剪贴板确认后自动触发快速翻译，不再要求再次点击翻译。
- 悬浮球剪贴板翻译窗改为居中的悬浮卡片。
- 主翻译页、快速翻译卡片、历史详情和悬浮翻译窗支持原文与译文朗读。
- 使用 Android 系统 `TextToSpeech`，按已选语言或文本字符特征选择朗读语言。

### 不包含

- 不做后台剪贴板监听。
- 不做无确认自动提交翻译。
- 不新增录音、音频或云端语音权限。
- 不做云端 TTS、语速调节、音色选择和语音包下载管理。

### 完成标准

- Debug APK 能成功构建。
- 单元测试通过。
- 剪贴板检测后显示中央确认卡片。
- 点击快速翻译后打开中央快速翻译卡片并自动开始翻译。
- 原文和译文朗读按钮在主翻译页、快速翻译卡片、历史详情和悬浮窗中可见。
- 设备不支持对应 TTS 语言时有明确提示，且不影响翻译。
- 悬浮球点击后的翻译窗位于屏幕中央，并保留关闭、复制和朗读能力。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/clipboard-tts-floating-card-design.png`。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：通过，命令在 60 秒上限内完成。
- `.\gradlew.bat assembleDebug --no-daemon --console=plain`：通过。
- Debug APK 已生成：`app/build/outputs/apk/debug/app-debug.apk`。
- `adb devices` 当前无在线设备，未能补充中央剪贴板卡片、自动翻译、朗读发声和悬浮窗居中卡片的真机点击验证。

## Task 021：应用内更新入口

### 目标

在 App 设置页补充应用内更新入口，读取 R2 的 `releases/latest.json` 更新清单，避免后续发布 APK 后用户没有入口检查和下载更新。

### 范围

- 生成设置页应用更新入口设计图并保存到 `docs/ui/`。
- 在设置页“关于与许可”模块加入“应用更新”入口。
- 从 `https://download.204152.xyz/releases/latest.json` 拉取 Android 更新清单。
- 对比当前 `versionCode` 与清单中的最新 `versionCode`。
- 显示检查中、已是最新、发现新版本、清单未配置安装包和失败状态。
- 发现可用新版本时提供下载更新按钮，打开清单中的 APK 地址。

### 不包含

- 不自动静默安装 APK。
- 不绕过 Android 系统安装确认。
- 不在本次生成或上传正式签名 APK。

### 完成标准

- 设置页存在明确的“应用更新 / 检查更新”入口。
- 点击入口会请求 R2 更新清单并更新页面状态。
- 清单中没有 APK 地址时给出明确提示。
- 单元测试通过。
- Debug APK 构建通过。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/app-update-entry-design.png`。
- 已新增 R2 更新清单解析与版本对比逻辑。
- 已在设置页“关于与许可”模块加入“应用更新”入口、检查状态、结果提示和下载更新按钮。
- R2 更新清单访问验证通过：`https://download.204152.xyz/releases/latest.json` 返回 200，当前为 `versionCode = 1` 且 `apkUrl` 为空的占位清单。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：通过。
- `.\gradlew.bat assembleDebug --no-daemon --console=plain`：通过。
- 当前未连接真机，设置页点击检查更新的设备侧交互验证待补充。

## Task 022：1.0.1 悬浮剪贴板修复与 R2 内置更新

### 目标

修复 Android 10+ 下悬浮球在其他 App 中无法稳定读取剪贴板的问题，并把本次优化作为 `1.0.1` 通过 Cloudflare R2 发布，验证应用内更新下载安装流程。

### 范围

- 新增透明剪贴板桥接 Activity，让悬浮球点击后以前台 Activity 身份读取剪贴板。
- 悬浮翻译 Service 接收桥接 Activity 传入的文本并直接翻译，不再依赖后台 Service 读取剪贴板。
- 将 App 版本提升为 `versionCode = 2`、`versionName = 1.0.1`。
- 应用内更新从“打开下载链接”升级为下载 APK、校验大小和 SHA256，并拉起系统安装器。
- 新增 FileProvider 和安装未知应用权限声明。
- 新增 R2 Debug 发版脚本，自动构建 APK、生成 `latest.json`、上传 APK 与 manifest。
- 更新 README、工作文档、任务文档和 TODO 日志，明确更新走 Cloudflare R2。

### 不包含

- 不配置正式 release keystore。
- 不做静默安装。
- 不做后台自动读取剪贴板或后台自动翻译。

### 完成标准

- 从其他 App 复制文本后，不打开主界面点击悬浮球也能读取并翻译。
- 设置页检查更新能发现 `1.0.1`，下载 APK 后校验并拉起系统安装器。
- R2 `releases/latest.json` 指向 `1.0.1` Debug APK。
- GitHub `main` 已推送本次 1.0.1 改动。
- Kotlin 编译、单元测试和 Debug APK 构建通过。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/v101-update-floating-clipboard-design.png`。
- 已新增透明 `ClipboardBridgeActivity`，悬浮球点击后通过前台 Activity 读取剪贴板，并把文本传回 `FloatingTranslateService` 翻译。
- 已将版本号提升为 `versionCode = 2`、`versionName = 1.0.1`。
- 已实现应用内更新包下载、大小校验、SHA256 校验、FileProvider 暴露 APK 和系统安装器拉起。
- 已新增 `scripts/publish-r2-debug-update.ps1`，用于 Debug 包 R2 发版。
- 已执行 R2 发版脚本，上传 `releases/ai-translate-1.0.1-debug.apk` 和 `releases/latest.json`。
- R2 manifest GET 返回 200，APK HEAD 返回 200，APK 公开大小为 `142576561` 字节。
- R2 manifest 已写入 SHA256：`33BF3BC2CEB546E1E5CB099A69295CE46F2C069BD0B01AD90EF1EEDB1E74B07D`。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：通过。
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`：通过。
- 本次 1.0.1 改动随提交同步到 GitHub `main`。
- 已成功构建临时 `versionCode = 1` 测试客户端；应用内安装真机验证待补充。
- 真机覆盖安装 1.0.1 后，在 Chrome 页面点击悬浮球，已能读取剪贴板并显示中央悬浮翻译卡片。
- 已重新上传包含桥接读取时机与任务栈修复的 1.0.1 Debug APK 到 R2。

## Task 023：悬浮剪贴板桥接任务栈修复

### 目标

修复悬浮球点击后虽然能读取第三方 App 剪贴板，但桥接 Activity 结束后回到 AI 翻译主界面的问题，让用户停留在原第三方 App，仅显示悬浮翻译卡片。

### 范围

- 将 `ClipboardBridgeActivity` 隔离到独立透明任务栈，避免复用主界面任务栈。
- 悬浮球启动桥接 Activity 时使用独立临时任务参数。
- 桥接读取完成后移除临时任务，返回原第三方 App。
- 保持读取失败提示和悬浮卡片翻译流程不变。

### 不包含

- 不修改翻译主界面视觉。
- 不改变应用内更新协议和 R2 manifest 格式。

### 完成标准

- 从浏览器、微信等第三方 App 点击悬浮球后，不跳回 AI 翻译主界面。
- 成功读取剪贴板时，原第三方 App 上方显示中央悬浮翻译卡片。
- 读取失败时，原第三方 App 上方显示明确错误提示。
- Kotlin 编译、单元测试和 Debug APK 构建通过。

### 验证记录

- 已将 `ClipboardBridgeActivity` 设置为独立透明任务栈：`${applicationId}.clipboard`。
- 悬浮球启动桥接 Activity 时使用独立临时任务参数，避免复用 AI 翻译主界面任务栈。
- 桥接读取完成后调用 `finishAndRemoveTask()`，移除临时任务。
- 真机验证：在 Chrome 页面点击悬浮球后，底层仍停留在 Chrome，并显示中央悬浮翻译卡片；`dumpsys window` 显示 `mFocusedApp` 仍为 `com.android.chrome`。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：通过。
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`：通过。
- 已执行 `scripts/publish-r2-debug-update.ps1`，重新上传 `1.0.1` Debug APK 和 `releases/latest.json`。
- R2 manifest GET 返回 200，APK HEAD 返回 200，APK 公开大小为 `142576561` 字节，SHA256 为 `33BF3BC2CEB546E1E5CB099A69295CE46F2C069BD0B01AD90EF1EEDB1E74B07D`。

## Task 024：1.0.2 系统文本朗读修复

### 目标

修复当前点击朗读后只提示“系统朗读不可用”的问题，把系统 TTS 朗读完善为可检测、可重试、可修复、可回退语言的稳定功能，并作为 `1.0.2` 通过 Cloudflare R2 发布。

### 范围

- 新增共享系统朗读控制器，统一主翻译页、快速翻译卡片、历史详情和悬浮翻译窗的 TTS 行为。
- 检测系统 TTS 引擎、初始化状态和语音包可用性。
- 初始化失败或无语音包时提供明确提示和修复入口。
- 扩展朗读语言候选回退：优先选择语言/文本推断，再回退设备默认语言、英文、简体中文。
- 设置页新增“文本朗读”模块，显示状态并提供重新检测、安装语音包、打开系统设置和测试朗读。
- 将版本提升为 `versionCode = 3`、`versionName = 1.0.2`，更新 R2 Debug 发版脚本与 manifest。

### 不包含

- 不接入云端 TTS。
- 不做音色、语速、缓存和费用控制。
- 不内置第三方语音引擎。

### 完成标准

- 点击任意朗读入口不会只停留在“系统朗读不可用”，而是显示可理解原因和修复入口。
- 系统 TTS 可用时，主页面、快速卡片、历史详情和悬浮窗均可朗读。
- 系统 TTS 不可用时，设置页能引导安装语音包或打开系统设置。
- Kotlin 编译、单元测试和 Debug APK 构建通过。
- R2 `releases/latest.json` 指向 `1.0.2` Debug APK。

### 验证记录

- UI 设计图已生成并保存到 `docs/ui/v102-tts-design.png`。
- 已新增共享系统朗读控制器，统一主翻译页、快速翻译卡片、历史详情和悬浮翻译窗的 TTS 行为。
- 已在设置页新增“文本朗读”模块，提供状态展示、重新检测、安装语音包、打开系统设置和测试朗读。
- 已扩展 `SpeechLocaleResolver`，支持候选 Locale 回退。
- 已在 Manifest 增加 TTS service、检查数据和安装数据的 package visibility 查询。
- 已将版本号提升为 `versionCode = 3`、`versionName = 1.0.2`。
- 已更新 `scripts/publish-r2-debug-update.ps1`，默认发布 `releases/ai-translate-1.0.2-debug.apk`。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：通过。
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`：通过。
- 已执行 R2 发版脚本，上传 `releases/ai-translate-1.0.2-debug.apk` 和 `releases/latest.json`。
- R2 manifest GET 返回 200，APK HEAD 返回 200，APK 公开大小为 `142357364` 字节。
- R2 manifest 已写入 SHA256：`C692F0798AAA54E0AD7D020D9C8F4F5611267CCDCBC788D03EC84295C88EDCF4`。
- 当前 `adb devices` 无在线设备，文本朗读真机发声和 1.0.1 到 1.0.2 应用内更新点击验证待设备重新连接后补充。

## Task 025：翻译页模型选择状态修复

### 目标

修复翻译页选择云端模型后，重启 App 界面仍显示云端模型但实际按离线模型翻译的问题，让界面展示、默认模式和真实翻译模式保持一致。

### 范围

- 翻译页统一模型选择时，同步保存对应的默认翻译模式。
- 选择云端模型时继续保存具体模型名称。
- 界面展示按当前模式派生模型名称，避免默认离线时误显示云端模型名。
- 增加 ViewModel 单元测试覆盖云端选择持久化。

### 不包含

- 不新增模型选择 UI 设计。
- 不改动云端翻译接口协议。
- 不改动离线模型下载和推理逻辑。

### 完成标准

- 选择云端模型后，DataStore 默认模式同步为云端。
- 重启 App 后会按已选择的云端模式发起翻译。
- 默认模式为离线时，翻译页模型入口显示离线模型，而不是云端模型名。
- Kotlin 编译和单元测试通过。

### 验证记录

- 已修复翻译页统一模型选择：选择云端 / 离线 / 自动时同步保存默认翻译模式。
- 已修复模型入口展示逻辑：默认离线时显示 `HY-MT 1.5B`，不再误显示云端模型名。
- 已增加 ViewModel 单元测试，覆盖选择云端模型后写入默认模式并按云端翻译。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：首次通过；追加状态刷新补丁后再次执行超过 60 秒上限被截断，但测试 XML 显示 8 个测试类共 35 个测试均为 0 failure / 0 error。
- `.\gradlew.bat :app:testDebugUnitTest --tests "com.mxwis.aitranslate.ui.TranslateViewModelTest" --no-daemon --console=plain`：通过，耗时 54 秒。
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`：通过。
- 当前 shell 中 `adb` 不在 PATH，未补充真机重启点击验证。

## Task 026：有道式翻译首页与拍照翻译首版

### 目标

把翻译首页改成“文字翻译主入口 + 快捷功能入口区”的有道式结构，并新增拍照翻译首版能力，支持拍照或相册导入图片后本地 OCR 识别，再复用现有翻译流程。

### 范围

- 更新新版首页设计图并保存到 `docs/ui/photo-translate-home-design.png`。
- 首页保留模型选择、语言切换和文字翻译主流程。
- 首页增加快捷入口：拍照翻译、相册导入、剪贴板、历史。
- 新增图片翻译面板：图片预览、识别文本编辑、译文、重新选择、带入首页、复制译文。
- 使用 Google ML Kit Text Recognition v2 本地 OCR，接入 Latin + Chinese 识别模型。
- 更新相机临时图片 FileProvider 路径。

### 不包含

- 不做实时相机取景翻译。
- 不上传图片到云端。
- 不实现文档翻译正文能力。

### 完成标准

- Debug APK 能成功构建。
- 单元测试覆盖 OCR 成功、OCR 空结果、图片文本翻译和带入首页。
- 模拟器首页能看到有道式快捷入口。
- 相册图片导入后能识别文字并进入图片翻译面板。
- 图片翻译面板能翻译、复制并带入首页。

### 验证记录

- 已使用 imagegen 生成新版首页设计图，保存到 `docs/ui/photo-translate-home-design.png`。
- 已接入 ML Kit Text Recognition v2，本地 OCR 同时使用 Latin 和 Chinese 识别模型，图片不上传。
- 已新增拍照 / 相册导入入口，复用现有 `FileProvider` 并补充相机临时缓存路径。
- 已新增图片翻译状态和底部面板，支持图片预览、识别文本编辑、翻译识别文本、复制译文、重新选择和带入首页。
- 已增加 ViewModel 单元测试，覆盖 OCR 成功、OCR 空结果、图片翻译使用当前语言 / 模型模式、带入首页同步。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：通过。
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`：通过。
- 模拟器验证：`Pixel_9_Pro` 安装新版 Debug APK 成功，首页可见拍照翻译、相册导入、剪贴板、历史四个快捷入口。
- 模拟器验证：拍照翻译入口可打开系统相机；相册导入入口可打开系统图片选择器。
- 模拟器验证：导入测试图片后，本地 OCR 成功识别中英文文本并进入图片翻译面板；样例中文和 `AI translate` 存在轻微误识别，已可在面板内手动编辑后再翻译。
- 验证截图已保存到 `docs/ui/prototype-screenshots/photo-translate-home-emulator-new.png`、`docs/ui/prototype-screenshots/photo-entry-after-tap.png`、`docs/ui/prototype-screenshots/gallery-entry-after-tap.png`、`docs/ui/prototype-screenshots/image-result-after-ocr.png`。
- 当前模拟器没有配置云端 API Key，也未下载离线模型；真实模型输出在单元测试中用 fake repository 覆盖，设备侧真实翻译需配置模型后再点测。

## Task 027：首页工具入口收纳优化

### 目标

把首页快捷功能区收纳到右上角工具入口里，减少主页面信息量，让首页优先服务文字翻译；点击工具图标后弹出工具面板，首版提供拍照翻译和相册导入，后续工具继续放入同一个入口。

### 范围

- 更新工具入口设计图并保存到 `docs/ui/translate-toolbox-design.png`。
- 首页右上角新增工具按钮。
- 移除首页直接展示的剪贴板、历史等快捷宫格。
- 点击工具按钮后弹出工具面板，展示拍照翻译和相册导入。
- 工具面板预留后续工具扩展说明，但不展示不可用按钮。

### 不包含

- 不新增文档翻译正文能力。
- 不改动 OCR 和图片翻译业务流程。
- 不改动底部历史导航。

### 完成标准

- 首页不再直接展示四宫格快捷入口。
- 右上角工具按钮可打开工具面板。
- 工具面板内拍照翻译、相册导入仍能触发原有流程。
- Kotlin 编译、单元测试、Debug APK 构建通过。
- 模拟器安装后能看到新版首页和工具弹窗。

### 验证记录

- 已使用 imagegen 生成工具入口设计图：`docs/ui/translate-toolbox-design.png`。
- 已移除首页四宫格快捷入口，首页仅保留右上角工具按钮作为拍照 / 相册入口。
- 已通过 `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- 已通过 `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`。
- 已通过 `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- 已安装新版 Debug APK 到模拟器 `emulator-5554` 并启动验证。
- 已截图验证首页和工具弹窗：
  - `docs/ui/prototype-screenshots/toolbox-home.png`
  - `docs/ui/prototype-screenshots/toolbox-sheet.png`
  - `docs/ui/prototype-screenshots/toolbox-final-visible.png`
- 已点击验证工具入口：
  - `相册导入` 可唤起系统照片选择器，截图：`docs/ui/prototype-screenshots/toolbox-gallery-picker.png`。
  - `拍照翻译` 可唤起系统相机，截图：`docs/ui/prototype-screenshots/toolbox-camera-launch-2.png`。

## Task 028：首页工具入口图标优化

### 目标

把首页右上角工具入口从九宫格图标调整为更明确的工具类图标，避免用户误解为应用菜单或功能宫格。

### 范围

- 更新工具入口图标优化设计图并保存到 `docs/ui/translate-tool-icon-design.png`。
- 替换首页右上角工具入口图标。
- 保持原有工具弹窗和拍照 / 相册入口流程不变。

### 不包含

- 不调整首页整体布局。
- 不新增新的工具项。
- 不改动 OCR 和翻译业务流程。

### 完成标准

- 首页右上角不再使用九宫格图标。
- 新图标语义更接近“工具 / 辅助能力 / 快捷入口”。
- 点击新图标仍可打开工具弹窗。
- Kotlin 编译通过，并安装到模拟器截图验证。

### 验证记录

- 已使用 imagegen 生成图标优化设计图：`docs/ui/translate-tool-icon-design.png`。
- 已将首页右上角工具入口从九宫格替换为魔法棒图标 `AutoFixHigh`。
- 已通过 `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- 已通过 `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- 已安装新版 Debug APK 到模拟器 `emulator-5554` 并启动验证。
- 已截图验证：
  - 首页新图标：`docs/ui/prototype-screenshots/tool-icon-home-ready.png`
  - 新图标点击后弹出工具面板：`docs/ui/prototype-screenshots/tool-icon-final-visible.png`

## Task 029：首页工具入口视觉对齐修正

### 目标

修正首页实际效果与设计图差异过大的问题，让翻译首页和工具弹窗更接近设计稿：白底、轻量、工具箱语义明确，避免灰脏背景和过重按钮。

### 范围

- 生成视觉对齐参考图并保存到 `docs/ui/translate-home-visual-alignment-design.png`。
- 将右上角工具入口从魔法棒调整为工具箱 / 公文包语义图标。
- 统一翻译首页背景为白底视觉，减少灰色块割裂。
- 优化工具弹窗卡片的留白、圆角、图标块、分隔线和底部提示。
- 保持拍照翻译、相册导入、OCR 和翻译流程不变。

### 不包含

- 不新增新的工具项。
- 不重做图片翻译业务流程。
- 不改动底部导航结构。

### 完成标准

- 首页实际截图与视觉参考图主要结构一致。
- 右上角工具入口不再像装饰按钮或 AI 魔法按钮，而是明确的工具入口。
- 工具弹窗视觉更轻，拍照翻译和相册导入仍可点击。
- Kotlin 编译和 Debug APK 构建通过。
- 安装到模拟器并截图验证。

### 验证记录

- 已使用 imagegen 生成视觉对齐参考图：`docs/ui/translate-home-visual-alignment-design.png`。
- 已将右上角工具入口从魔法棒改为工具箱 / 公文包图标，并调整为白底细描边按钮。
- 已将翻译首页主体背景统一为白底，输入卡和译文卡改为更轻的白色卡片。
- 已优化工具弹窗：更大的水平留白、更轻的操作卡片、更柔和图标块和虚线分隔。
- 已通过 `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- 已通过 `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- 模拟器空间不足时，先尝试保留数据卸载仍失败，随后清理模拟器内旧应用数据后安装新版成功；不影响项目文件。
- 已截图验证：
  - 首页：`docs/ui/prototype-screenshots/visual-alignment-home-ready.png`
  - 工具弹窗：`docs/ui/prototype-screenshots/visual-alignment-toolbox.png`
  - 工具入口点击可唤起系统相机：`docs/ui/prototype-screenshots/visual-alignment-camera-launch.png`

## Task 030：发布 1.0.3 内置更新包

### 目标

将近期模型选择修复、拍照翻译、首页工具入口和视觉修正整理为 `1.0.3` Debug 更新包，上传到 Cloudflare R2 内置更新通道，并提交推送到 GitHub。

### 范围

- 将默认版本提升为 `versionCode = 4`、`versionName = 1.0.3`。
- 更新 R2 Debug 发版脚本默认版本、APK 对象路径和更新说明。
- 运行 Kotlin 编译、单元测试、Debug APK 构建。
- 执行 R2 发版脚本，生成并上传 `releases/ai-translate-1.0.3-debug.apk` 和 `releases/latest.json`。
- 验证公开更新清单和 APK 可访问。
- 提交并推送 GitHub。

### 不包含

- 不配置正式 release keystore。
- 不发布 Google Play / 应用商店版本。
- 不变更内置更新协议格式。

### 完成标准

- App 默认版本号为 `1.0.3 (4)`。
- R2 `releases/latest.json` 指向 `1.0.3` Debug APK。
- APK 大小和 SHA256 写入 manifest 并通过公开访问校验。
- 构建和单元测试通过。
- GitHub `main` 分支收到本次发布提交。

### 验证记录

- 已将默认版本号提升为 `versionCode = 4`、`versionName = 1.0.3`。
- 已更新 `scripts/publish-r2-debug-update.ps1`，默认发布 `releases/ai-translate-1.0.3-debug.apk`。
- 已通过 `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- 已通过 `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`，耗时 49 秒。
- 已通过 `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- 已执行 `.\scripts\publish-r2-debug-update.ps1`，上传 `releases/ai-translate-1.0.3-debug.apk` 和 `releases/latest.json`。
- R2 manifest GET 返回 200，APK HEAD 返回 200，APK 公开大小为 `187507940` 字节。
- R2 manifest 已写入 SHA256：`AA4A7EB54D2A7742B0AEB39D677B8916DF283977863579E7F1353FC624E529D2`。
- 本次发布改动已提交并推送到 GitHub `main` 分支。

## Task 031：内置离线英汉词典首版

### 目标

在 App 内置一个许可证清晰、体积可控的英汉词库，让用户除了翻译句子，也能离线搜索英文单词并查看详细释义、音标、词性、考试标签、词频和词形变化。

### 词库调研结论

- 首版采用 GitHub `skywind3000/ECDICT`，仓库说明为英汉双解词典数据库，许可证为 MIT。
- ECDICT 完整 CSV 约 66MB，直接内置会继续推高 APK 体积；首版从完整 CSV 裁剪常用词子集。
- `ecdict.mini.csv` 只有约 4KB，是示例数据，不适合作为真实内置词库。
- 暂不采用 CC-CEDICT 作为首版内置来源，因为它主要是汉英词典，且 CC BY-SA 授权对 App 分发和衍生数据有额外共享要求。

### 范围

- 生成词典页设计图并保存到 `docs/ui/dictionary-lookup-design.png`。
- 新增精简 ECDICT 内置资源和许可证说明。
- 新增本地词典查询封装，支持精确匹配、大小写归一化和前缀建议。
- 新增底部“词典”页，提供搜索框、单词详情、空状态和建议词列表。
- 单词详情展示音标、中文释义、英文释义、标签、词频和词形变化。

### 不包含

- 不内置完整 66MB ECDICT 数据库。
- 不做中英双向完整词典检索。
- 不新增在线词典 API。
- 不实现生词本、背词计划或发音音频下载。

### 完成标准

- App 可离线查询内置常用英文单词。
- 查询结果包含中文释义和至少一种辅助信息（音标、英文释义、标签或词形变化）。
- 未命中时显示明确提示和相近建议。
- 单元测试覆盖精确查询、大小写查询和未命中建议。
- Kotlin 编译、单元测试和 Debug APK 构建通过。

### 验证记录

- 已使用 imagegen 生成词典页设计图：`docs/ui/dictionary-lookup-design.png`。
- 已从 ECDICT 完整 CSV 生成 20000 条常用英文单词子集：`app/src/main/assets/dictionary/ecdict_essential.tsv`。
- 已随包内置 ECDICT MIT 许可证和来源说明：`ECDICT_LICENSE.txt`、`ECDICT_SOURCE.txt`。
- APK 内确认包含词典资源，`ecdict_essential.tsv` 原始大小 `4582257` 字节，APK 内压缩后约 `2330238` 字节。
- 已确认内置词库包含 `reason`、`hello`、`dictionary` 等常用词。
- 已新增本地词典查询封装、底部“词典”页、搜索框、详情卡片、建议词列表和空状态。
- 已新增 ViewModel 单元测试，覆盖精确查询、大小写查询和未命中建议。
- `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`：通过。
- `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`：命令超过 60 秒上限被截断；测试结果 XML 显示 8 个测试类共 42 个测试均为 0 failure / 0 error。
- `.\gradlew.bat :app:testDebugUnitTest --tests "com.mxwis.aitranslate.ui.TranslateViewModelTest" --no-daemon --console=plain`：命令超过 60 秒上限被截断；对应测试结果 XML 显示 12 个测试均为 0 failure / 0 error。
- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`：通过。
- 当前尝试启动 `Pixel_9_Pro` 模拟器进行截图验证，但设备停留在 `offline`，已关闭卡住的模拟器进程；设备侧点击截图待模拟器正常后补充。

## Task 032：发布 1.0.4 内置词典更新包

### 目标

将内置离线英汉词典首版整理为 `1.0.4` Debug 更新包，上传到 Cloudflare R2 内置更新通道，并提交推送到 GitHub。

### 范围

- 将默认版本提升为 `versionCode = 5`、`versionName = 1.0.4`。
- 更新 R2 Debug 发版脚本默认版本、APK 对象路径和更新说明。
- 运行 Kotlin 编译、单元测试、Debug APK 构建。
- 执行 R2 发版脚本，生成并上传 `releases/ai-translate-1.0.4-debug.apk` 和 `releases/latest.json`。
- 验证公开更新清单和 APK 可访问。
- 提交并推送 GitHub。

### 不包含

- 不配置正式 release keystore。
- 不发布 Google Play / 应用商店版本。
- 不继续扩大内置词库规模。
- 不新增在线词典 API。

### 完成标准

- App 默认版本号为 `1.0.4 (5)`。
- R2 `releases/latest.json` 指向 `1.0.4` Debug APK。
- APK 大小和 SHA256 写入 manifest 并通过公开访问校验。
- 构建和单元测试完成验证。
- GitHub `main` 分支收到本次发布提交。

### 验证记录

- 已将默认版本号提升为 `versionCode = 5`、`versionName = 1.0.4`。
- 已更新 `scripts/publish-r2-debug-update.ps1`，默认发布 `releases/ai-translate-1.0.4-debug.apk`。
- 已通过 `.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`。
- 已通过 `.\gradlew.bat testDebugUnitTest --no-daemon --console=plain`，耗时 25 秒。
- 已通过 `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`。
- 已执行 `.\scripts\publish-r2-debug-update.ps1`，上传 `releases/ai-translate-1.0.4-debug.apk` 和 `releases/latest.json`。
- R2 manifest GET 返回 200，APK HEAD 返回 200，APK 公开大小为 `192197321` 字节。
- R2 manifest 已写入 SHA256：`9D23DC42093334EEDDAFECE3A5A7A156FDEC5B2FF4785BD33F72072EB2F9721F`。
- `app/build/outputs/apk/debug/output-metadata.json` 已确认 `versionCode = 5`、`versionName = 1.0.4`。
- 本次发布改动已提交并推送到 GitHub `main` 分支。
