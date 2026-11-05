import {test,expect} from '@playwright/test';import {installApi} from './fixtures/public-api.mjs';
test('L2 feed tabs issue distinct authorized server queries and show honest empty states',async({page})=>{
 const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 await page.route('**/api/v1/identity',r=>r.fulfill({json:{status:'ACTIVE',email_verified:true,services:{COMMUNITY:true},eligibility:{display_reviewed_at:'2026-01-01'}}}));
 const modes=[];await page.route('**/api/v1/posts/discovery*',r=>{modes.push(new URL(r.request().url()).searchParams.get('mode'));return r.fulfill({json:{items:[],hasNext:false,nextCursor:null}});});
 await page.goto('/');await page.getByRole('tab',{name:'Following',exact:true}).click();await expect(page.getByText('No permitted followed stories',{exact:true})).toBeVisible();expect(modes).toContain('FOLLOWING');
 await page.getByRole('tab',{name:'Saved',exact:true}).click();await expect(page.getByText('No permitted saved posts',{exact:true})).toBeVisible();expect(modes).toContain('SAVED');
 await page.getByRole('tab',{name:'Discover',exact:true}).click();expect(modes).toContain('DISCOVER');
});

test('L2 distinguishes no follows, no permitted stories and network failure',async({page})=>{
 const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 let followingAnyone=false;let unavailable=false;
 await page.route('**/api/v1/posts/discovery*',r=>unavailable?r.fulfill({status:503,json:{message:'Synthetic feed unavailable'}}):r.fulfill({json:{items:[],hasNext:false,nextCursor:null,followingAnyone}}));
 await page.goto('/');await page.getByRole('tab',{name:'Following',exact:true}).click();
 await expect(page.getByText('You are not following anyone yet',{exact:true})).toBeVisible();
 followingAnyone=true;await page.getByRole('tab',{name:'Discover',exact:true}).click();await page.getByRole('tab',{name:'Following',exact:true}).click();
 await expect(page.getByText('No permitted followed stories',{exact:true})).toBeVisible();
 unavailable=true;await page.getByRole('tab',{name:'Discover',exact:true}).click();await page.getByRole('tab',{name:'Following',exact:true}).click();
 await expect(page.getByText('Could not load the feed',{exact:true})).toBeVisible();await expect(page.getByText('You are not following anyone yet',{exact:true})).toHaveCount(0);
});

test('L2 Persian search and post actions have localized accessible names in RTL',async({page})=>{
 const state=await installApi(page,{completeActivation:true});state.signedIn=true;state.emailVerified=true;state.onboardingComplete=true;
 await page.goto('/welcome');await page.getByRole('button',{name:'فارسی',exact:true}).click();
 await page.goto('/search');await expect(page.locator('html')).toHaveAttribute('dir','rtl');
 const search=page.getByRole('textbox',{name:'جست‌وجو در گرین‌اوشن',exact:true});await expect(search).toBeVisible();
 await search.fill('synthetic');await expect(page.getByText('اعضا',{exact:true})).toBeVisible();await expect(page.getByText('پست‌ها',{exact:true})).toBeVisible();
 await expect(page.getByLabel('پاک‌کردن جست‌وجو',{exact:true})).toBeVisible();
 await page.route('**/api/v1/posts/discovery*',r=>r.fulfill({json:{items:[{id:'synthetic-localized-post',body:'متن آزمایشی',anonymous:true,author:null,visibility:'PUBLIC',createdAt:new Date().toISOString(),liked:false,bookmarked:false,likeCount:0,commentCount:0}],hasNext:false,nextCursor:null}}));
 await page.goto('/');await expect(page.getByRole('button',{name:'پسندیدن پست',exact:true})).toBeVisible();await expect(page.getByRole('button',{name:'ذخیرهٔ پست',exact:true})).toBeVisible();
 await expect(page.getByRole('button',{name:'خواندن پست ناشناس',exact:true})).toBeVisible();
});
