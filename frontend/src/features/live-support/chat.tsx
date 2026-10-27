import {router} from 'expo-router';
import {useRef,useState} from 'react';
import {Text,TextInput,View} from 'react-native';
import {AppButton} from '@/components/app-button';
import {ProductCard,ProductHeader,ProductPage} from '@/components/product-ui';
import {apiRequest,ApiError} from '@/lib/api/client';
import {useLanguage} from '@/localization/language-provider';
import {ChatMessage} from '@/features/platform/types';
import {colors,radius,spacing} from '@/theme/tokens';
import {useForegroundPoll} from './foreground';
import {canSend,LiveRequest,newClientId,statusCopy} from './policy';
import {Alternatives,Boundaries,supportError} from './ui';
export function LiveChat({id}:{id:string}){
 const {language,isRtl}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [session,setSession]=useState<LiveRequest|null>(null),[messages,setMessages]=useState<ChatMessage[]>([]),[text,setText]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false),[confirmed,setConfirmed]=useState(false);
 const lastPulse=useRef(0),retry=useRef<{id:string;body:string}|null>(null);
 const load=async(isCurrent:()=>boolean)=>{try{
  let value=await apiRequest<LiveRequest>(`/api/v1/listener-support/conversations/${id}`);if(!isCurrent())return;
  if(['ACTIVE','INTERRUPTED'].includes(value.status)&&Date.now()-lastPulse.current>=Math.max(5000,Math.floor(value.presenceSeconds/3)*1000)){lastPulse.current=Date.now();value=await apiRequest<LiveRequest>(`/api/v1/listener-support/requests/${value.id}/heartbeat`,{method:'POST'});if(!isCurrent())return;}
  setSession(value);
  if(value.status==='ENDED_SAFETY'){setConfirmed(false);setMessages([]);setText('');return;}
  const loaded=await apiRequest<ChatMessage[]>(`/api/v1/conversations/${id}/messages`);if(!isCurrent())return;setMessages(loaded);setConfirmed(true);setError('');
 }catch(caught){if(!isCurrent())return;setConfirmed(false);if(caught instanceof ApiError&&[401,403,404].includes(caught.status)){setMessages([]);setText('');setSession(null);}setError(supportError(caught,language==='fa'));}};
 const refresh=useForegroundPoll(load,3000,()=>setConfirmed(false));
 const send=async()=>{const body=text.trim();if(!body||busy||!confirmed||!canSend(session?.status))return;setBusy(true);setError('');if(!retry.current||retry.current.body!==body)retry.current={id:newClientId(),body};try{
  const message=await apiRequest<ChatMessage>(`/api/v1/conversations/${id}/messages`,{method:'POST',body:JSON.stringify({body,clientMessageId:retry.current.id})});
  setMessages(current=>current.some(m=>m.id===message.id)?current:[...current,message]);retry.current=null;setText('');
 }catch(caught){setError(supportError(caught,language==='fa'));await refresh();}finally{setBusy(false);}};
 const end=async()=>{if(!session||busy)return;setBusy(true);try{setSession(await apiRequest<LiveRequest>(`/api/v1/listener-support/sessions/${session.id}/end`,{method:'POST'}));setText('');await refresh();}catch(caught){setError(supportError(caught,language==='fa'));}finally{setBusy(false);}};
 const block=async()=>{if(!session||busy)return;setBusy(true);try{await apiRequest(`/api/v1/social/blocks/${session.peerId}`,{method:'PUT'});setMessages([]);setText('');await refresh();}catch(caught){setError(supportError(caught,language==='fa'));}finally{setBusy(false);}};
 return <ProductPage><View style={{gap:spacing.md,direction:isRtl?'rtl':'ltr'}}><ProductHeader backLabel={tr('Go back','بازگشت')} title={session?.peerName??tr('Live support conversation','گفت‌وگوی حمایت زنده')} subtitle={tr('Current session state is verified by the server. Either participant can end the conversation.','وضعیت فعلی جلسه در سرور بررسی می‌شود. هر یک از دو نفر می‌تواند گفت‌وگو را پایان دهد.')} /><Boundaries/>
 <Text role="status" accessibilityLabel={tr('Live support session status','وضعیت جلسهٔ حمایت زنده')} accessibilityLiveRegion="polite">{session?statusCopy[session.status][language==='fa'?1:0]:tr('Checking authorized session…','در حال بررسی مجوز جلسه…')}</Text>
 {!!error&&<Text accessibilityRole="alert" style={{color:colors.danger}}>{error}</Text>}
 <AppButton variant="secondary" label={tr('Refresh session','بازخوانی جلسه')} onPress={()=>void refresh()}/>
 {messages.map(message=><ProductCard key={message.id}><Text>{message.system?tr('Service boundary','محدودهٔ سرویس'):message.mine?tr('You','شما'):message.senderName}</Text><Text selectable>{message.body}</Text></ProductCard>)}
 <TextInput accessibilityLabel={tr('Live support message','پیام حمایت زنده')} multiline maxLength={5000} editable={!busy&&confirmed&&canSend(session?.status)} value={text} onChangeText={setText} placeholder={tr('Write a supportive message…','یک پیام حمایتی بنویسید…')} style={{minHeight:64,padding:spacing.md,borderWidth:1,borderColor:colors.border,borderRadius:radius.md,color:colors.ink,textAlign:isRtl?'right':'left'}}/>
 <AppButton label={tr('Send live message','ارسال پیام زنده')} disabled={!confirmed||!canSend(session?.status)||!text.trim()} loading={busy} onPress={()=>void send()}/>
 {session&&['ACTIVE','INTERRUPTED'].includes(session.status)&&<AppButton variant="secondary" label={tr('End support conversation','پایان گفت‌وگوی حمایت')} disabled={busy} onPress={()=>void end()}/>}
 {session&&<><AppButton variant="secondary" label={tr('Report conversation','گزارش گفت‌وگو')} onPress={()=>router.push({pathname:'/tools/[tool]',params:{tool:'report',targetType:'conversation',targetId:id}})}/><AppButton variant="secondary" label={tr('Block this participant','مسدودکردن این فرد')} disabled={busy} onPress={()=>void block()}/></>}
 <Alternatives/></View></ProductPage>;
}
