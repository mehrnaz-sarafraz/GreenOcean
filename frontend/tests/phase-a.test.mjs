import assert from 'node:assert/strict';
import test from 'node:test';
import { validateRegistration } from '../src/features/auth/registration-validation.ts';
import { fetchWithTimeout,singleFlight } from '../src/lib/api/request.ts';
const valid={email:'adult@example.test',password:'a sufficiently long phrase',countryCode:'IR',adultSelfAttested:true,termsVersion:1,accepted:true};
test('Happy: adult can submit minimal registration',()=>assert.deepEqual(validateRegistration(valid),{}));
test('Happy: optional display name, city, birth year and topics are unnecessary',()=>assert.deepEqual(Object.keys(validateRegistration({...valid})),[]));
test('Creative: long Unicode and spaces are allowed',()=>assert.deepEqual(validateRegistration({...valid,password:'رمز طولانی و قابل استفاده 🌊 '.repeat(3)}),{}));
test('Creative: password composition is not prescribed',()=>assert.deepEqual(validateRegistration({...valid,password:'a long lowercase passphrase'}),{}));
test('Creative: Unicode length counts code points',()=>assert.deepEqual(validateRegistration({...valid,password:'🌊🌱🌍🔑'.repeat(4)}),{}));
test('Confused: malformed email has field error',()=>assert.ok(validateRegistration({...valid,email:'wrong'}).email));
test('Confused: empty country has field error',()=>assert.ok(validateRegistration({...valid,countryCode:''}).countryCode));
test('Confused: terms must be loaded before submission',()=>assert.ok(validateRegistration({...valid,termsVersion:0}).termsVersion));
test('Confused: short password has field error',()=>assert.ok(validateRegistration({...valid,password:'too short'}).password));
test('Malicious: adult attestation cannot be omitted',()=>assert.ok(validateRegistration({...valid,adultSelfAttested:false}).adultSelfAttested));
test('Malicious: valid Terms acceptance is mandatory',()=>assert.ok(validateRegistration({...valid,accepted:false}).termsVersion));
test('Malicious: oversized password is bounded',()=>assert.ok(validateRegistration({...valid,password:'a'.repeat(129)}).password));
test('Malicious: malformed country is rejected',()=>assert.ok(validateRegistration({...valid,countryCode:'../IR'}).countryCode));
test('Creative: concurrent refresh callers share one operation',async()=>{
 let calls=0;let finish;const run=singleFlight(()=>{calls++;return new Promise(resolve=>{finish=resolve;});});
 const first=run(),second=run();assert.equal(calls,1);finish('rotated');assert.equal(await first,'rotated');assert.equal(await second,'rotated');
});
test('Confused: failed refresh can be retried',async()=>{
 let calls=0;const run=singleFlight(async()=>{if(++calls===1)throw new Error('offline');return 'ok';});
 await assert.rejects(run());assert.equal(await run(),'ok');assert.equal(calls,2);
});
test('Confused: network failure is recoverable and readable',async()=>{
 const original=globalThis.fetch;try{globalThis.fetch=async()=>{throw new TypeError('fetch failed');};await assert.rejects(fetchWithTimeout('https://example.test'),/Check your connection/);
 globalThis.fetch=async()=>new Response('{}');assert.equal((await fetchWithTimeout('https://example.test')).status,200);}finally{globalThis.fetch=original;}
});
test('Confused: timeout does not claim registration succeeded',async()=>{
 const original=globalThis.fetch;try{globalThis.fetch=async()=>{throw new DOMException('timeout','TimeoutError');};await assert.rejects(fetchWithTimeout('https://example.test'),/not confirmed/);}finally{globalThis.fetch=original;}
});
