import {expect} from '@playwright/test';
import {api,account} from './phase-l-helpers.mjs';
import {moderator} from './phase-m-helpers.mjs';
import {browserApi} from './phase-o-helpers.mjs';
export async function clinic(browser,request){
 const pro=await account(request),patient=await account(request),staff=await moderator(browser,request,{educationalAdmin:true});
 const uid=(await(await request.get(`${api}/auth/me`,{headers:pro.headers})).json()).userId;
 let r=await request.post(`${api}/professional-applications`,{headers:pro.headers,data:{professionalType:'COUNSELOR',title:'Synthetic counselor',specialization:'Synthetic education',bio:'Synthetic isolated clinician fixture, no real clinical service.',yearsOfExperience:1,specialties:['Synthetic'],languages:['en'],country:'IR',licenseNumber:'SYNTHETIC-P2-LICENSE',consultationModes:['TEXT'],documentNames:['qa.pdf']}});expect(r.status()).toBe(201);const application=await r.json();
 r=await request.post(`${api}/professional-applications/${application.id}/documents`,{headers:pro.headers,multipart:{file:{name:'qa.pdf',mimeType:'application/pdf',buffer:Buffer.from('%PDF-1.4\nSynthetic clinical QA credential only.\n%%EOF')}}});expect(r.status()).toBe(201);const doc=await r.json();
 expect((await browserApi(staff.page,`/api/v1/admin/professional-documents/${doc.id}/scan`,'POST',{reason:'Synthetic controlled scanner fixture'})).status).toBe(200);
 const detail=await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${application.id}`);
 expect((await browserApi(staff.page,`/api/v1/admin/professional-qualifications/${application.id}/decision`,'POST',{revision:detail.data.qualification_revision,state:'VERIFIED',reason:'Synthetic independent human verification fixture only',category:'COUNSELOR',verifiedName:'Synthetic clinician',issuer:'Synthetic QA issuer',licenseIdentifier:'SYNTHETIC-P2-LICENSE',method:'OFFICIAL_REGISTER_HUMAN',source:'https://my.pcoiran.ir/member/',evidence:'SYNTHETIC-QA-NOT-REAL-INDEPENDENT-VERIFICATION',validFrom:new Date(Date.now()-60000).toISOString(),validUntil:new Date(Date.now()+86400000).toISOString(),recheckAt:new Date(Date.now()+3600000).toISOString(),scopes:[{country:'IR',mode:'TEXT'}],verifiedSpecialties:['Synthetic scope']})).status).toBe(204);
 for(const purpose of ['PLATFORM','PROFESSIONAL_SCOPE','PATIENT_IDENTITY','DATA_PROCESSING','TERMS','OPERATIONS'])expect((await browserApi(staff.page,'/api/v1/admin/clinic-release-approvals','POST',{country:'IR',mode:'TEXT',purpose,evidence:'SYNTHETIC-QA-NOT-A-REAL-PLATFORM-APPROVAL',expiresAt:new Date(Date.now()+3600000).toISOString()})).status).toBe(204);
 const policy=await browserApi(staff.page,'/api/v1/clinic/policies','POST',{version:'live-qa-'+crypto.randomUUID(),commissionBps:0,providerFeeUnits:0,terms:'Synthetic explicit free consultation agreement',refundPolicy:'Synthetic no-show and cancellation review policy'});expect(policy.status).toBe(200);
 r=await request.post(`${api}/professional/calendar/slots`,{headers:pro.headers,data:{startsAt:new Date(Date.now()+7200000).toISOString(),endsAt:new Date(Date.now()+9000000).toISOString(),mode:'TEXT',timezone:'Asia/Tehran',priceUnits:0,publish:true}});expect(r.status()).toBe(200);const slot=(await r.json()).id;
 expect((await request.put(`${api}/clinic/slots/${slot}/offer`,{headers:pro.headers,data:{policyId:policy.data.id,approvalRequired:false}})).status()).toBe(204);
 return {pro,patient,staff,professionalId:uid,slot,policyId:policy.data.id};
}
