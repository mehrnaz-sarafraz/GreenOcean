import { useCallback, useRef, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { ProductPage, ProductHeader, ProductCard, EmptyState, Notice } from '@/components/product-ui';
import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';
import { useLanguage } from '@/localization/language-provider';
import { useAuth } from '@/features/auth/auth-provider';
import { useForegroundPoll } from '@/features/live-support/foreground';
import { colors, spacing } from '@/theme/tokens';

type Item = { id:string; post_id:string|null; comment_id:string|null; revision:number; outcome:string; reason_code:string; decision_note?:string; body?:string; appeal_id?:string; appeal_status?:string; appeal_reason?:string; appeal_note?:string };
export function SocialReview({ staff=false }: {staff?:boolean}) {
 const { language }=useLanguage(); const {user}=useAuth(); const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [items,setItems]=useState<Item[]>([]); const [notes,setNotes]=useState<Record<string,string>>({});
 const [purposes,setPurposes]=useState<Record<string,string>>({});const [authors,setAuthors]=useState<Record<string,string>>({});
 const [page,setPage]=useState(0); const [busy,setBusy]=useState(false); const [error,setError]=useState(''); const [loaded,setLoaded]=useState(false);
 const permitted=!staff||user?.roles.some(role=>role==='ADMIN'||role==='MODERATOR');
 const active=useRef(false);
 const endpoint=staff?'/api/v1/admin/social-moderation':'/api/v1/social-moderation/me';
 useFocusEffect(useCallback(()=>{let current=true;active.current=true;const epoch=tokenStorage.generation();setItems([]);setNotes({});setPurposes({});setAuthors({});setPage(0);setError('');setLoaded(false);
  if(permitted)void apiRequest<Item[]>(endpoint).then(r=>{if(current&&epoch===tokenStorage.generation()){setItems(r);setLoaded(true);}}).catch(e=>{if(current)setError(e.message);});
  return()=>{current=false;active.current=false;setItems([]);setNotes({});setPurposes({});setAuthors({});};
 },[endpoint,permitted]));
 useForegroundPoll(async isCurrent=>{if(!permitted)return;try{const result=await apiRequest<Item[]>(endpoint+'?page='+page);if(isCurrent()){setItems(result);setLoaded(true);}}catch(e){if(isCurrent()){setItems([]);setError(e instanceof Error?e.message:'Unable to load');}}},15000,()=>{setItems([]);setNotes({});setAuthors({});});
 async function refresh(next=0){const epoch=tokenStorage.generation();try{const result=await apiRequest<Item[]>(endpoint+'?page='+next);if(active.current&&epoch===tokenStorage.generation()){setItems(result);setPage(next);setLoaded(true);setError('');}}catch(e){if(active.current)setItems([]);throw e;}}
 async function act(item:Item,outcome?:string){if(busy)return;setBusy(true);setError('');const epoch=tokenStorage.generation();try{
  const path=staff?`/api/v1/admin/social-moderation/${item.id}/decision`:`/api/v1/social-moderation/${item.id}/appeal`;
  await apiRequest(path,{method:'POST',body:JSON.stringify(staff?{revision:item.revision,outcome,note:notes[item.id]}:{reason:notes[item.id]})});
  if(active.current&&epoch===tokenStorage.generation()){setNotes({});await refresh();}
 }catch(e){setError(e instanceof Error?e.message:tr('Unable to save. Reload before retrying.','ثبت نشد؛ پیش از تلاش دوباره بازخوانی کنید.'));}finally{setBusy(false);}}
 async function beginReview(item:Item){if(busy)return;setBusy(true);setError('');try{await apiRequest(`/api/v1/admin/social-moderation/${item.id}/begin-review`,{method:'POST',body:JSON.stringify({revision:item.revision})});await refresh(page);}catch(e){setError(e instanceof Error?e.message:'Unable to start review');}finally{setBusy(false);}}
 async function attribution(item:Item){if(busy)return;setBusy(true);setError('');const epoch=tokenStorage.generation();try{const result=await apiRequest<{authorId:string}>(`/api/v1/admin/social-moderation/${item.id}/attribution`,{method:'POST',body:JSON.stringify({purpose:purposes[item.id]})});if(active.current&&epoch===tokenStorage.generation())setAuthors(current=>({...current,[item.id]:result.authorId}));}catch(e){setError(e instanceof Error?e.message:'Unable to access');}finally{setBusy(false);}}
 const appealStatus=(value:string)=>({OPEN:tr('Awaiting review','در انتظار بررسی'),REVIEWING:tr('Under review','در حال بررسی'),UPHELD:tr('Decision upheld','تصمیم تأیید شد'),OVERTURNED:tr('Decision overturned','تصمیم نقض شد')}[value]||value);
 const status=(value:string)=>({ALLOW:tr('Published','منتشرشده'),REQUEST_REVISION:tr('Changes requested','نیازمند اصلاح'),HOLD_FOR_REVIEW:tr('Awaiting review','در انتظار بررسی'),BLOCK_PROHIBITED:tr('Not published','منتشرنشده')}[value]||value);
 return <ProductPage><ProductHeader title={staff?tr('Community safety review','بررسی ایمنی انجمن'):tr('My content decisions','تصمیم‌های محتوای من')} subtitle={tr('Review outcomes are not clinical assessments. Human response time is not guaranteed.','نتیجهٔ بررسی ارزیابی بالینی نیست. زمان پاسخ انسانی تضمین نمی‌شود.')} backLabel={tr('Go back','بازگشت')}/>
 {!permitted?<Notice title={tr('Restricted access','دسترسی محدود')} body={tr('Authorized staff access and recent security-key verification are required.','دسترسی کارکنان مجاز و تأیید اخیر کلید امنیتی لازم است.')}/>:<>
 {!!error&&<Text accessibilityRole="alert" style={{color:colors.danger}}>{error}</Text>}
 <AppButton label={tr('Refresh decisions','بازخوانی تصمیم‌ها')} variant="secondary" disabled={busy} onPress={()=>void refresh(page).catch(e=>setError(e.message))}/>
 {!loaded&&!error&&<Text accessibilityLiveRegion="polite">{tr('Loading…','در حال بارگذاری…')}</Text>}
 {loaded&&items.length===0&&<EmptyState icon="gavel" title={tr('No decisions to display','تصمیمی برای نمایش نیست')} body={tr('You may continue reading without publishing.','می‌توانید بدون انتشار به خواندن ادامه دهید.')}/>}
 {items.map(item=><ProductCard key={item.id}><Text accessibilityRole="header">{status(item.outcome)}</Text><Text>{tr('Revision','نسخه')} {item.revision} · {item.post_id?tr('Post','پست'):tr('Reply','پاسخ')}</Text>
 {staff&&<Text selectable>{item.body}</Text>}{!!item.decision_note&&<Text>{item.decision_note}</Text>}
 {!!item.appeal_id&&<Text accessibilityLiveRegion="polite">{tr('Appeal','اعتراض')}: {item.appeal_status?appealStatus(item.appeal_status):tr('Awaiting independent review','در انتظار بررسی مستقل')}</Text>}{!!item.appeal_note&&<Text>{item.appeal_note}</Text>}{staff&&!!item.appeal_reason&&<Text>{item.appeal_reason}</Text>}
 {(staff||(!item.appeal_id&&item.outcome!=='ALLOW'))&&<View style={{gap:spacing.sm}}><Text>{staff?tr('Give a respectful explanation visible to the author. Review the exact text above.','دلیل محترمانه و قابل نمایش به نویسنده بنویسید. متن همین نسخه را بررسی کنید.'):tr('Explain why this decision should be reviewed. Do not include unnecessary private information.','دلیل درخواست بررسی را بنویسید. اطلاعات خصوصی غیرضروری وارد نکنید.')}</Text><TextInput accessibilityLabel={tr('Review explanation','توضیح بررسی')} multiline maxLength={staff?1000:2000} value={notes[item.id]||''} onChangeText={value=>setNotes(current=>({...current,[item.id]:value}))} style={{borderWidth:1,borderColor:colors.border,padding:spacing.md,minHeight:90,textAlign:language==='fa'?'right':'left'}}/>
 {staff&&item.appeal_id&&item.appeal_status==='OPEN'&&<AppButton label={tr('Begin appeal review','شروع بررسی اعتراض')} variant="secondary" disabled={busy} onPress={()=>void beginReview(item)}/>}
 {staff?<><AppButton label={tr('Approve this revision','تأیید همین نسخه')} disabled={busy||!notes[item.id]?.trim()} onPress={()=>void act(item,'ALLOW')}/><AppButton label={tr('Request changes','درخواست اصلاح')} variant="secondary" disabled={busy||!notes[item.id]?.trim()} onPress={()=>void act(item,'REQUEST_REVISION')}/><AppButton label={tr('Reject publication','رد انتشار')} variant="secondary" disabled={busy||!notes[item.id]?.trim()} onPress={()=>void act(item,'BLOCK_PROHIBITED')}/></>:<AppButton label={tr('Submit appeal','ثبت اعتراض')} disabled={busy||!notes[item.id]?.trim()} onPress={()=>void act(item)}/>}</View>}
 {staff&&user?.roles.includes('ADMIN')&&<View style={{gap:spacing.sm}}><Text>{tr('Restricted attribution: only for a necessary safety investigation. Access is recorded.','انتساب محدود: فقط برای بررسی ضروری ایمنی. این دسترسی ثبت می‌شود.')}</Text><TextInput accessibilityLabel={tr('Investigation purpose','هدف بررسی')} maxLength={500} value={purposes[item.id]||''} onChangeText={value=>setPurposes(current=>({...current,[item.id]:value}))} style={{borderWidth:1,borderColor:colors.border,padding:spacing.md}}/><AppButton label={tr('Record purpose and reveal author account','ثبت هدف و نمایش حساب نویسنده')} variant="secondary" disabled={busy||(purposes[item.id]?.trim().length||0)<10} onPress={()=>void attribution(item)}/>{!!authors[item.id]&&<Text selectable>{authors[item.id]}</Text>}</View>}
 </ProductCard>)}
 {<View style={{gap:spacing.sm}}>{page>0&&<AppButton label={tr('Previous page','صفحهٔ قبل')} disabled={busy} onPress={()=>void refresh(page-1).catch(e=>setError(e.message))}/>}{items.length===20&&<AppButton label={tr('Next page','صفحهٔ بعد')} disabled={busy} onPress={()=>void refresh(page+1).catch(e=>setError(e.message))}/>}</View>}
 </>}</ProductPage>;
}
