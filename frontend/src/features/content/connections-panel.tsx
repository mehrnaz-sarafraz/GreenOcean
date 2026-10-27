import {useState} from 'react';
import {Text,View} from 'react-native';
import {AppButton} from '@/components/app-button';
import {apiRequest} from '@/lib/api/client';
import {useForegroundPoll} from '@/features/live-support/foreground';
import {useLanguage} from '@/localization/language-provider';
type Person={userId:string;displayName:string;username:string};
export function ConnectionsPanel(){
 const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [data,setData]=useState<Person[][]>([[],[],[],[]]);const [error,setError]=useState('');const [busy,setBusy]=useState(false);const [loaded,setLoaded]=useState(false);
 const refresh=useForegroundPoll(async current=>{try{const r=await Promise.all(['following','followers','follow-requests/incoming','follow-requests/outgoing'].map(path=>apiRequest<Person[]>('/api/v1/social/'+path)));if(current()){setData(r);setError('');setLoaded(true);}}catch(e){if(current()){setData([[],[],[],[]]);setError(e instanceof Error?e.message:'Unavailable');}}},15000,()=>{setData([[],[],[],[]]);setLoaded(false);});
 async function action(path:string,method:'POST'|'DELETE'){if(busy)return;setBusy(true);setError('');try{await apiRequest('/api/v1/social/'+path,{method});await refresh();}catch(e){setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 return <View style={{gap:16}}>{!!error&&<Text accessibilityRole="alert">{error}</Text>}{!loaded&&!error&&<Text>{tr('Loading connections…','در حال بارگیری ارتباط‌ها…')}</Text>}
 <Text accessibilityRole="header">{tr('Follow requests','درخواست‌های دنبال‌کردن')}</Text>{loaded&&!data[2].length&&<Text>{tr('No pending follow requests.','درخواست دنبال‌کردن معلقی نیست.')}</Text>}
 {data[2].map(p=><View key={p.userId} style={{gap:8}}><Text>{p.displayName} · @{p.username}</Text><AppButton label={tr('Accept follow request','پذیرش درخواست دنبال‌کردن')} disabled={busy} onPress={()=>void action(`follow-requests/${p.userId}/accept`,'POST')}/><AppButton label={tr('Decline follow request','رد درخواست دنبال‌کردن')} variant="secondary" disabled={busy} onPress={()=>void action(`follow-requests/${p.userId}`,'DELETE')}/></View>)}
 <Text accessibilityRole="header">{tr('Sent requests','درخواست‌های فرستاده‌شده')}</Text>{data[3].map(p=><View key={p.userId}><Text>{p.displayName} · @{p.username}</Text><AppButton label={tr('Cancel follow request','لغو درخواست دنبال‌کردن')} disabled={busy} variant="secondary" onPress={()=>void action(`follow-requests/outgoing/${p.userId}`,'DELETE')}/></View>)}
 <Text accessibilityRole="header">{tr('Following','دنبال‌شده‌ها')}</Text>{loaded&&!data[0].length&&<Text>{tr('You are following nobody. Reading and connecting are voluntary.','کسی را دنبال نمی‌کنید. خواندن و ارتباط‌گرفتن اختیاری است.')}</Text>}{data[0].map(p=><View key={p.userId}><Text>{p.displayName} · @{p.username}</Text><AppButton label={tr('Unfollow','لغو دنبال‌کردن')} disabled={busy} variant="secondary" onPress={()=>void action(`follows/${p.userId}`,'DELETE')}/></View>)}
 <Text accessibilityRole="header">{tr('Followers','دنبال‌کنندگان')}</Text>{data[1].map(p=><Text key={p.userId}>{p.displayName} · @{p.username}</Text>)}
 </View>;
}
