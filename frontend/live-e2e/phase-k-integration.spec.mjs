import {test,expect} from '@playwright/test';
import {randomUUID} from 'node:crypto';
import {readFile,writeFile,mkdir} from 'node:fs/promises';
const api='http://127.0.0.1:8080/api/v1',password='Synthetic Phase K integrated passphrase 🌊';
async function key(){return readFile('../backend/target/qa-mailbox-token','utf8');}
async function mailbox(email){const r=await fetch(`http://127.0.0.1:8099/snapshot?email=${encodeURIComponent(email)}`,{headers:{'X-QA-Key':await key()}});expect(r.status).toBe(200);return r.json();}
test.beforeEach(async()=>{expect((await fetch('http://127.0.0.1:8099/reset-rate-window',{method:'POST',headers:{'X-QA-Key':await key()}})).status).toBe(204);});

test('K integrated happy: one account from public signup through privacy, support, governance, password change and erasure',async({page,request})=>{
 test.setTimeout(120000);
 const email=`k-journey-${randomUUID()}@example.test`;
 await page.goto('/welcome');await page.getByRole('button',{name:'Create account',exact:true}).first().click();
 await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password (15–128 characters)',{exact:true}).fill(password);
 await page.getByLabel('Search country of residence',{exact:true}).fill('Iran');await page.getByRole('radio',{name:/Iran/}).click();
 await page.getByRole('checkbox',{name:/I self-attest/}).click();await page.getByRole('checkbox',{name:/I accept Terms/}).click();
 await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page).toHaveURL(/\/activate$/);
 const pending=await mailbox(email);expect(pending.account.status).toBe('PENDING');expect(pending.account.is_profile_private).toBe(true);expect(pending.acceptances).toBe(1);
 await expect.poll(async()=>(await mailbox(email)).code).toMatch(/^\d{6}$/);
 await page.getByLabel('Verification code',{exact:true}).fill((await mailbox(email)).code);await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Email verified');
 await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);
 const login=await request.post(`${api}/auth/login`,{data:{email,password}});expect(login.status()).toBe(200);const old=await login.json(),headers={Authorization:`Bearer ${old.accessToken}`};
 const profile=await(await request.get(`${api}/profiles/me`,{headers})).json();expect(profile.profilePrivate).toBe(true);
 await page.goto('/listener/match');await expect(page.getByText('No live listener is currently available',{exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Educational articles',exact:true}).click();await expect(page).toHaveURL(/\/articles$/);
 await page.goto('/privacy');await page.getByRole('button',{name:'Decline: Optional analytics',exact:true}).click();await expect(page.getByLabel('Optional analytics status',{exact:true})).toHaveText('Declined');
 await page.getByRole('button',{name:'Record receipt of current Privacy Notice',exact:true}).click();await expect(page.getByText('Recorded notice versions: 1',{exact:true})).toBeVisible();
 const archive=await(await request.get(`${api}/account/archive`,{headers})).json();expect(archive.governance.termsAcceptances).toHaveLength(1);expect(archive.governance.noticeReceipts).toHaveLength(1);expect(archive.governance.choices.find(c=>c.purpose==='OPTIONAL_ANALYTICS').decision).toBe('DENIED');
 const changed='Synthetic Phase K replacement password 🌊';await page.goto('/tools/account-security');await page.getByLabel('Current password',{exact:true}).fill(password);await page.getByLabel('New password',{exact:true}).fill(changed);await page.getByLabel('Confirm new password',{exact:true}).fill(changed);await page.getByRole('button',{name:'Update password',exact:true}).click();await expect(page).toHaveURL(/\/login$/);
 expect((await request.get(`${api}/auth/me`,{headers})).status()).toBe(401);
 await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password',{exact:true}).fill(changed);await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page).not.toHaveURL(/\/login$/);
 await page.goto('/tools/account-security');await page.getByLabel('Password to delete your account',{exact:true}).fill(changed);await page.getByLabel('Type DELETE to confirm',{exact:true}).fill('DELETE');await page.getByRole('button',{name:'Permanently delete my account',exact:true}).click();await expect(page).toHaveURL(/\/welcome$/);
 expect((await request.post(`${api}/auth/login`,{data:{email,password:changed}})).status()).toBe(401);expect((await request.get(`${api}/account/archive`,{headers})).status()).toBe(401);
});

test('K bounded synthetic reliability: four workers, auth/pagination/live contention/messages/report/export/erase',async({request})=>{
 test.setTimeout(120000);
 const samples=[];
 async function timed(scenario,work){const start=performance.now();const r=await work();samples.push({scenario,ms:Math.round((performance.now()-start)*100)/100,status:r.status()});return r;}
 const users=Array.from({length:4},()=>({email:`k-load-${randomUUID()}@example.test`}));
 for(const r of await Promise.all(users.map(u=>timed('registration',()=>request.post(`${api}/auth/register`,{data:{email:u.email,password,countryCode:'IR',adultSelfAttested:true,termsVersion:1}})))))expect(r.status()).toBe(201);
 for(const u of users){await expect.poll(async()=>(await mailbox(u.email)).code).toMatch(/^\d{6}$/);const r=await timed('verification',async()=>request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email:u.email,code:(await mailbox(u.email)).code}}));expect(r.status()).toBe(200);}
 await Promise.all(users.map(async u=>{const r=await timed('login',()=>request.post(`${api}/auth/login`,{data:{email:u.email,password}}));expect(r.status()).toBe(200);u.tokens=await r.json();}));
 await Promise.all(users.map(async u=>{const r=await timed('refresh',()=>request.post(`${api}/auth/refresh`,{data:{refreshToken:u.tokens.refreshToken}}));expect(r.status()).toBe(200);u.tokens=await r.json();u.headers={Authorization:`Bearer ${u.tokens.accessToken}`};}));
 for(let round=0;round<3;round++)await Promise.all(users.map(async u=>{const feed=await timed('privacy-feed-page10',()=>request.get(`${api}/posts/feed?page=0&size=10`,{headers:u.headers}));expect(feed.status()).toBe(200);const body=await feed.json();expect(Array.isArray(body.items)).toBe(true);expect(body.size).toBe(10);expect(body.items.length).toBeLessThanOrEqual(10);expect((await timed('profile',()=>request.get(`${api}/profiles/me`,{headers:u.headers}))).status()).toBe(200);}));
 const listener=users[0];expect((await fetch('http://127.0.0.1:8099/listener',{method:'POST',headers:{'X-QA-Key':await key(),'Content-Type':'application/json'},body:JSON.stringify({email:listener.email})})).status).toBe(200);
 const ready=await timed('listener-ready',()=>request.put(`${api}/listeners/me/availability`,{headers:listener.headers,data:{ready:true}}));expect(ready.status()).toBe(200);const lease=(await ready.json()).leaseId;
 for(const r of await Promise.all(Array.from({length:4},()=>timed('presence-heartbeat',()=>request.post(`${api}/listeners/me/heartbeat`,{headers:listener.headers,data:{leaseId:lease}})))))expect(r.status()).toBe(200);
 const listenerId=(await(await request.get(`${api}/auth/me`,{headers:listener.headers})).json()).userId;
 const attempts=await Promise.all(users.slice(1).map(u=>timed('last-slot-contention',()=>request.post(`${api}/listener-support/requests`,{headers:u.headers,data:{clientRequestId:randomUUID(),listenerId}}))));expect(attempts.map(r=>r.status()).sort()).toEqual([200,409,409]);
 const index=attempts.findIndex(r=>r.status()===200),member=users[index+1],offer=await attempts[index].json();
 for(const r of await Promise.all(Array.from({length:2},()=>timed('acceptance-contention',()=>request.post(`${api}/listener-support/requests/${offer.id}/accept`,{headers:listener.headers})))))expect(r.status()).toBe(200);
 const active=await timed('activation',()=>request.post(`${api}/listener-support/requests/${offer.id}/activate`,{headers:member.headers}));expect(active.status()).toBe(200);const conversation=(await active.json()).conversationId;
 for(const r of await Promise.all(Array.from({length:4},(_,n)=>timed('message-send',()=>request.post(`${api}/conversations/${conversation}/messages`,{headers:(n%2?listener:member).headers,data:{body:'Synthetic bounded K message '+n,clientMessageId:randomUUID()}})))))expect(r.status()).toBe(201);
 for(const r of await Promise.all(Array.from({length:4},()=>timed('message-poll',()=>request.get(`${api}/conversations/${conversation}/messages`,{headers:member.headers})))))expect(r.status()).toBe(200);
 expect((await timed('session-end',()=>request.post(`${api}/listener-support/sessions/${offer.id}/end`,{headers:member.headers}))).status()).toBe(200);
 const reports=await Promise.all(Array.from({length:4},()=>timed('report-contention',()=>request.post(`${api}/reports`,{headers:member.headers,data:{targetType:'CONVERSATION',targetId:conversation,reason:'OTHER'}}))));expect(reports.map(r=>r.status()).sort()).toEqual([201,409,409,409]);
 for(const r of await Promise.all(users.map(u=>timed('owner-export',()=>request.get(`${api}/account/archive`,{headers:u.headers})))))expect(r.status()).toBe(200);
 for(const r of await Promise.all(users.map(u=>timed('account-erasure',()=>request.delete(`${api}/account`,{headers:u.headers,data:{password}})))))expect(r.status()).toBe(204);
 const summary=Object.fromEntries([...new Set(samples.map(s=>s.scenario))].map(name=>{const rows=samples.filter(s=>s.scenario===name),times=rows.map(s=>s.ms).sort((a,b)=>a-b);return [name,{n:times.length,p50ms:times[Math.ceil(times.length*.5)-1],p95ms:times[Math.ceil(times.length*.95)-1],maxMs:times.at(-1),statuses:rows.map(s=>s.status)}];}));
 const evidence={environment:'GitHub runner, Java21/PostgreSQL18, loopback real HTTP; four workers max, synthetic accounts only; not production capacity or SLO evidence',samples,summary};await mkdir('test-results/live',{recursive:true});await writeFile('test-results/live/phase-k-performance.json',JSON.stringify(evidence,null,2));
});
