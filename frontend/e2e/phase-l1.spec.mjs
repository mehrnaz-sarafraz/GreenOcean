import {test,expect} from '@playwright/test';
import {installApi} from './fixtures/public-api.mjs';
async function setup(page){const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 await page.route('**/api/v1/identity',r=>r.fulfill({json:{status:'ACTIVE',email_verified:true,services:{COMMUNITY:true},eligibility:{display_reviewed_at:'2026-01-01T00:00:00Z'}}}));
 await page.route('**/api/v1/posts/drafts',r=>r.fulfill({json:[]}));return state;}
test('Confused L1: identity is first; no text editor or assumed named identity',async({page})=>{
 await setup(page);await page.goto('/create');await expect(page.getByRole('heading',{name:'How would you like this post to appear to other members?'})).toBeVisible();
 await expect(page.getByLabel('Post text',{exact:true})).toHaveCount(0);await expect(page.getByRole('button',{name:'Continue to writing',exact:true})).toBeDisabled();
 await page.getByRole('button',{name:'Anonymous',exact:true}).click();await page.getByRole('button',{name:'Continue to writing',exact:true}).click();await page.getByLabel('Post text',{exact:true}).fill('A general experience with no diagnosis');
 await page.getByRole('button',{name:'Back',exact:true}).click();await page.getByRole('button',{name:'Continue to writing',exact:true}).click();await expect(page.getByLabel('Post text',{exact:true})).toHaveValue('A general experience with no diagnosis');
 await page.getByRole('button',{name:'Review before publishing',exact:true}).click();await expect(page.getByRole('heading',{name:'Final review',exact:true})).toBeVisible();await expect(page.getByText('Identity: Anonymous',{exact:true})).toBeVisible();await expect(page.getByText('Audience: Approved followers',{exact:true})).toBeVisible();
});
test('Happy L1: final publish uses explicit intent and truthful pending status',async({page})=>{
 await setup(page);let sent;
 await page.route('**/api/v1/posts',r=>{sent=r.request().postDataJSON();return r.fulfill({status:201,json:{id:'pending-post',status:'UNDER_REVIEW',safetyOutcome:'HOLD_FOR_REVIEW'}});});
 await page.goto('/create');await page.getByRole('button',{name:'Social identity',exact:true}).click();await page.getByRole('button',{name:'Continue to writing',exact:true}).click();await page.getByLabel('Post text',{exact:true}).fill('My story');await page.getByRole('button',{name:'Review before publishing',exact:true}).click();await page.getByRole('button',{name:'Publish',exact:true}).click();
 await expect(page.getByRole('alert')).toContainText('Awaiting review');expect(sent.publicationIdentity).toBe('SOCIAL_IDENTITY');expect(sent.anonymous).toBe(false);expect(sent.categoryId).toBeNull();await expect(page.getByRole('button',{name:'Continue to writing',exact:true})).toBeDisabled();
});
test('Creative L1: Persian identity first and RTL text preserved',async({page})=>{
 await setup(page);await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();await page.goto('/create');await expect(page.locator('html')).toHaveAttribute('dir','rtl');await page.getByRole('button',{name:'ناشناس',exact:true}).click();await page.getByRole('button',{name:'ادامه به نوشتن',exact:true}).click();await page.getByLabel('متن پست',{exact:true}).fill('تجربه‌ام را می‌نویسم 🌊');await page.getByRole('button',{name:'بازبینی پیش از انتشار',exact:true}).click();await expect(page.getByText('تجربه‌ام را می‌نویسم 🌊',{exact:true})).toBeVisible();
});
