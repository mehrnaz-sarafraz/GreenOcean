import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';
import { apiRequest } from '@/lib/api/client';
import { usePlatformData } from '@/features/platform/data-provider';
import { optionsIn } from '@/features/platform/catalog';
import { UserPreferences } from '@/features/platform/types';
import { resumeStep, supportDestination } from '@/features/platform/onboarding-policy';
import { useLanguage } from '@/localization/language-provider';

export default function Onboarding() {
 const data = usePlatformData(); const { language, isRtl } = useLanguage();
 const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 if (!data.preferences) return <Screen><Text>{tr('Loading your private preferences…','در حال دریافت تنظیمات خصوصی…')}</Text>{!!data.error && <AppButton label={tr('Retry','تلاش دوباره')} onPress={()=>void data.refresh()}/>}</Screen>;
 return <OnboardingForm key={data.profile?.userId ?? 'owner'} preferences={data.preferences} rtl={isRtl}/>;
}
function OnboardingForm({preferences,rtl}:{preferences:UserPreferences;rtl:boolean}) {
 const {categories,options,setPreferences}=usePlatformData(); const {language}=useLanguage();
 const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [step,setStep]=useState(resumeStep(preferences.onboardingStep));
 const [topics,setTopics]=useState(preferences.supportTopics);
 const [support,setSupport]=useState(preferences.supportStyle??'');
 const [saving,setSaving]=useState(preferences.onboardingStatus==='NOT_STARTED');const [error,setError]=useState('');
 useEffect(()=>{if(preferences.onboardingStatus!=='NOT_STARTED')return;let active=true;apiRequest<UserPreferences>('/api/v1/preferences/me',{method:'PATCH',body:JSON.stringify({onboardingStatus:'IN_PROGRESS'})}).then(saved=>{if(active)setPreferences(saved);}).catch(()=>{if(active)setError('Could not save progress. Continue to retry.');}).finally(()=>{if(active)setSaving(false);});return()=>{active=false;};},[preferences.onboardingStatus,setPreferences]);
 async function save(action:'next'|'stepSkip'|'skip') {
  if(saving)return;setSaving(true);setError('');
  try {
   const finishing=step===3&&action!=='skip';
   const patch:Partial<UserPreferences>={onboardingStatus:action==='skip'?'SKIPPED':finishing?'COMPLETED':'IN_PROGRESS',onboardingStep:action==='skip'?(preferences.onboardingStep??0):step};
   if(action==='next'&&step===1&&preferences.topicCollectionEnabled)patch.supportTopics=topics;
   if(action==='next'&&step===2)patch.supportStyle=support;
   const saved=await apiRequest<UserPreferences>('/api/v1/preferences/me',{method:'PATCH',body:JSON.stringify(patch)});
   setPreferences(saved);
   if(action==='skip'||finishing){
    const identity=await apiRequest<{services:Record<string,boolean>}>('/api/v1/identity');
    router.replace(supportDestination(action==='skip'?null:saved.supportStyle,identity.services));
   } else setStep(value=>Math.min(3,value+1));
  }catch(caught){setError(caught instanceof Error?caught.message:tr('Could not save. Try again.','ذخیره نشد. دوباره تلاش کنید.'));}
  finally{setSaving(false);}
 }
 return <Screen scroll style={styles.screen}><View style={[styles.content,{direction:rtl?'rtl':'ltr'}]}>
  <View style={styles.top}><Text style={styles.brand}>GreenOcean</Text><Text style={styles.stepText}>{step} / 3</Text></View>
  <View style={styles.progress}>{[1,2,3].map(n=><View key={n} style={[styles.progressBar,n<=step&&styles.progressActive]}/>)}</View>
  {step===1&&<><Text accessibilityRole="header" style={styles.title}>{tr('Optional interests','علاقه‌مندی‌های اختیاری')}</Text><Text style={styles.subtitle}>{tr('You do not need to share a diagnosis. These private choices are stored only for you; feed ranking does not currently use them. You can clear them later.','نیازی به بیان تشخیص پزشکی نیست. این انتخاب‌ها فقط در تنظیمات خصوصی شما ذخیره می‌شوند و اکنون رتبه‌بندی محتوا را تغییر نمی‌دهند. بعداً می‌توانید پاکشان کنید.')}</Text>
   {!preferences.topicCollectionEnabled?<Text style={styles.subtitle}>{tr('Topic collection is currently paused for privacy review. Continue without selecting anything.','انتخاب موضوع تا پایان بررسی حریم خصوصی غیرفعال است. بدون انتخاب ادامه دهید.')}</Text>:<View style={styles.topicGrid}>{categories.map(topic=><Pressable key={topic.id} disabled={saving} accessibilityRole="checkbox" accessibilityState={{checked:topics.includes(topic.slug)}} accessibilityLabel={topic.name} onPress={()=>setTopics(items=>items.includes(topic.slug)?items.filter(v=>v!==topic.slug):[...items,topic.slug])} style={[styles.topic,topics.includes(topic.slug)&&styles.topicActive]}><Text style={styles.topicText}>{topic.name}</Text></Pressable>)}</View>}
   {!!(preferences.supportTopics.length||preferences.legacySupportTopics?.length)&&<AppButton variant="secondary" disabled={saving} label={tr('Clear saved topics','پاک کردن موضوعات ذخیره‌شده')} onPress={()=>{setSaving(true);void apiRequest<UserPreferences>('/api/v1/preferences/me',{method:'PATCH',body:JSON.stringify({supportTopics:[],clearLegacySupportTopics:true})}).then(saved=>{setPreferences(saved);setTopics([]);}).catch(()=>setError(tr('Could not clear topics. Try again.','پاک نشد. دوباره تلاش کنید.'))).finally(()=>setSaving(false));}}/>}
  </>}
  {step===2&&<><Text accessibilityRole="header" style={styles.title}>{tr('Where would you like to start?','از کجا شروع کنیم؟')}</Text><Text style={styles.subtitle}>{tr('Optional. This chooses your first screen, not your permissions. Change or clear it later. Listener support is non-clinical; availability is not guaranteed.','اختیاری است. فقط صفحهٔ شروع را تعیین می‌کند؛ مجوز دسترسی نمی‌دهد. بعداً تغییر دهید یا پاک کنید. حمایت شنونده درمان نیست و دسترسی فوری تضمین نمی‌شود.')}</Text><View style={styles.supportList}>{[{key:'',label:tr('No preference — explore Support','بدون ترجیح — کشف حمایت'),description:''},...optionsIn(options,'SUPPORT_STYLE')].map(option=><Pressable key={option.key||'none'} disabled={saving} accessibilityRole="radio" accessibilityState={{checked:support===option.key}} accessibilityLabel={option.label} onPress={()=>setSupport(option.key)} style={[styles.support,support===option.key&&styles.supportActive]}><View style={{flex:1}}><Text style={styles.supportTitle}>{option.label}</Text><Text style={styles.supportText}>{option.description}</Text></View>{support===option.key&&<AppIcon name="check_circle" color={colors.ocean600}/>}</Pressable>)}</View></>}
  {step===3&&<><Text accessibilityRole="header" style={styles.title}>{tr('You stay in control','اختیار با شماست')}</Text><Text style={styles.privacyText}>{tr('New profiles are private, public location and age are hidden, and unsolicited message requests are off. Your existing choices stay unchanged. Each post has its own audience; making a profile public never expands old posts.','پروفایل تازه خصوصی است، مکان و سن نمایش داده نمی‌شود و درخواست پیام ناخواسته بسته است. انتخاب‌های قبلی شما حفظ می‌شوند. هر پست مخاطب خودش را دارد و عمومی کردن پروفایل، پست‌های قبلی را عمومی نمی‌کند.')}</Text><Text style={styles.subtitle}>{tr('Blur covers permitted sensitive text visually; it is not encryption. Stronger discovery controls are separate. Mandatory moderation and blocking always apply. Adjust them in Privacy & Safety later.','پوشاندن متن حساس یک نمایش بصری است، نه رمزنگاری. کنترل‌های بیشترِ کشف محتوا جدا هستند. نظارت اجباری و مسدودسازی همیشه اعمال می‌شوند. بعداً در حریم خصوصی و ایمنی تغییر دهید.')}</Text><Text style={styles.boundaryText}>{tr('Community and listener support are non-clinical. Articles are educational, not treatment. Professional care depends on credentials, jurisdiction and availability. GreenOcean is not an emergency service.','حمایت جامعه و شنونده غیر‌درمانی است. مقاله آموزشی است، نه درمان. خدمات تخصصی به صلاحیت، منطقه و ظرفیت وابسته است. GreenOcean خدمات اضطراری نیست.')}</Text><AppButton variant="secondary" label={tr('Urgent help','کمک فوری')} onPress={()=>router.push('/(auth)/urgent-help')}/></>}
  {!!error&&<Text accessibilityRole="alert" style={{color:colors.danger}}>{error}</Text>}
  <View style={styles.actions}>{step>1&&<AppButton variant="secondary" disabled={saving} label={tr('Back','بازگشت')} onPress={()=>setStep(value=>value-1)}/>}<View style={{flex:1}}><AppButton label={step===3?tr('Enter GreenOcean','ورود به GreenOcean'):tr('Continue','ادامه')} loading={saving} onPress={()=>void save('next')}/></View></View>
  {step<3&&<AppButton variant="secondary" disabled={saving} label={tr('Skip this step','رد کردن این مرحله')} onPress={()=>void save('stepSkip')}/>}
  <AppButton variant="secondary" disabled={saving} label={tr('Skip for now','فعلاً رد کن')} onPress={()=>void save('skip')}/>
 </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, loading: { flex: 1, minHeight: 320, alignItems: 'center', justifyContent: 'center', gap: spacing.sm }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.lg, gap: spacing.lg }, top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, logo: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm }, brand: { color: colors.ocean800, fontSize: 17, fontWeight: '900' }, stepText: { color: colors.muted, fontSize: 10, fontWeight: '800' }, progress: { flexDirection: 'row', gap: spacing.sm }, progressBar: { flex: 1, height: 5, borderRadius: 3, backgroundColor: colors.border }, progressActive: { backgroundColor: colors.ocean500 }, eyebrow: { color: colors.ocean600, fontSize: 9, fontWeight: '900', letterSpacing: 1.3 }, title: { color: colors.ink, fontSize: typography.h1, lineHeight: 39, fontWeight: '900', marginTop: 5 }, subtitle: { color: colors.muted, fontSize: 12, lineHeight: 19, marginTop: spacing.sm },
  topicGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, topic: { flexGrow: 1, flexBasis: 200, minHeight: 72, flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.sm }, topicActive: { backgroundColor: colors.ocean50, borderColor: colors.ocean400 }, topicIcon: { width: 42, height: 42, borderRadius: radius.sm, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, topicIconActive: { backgroundColor: colors.ocean600 }, topicText: { flex: 1, color: colors.ink, fontSize: 11, fontWeight: '900' }, topicTextActive: { color: colors.ocean800 }, supportList: { gap: spacing.sm }, support: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md }, supportActive: { backgroundColor: colors.ocean50, borderColor: colors.ocean400 }, supportIcon: { width: 46, height: 46, borderRadius: radius.md, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, supportTitle: { color: colors.ink, fontSize: 12, fontWeight: '900' }, supportText: { color: colors.muted, fontSize: 9, marginTop: 3 },
  privacyCard: { alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.xl, padding: spacing.xl }, privacyIcon: { width: 72, height: 72, borderRadius: 36, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, privacyTitle: { color: colors.ink, fontSize: 18, fontWeight: '900', textAlign: 'center' }, privacyText: { color: colors.muted, fontSize: 11, lineHeight: 18, textAlign: 'center' }, privacyToggle: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.foam, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 7 }, privacyToggleActive: { backgroundColor: colors.ocean50 }, privacyToggleText: { color: colors.inkSoft, fontSize: 10, fontWeight: '800' }, boundary: { flexDirection: 'row', gap: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.lg, padding: spacing.md }, boundaryText: { flex: 1, color: colors.muted, fontSize: 9, lineHeight: 15 }, actions: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginTop: 'auto' }, back: { minHeight: 54, justifyContent: 'center', paddingHorizontal: spacing.lg }, backText: { color: colors.ocean700, fontWeight: '900' }, skip: { color: colors.muted, fontSize: 10, fontWeight: '800', textAlign: 'center', padding: spacing.sm },
});
