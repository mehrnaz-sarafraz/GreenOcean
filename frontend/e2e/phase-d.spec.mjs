import { test,expect } from '@playwright/test';
import { installApi } from './fixtures/public-api.mjs';
test('Happy D: pasted leading-zero code verifies and intended address is masked',async({page})=>{
 await installApi(page,{verificationCode:'000007'});await page.goto('/activate');
 await page.getByLabel('Email',{exact:true}).fill('person@organization.test');await expect(page.getByText('Intended address: p•••@o•••.test',{exact:true})).toBeVisible();
 await page.getByLabel('Verification code',{exact:true}).fill('000 007');await expect(page.getByLabel('Verification code',{exact:true})).toHaveValue('000007');
 await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Email verified');
});
test('Creative D: Persian digits paste and RTL feedback preserve numeric direction',async({page})=>{
 await installApi(page,{verificationCode:'000007'});await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();await page.goto('/activate');
 await page.getByLabel('ایمیل',{exact:true}).fill('adult@example.test');const code=page.getByLabel('کد تأیید',{exact:true});await code.fill('۰۰۰۰۰۷');await expect(code).toHaveValue('000007');await expect(code).toHaveCSS('text-align','left');
 await page.getByRole('button',{name:'تأیید ایمیل',exact:true}).click();await expect(page.getByRole('alert')).toContainText('ایمیل تأیید شد');
 await page.screenshot({path:'test-results/phase-d-persian.png',fullPage:true});
});
test('Confused D: incomplete code has field feedback and failed verification restores focus',async({page})=>{
 await installApi(page);await page.goto('/activate');await page.getByLabel('Email',{exact:true}).fill('adult@example.test');const code=page.getByLabel('Verification code',{exact:true});
 await code.fill('123');await expect(page.getByText('Enter all six digits of the code.',{exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'Verify email',exact:true})).toBeDisabled();
 await code.fill('000000');await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('invalid or expired');await expect(code).toBeFocused();
});
test('Confused D: resend cooldown is visible, bounded, and prevents repeated sending',async({page})=>{
 const state=await installApi(page,{resendSeconds:2});await page.goto('/activate');await page.getByLabel('Email',{exact:true}).fill('adult@example.test');
 const resend=page.getByRole('button',{name:'Resend code',exact:true});await resend.click();await expect(resend).toBeDisabled();await expect(page.getByText(/Resend available in/)).toBeVisible();expect(state.verificationRequests).toBe(1);
 await expect(resend).toBeEnabled();await resend.click();expect(state.verificationRequests).toBe(2);
});
test('Confused D: offline verification retains address/code and can be retried',async({page})=>{
 await installApi(page,{verificationFailures:1});await page.goto('/activate');await page.getByLabel('Email',{exact:true}).fill('adult@example.test');await page.getByLabel('Verification code',{exact:true}).fill('123456');
 await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Could not connect');await expect(page.getByLabel('Verification code',{exact:true})).toHaveValue('123456');
 await page.getByRole('button',{name:'Verify email',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Email verified');
});
test('Happy D: pending email correction requests password confirmation and returns to verification',async({page})=>{
 const state=await installApi(page);await page.goto('/activate');await page.getByLabel('Email',{exact:true}).fill('synthetic-adult@example.test');await page.getByLabel('Password',{exact:true}).fill('a long synthetic browser password');await page.getByRole('button',{name:'Sign in',exact:true}).click();
 await expect(page.getByRole('button',{name:'Sign out',exact:true})).toBeVisible();await page.getByRole('button',{name:'Edit email',exact:true}).click();
 await page.getByLabel('New email',{exact:true}).fill('corrected@example.test');await page.getByLabel('Password to confirm email correction',{exact:true}).fill('a long synthetic browser password');await page.getByRole('button',{name:'Confirm email correction',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('If the correction could be completed');await expect(page.getByLabel('Email',{exact:true})).toHaveValue('corrected@example.test');expect(state.corrections).toHaveLength(1);
});
test('Malicious D: anonymous correction does not expose an editable account action',async({page})=>{
 const state=await installApi(page);await page.goto('/activate');await page.getByRole('button',{name:'Edit email',exact:true}).click();
 await expect(page.getByText(/first sign in using the address originally entered/)).toBeVisible();await expect(page.getByRole('button',{name:'Confirm email correction',exact:true})).toHaveCount(0);expect(state.corrections).toHaveLength(0);
});
