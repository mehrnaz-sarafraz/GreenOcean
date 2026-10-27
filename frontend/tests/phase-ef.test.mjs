import test from 'node:test';import assert from 'node:assert/strict';
import {passwordBounds,safeRecoveryToken,needsBackgroundReauthentication,securityEventLabel} from '../src/features/auth/security-contracts.ts';
import {createSerialQueue} from '../src/lib/storage/serial-queue.ts';
test('Happy EF: approved password bounds use Unicode code points',()=>{assert.equal(passwordBounds('🌊'.repeat(15)),true);assert.equal(passwordBounds('🌊'.repeat(128)),true);assert.equal(passwordBounds('🌊'.repeat(129)),false);assert.equal(passwordBounds('a'.repeat(14)),false);});
test('Malicious EF: only bounded URL-safe recovery tokens are accepted',()=>{assert.equal(safeRecoveryToken('a'.repeat(64)),true);for(const value of ['','a'.repeat(63),'a'.repeat(65),'javascript:alert(1)','a'.repeat(63)+'%','../../private'])assert.equal(safeRecoveryToken(value),false);});
test('Creative EF: local background lock starts exactly at five minutes',()=>{assert.equal(needsBackgroundReauthentication(1000,300999),false);assert.equal(needsBackgroundReauthentication(1000,301000),true);});
test('Confused EF: event labels have safe localized fallbacks',()=>{assert.equal(securityEventLabel('LOGIN_SUCCESS',false),'Signed in');assert.equal(securityEventLabel('<script>',false),'Security event');assert.equal(securityEventLabel('LOGIN_SUCCESS',true),'ورود به حساب');});

test('Creative EF: overlapping rotations read the latest verifier after prior proof',async()=>{
 const queue=createSerialQueue();let release;const gate=new Promise(resolve=>{release=resolve;});let verifier='original';const observed=[];
 const proof=queue(async()=>{observed.push(verifier);await gate;verifier='after-proof';});
 const refresh=queue(async()=>{observed.push(verifier);verifier='after-refresh';});
 await Promise.resolve();assert.deepEqual(observed,['original']);release();await Promise.all([proof,refresh]);assert.deepEqual(observed,['original','after-proof']);assert.equal(verifier,'after-refresh');
});
test('Confused EF: partial credential write failure still permits queued logout cleanup',async()=>{
 const queue=createSerialQueue();const store={access:'old-access',refresh:'old-refresh'};
 const write=queue(async()=>{store.refresh='partially-written';throw new Error('Synthetic secure storage failure');});
 const clear=queue(async()=>{delete store.access;delete store.refresh;});const read=queue(async()=>({...store}));
 const results=await Promise.allSettled([write,clear,read]);assert.equal(results[0].status,'rejected');assert.equal(results[1].status,'fulfilled');assert.deepEqual(results[2],{status:'fulfilled',value:{}});
});
