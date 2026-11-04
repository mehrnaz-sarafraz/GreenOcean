import {test,expect} from '@playwright/test';
import {api,account,login,reset} from './phase-l-helpers.mjs';
import {moderator,syntheticStaffHeaders} from './phase-m-helpers.mjs';
test.beforeEach(reset);
test('Q2 real stack: two current WebAuthn administrators approve one-use anonymous attribution',async({page,request,browser})=>{
 test.setTimeout(240000);const owner=await account(request);const ownerId=(await(await request.get(`${api}/auth/me`,{headers:owner.headers})).json()).userId;
 const body='Synthetic Q2 quotation https://example.test/q2';await login(page,owner);await page.goto('/create');await page.getByRole('button',{name:'Anonymous',exact:true}).click();await page.getByRole('button',{name:'Continue to writing',exact:true}).click();await page.getByLabel('Post text',{exact:true}).fill(body);await page.getByRole('button',{name:'Eligible members',exact:true}).click();await page.getByRole('button',{name:'Review before publishing',exact:true}).click();await page.getByRole('switch',{name:'Confirm broader audience',exact:true}).click();await page.getByRole('button',{name:'Publish',exact:true}).click();await expect(page.getByRole('alert')).toContainText('review');
 const first=await moderator(browser,request,{educationalAdmin:true}),second=await moderator(browser,request,{educationalAdmin:true});
 try{const headers=syntheticStaffHeaders.get(first.page);await first.page.goto('https://localhost:8443/community-review');const card=first.page.getByText(body,{exact:true}).locator('..');await card.getByRole('button',{name:'Threat investigation',exact:true}).click();await card.getByRole('button',{name:'Request independent attribution approval',exact:true}).click();await expect(first.page.getByText('Exceptional evidence request:',{exact:false})).toBeVisible();
 const rows=await(await request.get(`${api}/admin/social-attribution-requests`,{headers})).json();const grant=rows[0];expect(grant).toBeTruthy();expect(JSON.stringify(rows)).not.toContain(ownerId);
 expect((await request.post(`${api}/admin/social-attribution-requests/${grant.id}/approve`,{headers,data:{revision:grant.revision}})).status()).toBe(403);
 await second.page.goto('https://localhost:8443/community-review');await second.page.getByRole('button',{name:'Refresh decisions',exact:true}).click();await second.page.getByRole('button',{name:'Independently approve exact evidence request',exact:true}).click();await expect(second.page.getByText(ownerId,{exact:true})).toHaveCount(0);
 await first.page.getByRole('button',{name:'Refresh decisions',exact:true}).click();await first.page.getByRole('button',{name:'Use this one-time approved grant',exact:true}).click();await expect(first.page.getByText(ownerId,{exact:true})).toBeVisible();await expect(first.page.getByRole('button',{name:'Use this one-time approved grant',exact:true})).toHaveCount(0);
 expect((await request.post(`${api}/admin/social-moderation/${grant.assessment_id}/attribution`,{headers,data:{requestId:grant.id}})).status()).toBe(409);
 expect((await request.post(`${api}/admin/social-moderation/${grant.assessment_id}/attribution`,{headers:owner.headers,data:{requestId:grant.id}})).status()).toBe(403);
 }finally{await first.context.close();await second.context.close();}
});
