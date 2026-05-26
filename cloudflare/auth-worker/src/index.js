const encoder = new TextEncoder();
const decoder = new TextDecoder();
const PASSWORD_ITERATIONS = 120000;
const TOKEN_TTL_SECONDS = 60 * 60 * 24 * 14;
const VERIFICATION_CODE_TTL_MS = 10 * 60 * 1000;
const VERIFICATION_CODE_COOLDOWN_MS = 60 * 1000;
const RESEND_EMAILS_ENDPOINT = "https://api.resend.com/emails";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET,POST,PUT,DELETE,OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type,Authorization",
};

export function normalizeUsername(username) {
  return String(username ?? "").trim().toLowerCase();
}

export function validateUsername(username) {
  const normalized = normalizeUsername(username);
  if (normalized.length < 3 || normalized.length > 32) {
    return "账号长度需要在 3 到 32 个字符之间";
  }
  if (!/^[a-z0-9_]+$/.test(normalized)) {
    return "账号只能包含小写字母、数字和下划线";
  }
  return null;
}

export function validatePassword(password) {
  const value = String(password ?? "");
  if (value.length < 6 || value.length > 72) {
    return "密码长度需要在 6 到 72 个字符之间";
  }
  return null;
}

export function normalizeEmail(email) {
  return String(email ?? "").trim().toLowerCase();
}

export function validateEmail(email) {
  const normalized = normalizeEmail(email);
  if (normalized.length < 6 || normalized.length > 254) {
    return "邮箱格式不正确";
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalized)) {
    return "邮箱格式不正确";
  }
  return null;
}

export function validateVerificationCode(code) {
  if (!/^\d{6}$/.test(String(code ?? "").trim())) {
    return "验证码需要是 6 位数字";
  }
  return null;
}

export async function hashPassword(password, saltBytes = crypto.getRandomValues(new Uint8Array(16))) {
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(password),
    "PBKDF2",
    false,
    ["deriveBits"],
  );
  const bits = await crypto.subtle.deriveBits(
    {
      name: "PBKDF2",
      salt: saltBytes,
      iterations: PASSWORD_ITERATIONS,
      hash: "SHA-256",
    },
    key,
    256,
  );
  return [
    "pbkdf2_sha256",
    String(PASSWORD_ITERATIONS),
    bytesToBase64Url(saltBytes),
    bytesToBase64Url(new Uint8Array(bits)),
  ].join("$");
}

export async function verifyPassword(password, storedHash) {
  const parts = String(storedHash ?? "").split("$");
  if (parts.length !== 4 || parts[0] !== "pbkdf2_sha256") return false;

  const iterations = Number(parts[1]);
  const saltBytes = base64UrlToBytes(parts[2]);
  const expectedHash = parts[3];
  if (!Number.isFinite(iterations) || iterations < 10000 || saltBytes.length === 0) return false;

  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(password),
    "PBKDF2",
    false,
    ["deriveBits"],
  );
  const bits = await crypto.subtle.deriveBits(
    {
      name: "PBKDF2",
      salt: saltBytes,
      iterations,
      hash: "SHA-256",
    },
    key,
    256,
  );
  return timingSafeEqual(bytesToBase64Url(new Uint8Array(bits)), expectedHash);
}

export async function signToken(payload, secret, nowSeconds = currentSeconds()) {
  const header = { alg: "HS256", typ: "JWT" };
  const body = {
    ...payload,
    iat: nowSeconds,
    exp: nowSeconds + TOKEN_TTL_SECONDS,
  };
  const unsignedToken = [
    stringToBase64Url(JSON.stringify(header)),
    stringToBase64Url(JSON.stringify(body)),
  ].join(".");
  const signature = await hmacSha256(unsignedToken, secret);
  return `${unsignedToken}.${signature}`;
}

