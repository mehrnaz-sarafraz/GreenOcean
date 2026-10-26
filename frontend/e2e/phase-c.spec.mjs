import { test, expect } from '@playwright/test';
import { installApi } from './fixtures/public-api.mjs';

const email='synthetic-adult@example.test', password='a long synthetic browser passphrase';
async function fill(page) {
 await page.goto('/register');
 await page.getByLabel('Email',{exact:true}).fill(email);
 await page.getByLabel('Password (15–128 characters)',{exact:true}).fill(password);
 await page.getByRole('radio',{name:/Iran/}).click();
 await page.getByRole('checkbox',{name:/I self-attest/}).click();
 await page.getByRole('checkbox',{name:/I accept Terms/}).click();
}
const submit=page=>page.getByRole('button',{name:'Create account',exact:true}).click();

test('Happy C: Welcome to activation, optional onboarding skip, sign out and sign back in',async({page})=>{
 const state=await installApi(page,{completeActivation:true});await page.goto('/welcome');
 await page.getByRole('button',{name:'Create account',exact:true}).first().click();
 await expect(page).toHaveURL(/\/register/);await fill(page);await submit(page);await expect(page).toHaveURL(/\/activate/);
 await expect(page.getByLabel('Email',{exact:true})).toHaveValue(email);
 await page.getByLabel('Verification code',{exact:true}).fill('123456');await page.getByRole('button',{name:'Verify email',exact:true}).click();
 await page.getByLabel('Password',{exact:true}).fill(password);await page.getByRole('button',{name:'Sign in',exact:true}).click();
 await expect(page.getByText('Skip for now',{exact:true})).toBeVisible();await page.getByText('Skip for now',{exact:true}).click();
 await expect.poll(()=>state.onboardingComplete).toBe(true);await expect(page).not.toHaveURL(/activate|onboarding/);
 // Reuse the established activation screen's sign-out control, then authenticate again.
 await page.goto('/activate');await page.getByRole('button',{name:'Sign out',exact:true}).click();
 await page.goto('/login');await page.getByLabel('Email',{exact:true}).fill(email);await page.getByLabel('Password',{exact:true}).fill(password);
 await page.getByRole('button',{name:'Sign in',exact:true}).click();await expect(page).not.toHaveURL(/login|activate|onboarding/);
});
test('Confused C: errors focus the input and clear when corrected',async({page})=>{
 await installApi(page);await fill(page);const field=page.getByLabel('Email',{exact:true});await field.fill('wrong');await submit(page);
 await expect(field).toBeFocused();await expect(page.getByText('Enter a valid email address.',{exact:true})).toBeVisible();
 await field.fill(email);await expect(page.getByText('Enter a valid email address.',{exact:true})).toHaveCount(0);
});
test('Confused C: changed Terms refresh version and require a new explicit acceptance',async({page})=>{
 const state=await installApi(page,{staleTerms:true});await fill(page);await submit(page);
 const terms=page.getByRole('checkbox',{name:/I accept Terms version 2/});await expect(terms).toHaveAttribute('aria-checked','false');
 await expect(page.getByLabel('Email',{exact:true})).toHaveValue(email);await submit(page);expect(state.registrations).toHaveLength(1);
 await terms.click();await submit(page);await expect(page).toHaveURL(/\/activate/);expect(state.registrations[1].termsVersion).toBe(2);
});
test('Confused C: pending registration prevents duplicate clicks',async({page})=>{
 const state=await installApi(page,{holdRegistration:true});await fill(page);await submit(page);
 await expect(page.getByText('Submitting registration…',{exact:true})).toBeVisible();
 await expect(page.getByRole('button',{name:'Create account',exact:true})).toBeDisabled();
 state.releaseRegistration();
 await expect(page).toHaveURL(/\/activate/);expect(state.registrations).toHaveLength(1);
});
test('Confused C: unknown outcome can continue verification without resubmitting',async({page})=>{
 const state=await installApi(page,{registrationFailures:1});await fill(page);await submit(page);
 await expect(page.getByText(/Your changes were not confirmed/)).toBeVisible();
 await page.getByRole('button',{name:'Already submitted? Verify email',exact:true}).click();await expect(page).toHaveURL(/\/activate/);
 await page.getByLabel('Email',{exact:true}).fill(email);await page.getByRole('button',{name:'Resend code',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('If the account exists');expect(state.registrations).toHaveLength(1);
});
test('Creative C: restart verification with email re-entry and no credentials in storage or URL',async({page})=>{
 await installApi(page);await fill(page);await submit(page);await expect(page).toHaveURL(/\/activate$/);await page.reload();
 await expect(page.getByLabel('Email',{exact:true})).toHaveValue('');await page.getByLabel('Email',{exact:true}).fill(email);
 await page.getByRole('button',{name:'Resend code',exact:true}).click();await expect(page.getByRole('alert')).toContainText('If the account exists');
 expect(await page.evaluate(()=>JSON.stringify({...localStorage,...sessionStorage}))).not.toContain(password);
 expect(page.url()).not.toContain(email);
});
for(const status of [429,503])test(`Confused C: safe ${status} response has recovery and keeps non-sensitive form data`,async({page})=>{
 await installApi(page,{registrationStatus:status});await fill(page);await submit(page);
 await expect(page.getByText(status===429?/Too many attempts/:/temporarily unavailable/)).toBeVisible();
 await expect(page.getByLabel('Email',{exact:true})).toHaveValue(email);await expect(page.getByText('Synthetic server detail')).toHaveCount(0);
});
test('Malicious C: common passwords are rejected before sending',async({page})=>{
 const state=await installApi(page);await fill(page);await page.getByLabel('Password (15–128 characters)',{exact:true}).fill('passwordpassword');await submit(page);
 await expect(page.getByText('Choose a less common password.')).toBeVisible();expect(state.registrations).toHaveLength(0);
});
test('Confused C: server password validation appears beside the input',async({page})=>{
 await installApi(page,{registrationStatus:400,fieldErrors:[{field:'password',message:'Choose another password.'}]});await fill(page);await submit(page);
 await expect(page.getByText('Choose another password.',{exact:true})).toBeVisible();await expect(page.getByLabel('Password (15–128 characters)',{exact:true})).toBeFocused();
});
for(const width of [320,1440])test(`Creative C: registration fits ${width}px with readable password controls`,async({page})=>{
 await page.setViewportSize({width,height:900});await installApi(page);await fill(page);
 await expect(page.getByRole('button',{name:'Show password',exact:true})).toBeVisible();
 expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true);
 await page.screenshot({path:`test-results/phase-c-${width}.png`,fullPage:true});
});
test('Creative C: Persian registration keeps credentials LTR and errors localized',async({page})=>{
 await installApi(page);await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();await page.goto('/register');
 const emailField=page.getByLabel('ایمیل',{exact:true});await emailField.fill('adult@example.test');
 await expect(emailField).toHaveCSS('text-align','left');await expect(emailField).toHaveCSS('direction','ltr');
 await page.getByRole('radio',{name:/ایران/}).click();await page.getByRole('button',{name:'ثبت‌نام',exact:true}).click();
 await expect(page.getByRole('alert').first()).toBeVisible();await page.screenshot({path:'test-results/phase-c-persian.png',fullPage:true});
});
