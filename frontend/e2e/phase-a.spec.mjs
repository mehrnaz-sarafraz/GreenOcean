import { test, expect } from '@playwright/test';
// Browser UI scenarios use a controlled API transport. Real authorization/database behavior is tested by JUnit.
const adultEmail='synthetic-adult@example.test';
const password='a long synthetic browser passphrase';
async function installApi(page, options={}) {
  const state={signedIn:false,registrations:[],registrationFailures:options.registrationFailures??0,countryFailures:options.countryFailures??0};
  await page.route('**/api/v1/**',async route=>{
    const req=route.request(),path=new URL(req.url()).pathname;
    const headers={'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true','Access-Control-Allow-Headers':'Authorization,Content-Type,Accept,X-GreenOcean-CSRF','Access-Control-Allow-Methods':'GET,POST,PATCH,DELETE,OPTIONS'};
    const reply=(body,status=200)=>route.fulfill({status,contentType:'application/json',headers,body:JSON.stringify(body)});
    if(req.method()==='OPTIONS')return route.fulfill({status:204,headers});
    if(path==='/api/v1/geography/countries'){
      if(state.countryFailures-->0)return route.abort('internetdisconnected');
      return reply([{code:'IR',name:'Iran',local_names:{fa:'ایران',ar:'إيران'}},{code:'US',name:'United States',local_names:{fa:'ایالات متحده',ar:'الولايات المتحدة'}}]);
    }
    if(path==='/api/v1/policies/terms')return reply({key:'terms',version:1,title:'Test Terms',content:'Synthetic QA Terms only',summary:'Test terms',publishedAt:'2026-01-01T00:00:00Z'});
    if(path==='/api/v1/public/resources')return reply({items:[{id:'qa-grounding',title:'Public grounding practice',body:'A synthetic all-age resource.'}],boundaries:['Listeners offer non-clinical support.','GreenOcean is not an emergency service.']});
    if(path==='/api/v1/auth/register'){
      state.registrations.push(req.postDataJSON());
      if(state.registrationFailures-->0)return route.abort('internetdisconnected');
      return reply({userId:'00000000-0000-0000-0000-000000000001',email:adultEmail,username:'member_synthetic'},201);
    }
    if(path==='/api/v1/account-assistance/email-verification/confirm')return req.postDataJSON().code==='123456'?reply({message:'Email address verified.'}):reply({message:'Verification code is invalid or expired'},400);
    if(path==='/api/v1/account-assistance/email-verification')return reply({message:'If the account exists, instructions have been queued.'},202);
    if(path==='/api/v1/auth/web/login'){state.signedIn=true;return reply({accessToken:'synthetic-access',refreshToken:'cookie',tokenType:'Bearer',expiresIn:900});}
    if(path==='/api/v1/auth/web/refresh')return state.signedIn?reply({accessToken:'synthetic-access',refreshToken:'cookie',tokenType:'Bearer',expiresIn:900}):reply({message:'No session'},401);
    if(path==='/api/v1/auth/web/logout'){state.signedIn=false;return route.fulfill({status:204,headers});}
    if(path==='/api/v1/auth/me')return state.signedIn&&req.headers().authorization==='Bearer synthetic-access'?reply({userId:'00000000-0000-0000-0000-000000000001',email:adultEmail,roles:['USER']}):reply({message:'No session'},401);
    if(path==='/api/v1/identity')return reply({status:'PENDING',email_verified:false,eligibility:{},services:{COMMUNITY:false,LISTENER:false,PROFESSIONAL_CARE:false}});
    if(path==='/api/v1/posts/feed'||path==='/api/v1/communities'||path==='/api/v1/notifications')return reply({items:[],page:0,size:20,hasNext:false});
    if(path==='/api/v1/profiles/me')return reply({userId:'00000000-0000-0000-0000-000000000001',username:'member_synthetic',displayName:'New member',profilePrivate:true});
    if(path==='/api/v1/preferences/me')return reply({onboardingComplete:false,supportTopics:[],strongerContentControls:true,blurSensitiveContent:true,allowMessageRequests:false});
    return reply([]);
  });
  return state;
}
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
  await expect(page.getByText('GreenOcean is not an emergency service.')).toBeVisible();
  await expect(page.getByRole('button',{name:'Urgent help • کمک فوری'})).toBeVisible();
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
