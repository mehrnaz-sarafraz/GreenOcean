import {GroupInbox} from '@/features/community/group-inbox';
import {useState} from 'react';
import {router} from 'expo-router';
import {Text,TextInput,View} from 'react-native';
import {Screen} from '@/components/screen';import {AppButton} from '@/components/app-button';
import {apiRequest} from '@/lib/api/client';import {useLanguage} from '@/localization/language-provider';
import {useForegroundPoll} from '@/features/live-support/foreground';
type Proposal={id:string;name:string;description:string;purpose:string;privacy_type:string;rules:string;revision:number;status:string;decision_note:string|null;community_id:string|null};
export default function GroupWorkspace(){
 const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [items,setItems]=useState<Proposal[]>([]);const [edit,setEdit]=useState<Proposal|null>(null);
 const [name,setName]=useState(''),[description,setDescription]=useState(''),[purpose,setPurpose]=useState(''),[rules,setRules]=useState('');const [privacy,setPrivacy]=useState('PRIVATE');const [busy,setBusy]=useState(false),[error,setError]=useState('');
 const refresh=useForegroundPoll(async current=>{try{const list=await apiRequest<Proposal[]>('/api/v1/community-proposals/mine');if(current()){setItems(list);setError('');}}catch(e){if(current()){setItems([]);setError(e instanceof Error?e.message:tr('Unavailable','در دسترس نیست'));}}},15000,()=>{setItems([]);setEdit(null);setName('');setDescription('');setPurpose('');setRules('');});
 const labels:Record<string,string>={DRAFT:tr('Draft','پیش‌نویس'),SUBMITTED:tr('Awaiting platform review','در انتظار بررسی پلتفرم'),NEEDS_REVISION:tr('Changes requested','نیازمند اصلاح'),APPROVED:tr('Approved','تأییدشده'),REJECTED:tr('Rejected','ردشده'),CANCELLED:tr('Withdrawn','پس‌گرفته‌شده')};
 async function save(){if(busy)return;setBusy(true);setError('');try{await apiRequest(edit?`/api/v1/community-proposals/${edit.id}`:'/api/v1/community-proposals',{method:edit?'PUT':'POST',body:JSON.stringify({name,description,purpose,privacyType:privacy,rules:rules.split('\n').filter(s=>s.trim()),revision:edit?.revision??0})});setEdit(null);setName('');setDescription('');setPurpose('');setRules('');await refresh();}catch(e){setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 async function move(p:Proposal,action:string){if(busy)return;setBusy(true);try{await apiRequest(`/api/v1/community-proposals/${p.id}/${action}`,{method:'POST',body:JSON.stringify({revision:p.revision})});await refresh();}catch(e){setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 return <Screen scroll><View style={{gap:16,direction:language==='fa'?'rtl':'ltr',maxWidth:720,width:'100%',alignSelf:'center'}}>
 <GroupInbox/><Text accessibilityRole="header">{tr('My groups and proposals','گروه‌ها و پیشنهادهای من')}</Text><Text>{tr('A proposal is private until independently approved. Peer support is not treatment or an emergency service. No review time is promised.','پیشنهاد تا تأیید مستقل خصوصی است. حمایت همتا درمان یا خدمات اضطراری نیست. زمان بررسی تضمین نمی‌شود.')}</Text>
 {!!error&&<Text accessibilityRole="alert">{error}</Text>}<AppButton label={tr('Refresh groups','دریافت دوبارهٔ گروه‌ها')} variant="secondary" onPress={()=>void refresh()}/>
 <TextInput accessibilityLabel={tr('Group name','نام گروه')} maxLength={80} value={name} onChangeText={setName}/><TextInput accessibilityLabel={tr('Group description','توضیح گروه')} maxLength={500} value={description} onChangeText={setDescription} multiline/>
 <TextInput accessibilityLabel={tr('Peer-support purpose','هدف حمایت همتا')} maxLength={1000} value={purpose} onChangeText={setPurpose} multiline/>
 {['PUBLIC','PRIVATE','HIDDEN'].map(p=><AppButton key={p} label={p==='PUBLIC'?tr('Public directory, members-only discussion','فهرست عمومی، گفت‌وگو فقط برای اعضا'):p==='PRIVATE'?tr('Private, approved membership','خصوصی، عضویت با تأیید'):tr('Hidden, targeted invitation only','پنهان، فقط دعوت هدفمند')} selected={privacy===p} variant={privacy===p?'primary':'secondary'} onPress={()=>setPrivacy(p)}/>)}
 <TextInput accessibilityLabel={tr('Group rules, one per line','قوانین گروه، هر قانون در یک سطر')} multiline value={rules} maxLength={10000} onChangeText={setRules}/>
 <AppButton label={tr('Save proposal','ذخیرهٔ پیشنهاد')} disabled={busy||!name.trim()||!purpose.trim()||!description.trim()||!rules.trim()} onPress={()=>void save()}/>
 {items.map(p=><View key={p.id} style={{gap:10,padding:16,borderWidth:1}}><Text>{p.name}</Text><Text accessibilityRole="alert">{labels[p.status]??p.status}</Text>{!!p.decision_note&&<Text>{p.decision_note}</Text>}
 {['DRAFT','NEEDS_REVISION'].includes(p.status)&&<><AppButton label={tr('Edit proposal','ویرایش پیشنهاد')} variant="secondary" onPress={()=>{setEdit(p);setName(p.name);setDescription(p.description);setPurpose(p.purpose);setPrivacy(p.privacy_type);setRules((JSON.parse(p.rules) as string[]).join('\n'));}}/><AppButton label={tr('Submit for platform review','ارسال برای بررسی پلتفرم')} disabled={busy} onPress={()=>void move(p,'submit')}/></>}
 {['DRAFT','SUBMITTED','NEEDS_REVISION'].includes(p.status)&&<AppButton label={tr('Withdraw proposal','پس‌گرفتن پیشنهاد')} variant="secondary" disabled={busy} onPress={()=>void move(p,'cancel')}/>}
 {p.community_id&&<AppButton label={tr('Open approved group','بازکردن گروه تأییدشده')} onPress={()=>router.push({pathname:'/(app)/community/[slug]',params:{slug:'g-'+p.community_id}})}/>}</View>)}
 </View></Screen>;
}
