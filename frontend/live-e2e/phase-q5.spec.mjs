import {test,expect} from '@playwright/test';
import {api,account,login,reset} from './phase-l-helpers.mjs';
test.beforeEach(reset);
test('Q5 real stack: pilot visitor stays unenrolled and cannot fabricate a release assessment',async({page,request})=>{
 test.setTimeout(120000);const member=await account(request);await login(page,member);await page.goto('/pilot');
 await expect(page.getByText('Pilot runtime is not enabled. No visitor is automatically enrolled.',{exact:true})).toBeVisible();
 await expect(page.getByText('Staff release assessment',{exact:true})).toHaveCount(0);
 const own=await request.get(`${api}/pilot/me`,{headers:member.headers});expect(own.status()).toBe(200);expect((await own.json()).invitations).toEqual([]);
 expect((await request.post(`${api}/pilot/assessments`,{headers:member.headers,data:{service:'COMMUNITY',countryCode:'IR',sourceCommit:'a'.repeat(40)}})).status()).toBe(403);
 const matrix=await request.get(`${api}/public/pilot-readiness?country=IR`);expect(matrix.status()).toBe(200);const rows=await matrix.json();expect(rows).toHaveLength(8);expect(rows.every(row=>row.assessmentState==='NOT_ASSESSED'&&row.assessmentDoesNotActivateService)).toBe(true);
 expect((await request.get(`${api}/account/archive`,{headers:member.headers})).status()).toBe(200);
});
