import { test, expect } from '@playwright/test';
import { readFile } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';

const api='http://127.0.0.1:8080/api/v1';
const secret='A synthetic live browser registration phrase 🌊';
const fresh=()=>`live-${randomUUID()}@example.test`;
async function snapshot(email) {
 const key=await readFile('../backend/target/qa-mailbox-token','utf8');
 const response=await fetch(`http://127.0.0.1:8099/snapshot?email=${encodeURIComponent(email)}`,{headers:{'X-QA-Key':key}});
 expect(response.status).toBe(200);return response.json();
}
async function fill(page,email,password=secret,{adult=true,terms=true}={}) {
 await page.goto('/register');await page.getByLabel('Email',{exact:true}).fill(email);
 await page.getByLabel('Password (15–128 characters)',{exact:true}).fill(password);
 await page.getByLabel('Search country of residence',{exact:true}).fill('Iran');
 await page.getByRole('radio',{name:/Iran/}).click();
 if(adult)await page.getByRole('checkbox',{name:/I self-attest/}).click();
 if(terms)await page.getByRole('checkbox',{name:/I accept Terms/}).click();
}
async function signup(page,email,password=secret) {
 await fill(page,email,password);await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page).toHaveURL(/\/activate$/);
}
async function verify(page,email) {
 await expect.poll(async()=>(await snapshot(email)).code,{timeout:15000}).toMatch(/^\d{6}$/);
 const {code}=await snapshot(email);
 await page.getByLabel('Verification code',{exact:true}).fill(code);await page.getByRole('button',{name:'Verify email',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('Email verified');
}
test('Happy live C: browser signup, delivered email, activation, optional onboarding, logout/login',async({page,request})=>{
 const email=fresh();await page.goto('/welcome');await page.getByRole('button',{name:'Create account',exact:true}).first().click();
 await expect(page).toHaveURL(/\/register/);await signup(page,email);
 let state=await snapshot(email);expect(state.accounts).toBe(1);expect(state.account.status).toBe('PENDING');expect(state.account.city).toBeNull();expect(state.account.birth_year).toBeNull();expect(state.account.modern_hash).toBe(true);expect(state.account.is_profile_private).toBe(true);expect(state.roles).toEqual(['USER']);expect(state.acceptances).toBe(1);
 const pending=await request.post(`${api}/auth/login`,{data:{email,password:secret}});expect(pending.status()).toBe(200);
 const {accessToken}=await pending.json();expect((await request.get(`${api}/posts/feed`,{headers:{Authorization:`Bearer ${accessToken}`}})).status()).toBe(403);
 await verify(page,email);state=await snapshot(email);expect(state.account.status).toBe('ACTIVE');expect(state.account.email_verified).toBe(true);
 await page.getByLabel('Password',{exact:true}).fill(secret);await page.getByRole('button',{name:'Sign in',exact:true}).click();
 await expect(page.getByText('Skip for now',{exact:true})).toBeVisible();await page.getByText('Skip for now',{exact:true}).click();await expect(page).not.toHaveURL(/activate|onboarding/);
 await page.goto('/activate');await page.getByRole('button',{name:'Sign out',exact:true}).click();await page.goto('/login');
 await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password',{exact:true}).fill(secret);await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page).not.toHaveURL(/login|activate|onboarding/);
});
test('Creative live C: maximal Unicode password, reload recovery, later Persian profile and username',async({page,request})=>{
 const email=fresh(),password='🌊🌍🌱🔑'.repeat(32);await signup(page,email,password);await page.reload();
 await page.getByLabel('Email',{exact:true}).fill(email);await verify(page,email);
 const result=await request.post(`${api}/auth/login`,{data:{email,password}});expect(result.status()).toBe(200);const {accessToken}=await result.json();
 const username='custom_'+randomUUID().replaceAll('-','').slice(0,18);
 const updated=await request.patch(`${api}/profiles/me`,{headers:{Authorization:`Bearer ${accessToken}`},data:{username,displayName:'نام نمایشی فارسی 🌊'}});
 expect(updated.status()).toBe(200);const state=await snapshot(email);expect(state.account.username).toBe(username);expect(state.account.display_name).toBe('نام نمایشی فارسی 🌊');expect(state.account.city).toBeNull();
});
test('Confused live C: wrong code, resend, duplicate signup and reopen remain recoverable',async({page,request})=>{
 const email=fresh();await signup(page,email);await expect.poll(async()=>(await snapshot(email)).code).toMatch(/^\d{6}$/);
 const original=(await snapshot(email)).code;const wrong=original==='000000'?'111111':'000000';
 await page.getByLabel('Verification code',{exact:true}).fill(wrong);await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('invalid or expired');
 await page.getByRole('button',{name:'Resend code',exact:true}).click();await expect(page.getByRole('alert')).toContainText('If the account exists');
 await expect.poll(async()=>(await snapshot(email)).deliveries).toBe(2);
 const duplicate=await request.post(`${api}/auth/register`,{data:{email:email.toUpperCase(),password:secret,countryCode:'IR',adultSelfAttested:true,termsVersion:1}});expect(duplicate.status()).toBe(201);expect(Object.keys(await duplicate.json())).toEqual(['message']);
 expect((await snapshot(email)).accounts).toBe(1);await verify(page,email);
});
test('Malicious live C: direct bypass and concurrent duplicate requests preserve server boundaries',async({page,request})=>{
 const email=fresh();await fill(page,email,secret,{adult:false});await page.getByRole('button',{name:'Create account',exact:true}).click();expect((await snapshot(email)).accounts).toBe(0);
 const base={email,password:secret,countryCode:'IR',adultSelfAttested:true,termsVersion:1};
 for(const patch of [{adultSelfAttested:false},{termsVersion:999},{countryCode:'ZZ'},{intendedRole:'ADMIN'}])expect((await request.post(`${api}/auth/register`,{data:{...base,...patch}})).status()).toBe(400);
 expect((await snapshot(email)).accounts).toBe(0);
 const attempts=await Promise.all([request.post(`${api}/auth/register`,{data:{...base,roles:['ADMIN'],isAdmin:true,ageMethod:'INDEPENDENT'}}),request.post(`${api}/auth/register`,{data:{...base,email:email.toUpperCase()}})]);
 expect(attempts.map(r=>r.status())).toEqual([201,201]);expect(await attempts[0].json()).toEqual(await attempts[1].json());
 const state=await snapshot(email);expect(state.accounts).toBe(1);expect(state.roles).toEqual(['USER']);expect(state.account.status).toBe('PENDING');
});
