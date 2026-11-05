import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
const actor='00000000-0000-0000-0000-000000000001',id='00000000-0000-0000-0000-000000000002';
async function setup(page,{staff=false,self=false,invite=false}={}){
 const auth=await installApi(page,{completeActivation:true});auth.signedIn=true;auth.emailVerified=true;auth.onboardingComplete=true;
 await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{userId:actor,email:'synthetic@example.test',roles:staff?['USER','TRUST_PLATFORM','PILOT_OWNER']:['USER']}}));
 const assessment={id,service:'COMMUNITY',country_code:'IR',source_commit:'a'.repeat(40),revision:3,created_by:id};
 await page.route('**/api/v1/public/pilot-readiness?*',r=>r.fulfill({json:[{service:'COMMUNITY',assessmentState:'NOT_ASSESSED'}]}));
 await page.route('**/api/v1/pilot/me',r=>r.fulfill({json:{invitations:invite?[{id,service:'COMMUNITY',state:'INVITED',revision:1,notice_version:1,expires_at:new Date(Date.now()+3600000).toISOString()}]:[],notice:invite?{version:1,title:'Synthetic optional pilot notice',content:'QA_ONLY optional notice'}:{},runtimePilotConfigured:false,existingRightsRemainAvailable:true}}));
 await page.route('**/api/v1/pilot/assessments',r=>r.fulfill({json:[assessment]}));
 await page.route(`**/api/v1/pilot/assessments/${id}`,r=>r.fulfill({json:{assessment,state:'APPROVAL_REQUIRED',missingCriteria:['DEVICES'],remainingRisks:['DEVICE_TEST_REQUIRED'],runtimeActivationChanged:false,evidence:[{id,criterion:'CI',state:'PASS',kind:'CI',reference:'QA_ONLY:fixture',author_id:self?actor:id,reviewer_id:null,artifact_sha256:'b'.repeat(64)}]}}));
}
test('Q5 ordinary visitor is never automatically enrolled',async({page})=>{
 await setup(page);let writes=0;page.on('request',r=>{if(r.url().includes('/pilot/')&&r.method()==='POST')writes++;});
 await page.goto('/pilot');await expect(page.getByText('Pilot runtime is not enabled. No visitor is automatically enrolled.',{exact:true})).toBeVisible();
 await expect(page.getByText('Staff release assessment',{exact:true})).toHaveCount(0);expect(writes).toBe(0);
});
test('Q5 optional notice must be explicitly acknowledged',async({page})=>{
 await setup(page,{invite:true});let body;
 await page.route(`**/api/v1/pilot/invitations/${id}/choice`,r=>{body=r.request().postDataJSON();return r.fulfill({status:204});});
 await page.goto('/pilot');const accept=page.getByRole('button',{name:'Accept this service invitation',exact:true});await expect(accept).toBeDisabled();
 await page.getByRole('button',{name:'I reviewed the current optional participation notice',exact:true}).click();await accept.click();await expect.poll(()=>body).toEqual({revision:1,accept:true});
});
test('Q5 author cannot independently review or approve incomplete evidence',async({page})=>{
 await setup(page,{staff:true,self:true});await page.goto('/pilot');await page.getByRole('button',{name:'Community and social · '+id,exact:true}).click();
 await expect(page.getByRole('button',{name:'Independently review evidence',exact:true})).toBeDisabled();
 await expect(page.getByRole('button',{name:'Record independent owner approval',exact:true})).toBeDisabled();
 await expect(page.getByRole('button',{name:'Create explicit service invitation',exact:true})).toBeDisabled();
});
test('Q5 lost assessment authority clears protected evidence',async({page})=>{
 await setup(page,{staff:true});await page.goto('/pilot');await page.getByRole('button',{name:'Community and social · '+id,exact:true}).click();await expect(page.getByText('QA_ONLY:fixture',{exact:true})).toBeVisible();
 await page.route('**/api/v1/pilot/assessments',r=>r.fulfill({status:403,json:{message:'Current purpose revoked'}}));await page.getByRole('button',{name:'Refresh pilot status',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('Current purpose revoked');await expect(page.getByText('QA_ONLY:fixture',{exact:true})).toHaveCount(0);
});
for(const width of [360,1280])test(`Q5 Persian optional pilot at ${width}`,async({page})=>{
 await setup(page,{invite:true});await page.setViewportSize({width,height:1100});await page.addInitScript(()=>localStorage.setItem('greenocean.language','fa'));
 await page.goto('/pilot');await expect(page.locator('html')).toHaveAttribute('dir','rtl');await expect(page.getByRole('heading',{name:'پایلوت کنترل‌شده',exact:true})).toBeVisible();
 await expect(page.getByRole('button',{name:'پذیرش دعوت همین خدمت',exact:true})).toBeDisabled();
});
