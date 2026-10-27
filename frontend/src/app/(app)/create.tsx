import { router, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { DisplayReview } from '@/features/auth/display-review';
import { AppButton } from '@/components/app-button';
import { Screen } from '@/components/screen';
import { PostItem } from '@/features/content/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing } from '@/theme/tokens';

type Identity='ANONYMOUS'|'SOCIAL_IDENTITY';
type Payload={body:string;publicationIdentity:Identity|null;visibility:'FOLLOWERS'|'COMMUNITY'|'PUBLIC';communityId:string|null;contentWarning:string;categoryId:string|null;postType:'EXPERIENCE'|'QUESTION'|'REFLECTION';mood:string};
type Draft={id:string;payload:string;revision:number;expires_at:string};
const blank=():Payload=>({body:'',publicationIdentity:null,visibility:'FOLLOWERS',communityId:null,contentWarning:'',categoryId:null,postType:'EXPERIENCE',mood:''});
export default function Create() {
 const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const {categories,communities,profile}=usePlatformData();
 const [stage,setStage]=useState<1|2|3>(1);const [value,setValue]=useState<Payload>(blank);
 const [reviewed,setReviewed]=useState<boolean|null>(null);const [broader,setBroader]=useState(false);
 const [draft,setDraft]=useState<Draft|null>(null);const [drafts,setDrafts]=useState<Draft[]>([]);
 const [busy,setBusy]=useState(false);const [error,setError]=useState('');const [notice,setNotice]=useState('');
 const patch=(next:Partial<Payload>)=>setValue(current=>({...current,...next}));
 useFocusEffect(useCallback(()=>{let active=true;const epoch=tokenStorage.generation();
  void apiRequest<{eligibility:{display_reviewed_at?:string}}>('/api/v1/identity').then(r=>{if(active&&epoch===tokenStorage.generation())setReviewed(Boolean(r.eligibility.display_reviewed_at));}).catch(()=>{if(active)setReviewed(false);});
  void apiRequest<Draft[]>('/api/v1/posts/drafts').then(r=>{if(active&&epoch===tokenStorage.generation())setDrafts(r);}).catch(()=>{if(active)setError(language==='fa'?'بارگیری پیش‌نویس‌ها انجام نشد.':'Could not load drafts.');});
  return()=>{active=false;};
 },[language]));
 function reset(){setValue(blank());setStage(1);setDraft(null);setBroader(false);setError('');}
 async function save(){if(busy)return;setBusy(true);setError('');const epoch=tokenStorage.generation();try{
  const saved=await apiRequest<Draft>(draft?`/api/v1/posts/drafts/${draft.id}`:'/api/v1/posts/drafts',{method:draft?'PUT':'POST',body:JSON.stringify({payload:value,revision:draft?.revision??0})});
  if(epoch!==tokenStorage.generation())return;setDraft(saved);setDrafts(current=>[saved,...current.filter(d=>d.id!==saved.id)]);setNotice(tr('Private draft saved; deleted 7 days after last edit.','پیش‌نویس خصوصی ذخیره شد؛ حذف ۷ روز پس از آخرین ویرایش.'));
 }catch(e){if(epoch===tokenStorage.generation())setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 async function discard(id:string){if(busy)return;setBusy(true);try{await apiRequest(`/api/v1/posts/drafts/${id}`,{method:'DELETE'});setDrafts(current=>current.filter(d=>d.id!==id));if(draft?.id===id)reset();}catch(e){setError(e instanceof Error?e.message:'Unable to discard');}finally{setBusy(false);}}
 async function publish(){if(busy)return;
  if(!value.publicationIdentity||!value.body.trim()||(value.visibility==='COMMUNITY'&&!value.communityId)||(value.visibility==='PUBLIC'&&!broader)){setError(tr('Choose identity, text and audience.','هویت، متن و مخاطبان را مشخص کنید.'));return;}
  const epoch=tokenStorage.generation();setBusy(true);setError('');
  try{const result=await apiRequest<PostItem>(draft?`/api/v1/posts/drafts/${draft.id}/publish`:'/api/v1/posts',{method:'POST',body:JSON.stringify({...value,anonymous:value.publicationIdentity==='ANONYMOUS',broaderAudienceConfirmed:broader,communityId:value.visibility==='COMMUNITY'?value.communityId:null})});
   if(epoch!==tokenStorage.generation())return;
   setNotice(result.status==='PUBLISHED'?tr('Published.','منتشر شد.'):result.safetyOutcome==='REQUEST_REVISION'?tr('Needs changes. Text saved privately in My posts.','نیازمند اصلاح؛ متن در «پست‌های من» خصوصی ذخیره شد.'):result.safetyOutcome==='BLOCK_PROHIBITED'?tr('Not published. Request review from My posts.','منتشر نشد؛ از «پست‌های من» درخواست بازبینی کنید.'):tr('Awaiting review. No response time is promised. Private until approved.','در انتظار بررسی؛ زمان پاسخ تضمین نمی‌شود. تا تأیید خصوصی می‌ماند.'));
   if(draft)setDrafts(current=>current.filter(d=>d.id!==draft.id));reset();
  }catch(e){if(epoch===tokenStorage.generation())setError(e instanceof Error?e.message:tr('Unable to publish; text preserved.','انتشار انجام نشد؛ متن حفظ شد.'));}finally{setBusy(false);}
 }
 if(reviewed===null)return <Screen><Text>{tr('Loading…','در حال بارگیری…')}</Text></Screen>;
 if(!reviewed&&stage!==1)return <Screen scroll><DisplayReview onReviewed={()=>setReviewed(true)}/></Screen>;
 const direction={textAlign:language==='fa'?'right' as const:'left' as const,writingDirection:language==='fa'?'rtl' as const:'ltr' as const};
 const audience=(a:string)=>a==='PUBLIC'?tr('Eligible members','اعضای مجاز'):a==='FOLLOWERS'?tr('Approved followers','دنبال‌کنندگان تأییدشده'):tr('Community members','اعضای گروه');
 return <Screen scroll><View style={styles.content}>
  <Text accessibilityRole="header" style={[styles.title,direction]}>{tr('Create a post','نوشتن پست')} · {stage}/3</Text>
  {!!notice&&<Text accessibilityRole="alert" style={direction}>{notice}</Text>}{!!error&&<Text accessibilityRole="alert" style={[styles.error,direction]}>{error}</Text>}
  <AppButton label={tr('My posts and decisions','پست‌های من و تصمیم‌ها')} variant="secondary" onPress={()=>router.push('/(app)/my-posts' as never)}/>
  {stage===1&&<>
   <Text accessibilityRole="header" style={[styles.title,direction]}>{tr('How would you like this post to appear to other members?','می‌خواهید این پست چگونه به اعضای دیگر نمایش داده شود؟')}</Text>
   <AppButton label={tr('Anonymous','ناشناس')} selected={value.publicationIdentity==='ANONYMOUS'} variant={value.publicationIdentity==='ANONYMOUS'?'primary':'secondary'} onPress={()=>patch({publicationIdentity:'ANONYMOUS'})}/>
   <Text style={direction}>{tr('Members cannot see your username or profile link. Authorized safety staff may identify the author for necessary investigations.','اعضا نام کاربری یا پیوند پروفایل را نمی‌بینند. کارکنان مجاز ایمنی در بررسی ضروری می‌توانند نویسنده را شناسایی کنند.')}</Text>
   <AppButton label={tr('Social identity','هویت اجتماعی')} selected={value.publicationIdentity==='SOCIAL_IDENTITY'} variant={value.publicationIdentity==='SOCIAL_IDENTITY'?'primary':'secondary'} onPress={()=>patch({publicationIdentity:'SOCIAL_IDENTITY'})}/>
   <Text style={direction}>{tr('Your social display name and username appear with eligible profile access. No email or legal name is automatically disclosed.','نام نمایشی و نام کاربری اجتماعی با دسترسی مجاز به پروفایل نمایش داده می‌شود؛ ایمیل یا نام قانونی خودکار افشا نمی‌شود.')}</Text>
   <AppButton label={tr('Continue to writing','ادامه به نوشتن')} disabled={!value.publicationIdentity||busy} onPress={()=>{setStage(2);setNotice('');}}/>
   <Text style={direction}>{tr('Private drafts · deleted 7 days after last edit','پیش‌نویس‌های خصوصی · حذف ۷ روز پس از آخرین ویرایش')}</Text>
   {drafts.map(d=><View key={d.id} style={styles.box}><AppButton label={tr('Resume draft','ادامهٔ پیش‌نویس')+' '+new Date(d.expires_at).toLocaleDateString()} variant="secondary" onPress={()=>{try{setValue(JSON.parse(d.payload));setDraft(d);setStage(1);setBroader(false);}catch{setError(tr('Unable to read draft','خواندن پیش‌نویس انجام نشد'));}}}/><AppButton label={tr('Discard draft','حذف پیش‌نویس')} variant="secondary" disabled={busy} onPress={()=>void discard(d.id)}/></View>)}
  </>}
  {stage===2&&<>
   <Text style={direction}>{value.publicationIdentity==='ANONYMOUS'?tr('Identity: Anonymous','هویت: ناشناس'):tr('Identity: Social identity','هویت: اجتماعی')}</Text>
   <TextInput accessibilityLabel={tr('Post text','متن پست')} multiline maxLength={10000} value={value.body} onChangeText={body=>patch({body})} style={[styles.editor,direction]}/><Text style={direction}>{value.body.length}/10000</Text>
   <Text style={direction}>{tr('Audience is independent of identity','مخاطبان مستقل از هویت هستند')}</Text>
   {(['FOLLOWERS','COMMUNITY','PUBLIC'] as const).map(a=><AppButton key={a} label={audience(a)} variant={value.visibility===a?'primary':'secondary'} onPress={()=>{patch({visibility:a,communityId:null});setBroader(false);}}/>)}
   {value.visibility==='COMMUNITY'&&communities.filter(c=>c.member).map(c=><AppButton key={c.id} label={c.name} variant={value.communityId===c.id?'primary':'secondary'} onPress={()=>patch({communityId:c.id})}/>)}
   <TextInput accessibilityLabel={tr('Optional content warning','هشدار اختیاری محتوا')} placeholder={tr('Optional content warning','هشدار اختیاری محتوا')} maxLength={120} value={value.contentWarning} onChangeText={contentWarning=>patch({contentWarning})} style={[styles.input,direction]}/>
   <Text style={direction}>{tr('Topic is optional. No health classification is required.','موضوع اختیاری است. هیچ طبقه‌بندی سلامتی الزامی نیست.')}</Text>
   <AppButton label={tr('No topic','بدون موضوع')} variant="secondary" onPress={()=>patch({categoryId:null})}/>
   {categories.map(c=><AppButton key={c.id} label={c.name} variant={value.categoryId===c.id?'primary':'secondary'} onPress={()=>patch({categoryId:c.id})}/>)}
   {(['EXPERIENCE','QUESTION','REFLECTION'] as const).map(type=><AppButton key={type} label={type==='EXPERIENCE'?tr('Experience','تجربه'):type==='QUESTION'?tr('Question','پرسش'):tr('Reflection','تأمل')} variant={value.postType===type?'primary':'secondary'} onPress={()=>patch({postType:type})}/>)}
   <TextInput accessibilityLabel={tr('Optional emotional context','زمینهٔ احساسی اختیاری')} maxLength={30} value={value.mood} onChangeText={mood=>patch({mood})} style={[styles.input,direction]}/>
   <AppButton label={tr('Review before publishing','بازبینی پیش از انتشار')} disabled={!value.body.trim()||(value.visibility==='COMMUNITY'&&!value.communityId)} onPress={()=>setStage(3)}/>
  </>}
  {stage===3&&<View style={styles.box}>
   <Text accessibilityRole="header" style={[styles.title,direction]}>{tr('Final review','بازبینی نهایی')}</Text>
   <Text style={direction}>{value.publicationIdentity==='ANONYMOUS'?tr('Identity: Anonymous','هویت: ناشناس'):tr('Identity: Social identity','هویت: اجتماعی')+' · '+(profile?.displayName??'')}</Text>
   <Text style={direction}>{tr('Audience','مخاطبان')}: {audience(value.visibility)}</Text>
   {!!value.communityId&&<Text style={direction}>{communities.find(c=>c.id===value.communityId)?.name}</Text>}
   <Text style={direction}>{value.contentWarning||tr('No content warning','بدون هشدار محتوا')}</Text>
   {!!value.categoryId&&<Text style={direction}>{categories.find(c=>c.id===value.categoryId)?.name}</Text>}
   <Text style={direction}>{value.body}</Text>
   {value.publicationIdentity==='ANONYMOUS'&&<Text style={direction}>{tr('Small audiences, timing and text details can still allow identity inference. Perfect anonymity is not guaranteed.','مخاطبان محدود، زمان و جزئیات متن ممکن است هویت را قابل حدس کند. ناشناسی مطلق تضمین نمی‌شود.')}</Text>}
   {value.visibility==='PUBLIC'&&<><Text style={direction}>{tr('I confirm eligible members beyond my followers may read this post.','تأیید می‌کنم اعضای مجاز خارج از دنبال‌کنندگانم می‌توانند این پست را بخوانند.')}</Text><Switch accessibilityLabel={tr('Confirm broader audience','تأیید مخاطب گسترده‌تر')} value={broader} onValueChange={setBroader}/></>}
   <AppButton label={busy?tr('Submitting…','در حال ارسال…'):tr('Publish','انتشار')} disabled={busy||(value.visibility==='PUBLIC'&&!broader)} onPress={()=>void publish()}/>
  </View>}
  {stage!==1&&<AppButton label={tr('Back','بازگشت')} variant="secondary" disabled={busy} onPress={()=>setStage(stage===3?2:1)}/>}
  {value.publicationIdentity&&<AppButton label={tr('Save private draft','ذخیرهٔ پیش‌نویس خصوصی')} variant="secondary" disabled={busy} onPress={()=>void save()}/>}
  {draft&&<AppButton label={tr('Discard current draft','حذف پیش‌نویس فعلی')} variant="secondary" disabled={busy} onPress={()=>void discard(draft.id)}/>}
 </View></Screen>;
}
const styles=StyleSheet.create({content:{width:'100%',maxWidth:layout.maxContent,alignSelf:'center',gap:spacing.md,padding:spacing.md},title:{fontSize:21,fontWeight:'800',color:colors.ink},box:{gap:spacing.md,padding:spacing.md,borderWidth:1,borderColor:colors.border,borderRadius:radius.md},editor:{minHeight:170,borderWidth:1,borderColor:colors.border,borderRadius:radius.md,padding:spacing.md,color:colors.ink,textAlignVertical:'top'},input:{minHeight:48,padding:spacing.sm,borderWidth:1,borderColor:colors.border,borderRadius:radius.sm,color:colors.ink},error:{color:colors.danger}});
