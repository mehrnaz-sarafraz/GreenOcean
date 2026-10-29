import {test,expect} from '@playwright/test';
import {api,account,login,reset,password} from './phase-l-helpers.mjs';
import {moderator} from './phase-m-helpers.mjs';
import {browserApi} from './phase-o-helpers.mjs';
test('independent credential quarantine, exact revision review and clinic release remain separate',async({browser,request,page})=>{
 await reset();const owner=await account(request);const uid=(await(await request.get(`${api}/auth/me`,{headers:owner.headers})).json()).userId;
 let r=await request.post(`${api}/professional-applications`,{headers:owner.headers,data:{professionalType:'COUNSELOR',title:'Synthetic counselor',specialization:'Synthetic education',bio:'Synthetic isolated test applicant, not a real licensed clinician.',yearsOfExperience:1,specialties:['Synthetic'],languages:['en'],country:'IR',licenseNumber:'SYNTHETIC-P1-LICENSE',consultationModes:['TEXT'],documentNames:['synthetic.pdf']}});expect(r.status()).toBe(201);const app=await r.json();
 r=await request.post(`${api}/professional-applications/${app.id}/documents`,{headers:owner.headers,multipart:{file:{name:'synthetic.pdf',mimeType:'application/pdf',buffer:Buffer.from('%PDF-1.4\nSynthetic isolated credential fixture only.\n%%EOF')}}});expect(r.status()).toBe(201);const document=await r.json();
 await login(page,owner);await page.goto('/tools/professional-workspace');await expect(page.getByText('No current independent qualification',{exact:true})).toBeVisible();
 const staff=await moderator(browser,request,{educationalAdmin:true});try{
  let detail=await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${app.id}`);expect(detail.status).toBe(200);expect(detail.data.documents[0].scan_status).toBe('PENDING');
  r=await request.post(`${api}/admin/professional-documents/${document.id}/scan`,{headers:owner.headers,data:{reason:'Synthetic unauthorized scan'}});expect(r.status()).toBe(403);
  const scan=await browserApi(staff.page,`/api/v1/admin/professional-documents/${document.id}/scan`,'POST',{reason:'Synthetic scanner integration check'});expect(scan.status).toBe(200);expect(scan.data.state).toBe('CLEAN');
  detail=await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${app.id}`);const revision=detail.data.qualification_revision;
  const decision={revision,state:'VERIFIED',reason:'Synthetic independent human evidence workflow only',category:'COUNSELOR',verifiedName:'Synthetic test clinician',issuer:'Synthetic test issuer',licenseIdentifier:'SYNTHETIC-P1-LICENSE',method:'OFFICIAL_REGISTER_HUMAN',source:'https://my.pcoiran.ir/member/',evidence:'SYNTHETIC-QA-NOT-A-REAL-LICENSE-CHECK',validFrom:new Date(Date.now()-60000).toISOString(),validUntil:new Date(Date.now()+86400000).toISOString(),recheckAt:new Date(Date.now()+3600000).toISOString(),scopes:[{country:'IR',mode:'TEXT'}],verifiedSpecialties:['Synthetic test scope']};
  expect((await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${app.id}/decision`,'POST',decision)).status).toBe(204);
  expect((await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${app.id}/decision`,'POST',decision)).status).toBe(409);
  const current=await request.post(`${api}/auth/login`,{data:{email:owner.email,password}});expect(current.status()).toBe(200);owner.headers.Authorization='Bearer '+(await current.json()).accessToken;
  const slot={startsAt:new Date(Date.now()+7200000).toISOString(),endsAt:new Date(Date.now()+9000000).toISOString(),mode:'TEXT',timezone:'Asia/Tehran',priceUnits:0,publish:true};
  expect((await request.post(`${api}/professional/calendar/slots`,{headers:owner.headers,data:slot})).status()).toBe(403);
  for(const purpose of ['PLATFORM','PROFESSIONAL_SCOPE','PATIENT_IDENTITY','DATA_PROCESSING','TERMS','OPERATIONS'])expect((await browserApi(staff.page,'/api/v1/admin/clinic-release-approvals','POST',{country:'IR',mode:'TEXT',purpose,evidence:'SYNTHETIC-QA-RELEASE-NOT-LEGAL-APPROVAL',expiresAt:new Date(Date.now()+3600000).toISOString()})).status).toBe(204);
  r=await request.post(`${api}/professional/calendar/slots`,{headers:owner.headers,data:{...slot,publish:false}});expect(r.status()).toBe(200);const id=(await r.json()).id;
  expect((await request.post(`${api}/professional/calendar/slots/${id}/publish`,{headers:owner.headers,data:{revision:1}})).status()).toBe(204);
  expect((await(await request.get(`${api}/professionals/${uid}/availability`,{headers:owner.headers})).json()).some(s=>s.id===id)).toBe(true);
  await page.reload();await expect(page.getByText('Current independent qualification',{exact:true})).toBeVisible();await expect(page.getByText('Free consultation metadata · TEXT',{exact:true})).toBeVisible();
  expect((await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${app.id}/decision`,'POST',{...decision,revision:revision+1,state:'REVOKED'})).status).toBe(204);
  expect((await request.get(`${api}/professionals/${uid}/availability`,{headers:owner.headers})).status()).toBe(404);
 }finally{await staff.context.close();}
});