export async function verifyToken(token, secret, nowSeconds = currentSeconds()) {
  const parts = String(token ?? "").split(".");
  if (parts.length !== 3) throw new Error("登录状态无效");

  const unsignedToken = `${parts[0]}.${parts[1]}`;
  const expectedSignature = await hmacSha256(unsignedToken, secret);
  if (!timingSafeEqual(expectedSignature, parts[2])) {
    throw new Error("登录状态无效");
  }

  const payload = JSON.parse(bytesToString(base64UrlToBytes(parts[1])));
  if (!payload.sub || Number(payload.exp) <= nowSeconds) {
    throw new Error("登录已过期，请重新登录");
  }
  return payload;
}

export async function hashVerificationCode(email, code, secret) {
  const normalized = normalizeEmail(email);
  const payload = `${normalized}:${String(code).trim()}:${secret}`;
  const digest = await crypto.subtle.digest("SHA-256", encoder.encode(payload));
  return bytesToBase64Url(new Uint8Array(digest));
}

export function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json; charset=utf-8",
    },
  });
}

export async function handleRequest(request, env) {
  if (request.method === "OPTIONS") {
    return new Response(null, { status: 204, headers: corsHeaders });
  }

  const url = new URL(request.url);
  const path = normalizePath(url.pathname);

  try {
    if (request.method === "POST" && path === "/auth/send-code") {
      return await sendRegistrationCode(request, env);
    }
    if (request.method === "POST" && path === "/auth/register") {
      return await register(request, env);
    }
    if (request.method === "POST" && path === "/auth/login") {
      return await login(request, env);
    }
    if (request.method === "GET" && path === "/auth/me") {
      const context = await requireUser(request, env);
      return jsonResponse({ user: toPublicUser(context.user) });
    }
    if (request.method === "POST" && path === "/auth/logout") {
      return jsonResponse({ ok: true });
    }
    if (request.method === "POST" && path === "/history") {
      return await createHistory(request, env);
    }
    if (request.method === "GET" && path === "/history") {
      return await listHistory(request, env, Number(url.searchParams.get("limit") ?? 50));
    }
    if (request.method === "PUT" && path === "/settings") {
      return await saveUserSettings(request, env);
    }
    if (request.method === "GET" && path === "/settings") {
      return await readUserSettings(request, env);
    }
    return jsonResponse({ message: "接口不存在" }, 404);
  } catch (error) {
    const status = error.statusCode || 500;
    return jsonResponse({ message: error.publicMessage || error.message || "服务器处理失败" }, status);
  }
}

async function register(request, env) {
  const body = await readJson(request);
  const username = normalizeUsername(body.username);
  const usernameError = validateUsername(username);
  if (usernameError) throw publicError(usernameError, 400);

  const passwordError = validatePassword(body.password);
  if (passwordError) throw publicError(passwordError, 400);

  const email = normalizeEmail(body.email);
  const verificationRequired = env.REQUIRE_EMAIL_VERIFICATION === "true" ||
    email ||
    String(body.verificationCode ?? "").trim();
  if (verificationRequired) {
    const emailError = validateEmail(email);
    if (emailError) throw publicError(emailError, 400);

    const codeError = validateVerificationCode(body.verificationCode);
    if (codeError) throw publicError(codeError, 400);

    const existing = await env.DB.prepare("SELECT id FROM users WHERE username = ? OR email = ?")
      .bind(username, email)
      .first();
    if (existing) throw publicError("账号或邮箱已存在", 409);

    await verifyRegistrationCode(env, email, body.verificationCode);

    const existingAfterCode = await env.DB.prepare("SELECT id FROM users WHERE username = ? OR email = ?")
      .bind(username, email)
      .first();
    if (existingAfterCode) throw publicError("账号或邮箱已存在", 409);
  } else {
    const existing = await env.DB.prepare("SELECT id FROM users WHERE username = ?")
      .bind(username)
      .first();
    if (existing) throw publicError("账号已存在", 409);
  }

  const now = Date.now();
  const user = {
    id: crypto.randomUUID(),
    username,
    email: verificationRequired ? email : null,
    created_at: now,
    last_login_at: now,
  };
  const passwordHash = await hashPassword(body.password);

  await env.DB.prepare(
    "INSERT INTO users (id, username, email, password_hash, created_at, last_login_at) VALUES (?, ?, ?, ?, ?, ?)",
  ).bind(user.id, user.username, user.email, passwordHash, user.created_at, user.last_login_at).run();

  if (verificationRequired) {
    await consumeRegistrationCode(env, email);
  }

  return authResponse(user, env);
}

