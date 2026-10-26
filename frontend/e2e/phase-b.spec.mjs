import { test,expect } from '@playwright/test';
import { installApi } from './fixtures/public-api.mjs';
async function setup(page,options={}){
 const state=await installApi(page,options);
 await page.route('**/api/v1/policies/**',async route=>{
  if(route.request().method()==='OPTIONS')return route.fallback();
  const key=new URL(route.request().url()).pathname.split('/').pop();
  if(options.delayPolicy)await new Promise(resolve=>setTimeout(resolve,options.delayPolicy));
  const headers={'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true'};
  if(options.failPolicy||!['terms','privacy','community-guidelines','cookies'].includes(key))return route.fulfill({status:404,contentType:'application/json',headers,body:JSON.stringify({message:'Published policy was not found'})});
  return route.fulfill({status:200,contentType:'application/json',headers,body:JSON.stringify({key,version:3,title:`QA published ${key}`,content:options.html?'A literal <script>window.phaseBInjected=true</script> string.':'The current synthetic published document.',publishedAt:'2026-01-02T00:00:00Z'})});
 });
 await page.route('**/api/v1/crisis-resources',async route=>{
  if(options.failCrisis)return route.abort('internetdisconnected');
  return route.fulfill({status:200,contentType:'application/json',headers:{'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true'},body:JSON.stringify([{id:'qa-resource',countryCode:'US',regionName:'United States',title:'Synthetic official resource',detail:'Synthetic directory entry for controlled QA. No real crisis number is provided.',actionUrl:options.badUrl?'javascript:window.phaseBInjected=true':'https://example.test/qa-resource',sourceUrl:'https://example.test/official',lastVerifiedOn:'2026-01-02',sortOrder:0}])});
 });
 return state;
}
async function screenshot(page,testInfo,name){const body=await page.screenshot({fullPage:true});await testInfo.attach(name,{body,contentType:'image/png'});}
async function noOverflow(page){expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth+1)).toBe(true);}

