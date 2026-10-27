import {test,expect} from '@playwright/test';import {randomUUID} from 'node:crypto';import {readFile} from 'node:fs/promises';
const api='http://127.0.0.1:8080/api/v1',password='Synthetic GH live passphrase 🌊';
async function account(request){
 const email=`gh-live-${randomUUID()}@example.test`;expect((await request.post(`${api}/auth/register`,{data:{email,password,countryCode:'IR',adultSelfAttested:true,termsVersion:1}})).status()).toBe(201);
 const key=await readFile('../backend/target/qa-mailbox-token','utf8');const mailbox=async()=>{const r=await fetch(`http://127.0.0.1:8099/snapshot?email=${encodeURIComponent(email)}`,{headers:{'X-QA-Key':key}});expect(r.status).toBe(200);return r.json();};
 await expect.poll(async()=>(await mailbox()).code).toMatch(/^\d{6}$/);expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code:(await mailbox()).code}})).status()).toBe(200);
 const r=await request.post(`${api}/auth/login`,{data:{email,password}});expect(r.status()).toBe(200);const token=(await r.json()).accessToken;
 return {email,headers:{Authorization:`Bearer ${token}`}};
}
async function login(page,email){await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page.getByRole('button',{name:'Skip for now',exact:true})).toBeVisible();}
test('Happy GH live: empty topics and professional knowledge complete to existing articles',async({page,request})=>{
 const owner=await account(request);await login(page,owner.email);await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('radio',{name:'Professional knowledge',exact:true}).click();await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('button',{name:'Enter GreenOcean',exact:true}).click();await expect(page).toHaveURL(/\/articles$/);
 const saved=await(await request.get(`${api}/preferences/me`,{headers:owner.headers})).json();expect(saved.onboardingStatus).toBe('COMPLETED');expect(saved.onboardingStep).toBe(3);expect(saved.supportTopics).toEqual([]);expect(saved.supportStyle).toBe('PROFESSIONAL_KNOWLEDGE');expect(saved.allowMessageRequests).toBe(false);
});
test('Confused GH live: interrupted progress reloads, Skip stays distinct and settings can reopen',async({page,request})=>{
 const owner=await account(request);await login(page,owner.email);await page.getByRole('button',{name:'Continue',exact:true}).click();await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();await page.reload();await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);await page.reload();await expect(page).toHaveURL(/\/support$/);
 const saved=await(await request.get(`${api}/preferences/me`,{headers:owner.headers})).json();expect(saved.onboardingStatus).toBe('SKIPPED');expect(saved.onboardingStep).toBe(1);
 await page.goto('/profile');await page.getByRole('button',{name:'Edit or resume optional preferences',exact:true}).click();await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();
});
test('Creative GH live: Persian profile and optional avatar removal persist without account identity changes',async({page,request})=>{
 const owner=await account(request);await login(page,owner.email);await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);await page.goto('/tools/profile-edit');await page.getByLabel('Display name',{exact:true}).fill('دریای سبز 🌊');await page.getByLabel('Avatar HTTPS URL (optional)',{exact:true}).fill('https://images.example.test/synthetic.png');await page.getByRole('button',{name:'Save profile',exact:true}).click();await expect(page.getByText('Profile changes saved to your account.',{exact:true})).toBeVisible();await page.getByRole('button',{name:'Remove avatar',exact:true}).click();await page.getByRole('button',{name:'Save profile',exact:true}).click();await expect.poll(async()=>(await(await request.get(`${api}/profiles/me`,{headers:owner.headers})).json()).avatarUrl).toBeNull();
 const profile=await(await request.get(`${api}/profiles/me`,{headers:owner.headers})).json();expect(profile.displayName).toBe('دریای سبز 🌊');expect(profile.profilePrivate).toBe(true);expect((await(await request.get(`${api}/auth/me`,{headers:owner.headers})).json()).email).toBe(owner.email);
});
test('Malicious GH live: browser publication cannot bypass per-post audience or leak private bio',async({page,request})=>{
 const owner=await account(request),viewer=await account(request);await login(page,owner.email);await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);
 expect((await request.post(`${api}/identity/display-reviewed`,{headers:owner.headers})).status()).toBe(200);
 expect((await request.patch(`${api}/profiles/me`,{headers:owner.headers,data:{bio:'GH private biography marker'}})).status()).toBe(200);
 const mine=await(await request.get(`${api}/profiles/me`,{headers:owner.headers})).json();const other=await(await request.get(`${api}/profiles/${mine.username}`,{headers:viewer.headers})).json();expect(other.bio).toBeNull();
 const created=await request.post(`${api}/posts`,{headers:owner.headers,data:{body:'Synthetic follower-only marker',anonymous:false,visibility:'FOLLOWERS'}});expect(created.status()).toBe(201);const id=(await created.json()).id;
 expect((await request.patch(`${api}/profiles/me`,{headers:owner.headers,data:{profilePrivate:false}})).status()).toBe(200);expect((await request.get(`${api}/posts/${id}`,{headers:viewer.headers})).status()).toBe(404);
 expect((await request.post(`${api}/posts`,{headers:owner.headers,data:{body:'Forbidden unconfirmed broad audience',anonymous:false,visibility:'PUBLIC'}})).status()).toBe(400);
 await page.goto('/create');await expect(page.getByRole('radio',{name:'Approved followers',exact:true})).toBeChecked();await page.getByRole('radio',{name:'Eligible members',exact:true}).click();await expect(page.getByRole('switch',{name:'Confirm broader audience',exact:true})).not.toBeChecked();
});