async function sendRegistrationCode(request, env) {
  const body = await readJson(request);
  const email = normalizeEmail(body.email);
  const emailError = validateEmail(email);
  if (emailError) throw publicError(emailError, 400);

  const username = normalizeUsername(body.username);
  if (username) {
    const usernameError = validateUsername(username);
    if (usernameError) throw publicError(usernameError, 400);
  }

  const existing = username
    ? await env.DB.prepare("SELECT id FROM users WHERE username = ? OR email = ?")
      .bind(username, email)
      .first()
    : await env.DB.prepare("SELECT id FROM users WHERE email = ?")
      .bind(email)
      .first();
  if (existing) throw publicError("账号或邮箱已存在", 409);

  const now = Date.now();
  const latest = await env.DB.prepare(
    `SELECT created_at FROM email_verification_codes
     WHERE email = ? AND purpose = 'register' AND consumed_at IS NULL
     ORDER BY created_at DESC LIMIT 1`,
  ).bind(email).first();
  if (latest && now - Number(latest.created_at) < VERIFICATION_CODE_COOLDOWN_MS) {
    throw publicError("验证码发送太频繁，请稍后再试", 429);
  }

  const code = generateVerificationCode();
  const id = crypto.randomUUID();
  const codeHash = await hashVerificationCode(email, code, tokenSecret(env));

  await env.DB.prepare(
    `INSERT INTO email_verification_codes
      (id, email, purpose, code_hash, created_at, expires_at, attempts, request_ip)
      VALUES (?, ?, 'register', ?, ?, ?, 0, ?)`,
  ).bind(
    id,
    email,
    codeHash,
    now,
    now + VERIFICATION_CODE_TTL_MS,
    request.headers.get("CF-Connecting-IP") || "",
  ).run();

  try {
    const resendMessageId = await sendVerificationEmail(env, email, code);
    await env.DB.prepare("UPDATE email_verification_codes SET resend_message_id = ? WHERE id = ?")
      .bind(resendMessageId, id)
      .run();
  } catch (error) {
    await env.DB.prepare("UPDATE email_verification_codes SET consumed_at = ? WHERE id = ?")
      .bind(Date.now(), id)
      .run();
    throw error;
  }

  return jsonResponse({
    ok: true,
    expiresInSeconds: Math.floor(VERIFICATION_CODE_TTL_MS / 1000),
    cooldownSeconds: Math.floor(VERIFICATION_CODE_COOLDOWN_MS / 1000),
  });
}

async function login(request, env) {
  const body = await readJson(request);
  const username = normalizeUsername(body.username);
  const user = await env.DB.prepare(
    "SELECT id, username, email, password_hash, created_at, last_login_at FROM users WHERE username = ?",
  ).bind(username).first();

  if (!user || !(await verifyPassword(body.password, user.password_hash))) {
    throw publicError("账号或密码错误", 401);
  }

  const now = Date.now();
  await env.DB.prepare("UPDATE users SET last_login_at = ? WHERE id = ?")
    .bind(now, user.id)
    .run();

  return authResponse({ ...user, last_login_at: now }, env);
}

async function verifyRegistrationCode(env, email, code) {
  const row = await env.DB.prepare(
    `SELECT id, code_hash, attempts, expires_at FROM email_verification_codes
     WHERE email = ? AND purpose = 'register' AND consumed_at IS NULL
     ORDER BY created_at DESC LIMIT 1`,
  ).bind(email).first();
  if (!row) throw publicError("请先获取邮箱验证码", 400);
  if (Number(row.expires_at) < Date.now()) throw publicError("验证码已过期，请重新获取", 400);
  if (Number(row.attempts) >= 5) throw publicError("验证码错误次数过多，请重新获取", 429);

  const expectedHash = await hashVerificationCode(email, code, tokenSecret(env));
  if (!timingSafeEqual(expectedHash, row.code_hash)) {
    await env.DB.prepare("UPDATE email_verification_codes SET attempts = attempts + 1 WHERE id = ?")
      .bind(row.id)
      .run();
    throw publicError("验证码不正确", 400);
  }
}

