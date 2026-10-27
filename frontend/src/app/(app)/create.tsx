import { router } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { DisplayReview } from '@/features/auth/display-review';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { SupportCategoryGroup } from '@/features/content/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { optionsIn } from '@/features/platform/catalog';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function Create() {
  const { t,language } = useLanguage();
  const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const { categories, profile, refresh, options, communities } = usePlatformData();
  const moods = optionsIn(options, 'POST_MOOD');
  const postTypes = optionsIn(options, 'POST_TYPE');
  const groupOptions = optionsIn(options, 'CATEGORY_GROUP');
  const [reviewed,setReviewed]=useState<boolean|null>(null);
  useEffect(()=>{void apiRequest<{eligibility:{display_reviewed_at?:string}}>('/api/v1/identity').then(identity=>setReviewed(Boolean(identity.eligibility.display_reviewed_at))).catch(()=>setReviewed(false));},[]);
  const [audience,setAudience]=useState<'FOLLOWERS'|'COMMUNITY'|'PUBLIC'>('FOLLOWERS');
  const [communityId,setCommunityId]=useState('');
  const [broaderConfirmed,setBroaderConfirmed]=useState(false);
  const [contentWarning,setContentWarning]=useState('');
  const [body, setBody] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [mood, setMood] = useState('');
  const [group, setGroup] = useState<SupportCategoryGroup | null>(null);
  const [categoryId, setCategoryId] = useState('');
  const [type, setType] = useState('EXPERIENCE');
  const [error, setError] = useState('');
  const [publishing, setPublishing] = useState(false);
  const visibleCategories = useMemo(() => categories.filter(category => category.group === group), [categories, group]);

  function chooseGroup(next: SupportCategoryGroup) {
    setGroup(next);
    setCategoryId('');
    setError('');
  }

  async function publish() {
    if (audience==='PUBLIC'&&!broaderConfirmed){setError('Confirm that eligible members beyond your followers may read this post.');return;}
    if(audience==='COMMUNITY'&&!communityId){setError('Choose a community you have joined.');return;}
    if (!body.trim() || !group || !categoryId) {
      setError(!group ? 'Choose whether your story is about an emotion, condition, or life experience.' : !categoryId ? 'Choose the topic that fits your story best.' : t('requiredFields'));
      return;
    }
    setPublishing(true); setError('');
    try {
      await apiRequest('/api/v1/posts', { method: 'POST', body: JSON.stringify({
        body: body.trim(), anonymous, visibility: audience, communityId: audience==='COMMUNITY'?communityId:null, broaderAudienceConfirmed:broaderConfirmed,
        contentWarning: contentWarning.trim() || null, categoryId, postType: type, mood: mood || null,
      }) });
      await refresh();
      router.replace('/(app)');
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not publish this story');
    } finally {
      setPublishing(false);
    }
  }

  if(reviewed !== true) return <Screen scroll><DisplayReview onReviewed={()=>setReviewed(true)} /></Screen>;
  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.header}>
      <Pressable onPress={() => router.back()}><AppIcon name="close" color={colors.ink} /></Pressable>
      <View style={styles.headerCopy}><Text style={styles.title}>{t('createPost')}</Text><Text style={styles.headerHint}>Your story, in the right space</Text></View>
      <View style={{ width: 24 }} />
    </View>

    <View style={styles.author}>
      <Avatar name={anonymous ? t('anonymous') : (profile?.displayName ?? 'GreenOcean member')} uri={anonymous ? null : profile?.avatarUrl} size={44} />
      <View><Text style={styles.authorName}>{anonymous ? t('anonymous') : (profile?.displayName ?? 'GreenOcean member')}</Text><View style={styles.visibility}><AppIcon name="public" size={14} color={colors.ocean600} /><Text style={styles.visibilityText}>{audience==='PUBLIC'?tr('Eligible members','اعضای مجاز'):audience==='COMMUNITY'?tr('Community members','اعضای گروه'):tr('Approved followers','دنبال‌کنندگان تأییدشده')}</Text></View></View>
    </View>

    <View style={styles.types}>{(['FOLLOWERS','COMMUNITY','PUBLIC'] as const).map(value=><Pressable key={value} accessibilityRole="radio" accessibilityState={{checked:audience===value}} aria-checked={audience===value} accessibilityLabel={value==='PUBLIC'?tr('Eligible members','اعضای مجاز'):value==='COMMUNITY'?tr('Community members','اعضای گروه'):tr('Approved followers','دنبال‌کنندگان تأییدشده')} onPress={()=>{setAudience(value);setBroaderConfirmed(false);}} style={[styles.type,audience===value&&styles.typeActive]}><Text style={[styles.typeText,audience===value&&styles.typeTextActive]}>{value==='PUBLIC'?tr('Eligible members','اعضای مجاز'):value==='COMMUNITY'?tr('Community members','اعضای گروه'):tr('Approved followers','دنبال‌کنندگان تأییدشده')}</Text></Pressable>)}</View>
    <Text style={styles.helper}>{tr('Profile privacy does not change a post’s audience. Historical posts keep their original restriction. Anonymous posts hide your identity from members; the platform can still attribute them for safety.','تنظیم پروفایل، مخاطب پست را تغییر نمی‌دهد. محدودیت پست‌های قبلی حفظ می‌شود. پست ناشناس هویت را از اعضا پنهان می‌کند؛ پلتفرم برای ایمنی همچنان می‌تواند نویسنده را شناسایی کند.')}</Text>
    {audience==='COMMUNITY'&&<View style={styles.types}>{communities.filter(c=>c.member).map(c=><Pressable key={c.id} accessibilityRole="radio" accessibilityState={{checked:communityId===c.id}} aria-checked={communityId===c.id} onPress={()=>setCommunityId(c.id)} style={[styles.type,communityId===c.id&&styles.typeActive]}><Text style={styles.typeText}>{c.name}</Text></Pressable>)}</View>}
    {audience==='PUBLIC'&&<View style={styles.tool}><Text style={styles.toolText}>{tr('I understand eligible members beyond my approved followers may read this post, even with a private profile.','می‌دانم حتی با پروفایل خصوصی، اعضای مجاز خارج از دنبال‌کنندگان تأییدشده می‌توانند این پست را بخوانند.')}</Text><Switch accessibilityLabel={tr('Confirm broader audience','تأیید مخاطب گسترده‌تر')} value={broaderConfirmed} onValueChange={setBroaderConfirmed}/></View>}
    <TextInput accessibilityLabel={tr('Content warning','هشدار محتوا')} value={contentWarning} onChangeText={setContentWarning} maxLength={120} placeholder={tr('Optional content warning','هشدار اختیاری محتوا')} style={styles.editor}/>
    <TextInput autoFocus multiline value={body} onChangeText={setBody} placeholder={t('sharePrompt')} placeholderTextColor={colors.muted} style={styles.editor} />

    <View><Text style={styles.step}>STEP 1</Text><Text style={styles.label}>What is this story mainly about? <Text style={styles.required}>*</Text></Text><Text style={styles.helper}>This helps people find experiences that feel relevant to them.</Text></View>
    <View style={styles.groupGrid}>{groupOptions.map(option => <Pressable key={option.key} onPress={() => chooseGroup(option.key as SupportCategoryGroup)} style={[styles.groupCard, group === option.key && { borderColor: option.accent || colors.ocean500, backgroundColor: option.softAccent || colors.ocean50 }]}>
      <View style={[styles.groupIcon, { backgroundColor: option.softAccent || colors.ocean50 }]}><AppIcon name={option.icon || 'category'} color={option.accent || colors.ocean600} /></View>
      <Text style={styles.groupTitle}>{option.label}</Text><Text style={styles.groupSubtitle}>{option.description}</Text>
      {group === option.key && <View style={[styles.check, { backgroundColor: option.accent || colors.ocean600 }]}><AppIcon name="check" size={14} color={colors.white} /></View>}
    </Pressable>)}</View>

    {group && <><View><Text style={styles.step}>STEP 2</Text><Text style={styles.label}>Choose the closest topic <Text style={styles.required}>*</Text></Text><Text style={styles.helper}>You can choose one primary topic. Add context in your story if more than one applies.</Text></View>
      <View style={styles.categoryGrid}>{visibleCategories.map(category => <Pressable key={category.id} onPress={() => { setCategoryId(category.id); setError(''); }} style={[styles.category, categoryId === category.id && { borderColor: category.color, backgroundColor: category.softColor }]}>
        <View style={[styles.categoryIcon, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={20} color={category.color} /></View>
        <View style={styles.categoryCopy}><Text style={[styles.categoryText, categoryId === category.id && { color: category.color }]}>{category.name}</Text><Text style={styles.categoryDescription}>{category.description}</Text></View>
        {categoryId === category.id && <AppIcon name="check_circle" filled size={20} color={category.color} />}
      </Pressable>)}</View></>}

    <Text style={styles.label}>What kind of post is this?</Text><View style={styles.types}>{postTypes.map(option => <Pressable key={option.key} onPress={() => setType(option.key)} style={[styles.type, type === option.key && styles.typeActive]}><Text style={[styles.typeText, type === option.key && styles.typeTextActive]}>{option.label}</Text></Pressable>)}</View>
    <Text style={styles.label}>{t('moodQuestion')}</Text><View style={styles.moods}>{moods.map(option => <Pressable key={option.key} onPress={() => setMood(option.label)} style={[styles.mood, mood === option.label && styles.moodActive]}><AppIcon name={option.icon || 'mood'} size={19} color={mood === option.label ? colors.white : colors.ocean600} /><Text style={[styles.moodText, mood === option.label && styles.moodTextActive]}>{option.label}</Text></Pressable>)}</View>

    <View style={styles.safetyNote}><AppIcon name="health_and_safety" color={colors.ocean600} /><View style={{ flex: 1 }}><Text style={styles.safetyTitle}>Share safely</Text><Text style={styles.safetyText}>For frightening or sensitive experiences, avoid names and identifying details. You can add a content note before publishing.</Text></View></View>
    <View style={styles.tools}><View style={styles.tool}><AppIcon name="visibility_off" color={colors.ocean600} /><Text style={styles.toolText}>{t('postAnonymously')}</Text><Switch value={anonymous} onValueChange={setAnonymous} trackColor={{ true: colors.ocean500 }} /></View><View style={styles.tool}><AppIcon name="shield" color={colors.coral} /><Text style={styles.toolText}>{t('contentWarning')}</Text><AppIcon name="chevron_right" color={colors.muted} /></View></View>
    {!!error && <Text style={styles.error}>{error}</Text>}
    <AppButton label={publishing ? 'Publishing…' : t('publish')} disabled={publishing} onPress={() => void publish()} />
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.lg },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, headerCopy: { alignItems: 'center' }, title: { fontSize: typography.h2, fontWeight: '900', color: colors.ink }, headerHint: { fontSize: 10, color: colors.muted, marginTop: 2 },
  author: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm }, authorName: { fontWeight: '900', color: colors.ink, textAlign: 'left' }, visibility: { flexDirection: 'row', alignItems: 'center', gap: 3, marginTop: 3 }, visibilityText: { fontSize: 11, color: colors.ocean600 },
  editor: { minHeight: 180, fontSize: 19, lineHeight: 30, color: colors.ink, textAlign: 'left', writingDirection: 'ltr', textAlignVertical: 'top', backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.lg },
  step: { color: colors.ocean600, fontSize: 10, fontWeight: '900', letterSpacing: 1.2, marginBottom: 4 }, label: { fontWeight: '900', color: colors.ink, textAlign: 'left', fontSize: 16 }, required: { color: colors.coral }, helper: { color: colors.muted, fontSize: 12, lineHeight: 18, marginTop: 4 },
  groupGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, groupCard: { flexGrow: 1, flexBasis: 190, minHeight: 132, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.white, padding: spacing.md, gap: 6 }, groupIcon: { width: 40, height: 40, borderRadius: radius.sm, alignItems: 'center', justifyContent: 'center' }, groupTitle: { fontWeight: '900', color: colors.ink, marginTop: 3 }, groupSubtitle: { fontSize: 11, color: colors.muted }, check: { position: 'absolute', top: 12, right: 12, width: 22, height: 22, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  categoryGrid: { gap: spacing.sm }, category: { minHeight: 76, flexDirection: 'row', alignItems: 'center', gap: spacing.sm, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.white, padding: spacing.sm }, categoryIcon: { width: 42, height: 42, borderRadius: radius.sm, alignItems: 'center', justifyContent: 'center' }, categoryCopy: { flex: 1 }, categoryText: { fontSize: 13, color: colors.ink, fontWeight: '900' }, categoryDescription: { fontSize: 10, color: colors.muted, marginTop: 3 },
  types: { flexDirection: 'row', gap: spacing.sm, flexWrap: 'wrap' }, type: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radius.pill, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.white }, typeActive: { backgroundColor: colors.ocean900, borderColor: colors.ocean900 }, typeText: { fontSize: 12, color: colors.muted, fontWeight: '700' }, typeTextActive: { color: colors.white },
  moods: { flexDirection: 'row', gap: spacing.sm, flexWrap: 'wrap' }, mood: { flexDirection: 'row', alignItems: 'center', gap: 5, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: spacing.sm, backgroundColor: colors.ocean50, borderWidth: 1, borderColor: colors.ocean200 }, moodActive: { backgroundColor: colors.ocean600, borderColor: colors.ocean600 }, moodText: { fontSize: 12, color: colors.ocean700, fontWeight: '800' }, moodTextActive: { color: colors.white },
  safetyNote: { flexDirection: 'row', gap: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.md, padding: spacing.md }, safetyTitle: { color: colors.ink, fontSize: 12, fontWeight: '900' }, safetyText: { color: colors.muted, fontSize: 10, lineHeight: 16, marginTop: 3 },
  tools: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, overflow: 'hidden' }, tool: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border }, toolText: { flex: 1, color: colors.inkSoft, fontWeight: '700' }, error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
});
