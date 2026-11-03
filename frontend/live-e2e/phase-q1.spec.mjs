import {test,expect} from '@playwright/test';
import {api,account,login,reset} from './phase-l-helpers.mjs';
import {moderator,syntheticStaffHeaders} from './phase-m-helpers.mjs';

test.beforeEach(reset);
test('Q1 real stack: purpose-isolated coordination, truthful coverage and independent source confirmation',async({page,request,browser})=>{
 test.setTimeout(240000);const reporter=await account(request),subject=await account(request);
 const subjectId=(await(await request.get(`${api}/auth/me`,{headers:subject.headers})).json()).userId;
 const response=await request.post(`${api}/reports`,{headers:reporter.headers,data:{targetType:'USER',targetId:subjectId,reason:'OTHER',description:'SYNTHETIC_Q1_NARRATIVE_NOT_FOR_SHARED_QUEUE'}});expect(response.status()).toBe(201);const report=await response.json();
 const worker=await moderator(browser,request,{trustOperator:true}),reviewer=await moderator(browser,request,{trustOperator:true});
 try{
  const wh=syntheticStaffHeaders.get(worker.page),rh=syntheticStaffHeaders.get(reviewer.page);
  const workerId=(await(await request.get(`${api}/auth/me`,{headers:wh})).json()).userId,reviewerId=(await(await request.get(`${api}/auth/me`,{headers:rh})).json()).userId;
  await worker.page.goto('https://localhost:8443/trust-center');await expect(worker.page.getByRole('heading',{name:'GreenOcean Trust & Safety Center',exact:true})).toBeVisible();
  const imported=await request.post(`${api}/trust/cases`,{headers:wh,data:{domain:'SOCIAL',sourceKind:'REPORT',sourceId:report.id}});expect(imported.status()).toBe(201);const {id}=await imported.json();
  await worker.page.getByRole('button',{name:'Refresh safety workspace',exact:true}).click();const card=worker.page.getByText(`Case reference: ${id}`,{exact:true}).locator('..');await expect(card).toBeVisible();await expect(worker.page.getByText('SYNTHETIC_Q1_NARRATIVE_NOT_FOR_SHARED_QUEUE',{exact:true})).toHaveCount(0);
  await card.getByRole('button',{name:'Triaged',exact:true}).click();await expect(card.getByText('Revision: 2',{exact:true})).toBeVisible();
  const noCoverage=await request.post(`${api}/trust/cases/${id}/transition`,{headers:wh,data:{domain:'SOCIAL',revision:2,state:'UNDER_REVIEW'}});expect(noCoverage.status()).toBe(409);
  const shift=async(headers,operatorId)=>{const r=await request.post(`${api}/trust/coverage`,{headers,data:{domain:'SOCIAL',operatorId,startsAt:new Date(Date.now()-60000).toISOString(),endsAt:new Date(Date.now()+3600000).toISOString(),capacity:5}});expect(r.status()).toBe(201);return (await r.json()).id;};
  const workerShift=await shift(rh,workerId),reviewerShift=await shift(wh,reviewerId);
  expect((await request.post(`${api}/trust/coverage/${workerShift}/acknowledge`,{headers:wh})).status()).toBe(204);expect((await request.post(`${api}/trust/coverage/${reviewerShift}/acknowledge`,{headers:rh})).status()).toBe(204);
  await card.getByRole('button',{name:'Under review',exact:true}).click();await expect(card.getByText('Revision: 3',{exact:true})).toBeVisible();
  expect((await request.patch(`${api}/admin/reports/${report.id}`,{headers:wh,data:{action:'DISMISS',note:'Synthetic independent source review; no private narrative is copied into coordination'}})).status()).toBe(204);
  await card.getByRole('button',{name:'Independent approval pending',exact:true}).click();await expect(card.getByText('Revision: 4',{exact:true})).toBeVisible();
  expect((await request.post(`${api}/trust/cases/${id}/transition`,{headers:wh,data:{domain:'SOCIAL',revision:4,state:'ACTIONED'}})).status()).toBe(403);
  expect((await request.post(`${api}/trust/cases/${id}/transition`,{headers:rh,data:{domain:'SOCIAL',revision:4,state:'ACTIONED'}})).status()).toBe(204);
  expect((await request.get(`${api}/trust/cases?domain=SOCIAL`,{headers:reporter.headers})).status()).toBe(403);
  await login(page,reporter);await page.goto('/safety-status');await expect(page.getByText('Source decision confirmed',{exact:true})).toBeVisible();
  const own=await(await request.get(`${api}/account/safety-cases`,{headers:subject.headers})).json();expect(JSON.stringify(own)).not.toContain(id);
  expect((await request.delete(`${api}/trust/coverage/${workerShift}`,{headers:rh})).status()).toBe(204);
 }finally{await worker.context.close();await reviewer.context.close();}
});
