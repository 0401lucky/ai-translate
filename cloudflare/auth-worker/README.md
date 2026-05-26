# AI 翻译 App Cloudflare 认证后端

这是课程项目用的 Cloudflare Workers + D1 后端，提供注册、登录、当前用户、历史同步和用户设置接口。

## 本地/部署准备

1. 创建 D1 数据库：

```bash
wrangler d1 create ai_translate_auth
```

2. 将返回的 `database_id` 填入 `wrangler.toml`。

3. 执行 D1 迁移：

```bash
wrangler d1 migrations apply ai_translate_auth
```

4. 设置 token 签名密钥：

```bash
wrangler secret put JWT_SECRET
```

5. 若启用邮箱验证码，先在 Resend 添加并验证发信子域名，例如 `send.204152.xyz`，再配置 Resend API Key：

```bash
wrangler secret put RESEND_API_KEY
```

本地开发时也需要在 `.dev.vars` 中提供同名变量：

```text
JWT_SECRET=replace-with-local-dev-secret
RESEND_API_KEY=re_xxxxxxxxx
```

默认发信地址在 `wrangler.toml` 中配置为 `AI Translate <noreply@send.204152.xyz>`。
当前 `REQUIRE_EMAIL_VERIFICATION=false`，用于兼容已经发布的旧版 App；确认 Resend 可发信并发布新版客户端后，可改为 `true` 强制注册验证码。

5. 本地开发或部署：

```bash
npm run dev
npm run deploy
```

Android 端通过 Gradle 参数覆盖后端地址：

```bash
./gradlew :app:assembleDebug -PauthBaseUrl=https://你的-worker.workers.dev
```
