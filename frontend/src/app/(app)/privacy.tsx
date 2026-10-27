import { router } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { Screen } from '@/components/screen';
import { AppButton } from '@/components/app-button';
import { useLanguage } from '@/localization/language-provider';
import { newClientId } from '@/features/live-support/policy';
import { apiRequest } from '@/lib/api/client';
import { colors,spacing } from '@/theme/tokens';
type Choice={purpose:string;decision:string;grantAvailable:boolean;noticeId:string|null};
type Rights={id:string;kind:string;status:string;resolution_reference:string|null};
type State={choices:Choice[];manualIntakeAvailable:boolean;responseTargetHours:number|null;rightsRequests:Rights[];noticeReceipts:unknown[]};
export default function PrivacyGovernance(){
 const {language,isRtl}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const kinds=['ACCESS','RECTIFICATION','ERASURE','RESTRICTION','OBJECTION','PORTABILITY','HUMAN_REVIEW'] as const;
 const [retryKeys,setRetryKeys]=useState<Record<string,string>>(()=>Object.fromEntries(['ACCESS','RECTIFICATION','ERASURE','RESTRICTION','OBJECTION','PORTABILITY','HUMAN_REVIEW'].map(k=>[k,newClientId()])));
 const [saved,setSaved]=useState<State|null>(null);const [error,setError]=useState('');const [busy,setBusy]=useState(false);
 const load=useCallback(async()=>{try{setSaved(await apiRequest<State>('/api/v1/account/privacy'));setError('');}catch(e){setError(e instanceof Error?e.message:'Could not load privacy controls');}},[]);
 useEffect(()=>{const timer=setTimeout(()=>void load(),0);return()=>clearTimeout(timer);},[load]);
 async function decide(choice:Choice,decision:string){if(busy)return;setBusy(true);setError('');try{setSaved(await apiRequest<State>('/api/v1/account/privacy/decisions',{method:'POST',body:JSON.stringify({purpose:choice.purpose,decision,noticeId:decision==='GRANTED'?choice.noticeId:null})}));}catch(e){setError(e instanceof Error?e.message:tr('Not saved. Try again.','ذخیره نشد. دوباره تلاش کنید.'));}finally{setBusy(false);}}
 async function receipt(){if(busy)return;setBusy(true);setError('');try{const notice=await apiRequest<{id:string}>('/api/v1/policies/privacy',{authenticated:false});await apiRequest('/api/v1/account/privacy/receipts',{method:'POST',body:JSON.stringify({noticeId:notice.id})});await load();}catch(e){setError(e instanceof Error?e.message:tr('Notice unavailable.','اطلاعیه در دسترس نیست.'));}finally{setBusy(false);}}
 async function requestRight(kind:string){if(busy)return;setBusy(true);setError('');try{await apiRequest('/api/v1/account/privacy/rights',{method:'POST',body:JSON.stringify({kind,idempotencyKey:retryKeys[kind]})});setRetryKeys(keys=>({...keys,[kind]:newClientId()}));await load();}catch(e){setError(e instanceof Error?e.message:tr('Request was not received.','درخواست دریافت نشد.'));}finally{setBusy(false);}}
 const names:Record<string,string>={SUPPORT_TOPICS:tr('Support topic processing','پردازش موضوعات حمایت'),OPTIONAL_ANALYTICS:tr('Optional analytics','تحلیل اختیاری'),OPTIONAL_COMMUNICATIONS:tr('Optional communications','ارتباطات اختیاری')};
 return <Screen scroll><View style={{gap:spacing.md,direction:isRtl?'rtl':'ltr'}}>
  <Text accessibilityRole="header" style={{fontSize:26,fontWeight:'800',color:colors.ink}}>{tr('Privacy, permissions & rights','حریم خصوصی، اجازه‌ها و حقوق داده')}</Text>
  <Text>{tr('Terms acceptance, receipt of privacy information and optional permissions are separate. Missing permission means no optional authorization. Declining does not close unrelated core features.','پذیرش شرایط، دریافت اطلاعیهٔ حریم خصوصی و اجازه‌های اختیاری جدا هستند. اجازهٔ ثبت‌نشده، رضایت محسوب نمی‌شود. ردکردن، قابلیت‌های اصلی نامرتبط را نمی‌بندد.')}</Text>
  {!!error&&<Text accessibilityRole="alert" style={{color:colors.danger}}>{error}</Text>}
  {!saved?<AppButton label={tr('Retry loading','دریافت دوباره')} onPress={()=>void load()}/>:<>
   {saved.choices.map(choice=><View key={choice.purpose} style={{gap:spacing.sm,padding:spacing.md,borderWidth:1,borderColor:colors.border,borderRadius:12}}>
    <Text accessibilityRole="header" style={{fontSize:18,fontWeight:'700'}}>{names[choice.purpose]}</Text><Text accessibilityLabel={names[choice.purpose]+' status'}>{choice.decision}</Text>
    {!choice.grantAvailable&&<Text>{tr('New authorization is unavailable pending approved purpose, notice and operational readiness.','اجازهٔ تازه تا تأیید هدف، اطلاعیه و آمادگی اجرایی در دسترس نیست.')}</Text>}
    <AppButton label={tr('Grant: ','اجازه دادن: ')+names[choice.purpose]} disabled={busy||!choice.grantAvailable} onPress={()=>void decide(choice,'GRANTED')}/>
    <AppButton label={tr('Decline: ','ردکردن: ')+names[choice.purpose]} variant="secondary" disabled={busy} onPress={()=>void decide(choice,'DENIED')}/>
    <AppButton label={tr('Withdraw: ','پس‌گرفتن: ')+names[choice.purpose]} variant="secondary" disabled={busy} onPress={()=>void decide(choice,'WITHDRAWN')}/>
   </View>)}
   <Text>{tr('Withdrawing support-topic permission clears saved and unmapped legacy topics. It does not erase posts, messages or account records; use the existing account erasure process for those.','پس‌گرفتن اجازهٔ موضوعات، موضوعات ذخیره‌شده و قدیمی را پاک می‌کند؛ پست، پیام و حساب را حذف نمی‌کند. برای آن‌ها از فرایند حذف حساب استفاده کنید.')}</Text>
   <AppButton label={tr('Read Privacy Notice','خواندن اطلاعیهٔ حریم خصوصی')} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'privacy'}})}/>
   <AppButton label={tr('Record receipt of current Privacy Notice','ثبت دریافت اطلاعیهٔ فعلی')} disabled={busy} variant="secondary" onPress={()=>void receipt()}/>
   <Text>{tr('Recorded notice versions: ','نسخه‌های دریافت‌شده: ')+saved.noticeReceipts.length}</Text>
   <AppButton label={tr('Export my data','خروجی داده‌های من')} variant="secondary" onPress={()=>router.push({pathname:'/(app)/tools/[tool]',params:{tool:'data-export'}})}/>
   <AppButton label={tr('Correct my profile','اصلاح پروفایل من')} variant="secondary" onPress={()=>router.push({pathname:'/(app)/tools/[tool]',params:{tool:'profile-edit'}})}/>
   <AppButton label={tr('Account deletion & security','حذف حساب و امنیت')} variant="secondary" onPress={()=>router.push({pathname:'/(app)/tools/[tool]',params:{tool:'account-security'}})}/>
   {!saved.manualIntakeAvailable?<Text>{tr('Human-review intake is not staffed yet. Existing self-service export, correction and deletion remain available; this does not waive applicable rights.','دریافت درخواست رسیدگی انسانی هنوز آماده نیست. خروجی، اصلاح و حذف حساب فعال‌اند؛ این وضعیت به‌معنای صرف‌نظر از حقوق قانونی نیست.')}</Text>:<View style={{gap:spacing.sm}}><Text>{tr('These are requests for human review, not automatic execution. Operational target in hours: ','این‌ها درخواست رسیدگی انسانی هستند، نه اجرای خودکار. هدف اجرایی پاسخ بر حسب ساعت: ')+saved.responseTargetHours}</Text>{kinds.map(kind=><AppButton key={kind} label={tr('Request human review: ','درخواست رسیدگی انسانی: ')+kind} disabled={busy||saved.rightsRequests.some(r=>r.kind===kind&&['RECEIVED','IN_REVIEW','NEEDS_INFORMATION'].includes(r.status))} onPress={()=>void requestRight(kind)}/>)}</View>}
   {saved.rightsRequests.map(r=><Text key={r.id}>{r.kind+' · '+r.status+' · '+r.id}</Text>)}
  </>}
 </View></Screen>;
}
