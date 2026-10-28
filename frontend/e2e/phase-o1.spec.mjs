import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
const id='00000000-0000-4000-8000-000000000011';
async function setup(page,{conflict=false,daily=false}={}){
 const auth=await installApi(page,{completeActivation:true});auth.signedIn=true;auth.emailVerified=true;auth.onboardingComplete=true;
 let rows=[],writes=0;await page.route('**/api/v1/wellbeing/**',async route=>{
  const path=new URL(route.request().url()).pathname,method=route.request().method();
  if(path.endsWith('/daily-reflection'))return route.fulfill({json:{available:true,set:{id,local_date:'2026-10-28',timezone:'UTC'},items:[1,2,3].map(position=>({position,question:'Reviewed synthetic question '+position,answer:null,revision:null}))}});
  if(path.endsWith('/responses')){writes++;return route.fulfill({json:{}});}
  if(method==='POST'){writes++;rows=[{...route.request().postDataJSON(),title:route.request().postDataJSON().title||'Untitled reflection',id,revision:0,created_at:new Date().toISOString()}];return route.fulfill({json:rows[0]});}
  if(method==='PATCH'){writes++;if(conflict)return route.fulfill({status:409,json:{message:'The entry changed. Reload before saving; your unsaved writing was not overwritten.'}});rows=[{...rows[0],...route.request().postDataJSON(),revision:1}];return route.fulfill({json:rows[0]});}
  if(method==='DELETE'){rows=[];return route.fulfill({status:204});}
  return route.fulfill({json:{items:rows,hasMore:false,page:0}});
 });return {writes:()=>writes};
}
test('O1 Happy: private journal explicit create, edit and delete',async({page})=>{
 const state=await setup(page);await page.goto('/tools/journal');await page.getByRole('textbox',{name:'Title',exact:true}).fill('My quiet note');await page.getByRole('textbox',{name:'Private writing',exact:true}).fill('خصوصی 🌿');expect(state.writes()).toBe(0);await page.getByRole('button',{name:'Save privately',exact:true}).click();await expect(page.getByText('Saved privately.',{exact:true})).toBeVisible();await page.getByRole('button',{name:'Edit My quiet note',exact:true}).click();await page.getByRole('textbox',{name:'Private writing',exact:true}).fill('edited private text');await page.getByRole('button',{name:'Save changes',exact:true}).click();await expect(page.getByText('edited private text',{exact:true})).toBeVisible();await page.getByRole('button',{name:'Delete My quiet note',exact:true}).click();await expect(page.getByRole('button',{name:'Edit My quiet note',exact:true})).toHaveCount(0);
});
test('O1 Creative: revision conflict retains long unsaved Unicode writing',async({page})=>{
 await setup(page,{conflict:true});await page.goto('/tools/journal');await page.getByRole('textbox',{name:'Private writing',exact:true}).fill('first');await page.getByRole('button',{name:'Save privately',exact:true}).click();await page.getByRole('button',{name:/^Edit /}).first().click();const text='فارسی English 🌿 '.repeat(200);await page.getByRole('textbox',{name:'Private writing',exact:true}).fill(text);await page.getByRole('button',{name:'Save changes',exact:true}).click();await expect(page.getByRole('alert')).toContainText('entry changed');await expect(page.getByRole('textbox',{name:'Private writing',exact:true})).toHaveValue(text);
});
test('O1 Confused: only one of three daily answers explicitly saves and earns no points',async({page})=>{
 const state=await setup(page);await page.goto('/tools/daily-reflection');await page.getByRole('textbox',{name:'Optional answer 1',exact:true}).fill('one optional answer');expect(state.writes()).toBe(0);await page.getByRole('button',{name:'Save answer 1',exact:true}).click();await expect(page.getByText('Saved privately.',{exact:true})).toBeVisible();expect(state.writes()).toBe(1);await expect(page.getByRole('textbox',{name:'Optional answer 2',exact:true})).toHaveValue('');await expect(page.getByText(/Answers earn no points/)).toBeVisible();
});
for(const width of [360,1280])test(`O1 Creative: Persian private writing fits RTL at ${width}`,async({page})=>{await page.setViewportSize({width,height:900});await setup(page);await page.addInitScript(()=>localStorage.setItem('greenocean.language','fa'));await page.goto('/tools/journal');await expect(page.getByRole('textbox',{name:'نوشتهٔ خصوصی',exact:true})).toBeVisible();await page.getByRole('textbox',{name:'نوشتهٔ خصوصی',exact:true}).fill('سلام English');expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1)).toBe(true);});
