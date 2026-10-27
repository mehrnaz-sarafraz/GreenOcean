import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
const listener={id:'00000000-0000-4000-8000-000000000001',displayName:'Synthetic listener',listeningStyle:'Reflective',languages:['English'],online:true};
const base={id:'00000000-0000-4000-8000-000000000002',peerId:listener.id,peerName:listener.displayName,listenerSide:false,expiresAt:new Date(Date.now()+90000).toISOString(),serverTime:new Date().toISOString(),presenceSeconds:45,reconnectSeconds:120,conversationId:null};
async function setup(page,options={}){
 const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 let current=null,writes=0,fail=options.fail??false;
 await page.route('**/api/v1/listener-support/**',async route=>{
  const path=new URL(route.request().url()).pathname,method=route.request().method();
  if(path.endsWith('/availability'))return route.fulfill({json:{enabled:true,offerSeconds:90,listeners:options.empty?[]:[listener]}});
  if(path.endsWith('/requests/me'))return route.fulfill({json:current?[current]:[]});
  if(path.endsWith('/requests')&&method==='POST'){writes++;if(fail){fail=false;return route.fulfill({status:503,json:{code:'TECHNICAL_FAILURE',message:'Synthetic request unavailable'}});}current={...base,status:'REQUESTED'};return route.fulfill({json:current});}
  if(path.endsWith('/cancel')){current={...current,status:'CANCELLED'};return route.fulfill({json:current});}
  if(path.endsWith('/activate')){current={...current,status:'EXPIRED'};return route.fulfill({status:409,json:{code:'REQUEST_EXPIRED',message:'Request expired'}});}
  return route.fulfill({json:current});
 });
 return {writes:()=>writes,accept:()=>{current={...current,status:'ACCEPTED',activationExpiresAt:new Date(Date.now()+60000).toISOString(),serverTime:new Date().toISOString()};}};
}
test('Happy I UI: navigation and confirmation never imply acceptance; explicit request can be cancelled',async({page})=>{
 const state=await setup(page);await page.goto('/listener/match');await page.getByRole('button',{name:'Review request to Synthetic listener',exact:true}).click();expect(state.writes()).toBe(0);await page.getByRole('button',{name:'Request live conversation',exact:true}).click();await expect(page.getByText('Waiting for listener acceptance',{exact:true})).toBeVisible();expect(state.writes()).toBe(1);await expect(page.getByRole('button',{name:'Enter accepted conversation',exact:true})).toHaveCount(0);await page.getByRole('button',{name:'Cancel request',exact:true}).click();await expect(page.getByText('Request cancelled',{exact:true})).toBeVisible();
});
test('Confused I UI: save failure stays recoverable and acceptance alone does not navigate to chat',async({page})=>{
 const state=await setup(page,{fail:true});await page.goto('/listener/match');await page.getByRole('button',{name:'Review request to Synthetic listener',exact:true}).click();await page.getByRole('button',{name:'Request live conversation',exact:true}).click();await expect(page.getByText('Synthetic request unavailable',{exact:true})).toBeVisible();await expect(page.getByText('Waiting for listener acceptance',{exact:true})).toHaveCount(0);await page.getByRole('button',{name:'Request live conversation',exact:true}).click();await expect(page.getByText('Waiting for listener acceptance',{exact:true})).toBeVisible();state.accept();await expect(page.getByText('Listener accepted — ready to enter',{exact:true})).toBeVisible();await expect(page).toHaveURL(/\/listener\/match$/);await page.getByRole('button',{name:'Enter accepted conversation',exact:true}).click();await expect(page.getByText('Request expired',{exact:true})).toBeVisible();await expect(page).toHaveURL(/\/listener\/match$/);
});
for(const width of [360,1280])test(`Creative I UI: Persian RTL and empty availability at width ${width}`,async({page})=>{
 await setup(page,{empty:true});await page.addInitScript(()=>localStorage.setItem('greenocean.language','fa'));await page.setViewportSize({width,height:900});await page.goto('/listener/match');await expect(page.locator('html')).toHaveAttribute('dir','rtl');await expect(page.getByText('اکنون شنوندهٔ زنده‌ای در دسترس نیست',{exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'یافتن کمک فوری',exact:true})).toBeVisible();expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+2)).toBe(true);
});
