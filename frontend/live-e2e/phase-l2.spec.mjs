import {test,expect} from '@playwright/test';import {api,account,login,reset} from './phase-l-helpers.mjs';
test.beforeEach(reset);
test('L2 real stack: private follow consent, named Following, anonymous Saved and threaded replies',async({page,request,browser})=>{
 const author=await account(request),viewer=await account(request);const ownerProfile=await(await request.get(`${api}/profiles/me`,{headers:author.headers})).json();
 const create=async(body,anonymous)=>(await request.post(`${api}/posts`,{headers:author.headers,data:{body,anonymous,publicationIdentity:anonymous?'ANONYMOUS':'SOCIAL_IDENTITY',visibility:'PUBLIC',broaderAudienceConfirmed:true}})).json();
 const named=await create('L2 named followed reflection',false),anonymous=await create('L2 anonymous neutral reflection',true);
 expect((await request.put(`${api}/social/follows/${ownerProfile.userId}`,{headers:viewer.headers})).status()).toBe(200);
 await login(page,author);await page.goto('/tools/connections');await page.getByRole('button',{name:'Accept follow request',exact:true}).click();await expect(page.getByText('No pending follow requests.',{exact:true})).toBeVisible();
 const context=await browser.newContext();const reader=await context.newPage();try{await login(reader,viewer);await reader.goto('/');await reader.getByRole('tab',{name:'Following',exact:true}).click();await expect(reader.getByText(named.body,{exact:true})).toBeVisible();await expect(reader.getByText(anonymous.body,{exact:true})).toHaveCount(0);
 expect((await request.put(`${api}/posts/${anonymous.id}/bookmark`,{headers:viewer.headers})).status()).toBe(204);await reader.getByRole('tab',{name:'Saved',exact:true}).click();await expect(reader.getByText(anonymous.body,{exact:true})).toBeVisible();
 expect((await request.post(`${api}/posts/${anonymous.id}/comments`,{headers:author.headers,data:{body:'Thank you for listening',anonymous:false}})).status()).toBe(201);
 await reader.goto(`/post/${anonymous.id}`);await reader.getByRole('button',{name:'Reply',exact:true}).click();await reader.getByLabel('Reply text',{exact:true}).fill('A thoughtful supportive response');await reader.getByRole('button',{name:'Send reply',exact:true}).click();await expect(reader.getByText('Reply published.',{exact:true})).toBeVisible();await expect(reader.getByText('A thoughtful supportive response',{exact:true})).toBeVisible();
 const following=await(await request.get(`${api}/posts/discovery?mode=FOLLOWING`,{headers:viewer.headers})).json();expect(following.items.map(p=>p.id)).toContain(named.id);expect(following.items.map(p=>p.id)).not.toContain(anonymous.id);
 }finally{await context.close();}
});
