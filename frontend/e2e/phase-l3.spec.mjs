import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
async function setup(page,staff=false){const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 await page.route('**/api/v1/identity',r=>r.fulfill({json:{status:'ACTIVE',email_verified:true,services:{COMMUNITY:true},eligibility:{display_reviewed_at:'2026-01-01'}}}));
 if(staff)await page.route('**/api/v1/auth/me',r=>r.fulfill({json:{userId:'00000000-0000-0000-0000-000000000001',email:'synthetic@example.test',roles:['MODERATOR']}}));
}
const item={id:'00000000-0000-0000-0000-000000000002',post_id:'00000000-0000-0000-0000-000000000003',revision:3,outcome:'HOLD_FOR_REVIEW',reason_code:'CONTEXT_REVIEW',body:'Synthetic evidence only'};
test('L3 author sees truthful pending decision and can appeal once with recovery after failure',async({page})=>{
 await setup(page);let appealed=false,attempt=0;
 await page.route('**/api/v1/social-moderation/me*',r=>r.fulfill({json:[{...item,appeal_id:appealed?'appeal-id':null,appeal_status:appealed?'OPEN':null}]}));
 await page.route('**/api/v1/social-moderation/*/appeal',r=>{attempt++;if(attempt===1)return r.fulfill({status:503,json:{message:'Review service unavailable'}});expect(r.request().postDataJSON()).toEqual({reason:'Please review the quotation context.'});appealed=true;return r.fulfill({status:201,json:{id:'appeal-id'}});});
 await page.goto('/content-decisions');await expect(page.getByText('Awaiting review',{exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'Submit appeal',exact:true})).toBeDisabled();
 await page.getByLabel('Review explanation',{exact:true}).fill('Please review the quotation context.');await page.getByRole('button',{name:'Submit appeal',exact:true}).click();await expect(page.getByRole('alert')).toContainText('Review service unavailable');await expect(page.getByLabel('Review explanation',{exact:true})).toHaveValue('Please review the quotation context.');
 await page.getByRole('button',{name:'Submit appeal',exact:true}).click();await expect(page.getByText('Appeal: Awaiting review',{exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'Submit appeal',exact:true})).toHaveCount(0);
});
test('L3 staff review submits the exact displayed revision and a required explanation',async({page})=>{
 await setup(page,true);let decided=false;
 await page.route('**/api/v1/admin/social-moderation**',r=>{if(r.request().method()==='GET')return r.fulfill({json:decided?[]:[item]});const data=r.request().postDataJSON();expect(data).toEqual({revision:3,outcome:'ALLOW',note:'Context permits this synthetic example.'});decided=true;return r.fulfill({status:204});});
 await page.goto('/community-review');await expect(page.getByText(item.body,{exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'Approve this revision',exact:true})).toBeDisabled();await page.getByLabel('Review explanation',{exact:true}).fill('Context permits this synthetic example.');await page.getByRole('button',{name:'Approve this revision',exact:true}).click();await expect(page.getByText('No decisions to display',{exact:true})).toBeVisible();
});
test('L3 ordinary member cannot open staff evidence and Persian review copy supports RTL',async({page})=>{
 await setup(page);await page.goto('/community-review');await expect(page.getByText('Restricted access',{exact:true})).toBeVisible();await expect(page.getByText(item.body,{exact:true})).toHaveCount(0);
 await page.evaluate(()=>localStorage.setItem('greenocean.language','fa'));await page.route('**/api/v1/social-moderation/me*',r=>r.fulfill({json:[item]}));await page.goto('/content-decisions');
 // The app language control is authoritative; persistence key is checked in other language suites.
 await expect(page.getByLabel('توضیح بررسی',{exact:true})).toBeVisible(); await expect(page.locator('html')).toHaveAttribute('dir','rtl');
});
