import assert from 'node:assert/strict';
import test from 'node:test';
import { publicCopy } from '../src/localization/public-copy.ts';
import { accountEntry,isInformationId,isAssistanceId,policyKey,safeResourceUrl } from '../src/features/public/contracts.ts';
for(const locale of ['en','fa']){
 const copy=publicCopy[locale];
 test(`Happy B-01/B-02 ${locale}: distinct truthful paths and account boundaries`,()=>{assert.equal(copy.pathways.length,3);assert.equal(new Set(copy.pathways.map(p=>p.id)).size,3);for(const p of copy.pathways)assert.ok(p.title&&p.body&&p.action);assert.ok(copy.boundary&&copy.availability&&copy.noDisclosure);});
 test(`Happy B-07/B-10 ${locale}: public information and policy destinations are allowlisted`,()=>{for(const link of [...copy.infoLinks,...copy.policyLinks])assert.ok(policyKey(link.id)||isInformationId(link.id));assert.equal(copy.steps.length,4);assert.ok(copy.faqs.length>=8);});
 test(`Creative B-09 ${locale}: all localized public strings are populated`,()=>{function walk(v){if(typeof v==='string')assert.ok(v.trim());else for(const child of Object.values(v))walk(child);}walk(copy);});
}
test('Creative B-09: Persian public copy mirrors English structure',()=>{function shape(v){return Array.isArray(v)?v.map(shape):v&&typeof v==='object'?Object.fromEntries(Object.entries(v).map(([k,x])=>[k,shape(x)])):typeof v;}assert.deepEqual(shape(publicCopy.fa),shape(publicCopy.en));});
test('Confused B-03/B-04: loading is not treated as signed out',()=>{assert.equal(accountEntry('loading'),'loading');assert.equal(accountEntry('authenticated'),'account');assert.equal(accountEntry('unauthenticated'),'guest');});
test('Confused B-05: missing region and network errors have explicit copy',()=>{for(const c of Object.values(publicCopy))assert.ok(c.noCountry&&c.noLocal&&c.directoryError&&c.retry);});
test('Confused B-07: policy failure never claims a cached policy is current',()=>{assert.match(publicCopy.en.policyUnavailable,/cannot show a hardcoded or older copy/);});
test('Malicious B-07/B-10: unknown, array and redirect-like route parameters fail closed',()=>{for(const v of ['https://evil.test','//evil.test','../admin','privacy<script>',{},['terms'],null]){assert.equal(isInformationId(v),false);assert.equal(isAssistanceId(v),false);assert.equal(policyKey(v),null);}});
test('Malicious B-05: untrusted protocols, credentials, controls and USSD are rejected',()=>{for(const url of ['javascript:alert(1)','data:text/html,attack','file:///tmp/private','https://user:pass@evil.test','https://example.test/\nattack','tel:*123#','tel:12;secret','//evil.test'])assert.equal(safeResourceUrl(url,true),null);});
test('Happy B-05: approved HTTPS URLs survive validation without redirects',()=>{assert.equal(safeResourceUrl('https://example.test/official'),'https://example.test/official');});
test('Malicious B-06/B-08: no fabricated counts or instantaneous-capacity promises',()=>{const copy=JSON.stringify(publicCopy.en);assert.doesNotMatch(copy,/\d+ listeners online|instant matching|guaranteed improvement|24\/7/i);assert.match(publicCopy.en.availability,/does not guarantee/);});