async function consumeRegistrationCode(env, email) {
  await env.DB.prepare(
    `UPDATE email_verification_codes
     SET consumed_at = ?
     WHERE id = (
       SELECT id FROM email_verification_codes
       WHERE email = ? AND purpose = 'register' AND consumed_at IS NULL
       ORDER BY created_at DESC LIMIT 1
     )`,
  ).bind(Date.now(), email).run();
}

async function sendVerificationEmail(env, email, code) {
  if (!env.RESEND_API_KEY) throw publicError("Resend API Key 未配置", 500);
  if (!env.RESEND_FROM_EMAIL) throw publicError("Resend 发信地址未配置", 500);

  const response = await fetch(RESEND_EMAILS_ENDPOINT, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${env.RESEND_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      from: env.RESEND_FROM_EMAIL,
      to: [email],
      subject: "AI 翻译 App 注册验证码",
      text: `你的 AI 翻译 App 注册验证码是 ${code}，10 分钟内有效。若非本人操作，请忽略本邮件。`,
      html: buildVerificationEmailHtml(code),
    }),
  });

  const responseText = await response.text();
  if (!response.ok) {
    throw publicError(`验证码邮件发送失败：${parseResendError(responseText, response.status)}`, 502);
  }
  return runCatchingJson(responseText).id || "";
}

function buildVerificationEmailHtml(code) {
  return `
    <div style="font-family:Arial,'Microsoft YaHei',sans-serif;line-height:1.7;color:#172033;">
      <h2>AI 翻译 App 注册验证码</h2>
      <p>你的验证码是：</p>
      <p style="font-size:28px;font-weight:700;letter-spacing:6px;color:#2563eb;">${code}</p>
      <p>验证码 10 分钟内有效。若非本人操作，请忽略本邮件。</p>
    </div>
  `;
}

function parseResendError(responseText, status) {
  const json = runCatchingJson(responseText);
  return json.message || json.error || `HTTP ${status}`;
}

function runCatchingJson(value) {
  try {
    return JSON.parse(value);
  } catch {
    return {};
  }
}

function generateVerificationCode() {
  const value = crypto.getRandomValues(new Uint32Array(1))[0] % 1000000;
  return String(value).padStart(6, "0");
}

