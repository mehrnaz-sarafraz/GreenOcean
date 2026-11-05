import {router} from 'expo-router';
import {useState} from 'react';
import {Text,View} from 'react-native';
import {Screen} from '@/components/screen';
import {AppButton} from '@/components/app-button';
import {apiRequest} from '@/lib/api/client';
import {useForegroundPoll} from '@/features/live-support/foreground';
import {useAuth} from '@/features/auth/auth-provider';
import {useLanguage} from '@/localization/language-provider';
type Request={id:string;status:'PENDING'|'DECLINED'|'CANCELLED';requester_id:string;request_target_id:string;display_name:string;username:string};
export default function MessageRequests(){
 const {user}=useAuth();const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [items,setItems]=useState<Request[]>([]);const [error,setError]=useState('');const [busy,setBusy]=useState(false);const [loaded,setLoaded]=useState(false);
 const refresh=useForegroundPoll(async current=>{try{const r=await apiRequest<Request[]>('/api/v1/conversations/requests');if(current()){setItems(r);setLoaded(true);setError('');}}catch(e){if(current()){setItems([]);setError(e instanceof Error?e.message:'Unavailable');}}},10000,()=>{setItems([]);setLoaded(false);});
 async function decide(id:string,action:'ACCEPT'|'DECLINE'|'CANCEL'){if(busy)return;setBusy(true);setError('');try{await apiRequest(`/api/v1/conversations/requests/${id}/decision`,{method:'POST',body:JSON.stringify({action})});await refresh();if(action==='ACCEPT')router.push({pathname:'/chat/[id]',params:{id}});}catch(e){setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 return <Screen scroll><View style={{gap:16,maxWidth:720,width:'100%',alignSelf:'center'}}><Text accessibilityRole="header">{tr('Message requests','درخواست‌های پیام')}</Text>
 <Text>{tr('No conversation messages are available before acceptance. Declining is voluntary. Requests do not create a listener session.','پیش از پذیرش، پیام‌های گفتگو در دسترس نیستند. رد درخواست اختیاری است. این درخواست‌ها جلسهٔ شنونده ایجاد نمی‌کنند.')}</Text>
 <AppButton label={tr('Message permissions','مجوزهای پیام')} variant="secondary" onPress={()=>router.push('/tools/message-requests' as never)}/>
 {!!error&&<Text accessibilityRole="alert">{error}</Text>}{!loaded&&!error&&<Text>{tr('Loading…','در حال بارگیری…')}</Text>}
 {loaded&&!items.length&&<Text>{tr('No message requests.','درخواست پیامی وجود ندارد.')}</Text>}
 {items.map(r=><View key={r.id} style={{gap:10,padding:16,borderWidth:1,borderColor:'#b9d2d0'}}><Text>{r.display_name} · @{r.username}</Text><Text>{r.status==='PENDING'?tr('Awaiting consent','در انتظار رضایت'):r.status==='DECLINED'?tr('Declined','ردشده'):tr('Cancelled','لغوشده')}</Text>
 {r.status==='PENDING'&&(r.request_target_id===user?.userId?<><AppButton label={tr('Accept request','پذیرش درخواست')} disabled={busy} onPress={()=>void decide(r.id,'ACCEPT')}/><AppButton label={tr('Decline request','رد درخواست')} disabled={busy} variant="secondary" onPress={()=>void decide(r.id,'DECLINE')}/></>:<AppButton label={tr('Cancel request','لغو درخواست')} disabled={busy} variant="secondary" onPress={()=>void decide(r.id,'CANCEL')}/>)}
 <AppButton label={tr('Block account','مسدودکردن حساب')} disabled={busy} variant="secondary" onPress={()=>{setBusy(true);void apiRequest(`/api/v1/social/blocks/${r.request_target_id===user?.userId?r.requester_id:r.request_target_id}`,{method:'PUT'}).then(()=>refresh()).catch(e=>setError(e.message)).finally(()=>setBusy(false));}}/>
 <AppButton label={tr('Report request','گزارش درخواست')} variant="secondary" onPress={()=>router.push({pathname:'/tools/[tool]',params:{tool:'report',targetType:'CONVERSATION',targetId:r.id}} as never)}/>
 </View>)}
 </View></Screen>;
}
