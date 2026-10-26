import { test, expect } from '@playwright/test';
import { installApi } from './fixtures/public-api.mjs';
// Browser UI scenarios use a controlled API transport. Real authorization/database behavior is tested by JUnit.
const adultEmail='synthetic-adult@example.test';
const password='a long synthetic browser passphrase';
async function fillRegistration(page,{adult=true,terms=true,secret=password}={}) {
  await page.goto('/register');
  await page.getByLabel('Email',{exact:true}).fill(adultEmail);
  await page.getByLabel('Password (15–128 characters)',{exact:true}).fill(secret);
  await page.getByRole('radio',{name:/Iran/}).click();
  if(adult)await page.getByRole('checkbox',{name:/I self-attest/}).click();
  if(terms)await page.getByRole('checkbox',{name:/I accept Terms/}).click();
}

test('Happy: anonymous visitors can use public resources',async({page})=>{
  await installApi(page);await page.goto('/resources');
  await expect(page.getByText('Public grounding practice')).toBeVisible();
  await expect(page.getByText(/GreenOcean is not an emergency service/)).toBeVisible();
  await expect(page.getByRole('button',{name:'Urgent help'})).toBeVisible();
});
test('Happy: minimal registration leads to email verification',async({page})=>{
  const state=await installApi(page);await fillRegistration(page);
  await page.getByRole('button',{name:'Create account',exact:true}).click();
  await expect(page).toHaveURL(/\/activate/);expect(state.registrations).toHaveLength(1);
  await page.getByLabel('Verification code',{exact:true}).fill('123456');
  await page.getByRole('button',{name:'Verify email',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('Email verified');
});
test('Happy: current Terms can be opened before acceptance',async({page})=>{
  await installApi(page);await page.goto('/register');await page.getByRole('button',{name:'Read Terms',exact:true}).click();
  await expect(page).toHaveURL(/\/info\/terms/);
});
test('Creative: long Unicode password needs no optional identity or medical fields',async({page})=>{
  const state=await installApi(page);const secret='رمز عبور طولانی و امن برای آزمایش 🌊 '.repeat(2);await fillRegistration(page,{secret});
  await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page).toHaveURL(/\/activate/);
  expect(state.registrations[0]).toEqual({email:adultEmail,password:secret,countryCode:'IR',adultSelfAttested:true,termsVersion:1});
});
test('Creative: password visibility can be toggled accessibly',async({page})=>{
  await installApi(page);await page.goto('/register');const input=page.getByLabel('Password (15–128 characters)',{exact:true});
  await input.fill(password);await expect(input).toHaveAttribute('type','password');
  await page.getByRole('button',{name:'Show password',exact:true}).click();await expect(input).toHaveJSProperty('type','text');
  await page.getByRole('button',{name:'Hide password',exact:true}).click();await expect(input).toHaveValue(password);
});
test('Creative: country choice can be changed before submission',async({page})=>{
  const state=await installApi(page);await fillRegistration(page);await page.getByLabel('Search country of residence').fill('United');
  await page.getByRole('radio',{name:/United States/}).click();await page.getByRole('button',{name:'Create account',exact:true}).click();
  await expect(page).toHaveURL(/\/activate/);expect(state.registrations[0].countryCode).toBe('US');
});
test('Confused: invalid email and short password give recoverable field errors',async({page})=>{
  const state=await installApi(page);await fillRegistration(page);await page.getByLabel('Email',{exact:true}).fill('wrong');await page.getByLabel('Password (15–128 characters)',{exact:true}).fill('short');
  await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page.getByText('Enter a valid email address.')).toBeVisible();
  await expect(page.getByText(/Use 15–128 characters/)).toBeVisible();expect(state.registrations).toHaveLength(0);
});
test('Confused: interrupted registration keeps form values and can be retried',async({page})=>{
  const state=await installApi(page,{registrationFailures:1});await fillRegistration(page);await page.getByRole('button',{name:'Create account',exact:true}).click();
  await expect(page.getByText(/Could not connect/)).toBeVisible();await expect(page.getByLabel('Email',{exact:true})).toHaveValue(adultEmail);
  await expect(page.getByLabel('Password (15–128 characters)',{exact:true})).toHaveValue(password);
  await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page).toHaveURL(/\/activate/);expect(state.registrations).toHaveLength(2);
});
test('Confused: expired code can be corrected without restarting signup',async({page})=>{
  await installApi(page);await fillRegistration(page);await page.getByRole('button',{name:'Create account',exact:true}).click();await expect(page).toHaveURL(/\/activate/);
  await page.getByLabel('Verification code',{exact:true}).fill('000000');await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('invalid or expired');
  await page.getByLabel('Verification code',{exact:true}).fill('123456');await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Email verified');
});
test('Malicious: omitting adult attestation cannot submit the form',async({page})=>{
  const state=await installApi(page);await fillRegistration(page,{adult:false});await page.getByRole('button',{name:'Create account',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('Social accounts are for adults 18+');expect(state.registrations).toHaveLength(0);
  await page.getByRole('button',{name:'Browse public resources',exact:true}).click();await expect(page.getByText('Public grounding practice')).toBeVisible();
});
test('Malicious: current Terms cannot be skipped',async({page})=>{
  const state=await installApi(page);await fillRegistration(page,{terms:false});await page.getByRole('button',{name:'Create account',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('Review and accept the current Terms');expect(state.registrations).toHaveLength(0);
});
test('Malicious: web login never persists tokens in browser storage',async({page})=>{
  await installApi(page);await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(adultEmail);await page.getByLabel('Password',{exact:true}).fill(password);
  await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page).toHaveURL(/\/activate/);
  expect(await page.evaluate(()=>[localStorage.getItem('greenocean.accessToken'),localStorage.getItem('greenocean.refreshToken'),sessionStorage.getItem('greenocean.refreshToken')])).toEqual([null,null,null]);
});
