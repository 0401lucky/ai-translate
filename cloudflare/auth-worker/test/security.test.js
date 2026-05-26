import assert from "node:assert/strict";
import test from "node:test";
import {
  hashPassword,
  normalizeUsername,
  signToken,
  validatePassword,
  validateUsername,
  verifyPassword,
  verifyToken,
} from "../src/index.js";

test("账号会统一转为小写并校验格式", () => {
  assert.equal(normalizeUsername("  Alice_01  "), "alice_01");
  assert.equal(validateUsername("alice_01"), null);
  assert.equal(validateUsername("ab"), "账号长度需要在 3 到 32 个字符之间");
  assert.equal(validateUsername("alice-01"), "账号只能包含小写字母、数字和下划线");
});

test("密码长度校验覆盖过短输入", () => {
  assert.equal(validatePassword("123456"), null);
  assert.equal(validatePassword("12345"), "密码长度需要在 6 到 72 个字符之间");
});

test("PBKDF2 密码哈希可以验证正确密码并拒绝错误密码", async () => {
  const salt = new Uint8Array(16).fill(7);
  const storedHash = await hashPassword("correct-password", salt);

  assert.equal(await verifyPassword("correct-password", storedHash), true);
  assert.equal(await verifyPassword("wrong-password", storedHash), false);
});

test("登录 token 会校验签名和过期时间", async () => {
  const token = await signToken({ sub: "user-1", username: "alice" }, "secret", 1000);
  const payload = await verifyToken(token, "secret", 1001);

  assert.equal(payload.sub, "user-1");
  await assert.rejects(() => verifyToken(token, "other-secret", 1001), /登录状态无效/);
  await assert.rejects(() => verifyToken(token, "secret", 1000 + 60 * 60 * 24 * 15), /登录已过期/);
});
