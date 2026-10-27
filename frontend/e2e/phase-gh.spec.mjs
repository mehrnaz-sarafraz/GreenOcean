import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
async function setup(page,initial={}){
 const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;
 const preferences={onboardingComplete:false,onboardingStatus:'NOT_STARTED',onboardingStep:0,supportTopics:[],supportStyle:null,strongerContentControls:true,blurSensitiveContent:true,allowMessageRequests:false,mutedTerms:[],topicCollectionEnabled:false,...initial};
 const writes=[];let fail=false;
 await page.route('**/api/v1/preferences/me',async r=>{if(r.request().method()==='PATCH'){if(fail){fail=false;return r.fulfill({status:503,json:{message:'Synthetic save unavailable'}});}const patch=r.request().postDataJSON();writes.push(patch);Object.assign(preferences,patch);preferences.onboardingComplete=['SKIPPED','COMPLETED','LEGACY_UNKNOWN'].includes(preferences.onboardingStatus);}return r.fulfill({json:preferences});});
 await page.route('**/api/v1/catalog/options*',r=>r.fulfill({json:[['SHARED_EXPERIENCES','Shared experiences'],['SUPPORT_GROUPS','Support groups'],['LISTENER','One-to-one listener'],['PROFESSIONAL_KNOWLEDGE','Professional knowledge']].map(([key,label])=>({id:key,group:'SUPPORT_STYLE',key,label,description:'Optional support entry',sortOrder:1}))}));
 return {preferences,writes,failNext:()=>{fail=true;}};
}
test('Happy GH UI: optional zero-input completion persists three steps and opens Support',async({page})=>{
 const s=await setup(page);await page.goto('/onboarding');await expect(page.getByRole('heading',{name:'Optional interests'})).toBeVisible();await page.getByRole('button',{name:'Continue',exact:true}).click();await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('button',{name:'Enter GreenOcean',exact:true}).click();await expect(page).toHaveURL(/\/support$/);expect(s.preferences.onboardingStatus).toBe('COMPLETED');expect(s.preferences.onboardingStep).toBe(3);expect(s.preferences.supportTopics).toEqual([]);expect(s.preferences.supportStyle).toBe('');
});
test('Confused GH UI: Skip preserves safety and distinct state without redirect loop',async({page})=>{
 const s=await setup(page);await page.goto('/onboarding');await page.getByRole('button',{name:'Skip for now',exact:true}).click();await expect(page).toHaveURL(/\/support$/);await page.reload();await expect(page).toHaveURL(/\/support$/);expect(s.preferences.onboardingStatus).toBe('SKIPPED');expect(s.preferences.strongerContentControls).toBe(true);expect(s.preferences.blurSensitiveContent).toBe(true);expect(s.preferences.allowMessageRequests).toBe(false);
});
test('Creative GH UI: confirmed step resumes after reload and support style routes to articles',async({page})=>{
 const s=await setup(page,{onboardingStatus:'IN_PROGRESS',onboardingStep:1});await page.goto('/onboarding');await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();await page.reload();await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();await page.getByRole('radio',{name:'Professional knowledge',exact:true}).click();await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('button',{name:'Enter GreenOcean',exact:true}).click();await expect(page).toHaveURL(/\/articles$/);expect(s.preferences.supportStyle).toBe('PROFESSIONAL_KNOWLEDGE');
});
test('Confused GH UI: failed save does not advance or pretend to persist progress',async({page})=>{
 const s=await setup(page,{onboardingStatus:'IN_PROGRESS'});await page.goto('/onboarding');await expect(page.getByRole('heading',{name:'Optional interests'})).toBeVisible();s.failNext();await page.getByRole('button',{name:'Continue',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Synthetic save unavailable');expect(s.preferences.onboardingStep).toBe(0);await expect(page.getByRole('heading',{name:'Optional interests'})).toBeVisible();await page.getByRole('button',{name:'Continue',exact:true}).click();await expect(page.getByRole('heading',{name:'Where would you like to start?'})).toBeVisible();
});
test('Malicious GH UI: legacy professional preference never grants application navigation',async({page})=>{
 await setup(page,{intendedRole:'PROFESSIONAL',onboardingStep:2,onboardingStatus:'IN_PROGRESS'});await page.goto('/onboarding');await page.getByRole('button',{name:'Enter GreenOcean',exact:true}).click();await expect(page).toHaveURL(/\/support$/);expect(page.url()).not.toContain('/professional/');
});
test('Confused GH UI: privacy saving failure rolls back and reports unsaved change',async({page})=>{
 const s=await setup(page,{onboardingStatus:'SKIPPED',onboardingComplete:true});await page.goto('/safety-settings');const toggle=page.getByRole('switch',{name:'Allow message requests',exact:true});await expect(toggle).not.toBeChecked();s.failNext();await toggle.click();await expect(page.getByRole('alert')).toContainText('previous settings have been restored');await expect(toggle).not.toBeChecked();expect(s.preferences.allowMessageRequests).toBe(false);
});
for(const width of [360,1280])test('Creative GH UI: Persian RTL optional onboarding at width '+width,async({page})=>{
 await page.setViewportSize({width,height:860});await setup(page,{onboardingStatus:'IN_PROGRESS'});await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();await page.goto('/onboarding');await expect(page.getByRole('heading',{name:'علاقه‌مندی‌های اختیاری',exact:true})).toBeVisible();await expect(page.locator('html')).toHaveAttribute('dir','rtl');await page.getByRole('button',{name:'فعلاً رد کن',exact:true}).click();await expect(page).toHaveURL(/\/support$/);expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth)).toBe(true);
});
