import {test,expect} from '@playwright/test';
import {randomUUID} from 'node:crypto';
import {readFile} from 'node:fs/promises';
const api='http://127.0.0.1:8080/api/v1',password='Synthetic I support passphrase 2026 🌊';
async function key(){return readFile('../backend/target/qa-mailbox-token','utf8');}
test.beforeEach(async()=>{expect((await fetch('http://127.0.0.1:8099/reset-rate-window',{method:'POST',headers:{'X-QA-Key':await key()}})).status).toBe(204);});
async function account(request){
 const email=`i-live-${randomUUID()}@example.test`;
 expect((await request.post(`${api}/auth/register`,{data:{email,password,countryCode:'IR',adultSelfAttested:true,termsVersion:1}})).status()).toBe(201);
 const mailbox=async()=>{const r=await fetch(`http://127.0.0.1:8099/snapshot?email=${encodeURIComponent(email)}`,{headers:{'X-QA-Key':await key()}});return r.json();};
 await expect.poll(async()=>(await mailbox()).code).toMatch(/^\d{6}$/);
 expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code:(await mailbox()).code}})).status()).toBe(200);
 const signed=await request.post(`${api}/auth/login`,{data:{email,password}});expect(signed.status()).toBe(200);
 return {email,headers:{Authorization:`Bearer ${(await signed.json()).accessToken}`}};
}
async function provision(user,unapproved=false){const r=await fetch('http://127.0.0.1:8099/listener',{method:'POST',headers:{'Content-Type':'application/json','X-QA-Key':await key()},body:JSON.stringify({email:user.email,unapproved})});expect(r.status).toBe(200);}
async function login(page,user){await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(user.email);await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);}
test('Happy I live: real listener acceptance, explicit activation, two-party Unicode messages, reload and end',async({page,browser,request})=>{
 const member=await account(request),listener=await account(request);await provision(listener);
 expect((await request.patch(`${api}/profiles/me`,{headers:listener.headers,data:{displayName:'شنوندهٔ آزمایشی'}})).status()).toBe(200);
 const context=await browser.newContext(),peer=await context.newPage();
 try{
  await login(peer,listener);await peer.goto('/listener/workspace');await peer.getByRole('button',{name:'Become available on this device',exact:true}).click();await expect(peer.getByText('Available for a live request',{exact:true})).toBeVisible();
  await login(page,member);await page.goto('/listener/match');await page.getByRole('button',{name:'Review request to شنوندهٔ آزمایشی',exact:true}).click();await page.getByRole('button',{name:'Request live conversation',exact:true}).click();
  await expect(page.getByText('Waiting for listener acceptance',{exact:true})).toBeVisible();expect((await(await request.get(`${api}/conversations`,{headers:member.headers})).json()).filter(x=>x.serviceKind==='LISTENER')).toHaveLength(0);
  await peer.getByRole('button',{name:'Accept live request',exact:true}).click();await expect(page.getByText('Listener accepted — ready to enter',{exact:true})).toBeVisible();expect((await(await request.get(`${api}/conversations`,{headers:member.headers})).json()).filter(x=>x.serviceKind==='LISTENER')).toHaveLength(0);
  await page.getByRole('button',{name:'Enter accepted conversation',exact:true}).click();await expect(page).toHaveURL(/\/chat\//);await expect(page.getByRole('status',{name:'Live support session status',exact:true})).toHaveText('Live conversation active');
  await peer.getByRole('button',{name:'Open support conversation',exact:true}).click();await expect(peer.getByRole('status',{name:'Live support session status',exact:true})).toHaveText('Live conversation active');
  await page.getByLabel('Live support message',{exact:true}).fill('سلام، یک پیام آزمایشی 🌊');await page.getByRole('button',{name:'Send live message',exact:true}).click();await expect(peer.getByText('سلام، یک پیام آزمایشی 🌊',{exact:true})).toBeVisible();
  await peer.getByLabel('Live support message',{exact:true}).fill('I am listening — synthetic peer reply.');await peer.getByRole('button',{name:'Send live message',exact:true}).click();await expect(page.getByText('I am listening — synthetic peer reply.',{exact:true})).toBeVisible();
  await page.reload();await expect(page.getByText('I am listening — synthetic peer reply.',{exact:true})).toBeVisible();
  await page.getByRole('button',{name:'End support conversation',exact:true}).click();await expect(page.getByText('Conversation ended',{exact:true})).toBeVisible();await expect(peer.getByText('Conversation ended',{exact:true})).toBeVisible();await expect(peer.getByRole('button',{name:'Send live message',exact:true})).toBeDisabled();
  const mine=await(await request.get(`${api}/listener-support/requests/me`,{headers:member.headers})).json();expect(mine[0].status).toBe('COMPLETED');
  expect((await request.post(`${api}/conversations/${mine[0].conversationId}/messages`,{headers:listener.headers,data:{body:'stale send',clientMessageId:randomUUID()}})).status()).toBe(409);
  expect((await request.post(`${api}/reports`,{headers:member.headers,data:{targetType:'CONVERSATION',targetId:mine[0].conversationId,reason:'HARASSMENT'}})).status()).toBe(201);
  await page.getByRole('button',{name:'Block this participant',exact:true}).click();await expect(page.getByText('I am listening — synthetic peer reply.',{exact:true})).toHaveCount(0);
 }finally{await context.close();}
});
test('Confused I live: no available listener gives honest alternatives and does not create a queue',async({page,request})=>{
 const member=await account(request);await login(page,member);await page.goto('/listener/match');await expect(page.getByText('No live listener is currently available',{exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'Find urgent help',exact:true})).toBeVisible();
 expect((await(await request.get(`${api}/listener-support/requests/me`,{headers:member.headers})).json())).toHaveLength(0);await page.getByRole('button',{name:'Educational articles',exact:true}).click();await expect(page).toHaveURL(/\/articles$/);
});
test('Malicious I live: self-completed training without independent approval cannot claim readiness',async({page,request})=>{
 const unapproved=await account(request);await provision(unapproved,true);await login(page,unapproved);await page.goto('/listener/workspace');await expect(page.getByText('Live listener access is unavailable',{exact:true})).toBeVisible();
 const r=await request.put(`${api}/listeners/me/availability`,{headers:unapproved.headers,data:{ready:true}});expect(r.status()).toBe(403);expect((await r.json()).code).toBe('LISTENER_NOT_AUTHORIZED');
});
