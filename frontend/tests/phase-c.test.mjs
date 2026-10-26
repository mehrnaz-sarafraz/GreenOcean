import assert from 'node:assert/strict';
import test from 'node:test';
import { validateRegistration, registrationFailure } from '../src/features/auth/registration-validation.ts';

const valid={email:'adult@example.test',password:'a sufficient synthetic passphrase',countryCode:'IR',adultSelfAttested:true,termsVersion:1,accepted:true};
test('Happy C: minimal payload remains sufficient',()=>assert.deepEqual(validateRegistration(valid),{}));
test('Creative C: whitespace plus addressing and punycode are supported',()=>{
 for(const email of ['  A.B+tag@EXAMPLE.TEST  ',"o'brien@example.test",'a@xn--mgbh0fb.xn--kgbechtv'])assert.deepEqual(validateRegistration({...valid,email}),{});
});
test('Creative C: exactly 128 Unicode code points work',()=>assert.deepEqual(validateRegistration({...valid,password:'🌊🌍🌱🔑'.repeat(32)}),{}));
test('Confused C: common and low diversity passwords match server feedback',()=>{
 for(const password of ['passwordpassword','x'.repeat(30),'123456789012345'])assert.ok(validateRegistration({...valid,password}).password);
});
test('Malicious C: malformed oversized and unsupported email fails locally',()=>{
 for(const email of ['a..b@example.test','a@-example.test','a@example..test','a@domain','فارسی@example.test','x'.repeat(65)+'@example.test','x@'+'domain.'.repeat(50)+'test'])assert.ok(validateRegistration({...valid,email}).email);
});
test('Confused C: server fields are actionable and policy changes request a refresh',()=>{
 const failure=registrationFailure({status:400,fieldErrors:[{field:'termsVersion',message:'Review the current Terms'}]});
 assert.equal(failure.refreshTerms,true);assert.equal(failure.fields.termsVersion,'Review the current Terms');
});
test('Malicious C: unrelated server fields and internals are not displayed',()=>{
 const failure=registrationFailure({status:500,message:'SQL credentials secret',fieldErrors:[{field:'internal',message:'secret'}]});
 assert.deepEqual(failure.fields,{});assert.doesNotMatch(failure.message,/secret|SQL/);
});
test('Confused C: rate limits include a bounded recovery instruction',()=>assert.match(registrationFailure({status:429,retryAfter:120}).message,/Wait 120 seconds/));
test('Confused C: offline and lost response never claim success',()=>{
 const failure=registrationFailure({});assert.match(failure.message,/not confirmed/);assert.match(failure.message,/verification/);
});
test('Confused C: errors are localized for Persian',()=>assert.match(registrationFailure({status:429},true).message,/ثانیه/));
