import {expect} from '@playwright/test';import {randomUUID} from 'node:crypto';import {readFile} from 'node:fs/promises';
export const api='http://127.0.0.1:8080/api/v1',password='Synthetic L live passphrase 🌊';
export async function reset(){const key=await readFile('../backend/target/qa-mailbox-token','utf8');expect((await fetch('http://127.0.0.1:8099/reset-rate-window',{method:'POST',headers:{'X-QA-Key':key}})).status).toBe(204);}
export async function account(request){const email=`l-live-${randomUUID()}@example.test`;expect((await request.post(`${api}/auth/register`,{data:{email,password,countryCode:'IR',adultSelfAttested:true,termsVersion:1}})).status()).toBe(201);
 const key=await readFile('../backend/target/qa-mailbox-token','utf8');const mailbox=async()=>(await fetch(`http://127.0.0.1:8099/snapshot?email=${encodeURIComponent(email)}`,{headers:{'X-QA-Key':key}})).json();
 await expect.poll(async()=>(await mailbox()).code).toMatch(/^\d{6}$/);expect((await request.post(`${api}/account-assistance/email-verification/confirm`,{data:{email,code:(await mailbox()).code}})).status()).toBe(200);
 const login=await request.post(`${api}/auth/login`,{data:{email,password}});expect(login.status()).toBe(200);const headers={Authorization:`Bearer ${(await login.json()).accessToken}`};
 expect((await request.post(`${api}/identity/display-reviewed`,{headers})).status()).toBe(200);return {email,headers};}
export async function login(page,owner){await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(owner.email);await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);}