async function createHistory(request, env) {
  const context = await requireUser(request, env);
  const body = await readJson(request);
  const sourceText = requiredString(body.sourceText, "原文不能为空");
  const translatedText = requiredString(body.translatedText, "译文不能为空");
  const createdAt = Number(body.createdAt || Date.now());
  const id = requiredString(body.id || crypto.randomUUID(), "历史记录 id 不能为空");

  await env.DB.prepare(
    `INSERT OR REPLACE INTO translation_history
      (id, user_id, source_text, translated_text, source_language, target_language, mode, created_at, synced_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
  ).bind(
    id,
    context.user.id,
    sourceText,
    translatedText,
    String(body.sourceLanguage || "未知"),
    String(body.targetLanguage || "未知"),
    String(body.mode || "未知"),
    createdAt,
    Date.now(),
  ).run();

  return jsonResponse({ ok: true, id });
}

async function listHistory(request, env, limit) {
  const context = await requireUser(request, env);
  const safeLimit = Math.max(1, Math.min(Number.isFinite(limit) ? limit : 50, 100));
  const result = await env.DB.prepare(
    `SELECT id, source_text, translated_text, source_language, target_language, mode, created_at, synced_at
     FROM translation_history
     WHERE user_id = ?
     ORDER BY created_at DESC
     LIMIT ?`,
  ).bind(context.user.id, safeLimit).all();

  return jsonResponse({
    items: (result.results || []).map((item) => ({
      id: item.id,
      sourceText: item.source_text,
      translatedText: item.translated_text,
      sourceLanguage: item.source_language,
      targetLanguage: item.target_language,
      mode: item.mode,
      createdAt: item.created_at,
      syncedAt: item.synced_at,
    })),
  });
}

async function saveUserSettings(request, env) {
  const context = await requireUser(request, env);
  const body = await readJson(request);
  const settingsJson = JSON.stringify(body.settings ?? {});
  await env.DB.prepare(
    `INSERT INTO user_settings (user_id, settings_json, updated_at)
      VALUES (?, ?, ?)
      ON CONFLICT(user_id) DO UPDATE SET settings_json = excluded.settings_json, updated_at = excluded.updated_at`,
  ).bind(context.user.id, settingsJson, Date.now()).run();
  return jsonResponse({ ok: true });
}

async function readUserSettings(request, env) {
  const context = await requireUser(request, env);
  const row = await env.DB.prepare("SELECT settings_json, updated_at FROM user_settings WHERE user_id = ?")
    .bind(context.user.id)
    .first();
  return jsonResponse({
    settings: row ? JSON.parse(row.settings_json) : {},
    updatedAt: row?.updated_at ?? null,
  });
}

async function requireUser(request, env) {
  const header = request.headers.get("Authorization") || "";
  const token = header.startsWith("Bearer ") ? header.slice("Bearer ".length).trim() : "";
  if (!token) throw publicError("请先登录", 401);

  const payload = await verifyToken(token, tokenSecret(env)).catch((error) => {
    throw publicError(error.message || "登录状态无效", 401);
  });
  const user = await env.DB.prepare("SELECT id, username, email, created_at, last_login_at FROM users WHERE id = ?")
    .bind(payload.sub)
    .first();
  if (!user) throw publicError("账号不存在或已失效", 401);
  return { user, payload };
}

async function authResponse(user, env) {
  const token = await signToken({ sub: user.id, username: user.username }, tokenSecret(env));
  return jsonResponse({
    token,
    expiresAt: (currentSeconds() + TOKEN_TTL_SECONDS) * 1000,
    user: toPublicUser(user),
  });
}

function toPublicUser(user) {
  return {
    id: user.id,
    username: user.username,
    email: user.email || null,
  };
}

async function readJson(request) {
  try {
    return await request.json();
  } catch {
    throw publicError("请求体需要是 JSON", 400);
  }
}

function requiredString(value, message) {
  const text = String(value ?? "").trim();
  if (!text) throw publicError(message, 400);
  return text;
}

function publicError(message, statusCode) {
  const error = new Error(message);
  error.publicMessage = message;
  error.statusCode = statusCode;
  return error;
}

function tokenSecret(env) {
  if (!env.JWT_SECRET) {
    throw publicError("服务端密钥未配置", 500);
  }
  return env.JWT_SECRET;
}

function normalizePath(pathname) {
  const path = pathname.replace(/\/+$/, "");
  return path || "/";
}

function currentSeconds() {
  return Math.floor(Date.now() / 1000);
}

async function hmacSha256(value, secret) {
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign("HMAC", key, encoder.encode(value));
  return bytesToBase64Url(new Uint8Array(signature));
}

function stringToBase64Url(value) {
  return bytesToBase64Url(encoder.encode(value));
}

function bytesToString(bytes) {
  return decoder.decode(bytes);
}

function bytesToBase64Url(bytes) {
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  const base64 = typeof btoa === "function"
    ? btoa(binary)
    : Buffer.from(binary, "binary").toString("base64");
  return base64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function base64UrlToBytes(value) {
  const base64 = String(value).replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
  const binary = typeof atob === "function"
    ? atob(padded)
    : Buffer.from(padded, "base64").toString("binary");
  return Uint8Array.from(binary, (char) => char.charCodeAt(0));
}

function timingSafeEqual(a, b) {
  const left = String(a);
  const right = String(b);
  if (left.length === 0 || right.length === 0) return false;
  let diff = left.length ^ right.length;
  const maxLength = Math.max(left.length, right.length);
  for (let i = 0; i < maxLength; i += 1) {
    diff |= left.charCodeAt(i % left.length) ^ right.charCodeAt(i % right.length);
  }
  return diff === 0;
}

export default {
  fetch: handleRequest,
};
