import {test,expect} from '@playwright/test';
import {api,account,login,reset} from './phase-l-helpers.mjs';
import {clinic} from './clinic-helpers.mjs';
test('P2 real database booking, immutable invoice, denied foreign access and cancellation',async({browser,request,page})=>{
 await reset();const c=await clinic(browser,request);try{
 const booking={slotId:c.slot,retryKey:crypto.randomUUID(),slotRevision:2,policyId:c.policyId,expectedUnits:0,confirmed:true};
 let r=await request.post(`${api}/clinic/appointments`,{headers:c.patient.headers,data:booking});expect(r.status()).toBe(200);const a=await r.json();expect(a.status).toBe('CONFIRMED');
 r=await request.post(`${api}/clinic/appointments`,{headers:c.patient.headers,data:booking});expect((await r.json()).id).toBe(a.id);
 const foreign=await account(request);expect((await request.get(`${api}/clinic/appointments/${a.id}`,{headers:foreign.headers})).status()).toBe(404);
 expect((await request.post(`${api}/clinic/appointments`,{headers:foreign.headers,data:{...booking,retryKey:crypto.randomUUID()}})).status()).toBe(409);
 expect((await request.patch(`${api}/professional/consultations/${a.id}`,{headers:c.pro.headers,data:{status:'COMPLETED'}})).status()).toBe(404);
 await login(page,c.patient);await page.goto('/clinic');await expect(page.getByRole('heading',{name:'My Payments',exact:true})).toBeVisible();await expect(page.getByText(a.id,{exact:true})).toBeVisible();
 await page.getByLabel('Action reason or dispute explanation',{exact:true}).fill('Synthetic voluntary cancellation');await page.getByRole('button',{name:'Cancel this appointment',exact:true}).click();await expect(page.getByText(/CANCELLED_BY_PATIENT/).first()).toBeVisible();
 r=await request.get(`${api}/professional/workspace`,{headers:c.pro.headers});expect((await r.json()).slots.find(s=>s.id===c.slot).status).toBe('CANCELLED');
 }finally{await c.staff.context.close();}
});
