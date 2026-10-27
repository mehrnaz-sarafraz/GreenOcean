import {router} from 'expo-router';
import {useEffect,useRef,useState} from 'react';
import {Text,View} from 'react-native';
import {AppButton} from '@/components/app-button';
import {ProductCard,ProductHeader,ProductPage} from '@/components/product-ui';
import {apiRequest} from '@/lib/api/client';
import {useLanguage} from '@/localization/language-provider';
import {colors,spacing} from '@/theme/tokens';
import {Availability,availabilityCopy,LiveRequest,statusCopy,remainingSeconds} from './policy';
import {useForegroundPoll} from './foreground';
import {Alternatives,Boundaries,supportError} from './ui';
export function ListenerWorkspace(){
 const {language,isRtl}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [availability,setAvailability]=useState<Availability|null>(null),[offer,setOffer]=useState<LiveRequest|null>(null),[error,setError]=useState(''),[busy,setBusy]=useState(false),[now,setNow]=useState(()=>Date.now()),[observedAt,setObservedAt]=useState(0);
 const lease=useRef<string|null>(null),lastBeat=useRef(0);
 const storeAvailability=(value:Availability)=>{setAvailability(value);lease.current=value.leaseId;};
 const leave=()=>{const current=lease.current;lease.current=null;if(current)void apiRequest('/api/v1/listeners/me/availability',{method:'PUT',body:JSON.stringify({ready:false,leaseId:current})}).catch(()=>{});};
 const load=async()=>{try{
  const value=await apiRequest<Availability>('/api/v1/listeners/me/availability');storeAvailability(value);
  if(!value.enabled||value.state==='UNAVAILABLE'){setOffer(null);return;}
  const incoming=await apiRequest<LiveRequest[]>('/api/v1/listener-support/incoming');if(incoming[0])setOffer(incoming[0]);else if(offer&&['REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED'].includes(offer.status))setOffer(await apiRequest<LiveRequest>(`/api/v1/listener-support/requests/${offer.id}`));setObservedAt(Date.now());
  if(Date.now()-lastBeat.current>=value.heartbeatSeconds*1000){lastBeat.current=Date.now();if(lease.current&&['AVAILABLE_FOR_REQUESTS','BUSY'].includes(value.state))storeAvailability(await apiRequest<Availability>('/api/v1/listeners/me/heartbeat',{method:'POST',body:JSON.stringify({leaseId:lease.current})}));if(incoming[0]&&['ACCEPTED','ACTIVE','INTERRUPTED'].includes(incoming[0].status))setOffer(await apiRequest<LiveRequest>(`/api/v1/listener-support/requests/${incoming[0].id}/heartbeat`,{method:'POST'}));}
  setError('');
 }catch(caught){setOffer(null);setError(supportError(caught,language==='fa'));}};
 useForegroundPoll(load,2500,leave);
 useEffect(()=>{const timer=setInterval(()=>setNow(Date.now()),1000);return()=>clearInterval(timer);},[]);
 const ready=async(value:boolean)=>{if(busy)return;setBusy(true);try{storeAvailability(await apiRequest<Availability>('/api/v1/listeners/me/availability',{method:'PUT',body:JSON.stringify({ready:value,leaseId:lease.current})}));setError('');await load();}catch(caught){setError(supportError(caught,language==='fa'));}finally{setBusy(false);}};
 const respond=async(action:'accept'|'decline')=>{if(!offer||busy)return;setBusy(true);try{setOffer(await apiRequest<LiveRequest>(`/api/v1/listener-support/requests/${offer.id}/${action}`,{method:'POST'}));setObservedAt(Date.now());setError('');}catch(caught){setError(supportError(caught,language==='fa'));await load();}finally{setBusy(false);}};
 return <ProductPage><View style={{gap:spacing.md,direction:isRtl?'rtl':'ltr'}}><ProductHeader backLabel={tr('Go back','بازگشت')} title={tr('Listener workspace','محیط کار شنونده')} subtitle={tr('Live readiness requires independent practice approval and a recent authenticated foreground heartbeat.','آمادگی زنده به تأیید مستقل تمرین و ضربان معتبرِ اخیر در پیش‌زمینه نیاز دارد.')} /><Boundaries/>
 {!!error&&<Text accessibilityRole="alert" style={{color:colors.danger}}>{error}</Text>}
 <Text accessibilityLiveRegion="polite">{availability?availabilityCopy[availability.state][language==='fa'?1:0]:tr('Checking readiness…','در حال بررسی آمادگی…')}</Text>
 <AppButton label={tr('Become available on this device','آماده‌شدن در این دستگاه')} disabled={busy||!availability?.enabled||availability.state==='BUSY'} onPress={()=>void ready(true)}/>
 <AppButton variant="secondary" label={tr('Stop accepting requests','توقف پذیرش درخواست')} loading={busy} disabled={!availability?.leaseId} onPress={()=>void ready(false)}/>
 <AppButton variant="secondary" label={tr('Refresh listener state','بازخوانی وضعیت شنونده')} onPress={()=>void load()}/>
 {offer&&<ProductCard><Text accessibilityRole="header">{offer.peerName}</Text><Text accessibilityLiveRegion="polite">{statusCopy[offer.status][language==='fa'?1:0]}</Text><Text>{tr('Server offer deadline','مهلت درخواست سرور')}: {remainingSeconds(offer.status==='ACCEPTED'?offer.activationExpiresAt:offer.expiresAt,offer.serverTime,observedAt,now)}</Text>{offer.status==='REQUESTED'&&<><AppButton label={tr('Accept live request','پذیرش درخواست زنده')} disabled={remainingSeconds(offer.expiresAt,offer.serverTime,observedAt,now)===0} loading={busy} onPress={()=>void respond('accept')}/><AppButton variant="secondary" label={tr('Decline request','رد درخواست')} disabled={busy} onPress={()=>void respond('decline')}/></>}{offer.status==='ACCEPTED'&&<Text>{tr('Waiting for the member to choose to enter. No chat exists yet.','منتظر ورود اختیاری عضو هستیم. هنوز گفت‌وگویی ایجاد نشده است.')}</Text>}{['ACTIVE','INTERRUPTED'].includes(offer.status)&&offer.conversationId&&<AppButton label={tr('Open support conversation','بازکردن گفت‌وگوی حمایت')} onPress={()=>router.push({pathname:'/chat/[id]',params:{id:offer.conversationId!,liveRequestId:offer.id}})}/>}</ProductCard>}
 <Alternatives/></View></ProductPage>;
}
