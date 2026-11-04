import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
const actor='00000000-0000-0000-0000-000000000001';
const id='00000000-0000-0000-0000-000000000002';
const subject='00000000-0000-0000-0000-000000000003';
const hash='a'.repeat(64);
async function setup(page,{self=false,ordinary=false}={}){
  const auth=await installApi(page,{completeActivation:true});
  auth.signedIn=true;auth.emailVerified=true;auth.onboardingComplete=true;
  await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{userId:actor,email:'synthetic-reviewer@example.test',roles:ordinary?['USER']:['USER','TRUST_ACCOUNT']}}));
  const run={id,source_commit:'035879e196e99ed1ffc1097c3363acf7287040b0',manifest_sha256:hash,prepared_by:self?actor:id,reviewer_id:null,expires_at:new Date(Date.now()+3600000).toISOString()};
  await page.route('**/api/v1/recovery/status',r=>r.fulfill({json:{restoreMode:true,replayReady:false,runs:[run]}}));
  await page.route(`**/api/v1/recovery/runs/${id}`,r=>r.fulfill({json:{run,subjects:[{subject_id:subject,domain:'WELLBEING'}],stoppedServices:[{service:'CLINIC'}]}}));
}
test('Q4 independent operator inspects exact replay before approval',async({page})=>{
  await setup(page);let approved=false;
  await page.route(`**/api/v1/recovery/runs/${id}/review`,r=>{approved=true;return r.fulfill({status:204});});
  await page.goto('/recovery-review');
  await page.getByRole('button',{name:'Refresh recovery status',exact:true}).click();
  await expect(page.getByText('Data access remains blocked pending independent review.',{exact:true})).toBeVisible();
  await page.getByRole('button',{name:'Inspect replay restrictions',exact:true}).click();
  await expect(page.getByText(subject+' · WELLBEING',{exact:true})).toBeVisible();
  await expect(page.getByRole('button',{name:'Record independent replay review',exact:true})).toBeDisabled();
  await page.getByRole('button',{name:'I independently compared the exact source and current rights manifest',exact:true}).click();
  await page.getByRole('button',{name:'Record independent replay review',exact:true}).click();
  await expect.poll(()=>approved).toBe(true);
  await expect(page.getByText(subject+' · WELLBEING',{exact:true})).toHaveCount(0);
});
test('Q4 preparation cannot approve its own recovery',async({page})=>{
  await setup(page,{self:true});await page.goto('/recovery-review');
  await page.getByRole('button',{name:'Refresh recovery status',exact:true}).click();
  await page.getByRole('button',{name:'Inspect replay restrictions',exact:true}).click();
  await expect(page.getByRole('button',{name:'I independently compared the exact source and current rights manifest',exact:true})).toBeDisabled();
  await expect(page.getByRole('button',{name:'Record independent replay review',exact:true})).toBeDisabled();
});
test('Q4 rejected purpose clears previously inspected restrictions',async({page})=>{
  await setup(page);await page.goto('/recovery-review');
  await page.getByRole('button',{name:'Refresh recovery status',exact:true}).click();
  await page.getByRole('button',{name:'Inspect replay restrictions',exact:true}).click();
  await page.route('**/api/v1/recovery/status',r=>r.fulfill({status:403,json:{message:'Current recovery authority revoked'}}));
  await page.getByRole('button',{name:'Refresh recovery status',exact:true}).click();
  await expect(page.getByRole('alert')).toContainText('Current recovery authority revoked');
  await expect(page.getByText(subject+' · WELLBEING',{exact:true})).toHaveCount(0);
});
test('Q4 ordinary account cannot request a recovery workspace',async({page})=>{
  await setup(page,{ordinary:true});let reads=0;
  await page.route('**/api/v1/recovery/status',r=>{reads++;return r.fulfill({status:403,json:{message:'Forbidden'}});});
  await page.goto('/recovery-review');
  await expect(page.getByText('Current account-safety authority is required.',{exact:true})).toBeVisible();
  await expect(page.getByRole('button',{name:'Refresh recovery status',exact:true})).toHaveCount(0);
  expect(reads).toBe(0);
});
test('Q4 background clears protected metadata and rejects delayed inspection',async({page})=>{
  await setup(page);await page.goto('/recovery-review');
  await page.getByRole('button',{name:'Refresh recovery status',exact:true}).click();
  let release,started=false;const gate=new Promise(resolve=>{release=resolve;});
  await page.route(`**/api/v1/recovery/runs/${id}`,async r=>{started=true;await gate;return r.fulfill({json:{
    run:{id,manifest_sha256:hash,prepared_by:id},subjects:[{subject_id:subject,domain:'WELLBEING'}],stoppedServices:[]}});});
  await page.getByRole('button',{name:'Inspect replay restrictions',exact:true}).click();
  await expect.poll(()=>started).toBe(true);
  await page.evaluate(()=>{Object.defineProperty(document,'hidden',{configurable:true,get:()=>true});document.dispatchEvent(new Event('visibilitychange'));});
  release();await expect(page.getByText(subject+' · WELLBEING',{exact:true})).toHaveCount(0);
  await page.evaluate(()=>{Object.defineProperty(document,'hidden',{configurable:true,get:()=>false});document.dispatchEvent(new Event('visibilitychange'));});
  await expect(page.getByRole('button',{name:'Inspect replay restrictions',exact:true})).toBeVisible();
  await expect(page.getByText(subject+' · WELLBEING',{exact:true})).toHaveCount(0);
});
for(const width of [360,1280])test(`Q4 Persian recovery review at ${width}`,async({page})=>{
  await setup(page);await page.setViewportSize({width,height:1100});
  await page.addInitScript(()=>localStorage.setItem('greenocean.language','fa'));
  await page.goto('/recovery-review');await expect(page.locator('html')).toHaveAttribute('dir','rtl');
  await expect(page.getByRole('heading',{name:'بازبینی حریم خصوصی پس از بازیابی',exact:true})).toBeVisible();
  await page.getByRole('button',{name:'بازخوانی وضعیت بازیابی',exact:true}).click();
  await expect(page.getByText('دسترسی داده تا بازبینی مستقل مسدود است.',{exact:true})).toBeVisible();
});
