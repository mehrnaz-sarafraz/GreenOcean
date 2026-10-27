import {test,expect} from '@playwright/test';import {installApi} from './fixtures/public-api.mjs';
test('L2 feed tabs issue distinct authorized server queries and show honest empty states',async({page})=>{
 const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 await page.route('**/api/v1/identity',r=>r.fulfill({json:{status:'ACTIVE',email_verified:true,services:{COMMUNITY:true},eligibility:{display_reviewed_at:'2026-01-01'}}}));
 const modes=[];await page.route('**/api/v1/posts/discovery*',r=>{modes.push(new URL(r.request().url()).searchParams.get('mode'));return r.fulfill({json:{items:[],hasNext:false,nextCursor:null}});});
 await page.goto('/');await page.getByRole('tab',{name:'Following',exact:true}).click();await expect(page.getByText('No permitted followed stories',{exact:true})).toBeVisible();expect(modes).toContain('FOLLOWING');
 await page.getByRole('tab',{name:'Saved',exact:true}).click();await expect(page.getByText('No permitted saved posts',{exact:true})).toBeVisible();expect(modes).toContain('SAVED');
 await page.getByRole('tab',{name:'Discover',exact:true}).click();expect(modes).toContain('DISCOVER');
});
