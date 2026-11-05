import {useRef,useState} from 'react';
import {Text,TextInput,View} from 'react-native';
import {router} from 'expo-router';
import {ProductPage,ProductHeader,ProductCard} from '@/components/product-ui';
import {AppButton} from '@/components/app-button';
import {useAuth} from '@/features/auth/auth-provider';
import {useLanguage} from '@/localization/language-provider';
import {useForegroundPoll} from '@/features/live-support/foreground';
import {apiRequest} from '@/lib/api/client';
import {tokenStorage} from '@/lib/storage/token-storage';
type Invitation={id:string;service:string;state:string;revision:number;notice_version:number;expires_at:string};
type Own={invitations:Invitation[];notice:{version?:number;title?:string;content?:string};runtimePilotConfigured:boolean;existingRightsRemainAvailable:boolean};
type Assessment={id:string;service:string;source_commit:string;country_code:string;revision:number;created_by:string};
type Evidence={id:string;criterion:string;state:string;kind:string;reference:string;author_id:string;reviewer_id:string|null;artifact_sha256:string};
type Detail={assessment:Assessment;state:string;missingCriteria:string[];remainingRisks:string[];evidence:Evidence[];runtimeActivationChanged:boolean};
const words:Record<string,[string,string]>={COMMUNITY:['Community and social','جامعه و ارتباط اجتماعی'],WELLBEING_EDUCATION:['Private self-care and education','خودمراقبتی خصوصی و آموزش'],LISTENERS:['Peer listeners','شنونده‌های همتا'],PROFESSIONAL_BOOKING:['Professional booking','نوبت متخصص'],CLINICAL_TEXT:['Clinical text','گفت‌وگوی متنی بالینی'],CLINICAL_MEDIA:['Clinical audio/video','صوت و تصویر بالینی'],REAL_PAYMENTS:['Real payments','پرداخت واقعی'],CLINICAL_RECORDS:['Clinical records','پروندهٔ بالینی'],NOT_ASSESSED:['Not assessed','ارزیابی نشده'],TECHNICALLY_READY:['Technical checks ready; other approvals required','آمادهٔ فنی؛ نیازمند سایر تأییدها'],APPROVAL_REQUIRED:['Approval required','نیازمند تأیید'],OPERATIONALLY_BLOCKED:['Operating prerequisites missing','پیش‌نیازهای عملیاتی آماده نیست'],NO_GO:['Not authorized for release','مجاز به انتشار نیست'],GO_RECOMMENDED:['Recommended; not activated','پیشنهاد شده؛ فعال نشده'],OWNER_APPROVED:['Owner approval recorded; runtime controls still apply','تأیید مالک ثبت شده؛ کنترل‌های اجرا همچنان لازم‌اند'],INVITED:['Invited','دعوت‌شده'],ACCEPTED:['Participation accepted','مشارکت پذیرفته‌شده'],OPTED_OUT:['Participation ended','مشارکت پایان‌یافته'],REVOKED:['Invitation revoked','دعوت لغو شده']};
export function PilotCenter(){const {user}=useAuth();return <Workspace key={String(user?.userId)+':'+String(user?.roles.join(','))}/>;}
function Workspace(){
 const {user}=useAuth();const {language,isRtl}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const label=(key:string)=>words[key]?.[language==='fa'?1:0]??key;
 const staff=!!user?.roles.includes('TRUST_PLATFORM'),owner=!!user?.roles.includes('PILOT_OWNER');
 const [own,setOwn]=useState<Own|null>(null),[matrix,setMatrix]=useState<{service:string;assessmentState:string}[]>([]),[rows,setRows]=useState<Assessment[]>([]),[detail,setDetail]=useState<Detail|null>(null);
 const [busy,setBusy]=useState(false),[error,setError]=useState(''),[consent,setConsent]=useState(false);
 const [service,setService]=useState('COMMUNITY'),[country,setCountry]=useState('IR'),[source,setSource]=useState(''),[evidence,setEvidence]=useState(''),[target,setTarget]=useState('');
 const generation=useRef(0);
 const clear=()=>{generation.current++;setOwn(null);setMatrix([]);setRows([]);setDetail(null);setConsent(false);setEvidence('');setTarget('');setBusy(false);};
 const perform=async(action:(valid:()=>boolean)=>Promise<void>)=>{
  if(busy)return;const current=generation.current,epoch=tokenStorage.generation(),valid=()=>current===generation.current&&epoch===tokenStorage.generation();setBusy(true);setError('');
  try{await action(valid);}catch(e){if(valid()){setOwn(null);setRows([]);setDetail(null);setConsent(false);setError(e instanceof Error?e.message:tr('Please retry.','دوباره تلاش کنید.'));}}finally{if(valid())setBusy(false);}
 };
 const refresh=async(valid:()=>boolean)=>{const [me,status]=await Promise.all([apiRequest<Own>('/api/v1/pilot/me'),apiRequest<{service:string;assessmentState:string}[]>(`/api/v1/public/pilot-readiness?country=${encodeURIComponent(country)}`)]);const assessments=staff?await apiRequest<Assessment[]>('/api/v1/pilot/assessments'):[];const selected=detail&&assessments.some(item=>item.id===detail.assessment.id)?await apiRequest<Detail>(`/api/v1/pilot/assessments/${detail.assessment.id}`):null;if(valid()){setOwn(me);setMatrix(status);setRows(assessments);setDetail(selected);setConsent(false);}};
 const inspect=async(id:string,valid:()=>boolean)=>{setDetail(null);const item=await apiRequest<Detail>(`/api/v1/pilot/assessments/${id}`);if(valid())setDetail(item);};
 const send=async(path:string,body:unknown,valid:()=>boolean)=>{await apiRequest('/api/v1/pilot/'+path,{method:'POST',body:JSON.stringify(body)});if(valid())await refresh(valid);};
 useForegroundPoll(async current=>{if(!current())return;await perform(async valid=>{await refresh(()=>current()&&valid());});},30000,clear);
 return <ProductPage><View style={{gap:14,direction:isRtl?'rtl':'ltr'}}>
  <ProductHeader title={tr('Controlled pilot','پایلوت کنترل‌شده')} subtitle={tr('Explicit invitations,optional participation,and separate service approval.','دعوت صریح، مشارکت اختیاری و تأیید مستقل هر خدمت.')}/>
  <Text>{tr('Listeners provide nonclinical peer support. Clinical care requires separately authorized professionals and services. GreenOcean is not an emergency service.','شنونده‌ها حمایت همتای غیر‌بالینی ارائه می‌کنند. خدمات بالینی به متخصص و مجوزهای مستقل نیاز دارند. دریای سبز خدمت اضطراری نیست.')}</Text>
  <Text>{tr('Assessment approval does not activate a service or promise immediate help. The relevant service shows current availability.','تأیید ارزیابی، خدمت را فعال نمی‌کند و وعدهٔ کمک فوری نیست. وضعیت فعلی را در همان خدمت ببینید.')}</Text>
  <AppButton label={tr('Refresh pilot status','بازخوانی وضعیت پایلوت')} disabled={busy} onPress={()=>void perform(refresh)}/>
  {!!error&&<Text accessibilityRole="alert">{error}</Text>}
  {own&&!own.runtimePilotConfigured&&<Text>{tr('Pilot runtime is not enabled. No visitor is automatically enrolled.','اجرای پایلوت فعال نیست. هیچ بازدیدکننده‌ای خودکار عضو نمی‌شود.')}</Text>}
  {matrix.map(row=><ProductCard key={row.service}><Text>{label(row.service)}</Text><Text>{label(row.assessmentState)}</Text></ProductCard>)}
  {!!own?.notice.content&&<ProductCard><Text>{own.notice.title}</Text><Text>{own.notice.content}</Text><AppButton variant="secondary" label={(consent?'✓ ':'')+tr('I reviewed the current optional participation notice','اطلاعیهٔ فعلی مشارکت اختیاری را خواندم')} onPress={()=>setConsent(!consent)}/></ProductCard>}
  {own?.invitations.map(invite=><ProductCard key={invite.id}><Text>{label(invite.service)} · {label(invite.state)}</Text><Text>{new Date(invite.expires_at).toLocaleString(language)}</Text>
   {invite.state==='INVITED'&&<AppButton label={tr('Accept this service invitation','پذیرش دعوت همین خدمت')} disabled={busy||!consent||own.notice.version!==invite.notice_version} onPress={()=>void perform(valid=>send(`invitations/${invite.id}/choice`,{revision:invite.revision,accept:true},valid))}/>}
   {['INVITED','ACCEPTED'].includes(invite.state)&&<AppButton variant="secondary" label={tr('Leave this pilot service','خروج از پایلوت این خدمت')} disabled={busy} onPress={()=>void perform(valid=>send(`invitations/${invite.id}/choice`,{revision:invite.revision,accept:false},valid))}/>}
  </ProductCard>)}
  <Text>{tr('Leaving stops new pilot participation. Your account rights,existing private records,bookings and financial obligations have separate controls. End or cancel existing sessions/appointments through their service controls. No new sensitive satisfaction data is collected here.','خروج، مشارکت جدید را متوقف می‌کند. حقوق حساب، اطلاعات خصوصی موجود، نوبت‌ها و تعهدات مالی کنترل‌های جدا دارند. جلسه و نوبت موجود را از کنترل همان خدمت پایان دهید یا لغو کنید. اینجا دادهٔ حساس تازه‌ای برای سنجش رضایت جمع‌آوری نمی‌شود.')}</Text>
  <AppButton variant="secondary" label={tr('Support and feedback','پشتیبانی و بازخورد')} onPress={()=>router.push('/support' as never)}/>
  <AppButton variant="secondary" label={tr('Privacy and account rights','حریم خصوصی و حقوق حساب')} onPress={()=>router.push('/privacy' as never)}/>
  {staff&&<ProductCard><Text>{tr('Staff release assessment','ارزیابی انتشار کارکنان')}</Text>
   <TextInput accessibilityLabel={tr("Pilot service","خدمت پایلوت")} value={service} onChangeText={setService}/><TextInput accessibilityLabel={tr("Pilot country","کشور پایلوت")} value={country} onChangeText={setCountry}/><TextInput accessibilityLabel={tr("Exact pilot source commit","کامیت دقیق نسخهٔ پایلوت")} value={source} onChangeText={setSource}/>
   <AppButton label={tr('Create source-specific assessment','ساخت ارزیابی برای نسخهٔ دقیق')} disabled={busy||! /^[a-f0-9]{40}$/.test(source)} onPress={()=>void perform(valid=>send('assessments',{service,countryCode:country,sourceCommit:source},valid))}/>
   <AppButton variant="secondary" label={tr('Verify security key','تأیید کلید امنیتی')} onPress={()=>router.push('/tools/account-security' as never)}/>
   {rows.map(row=><AppButton key={row.id} variant="secondary" label={label(row.service)+' · '+row.id} disabled={busy} onPress={()=>void perform(valid=>inspect(row.id,valid))}/>)}
  </ProductCard>}
  {staff&&detail&&<ProductCard><Text>{label(detail.assessment.service)} · {label(detail.state)}</Text><Text>{detail.assessment.source_commit}</Text><Text>{tr('Remaining risks: ','ریسک‌های باقی‌مانده: ')}{detail.remainingRisks.join(', ')}</Text>
   <Text>{tr('Record actual evidence and exact tests; synthetic evidence never closes a launch requirement. Reviewers must independently inspect the referenced artifact and digest.','شواهد واقعی و آزمون‌های دقیق را ثبت کنید؛ دادهٔ مصنوعی الزام راه‌اندازی را نمی‌بندد. بازبین باید مدرک و هش ارجاع‌شده را مستقل بررسی کند.')}</Text>
   <TextInput accessibilityLabel={tr('Release evidence JSON','شواهد انتشار در قالب JSON')} value={evidence} onChangeText={setEvidence} multiline maxLength={20000} style={{minHeight:120,borderWidth:1,padding:8}}/>
   <AppButton label={tr('Record evidence for independent review','ثبت شواهد برای بازبینی مستقل')} disabled={busy||!evidence.trim()} onPress={()=>void perform(valid=>send(`assessments/${detail.assessment.id}/evidence`,{...JSON.parse(evidence),revision:detail.assessment.revision},valid))}/>
   {detail.evidence.map(item=><View key={item.id} style={{gap:8}}><Text>{item.criterion} · {item.state} · {item.kind}</Text><Text>{item.reference}</Text><Text>{item.artifact_sha256}</Text>
    {!item.reviewer_id&&<AppButton label={tr('Independently review evidence','بازبینی مستقل شواهد')} disabled={busy||item.author_id===user?.userId} onPress={()=>void perform(valid=>send(`assessments/${detail.assessment.id}/evidence/${item.id}/review`,{revision:detail.assessment.revision},valid))}/>}
   </View>)}
   {owner&&<AppButton label={tr('Record independent owner approval','ثبت تأیید مستقل مالک')} disabled={busy||detail.state!=='GO_RECOMMENDED'||detail.assessment.created_by===user?.userId} onPress={()=>void perform(valid=>send(`assessments/${detail.assessment.id}/owner-approval`,{revision:detail.assessment.revision},valid))}/>}
   <AppButton variant="secondary" label={tr('Stop this assessment','توقف این ارزیابی')} disabled={busy} onPress={()=>void perform(valid=>send(`assessments/${detail.assessment.id}/revoke`,{},valid))}/>
   {owner&&<><TextInput accessibilityLabel={tr('Explicit eligible participant account ID','شناسهٔ حساب واجد شرایط برای دعوت صریح')} value={target} onChangeText={setTarget}/><AppButton label={tr('Create explicit service invitation','ساخت دعوت صریح همین خدمت')} disabled={busy||detail.state!=='OWNER_APPROVED'||! /^[a-f0-9-]{36}$/.test(target)} onPress={()=>void perform(valid=>send(`assessments/${detail.assessment.id}/invitations`,{userId:target},valid))}/></>}
  </ProductCard>}
 </View></ProductPage>;
}
