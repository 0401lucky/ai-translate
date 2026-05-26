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

本地开发时也需要在 `.dev.vars` 中提供同名变量：

```text
JWT_SECRET=replace-with-local-dev-secret
```

5. 本地开发或部署：

```bash
npm run dev
npm run deploy
```

Android 端通过 Gradle 参数覆盖后端地址：

```bash
./gradlew :app:assembleDebug -PauthBaseUrl=https://你的-worker.workers.dev
```