test('Happy B-01/B-02/B-06/B-08: mission, distinct support and honest capacity',async({page})=>{
 await setup(page);await page.goto('/welcome');await expect(page.getByRole('heading',{level:1,name:/Find understanding/})).toBeVisible();
 for(const title of ['Peer community','Trained listeners','Professional support'])await expect(page.getByRole('heading',{name:title,exact:true})).toBeVisible();
 await expect(page.getByText(/Account creation does not guarantee/)).toBeVisible();await expect(page.getByText(/Community and listener support do not replace medical care/).first()).toBeVisible();
});
test('Happy B-03/B-04: registration and login use existing Phase A routes',async({page})=>{
 await setup(page);await page.goto('/welcome');await page.getByRole('button',{name:'Create account',exact:true}).first().click();await expect(page).toHaveURL(/\/register/);await expect(page.getByLabel('Email',{exact:true})).toBeVisible();
 await page.goBack();await page.getByRole('button',{name:'Sign in',exact:true}).first().click();await expect(page).toHaveURL(/\/login/);
});
test('Happy B-05: urgent help is public, manual and has an honest no-local state',async({page})=>{
 await setup(page);await page.goto('/welcome');await page.getByRole('button',{name:'Urgent help',exact:true}).first().click();
 await expect(page.getByText('Select your country to check the directory. No GPS permission is needed.')).toBeVisible();
 await page.getByLabel('Search countries').fill('Iran');await page.getByRole('button',{name:'Iran',exact:true}).click();await expect(page.getByText(/do not have a verified local resource/)).toBeVisible();
 await page.getByLabel('Search countries').fill('United');await page.getByRole('button',{name:'United States',exact:true}).click();await expect(page.getByRole('heading',{name:'Synthetic official resource'})).toBeVisible();
});
test('Happy B-07: deep-linked current published policies show version and text',async({page})=>{
 await setup(page);await page.goto('/info/privacy');await expect(page.getByRole('heading',{name:'QA published privacy'})).toBeVisible();await expect(page.getByText(/Version 3/)).toBeVisible();await page.reload();await expect(page.getByText('The current synthetic published document.')).toBeVisible();
});
test('Happy B-10: pathway explanations and FAQ need no account',async({page})=>{
 await setup(page);await page.goto('/welcome');await page.getByRole('button',{name:'About listener support'}).click();await expect(page.getByRole('heading',{level:1,name:'Trained listener support'})).toBeVisible();await expect(page.getByText(/Approved listeners provide supportive, non-clinical listening/)).toBeVisible();
 await page.getByRole('button',{name:'FAQ',exact:true}).click();await expect(page.getByRole('heading',{name:'Is a listener a therapist?'})).toBeVisible();
});
test('Creative B-09: Persian RTL, persistence and English return',async({page},testInfo)=>{
 await setup(page);await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();await expect(page.locator('html')).toHaveAttribute('dir','rtl');await expect(page.getByRole('heading',{level:1})).toContainText('ارتباط معنادار');await screenshot(page,testInfo,'welcome-rtl');
 await page.reload();await expect(page.locator('html')).toHaveAttribute('lang','fa');await page.getByRole('button',{name:'English',exact:true}).click();await expect(page.locator('html')).toHaveAttribute('dir','ltr');
});
for(const width of [320,375,390,768,1024,1440])test(`Creative B-09: responsive Welcome at ${width}px`,async({page},testInfo)=>{
 await page.setViewportSize({width,height:900});await setup(page);await page.goto('/welcome');await expect(page.getByRole('heading',{level:1,name:/Find understanding/})).toBeVisible();await noOverflow(page);await page.getByRole('button',{name:'Create account',exact:true}).last().scrollIntoViewIfNeeded();await expect(page.getByRole('button',{name:'Create account',exact:true}).last()).toBeVisible();await screenshot(page,testInfo,`welcome-${width}`);
});
test('Creative B-09: keyboard focus is visible and order is usable',async({page})=>{
 await setup(page);await page.goto('/welcome');const english=page.getByRole('button',{name:'English',exact:true});await english.focus();await expect(english).toBeFocused();await expect(english).toHaveCSS('border-top-width','3px');await page.keyboard.press('Tab');await expect(page.getByRole('button',{name:'فارسی',exact:true})).toBeFocused();await page.keyboard.press('Tab');await expect(page.getByRole('button',{name:'Urgent help',exact:true}).first()).toBeFocused();
});
test('Creative B-09: enlarged text, landscape and reduced motion remain functional',async({page},testInfo)=>{
 await page.setViewportSize({width:390,height:844});await page.emulateMedia({reducedMotion:'reduce'});await setup(page);await page.goto('/welcome');await expect(page.getByRole('heading',{level:1,name:/Find understanding/})).toBeVisible();
 await page.evaluate(()=>{document.querySelectorAll('[data-testid="public-text"],[role="heading"]').forEach(el=>{const s=getComputedStyle(el);el.style.fontSize=parseFloat(s.fontSize)*2+'px';el.style.lineHeight=parseFloat(s.lineHeight)*2+'px';});});await noOverflow(page);await screenshot(page,testInfo,'welcome-large-text');await page.setViewportSize({width:844,height:390});await noOverflow(page);await page.getByRole('button',{name:'Public resources',exact:true}).first().click();await expect(page).toHaveURL(/\/resources/);
});
test('Creative B-07/B-10: back and forward preserve public navigation',async({page})=>{
 await setup(page);await page.goto('/welcome');await page.getByRole('button',{name:'How it works',exact:true}).first().click();await expect(page).toHaveURL(/\/info\/how-it-works/);await page.goBack();await expect(page).toHaveURL(/\/welcome/);await page.goForward();await expect(page.getByRole('heading',{level:1,name:'How GreenOcean works'})).toBeVisible();
});
test('Confused B-03: repeated CTA taps do not duplicate navigation',async({page})=>{
 await setup(page);await page.goto('/welcome');const button=page.getByRole('button',{name:'Create account',exact:true}).first();await expect(button).toBeVisible();await button.evaluate(el=>{el.click();el.click();});await expect(page).toHaveURL(/\/register/);await page.goBack();await expect(page).toHaveURL(/\/welcome/);
});
test('Confused B-07: policy failures show an honest error and retry',async({page},testInfo)=>{
 await setup(page,{failPolicy:true});await page.goto('/info/privacy');await expect(page.getByRole('alert')).toContainText('current published document is unavailable');await expect(page.getByText('The current synthetic published document.')).toHaveCount(0);await screenshot(page,testInfo,'policy-error');await page.getByRole('button',{name:'Try again',exact:true}).click();await expect(page.getByRole('alert')).toBeVisible();await page.getByRole('button',{name:'Back to welcome'}).click();await expect(page).toHaveURL(/\/welcome/);
});
test('Confused B-04: loading authentication does not offer signup or private content',async({page})=>{
 await setup(page);await page.route('**/api/v1/auth/me',async route=>{await new Promise(r=>setTimeout(r,1500));await route.fulfill({status:401,contentType:'application/json',headers:{'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true'},body:'{}'});});await page.goto('/welcome');await expect(page.getByText('Checking your session…').first()).toBeVisible();await expect(page.getByRole('button',{name:'Create account',exact:true})).toHaveCount(0);await expect(page.getByRole('button',{name:'Urgent help',exact:true}).first()).toBeVisible();
});
test('Confused B-05: failed urgent directory keeps emergency guidance available',async({page})=>{
 await setup(page,{failCrisis:true});await page.goto('/urgent-help');await expect(page.getByRole('alert')).toContainText('urgent directory is unavailable');await expect(page.getByRole('heading',{name:'If someone is in immediate danger'})).toBeVisible();await expect(page.getByRole('button',{name:'Try again'})).toBeVisible();
});
test('Confused B-04: an authenticated visitor returns to permitted activation',async({page})=>{
 const state=await setup(page);state.signedIn=true;await page.goto('/welcome');await expect(page.getByRole('button',{name:'Continue to your account'}).first()).toBeVisible();await expect(page.getByRole('button',{name:'Create account',exact:true})).toHaveCount(0);await page.getByRole('button',{name:'Continue to your account'}).first().click();await expect(page).toHaveURL(/\/activate/);
});
test('Confused B-04: an expired session returns to a usable guest welcome',async({page})=>{
 const state=await setup(page);state.signedIn=true;await page.goto('/welcome');await expect(page.getByRole('button',{name:'Continue to your account'}).first()).toBeVisible();state.signedIn=false;
 await page.route('**/api/v1/identity',route=>route.fulfill({status:401,contentType:'application/json',headers:{'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true'},body:'{}'}));
 await page.getByRole('button',{name:'Continue to your account'}).first().click();await expect(page.getByRole('button',{name:'Create account',exact:true}).first()).toBeVisible();await expect(page).toHaveURL(/\/welcome/);await expect(page.getByRole('button',{name:'Continue to your account'})).toHaveCount(0);
});

