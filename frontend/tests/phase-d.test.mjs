import assert from 'node:assert/strict';
import test from 'node:test';
import { normalizeVerificationCode,validVerificationCode,maskVerificationEmail,cooldownSeconds } from '../src/features/auth/verification.ts';
test('Happy D: six ASCII digits including leading zeros are valid',()=>assert.equal(validVerificationCode('000007'),true));
test('Creative D: pasted Persian Arabic and spaced digits normalize safely',()=>{
 for(const text of ['۰۰۰۰۰۷','٠٠٠٠٠٧','000 007','000-007'])assert.equal(normalizeVerificationCode(text),'000007');
});
test('Confused D: incomplete and malformed codes stay invalid',()=>{for(const text of ['','123','abcdef','1234567'])assert.equal(validVerificationCode(normalizeVerificationCode(text)),false);});
test('Malicious D: normalization does not strip arbitrary letters into a valid code',()=>assert.equal(validVerificationCode(normalizeVerificationCode('a123456')),false));
test('Happy D: recipient hints omit the full local part and domain',()=>{const masked=maskVerificationEmail('person@organization.test');assert.equal(masked,'p•••@o•••.test');assert.ok(!masked.includes('person')&&!masked.includes('organization'));});
test('Confused D: malformed email has no misleading recipient hint',()=>{for(const email of ['wrong','a@b','a@@b.test'])assert.equal(maskVerificationEmail(email),'');});
test('Creative D: cooldown is bounded at the boundary without deciding server validity',()=>{assert.equal(cooldownSeconds(60000,59000),1);assert.equal(cooldownSeconds(60000,60000),0);assert.equal(cooldownSeconds(60000,61000),0);});
