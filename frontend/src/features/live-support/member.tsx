import {router} from 'expo-router';
import {useEffect,useRef,useState} from 'react';
import {Text,View} from 'react-native';
import {AppButton} from '@/components/app-button';
import {EmptyState,ProductCard,ProductHeader,ProductPage,ProductSection} from '@/components/product-ui';
import {apiRequest} from '@/lib/api/client';
import {useLanguage} from '@/localization/language-provider';
import {Listener} from '@/features/platform/types';
import {usePlatformData} from '@/features/platform/data-provider';
import {colors,spacing} from '@/theme/tokens';
import {useForegroundPoll} from './foreground';
import {isOpen,LiveRequest,newClientId,remainingSeconds,statusCopy} from './policy';
import {Alternatives,Boundaries,supportError} from './ui';
export function LiveMember(){
 const {language,isRtl}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;const {refresh}=usePlatformData();
 const [listeners,setListeners]=useState<Listener[]>([]),[enabled,setEnabled]=useState(false),[loading,setLoading]=useState(true),[offerSeconds,setOfferSeconds]=useState(90),[busy,setBusy]=useState(false),[error,setError]=useState('');
 const [request,setRequest]=useState<LiveRequest|null>(null),[confirm,setConfirm]=useState(false),[target,setTarget]=useState<string|null>(null),[now,setNow]=useState(()=>Date.now()),[observedAt,setObservedAt]=useState(0);
 const requestRef=useRef<LiveRequest|null>(null),restored=useRef(false),key=useRef<string|null>(null);
 const save=(value:LiveRequest)=>{setObservedAt(Date.now());requestRef.current=value;setRequest(value);};
 const load=async(isCurrent:()=>boolean)=>{try{
  if(requestRef.current&&isOpen(requestRef.current.status)){const updated=await apiRequest<LiveRequest>(`/api/v1/listener-support/requests/${requestRef.current.id}`);if(!isCurrent())return;save(updated);}
  else if(!restored.current){const mine=await apiRequest<LiveRequest[]>('/api/v1/listener-support/requests/me');if(!isCurrent())return;restored.current=true;const latest=mine.find(r=>!r.listenerSide);if(latest)save(latest);}
  if(!requestRef.current||!isOpen(requestRef.current.status)){const result=await apiRequest<{enabled:boolean;listeners:Listener[];offerSeconds:number}>('/api/v1/listener-support/availability');if(!isCurrent())return;setListeners(result.listeners);setEnabled(result.enabled);setOfferSeconds(result.offerSeconds);}
  setError('');
 }catch(caught){if(isCurrent())setError(supportError(caught,language==='fa'));}finally{if(isCurrent())setLoading(false);}};
 const refreshSupport=useForegroundPoll(load,2500);
 useEffect(()=>{const timer=setInterval(()=>setNow(Date.now()),1000);return()=>clearInterval(timer);},[]);
 const perform=async(action:string)=>{if(!request||busy)return;setBusy(true);setError('');try{
  const value=await apiRequest<LiveRequest>(`/api/v1/listener-support/requests/${request.id}/${action}`,{method:'POST'});save(value);
  if(action==='activate'&&value.status==='ACTIVE'&&value.conversationId){await refresh();router.push({pathname:'/chat/[id]',params:{id:value.conversationId,liveRequestId:value.id}});}
 }catch(caught){setError(supportError(caught,language==='fa'));}finally{setBusy(false);}};
 const create=async()=>{if(busy)return;setBusy(true);setError('');key.current??=newClientId();try{save(await apiRequest<LiveRequest>('/api/v1/listener-support/requests',{method:'POST',body:JSON.stringify({clientRequestId:key.current,listenerId:target})}));key.current=null;setConfirm(false);}catch(caught){setError(supportError(caught,language==='fa'));}finally{setBusy(false);}};
 const seconds=request?remainingSeconds(request.status==='ACCEPTED'?request.activationExpiresAt:request.expiresAt,request.serverTime,observedAt,now):0;
 return <ProductPage><View style={{gap:spacing.md,direction:isRtl?'rtl':'ltr'}}><ProductHeader backLabel={tr('Go back','بازگشت')} title={tr('Live peer listener support','حمایت زندهٔ شنوندهٔ همتا')} subtitle={tr('Request a short-lived offer. No conversation opens until real acceptance and your explicit entry.','یک درخواست کوتاه‌مدت بفرستید؛ گفت‌وگو فقط پس از پذیرش واقعی و ورود اختیاری شما باز می‌شود.')} /><Boundaries/>
 {!!error&&<Text accessibilityRole="alert" style={{color:colors.danger}}>{error}</Text>}
 {request?<><ProductCard><Text accessibilityRole="header">{request.peerName}</Text><Text accessibilityLiveRegion="polite">{['REQUESTED','ACCEPTED'].includes(request.status)&&seconds===0?tr('Deadline reached — refresh to confirm the server state','مهلت نمایش‌داده‌شده تمام شد؛ وضعیت سرور را دوباره بررسی کنید'):statusCopy[request.status][language==='fa'?1:0]}</Text>{['REQUESTED','ACCEPTED'].includes(request.status)&&<Text>{tr('Server deadline countdown','زمان باقی‌مانده تا مهلت سرور')}: {seconds}</Text>}</ProductCard>
 {request.status==='REQUESTED'&&<AppButton label={tr('Cancel request','لغو درخواست')} loading={busy} onPress={()=>void perform('cancel')}/>}
 {request.status==='ACCEPTED'&&<><AppButton label={tr('Enter accepted conversation','ورود به گفت‌وگوی پذیرفته‌شده')} loading={busy} disabled={seconds===0} onPress={()=>void perform('activate')}/><AppButton variant="secondary" label={tr('Cancel request','لغو درخواست')} disabled={busy} onPress={()=>void perform('cancel')}/></>}
 {['ACTIVE','INTERRUPTED'].includes(request.status)&&request.conversationId&&<AppButton label={tr('Open support conversation','بازکردن گفت‌وگوی حمایت')} onPress={()=>router.push({pathname:'/chat/[id]',params:{id:request.conversationId!,liveRequestId:request.id}})}/>}
 {!isOpen(request.status)&&<AppButton label={tr('Return to listener discovery','بازگشت به فهرست شنوندگان')} onPress={()=>{requestRef.current=null;setRequest(null);key.current=null;void refreshSupport();}}/>}</>:
 confirm?<><ProductCard><Text>{tr(`A listener has up to ${offerSeconds} seconds to accept. There is no waiting queue and no automatic retry. You can cancel.`,`شنونده تا ${offerSeconds} ثانیه برای پذیرش فرصت دارد. صف انتظار یا تلاش مجدد خودکار وجود ندارد و می‌توانید لغو کنید.`)}</Text></ProductCard><AppButton label={tr('Request live conversation','درخواست گفت‌وگوی زنده')} loading={busy} onPress={()=>void create()}/><AppButton variant="secondary" label={tr('Back to availability','بازگشت به وضعیت دسترسی')} disabled={busy} onPress={()=>setConfirm(false)}/></>:
 <><AppButton label={tr('Check current availability','بررسی دسترسی فعلی')} loading={loading} onPress={()=>void refreshSupport()}/>{!loading&&!listeners.length&&<EmptyState icon="support_agent" title={tr(enabled?'No live listener is currently available':'Live support is not currently enabled',enabled?'اکنون شنوندهٔ زنده‌ای در دسترس نیست':'حمایت زنده در حال حاضر فعال نیست')} body={tr('No chat or waiting-list entry was created. You can check again or choose another path.','گفت‌وگو یا موردی در صف انتظار ایجاد نشده است. دوباره بررسی کنید یا راه دیگری را انتخاب کنید.')}/>}
 {!!listeners.length&&<ProductSection title={tr('Listeners accepting requests','شنوندگان آمادهٔ دریافت درخواست')}>{listeners.map(listener=><ProductCard key={listener.id}><Text accessibilityRole="header">{listener.displayName}</Text><Text>{listener.listeningStyle}</Text><AppButton label={tr(`Review request to ${listener.displayName}`,`بررسی درخواست به ${listener.displayName}`)} onPress={()=>{setTarget(listener.id);setConfirm(true);}}/></ProductCard>)}<AppButton variant="secondary" label={tr('Review request to an available listener','بررسی درخواست به یک شنوندهٔ آماده')} onPress={()=>{setTarget(null);setConfirm(true);}}/></ProductSection>}</>}
 <Alternatives/><AppButton variant="secondary" label={tr('Listener workspace','محیط کار شنونده')} onPress={()=>router.push({pathname:'/listener/[action]',params:{action:'workspace'}})}/></View></ProductPage>;
}
