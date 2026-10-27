import { test,expect } from '@playwright/test';
import { readFile } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
const api='http://127.0.0.1:8080/api/v1',password='A synthetic Phase D live passphrase 🌊';
const fresh=()=>`live-d-${randomUUID()}@example.test`;
async function snapshot(email){const key=await readFile('../backend/target/qa-mailbox-token','utf8');const response=await fetch(`http://127.0.0.1:8099/snapshot?email=${encodeURIComponent(email)}`,{headers:{'X-QA-Key':key}});expect(response.status).toBe(200);return response.json();}
async function register(page,email,country='Iran'){
 await page.goto('/register');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password (15–128 characters)',{exact:true}).fill(password);
 await page.getByLabel('Search country of residence',{exact:true}).fill(country);await page.getByRole('radio',{name:new RegExp(country)}).click();await page.getByRole('checkbox',{name:/I self-attest/}).click();await page.getByRole('checkbox',{name:/I accept Terms/}).click();await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page).toHaveURL(/\/activate$/);
 await expect.poll(async()=>(await snapshot(email)).code).toMatch(/^\d{6}$/);
}
test('Happy live D: owner corrects typo, old credentials retire, new email activates same account',async({page,request})=>{
 const original=fresh(),corrected=fresh();await register(page,original);const before=await snapshot(original),oldCode=before.code;
 const oldLogin=await request.post(`${api}/auth/login`,{data:{email:original,password}});expect(oldLogin.status()).toBe(200);const {accessToken:oldToken}=await oldLogin.json();
 await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page.getByRole('button',{name:'Sign out',exact:true})).toBeVisible();
 await page.getByRole('button',{name:'Edit email',exact:true}).click();await page.getByLabel('New email',{exact:true}).fill(corrected);await page.getByLabel('Password to confirm email correction',{exact:true}).fill(password);await page.getByRole('button',{name:'Confirm email correction',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('If the correction could be completed');await expect.poll(async()=>(await snapshot(corrected)).code).toMatch(/^\d{6}$/);
 const changed=await snapshot(corrected);expect(changed.account.id).toBe(before.account.id);expect(changed.account.status).toBe('PENDING');expect(changed.roles).toEqual(['USER']);expect((await snapshot(original)).accounts).toBe(0);
 expect((await request.get(`${api}/auth/me`,{headers:{Authorization:`Bearer ${oldToken}`}})).status()).toBe(401);
 expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email:original,code:oldCode}})).status()).toBe(400);
 await page.getByLabel('Verification code',{exact:true}).fill(changed.code);await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Email verified');
 expect((await snapshot(corrected)).account.email_verified).toBe(true);await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page.getByText('Skip for now',{exact:true})).toBeVisible();
});
test('Creative live D: pasted Persian code verifies ownership without opening unsupported regional service',async({page,request})=>{
 const email=fresh();await register(page,email,'United States');const code=(await snapshot(email)).code;
 await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();await page.goto('/activate');await page.getByLabel('ایمیل',{exact:true}).fill(email);
 await page.getByLabel('کد تأیید',{exact:true}).fill(code.replace(/\d/g,d=>String.fromCharCode(0x06f0+Number(d))));await page.getByRole('button',{name:'تأیید ایمیل',exact:true}).click();await expect(page.getByRole('alert')).toContainText('ایمیل تأیید شد');
 const login=await request.post(`${api}/auth/login`,{data:{email,password}});const tokens=await login.json();expect((await snapshot(email)).account.email_verified).toBe(true);expect((await request.get(`${api}/posts/feed`,{headers:{Authorization:`Bearer ${tokens.accessToken}`}})).status()).toBe(403);
});
test('Confused live D: resend retires delayed old mail and newest delivered code recovers',async({page,request})=>{
 const email=fresh();await register(page,email);const old=(await snapshot(email)).code;
 await page.getByRole('button',{name:'Resend code',exact:true}).click();await expect(page.getByRole('alert')).toContainText('If the account exists');await expect.poll(async()=>(await snapshot(email)).deliveries).toBe(2);
 const current=await snapshot(email);expect(current.currentChallenges).toBe(1);expect(current.retiredChallenges).toBe(1);expect(current.code).not.toBe(old);
 expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code:old}})).status()).toBe(400);
 await page.getByLabel('Verification code',{exact:true}).fill(current.code);await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Email verified');
});
test('Malicious live D: guessing across requests and resend cannot reset account budget',async({page,request})=>{
 const email=fresh();await register(page,email);const code=(await snapshot(email)).code,wrong=code==='000000'?'111111':'000000';
 for(let n=0;n<5;n++)expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code:wrong}})).status()).toBe(400);
 const limited=await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code}});expect(limited.status()).toBe(429);expect(limited.headers()['retry-after']).toBe('900');
 await request.post(`${api}/account-assistance/email-verification`,{data:{email}});await expect.poll(async()=>(await snapshot(email)).deliveries).toBe(2);
 const latest=(await snapshot(email)).code;expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code:latest}})).status()).toBe(429);expect((await snapshot(email)).account.email_verified).toBe(false);
});