test('Confused B-04: interrupted session confirmation keeps sign-in recoverable',async({page})=>{
 const state=await setup(page);await page.route('**/api/v1/auth/me',route=>state.signedIn?route.abort('internetdisconnected'):route.fallback());await page.goto('/login');
 await page.getByLabel('Email',{exact:true}).fill('synthetic-adult@example.test');await page.getByLabel('Password',{exact:true}).fill('synthetic password for controlled QA');await page.getByRole('button',{name:'Sign in',exact:true}).click();
 await expect(page.getByRole('alert')).toBeVisible();await expect(page.getByLabel('Email',{exact:true})).toHaveValue('synthetic-adult@example.test');await expect(page.getByRole('button',{name:'Sign in',exact:true})).toBeEnabled();
});

test('Malicious B-10: guest Welcome does not request private member endpoints',async({page})=>{
 const requests=[];page.on('request',r=>requests.push(new URL(r.url()).pathname));await setup(page);await page.goto('/welcome');await expect(page.getByRole('button',{name:'Create account',exact:true}).first()).toBeVisible();expect(requests.some(p=>/\/api\/v1\/(posts|conversations|professionals|listeners|preferences|profiles)/.test(p))).toBe(false);
});
test('Malicious B-07: malformed information routes do not redirect externally',async({page})=>{
 await setup(page);await page.goto('/info/not-a-public-page?returnTo=https://evil.test');await expect(page.getByRole('heading',{name:'Information page not found'})).toBeVisible();expect(new URL(page.url()).host).toBe('127.0.0.1:8081');
});
test('Malicious B-07: untrusted published text is rendered literally',async({page})=>{
 await setup(page,{html:true});await page.goto('/info/terms');await expect(page.getByText(/A literal <script>/)).toBeVisible();expect(await page.evaluate(()=>window.phaseBInjected)).toBeUndefined();
});
test('Malicious B-05: unsafe resource URL cannot execute script',async({page})=>{
 await setup(page,{badUrl:true});await page.goto('/urgent-help');await page.getByLabel('Search countries').fill('United');await page.getByRole('button',{name:'United States',exact:true}).click();await expect(page.getByRole('button',{name:'Open resource',exact:true})).toBeDisabled();expect(await page.evaluate(()=>window.phaseBInjected)).toBeUndefined();
});
test('Malicious B-07: absent age policy is not replaced by a fabricated document',async({page})=>{
 await setup(page);await page.goto('/info/age-eligibility');await expect(page.getByRole('alert')).toContainText('current published document is unavailable');await expect(page.getByText(/Version \d/)).toHaveCount(0);
});
test('Confused B-07: a late policy response cannot replace a newer document',async({page})=>{
 await setup(page);await page.route('**/api/v1/policies/privacy',async route=>{await new Promise(r=>setTimeout(r,1200));await route.fulfill({status:200,contentType:'application/json',headers:{'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true'},body:JSON.stringify({key:'privacy',version:1,title:'Old delayed privacy',content:'Stale QA document',publishedAt:'2026-01-01T00:00:00Z'})});});
 await page.goto('/info/privacy');await expect(page.getByText('Loading the current published document…')).toBeVisible();await page.getByRole('button',{name:'Terms of use',exact:true}).click();await expect(page.getByRole('heading',{name:'QA published terms'})).toBeVisible();await page.waitForTimeout(1400);await expect(page.getByText('Stale QA document')).toBeHidden();await expect(page.getByRole('heading',{name:'QA published terms'})).toBeVisible();
});
