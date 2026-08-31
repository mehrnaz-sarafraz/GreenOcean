import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { SupportCategoryGroup } from '@/features/content/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

const moods = [
  ['sentiment_satisfied', 'Calm'],
  ['wb_sunny', 'Hopeful'],
  ['cloud', 'Low'],
  ['air', 'Anxious'],
  ['bolt', 'Overwhelmed'],
];

const postTypes = [
  ['EXPERIENCE', 'Lived experience'],
  ['QUESTION', 'Seeking support'],
  ['REFLECTION', 'Reflection'],
];

const groupOptions: { id: SupportCategoryGroup; title: string; subtitle: string; icon: string; color: string; softColor: string }[] = [
  { id: 'EMOTION', title: 'An emotion', subtitle: 'How I feel right now', icon: 'mood', color: colors.sky, softColor: colors.skySoft },
  { id: 'CONDITION', title: 'A condition', subtitle: 'Symptoms or a diagnosis', icon: 'psychology', color: colors.lavender, softColor: colors.lavenderSoft },
  { id: 'LIFE_EXPERIENCE', title: 'A life experience', subtitle: 'Something I lived through', icon: 'auto_stories', color: colors.coral, softColor: colors.coralSoft },
];

export default function Create() {
  const { t } = useLanguage();
  const { categories, profile, refresh } = usePlatformData();
  const [body, setBody] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [mood, setMood] = useState('');
  const [group, setGroup] = useState<SupportCategoryGroup | null>(null);
  const [categoryId, setCategoryId] = useState('');
  const [type, setType] = useState('EXPERIENCE');
  const [error, setError] = useState('');
  const [publishing, setPublishing] = useState(false);
  const visibleCategories = useMemo(() => categories.filter(category => category.group === group), [group]);

  function chooseGroup(next: SupportCategoryGroup) {
    setGroup(next);
    setCategoryId('');
    setError('');
  }

  async function publish() {
    if (!body.trim() || !group || !categoryId) {
      setError(!group ? 'Choose whether your story is about an emotion, condition, or life experience.' : !categoryId ? 'Choose the topic that fits your story best.' : t('requiredFields'));
      return;
    }
    setPublishing(true); setError('');
    try {
      await apiRequest('/api/v1/posts', { method: 'POST', body: JSON.stringify({
        body: body.trim(), anonymous, visibility: 'PUBLIC', communityId: null,
        contentWarning: null, categoryId, postType: type, mood: mood || null,
      }) });
      await refresh();
      router.replace('/(app)');
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not publish this story');
    } finally {
      setPublishing(false);
    }
  }

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.header}>
      <Pressable onPress={() => router.back()}><AppIcon name="close" color={colors.ink} /></Pressable>
      <View style={styles.headerCopy}><Text style={styles.title}>{t('createPost')}</Text><Text style={styles.headerHint}>Your story, in the right space</Text></View>
      <View style={{ width: 24 }} />
    </View>

    <View style={styles.author}>
      <Avatar name={anonymous ? t('anonymous') : (profile?.displayName ?? 'GreenOcean member')} uri={anonymous ? null : profile?.avatarUrl} size={44} />
      <View><Text style={styles.authorName}>{anonymous ? t('anonymous') : (profile?.displayName ?? 'GreenOcean member')}</Text><View style={styles.visibility}><AppIcon name="public" size={14} color={colors.ocean600} /><Text style={styles.visibilityText}>{t('public')}</Text></View></View>
    </View>

    <TextInput autoFocus multiline value={body} onChangeText={setBody} placeholder={t('sharePrompt')} placeholderTextColor={colors.muted} style={styles.editor} />

    <View><Text style={styles.step}>STEP 1</Text><Text style={styles.label}>What is this story mainly about? <Text style={styles.required}>*</Text></Text><Text style={styles.helper}>This helps people find experiences that feel relevant to them.</Text></View>
    <View style={styles.groupGrid}>{groupOptions.map(option => <Pressable key={option.id} onPress={() => chooseGroup(option.id)} style={[styles.groupCard, group === option.id && { borderColor: option.color, backgroundColor: option.softColor }]}>
      <View style={[styles.groupIcon, { backgroundColor: option.softColor }]}><AppIcon name={option.icon} color={option.color} /></View>
      <Text style={styles.groupTitle}>{option.title}</Text><Text style={styles.groupSubtitle}>{option.subtitle}</Text>
      {group === option.id && <View style={[styles.check, { backgroundColor: option.color }]}><AppIcon name="check" size={14} color={colors.white} /></View>}
    </Pressable>)}</View>

    {group && <><View><Text style={styles.step}>STEP 2</Text><Text style={styles.label}>Choose the closest topic <Text style={styles.required}>*</Text></Text><Text style={styles.helper}>You can choose one primary topic. Add context in your story if more than one applies.</Text></View>
      <View style={styles.categoryGrid}>{visibleCategories.map(category => <Pressable key={category.id} onPress={() => { setCategoryId(category.id); setError(''); }} style={[styles.category, categoryId === category.id && { borderColor: category.color, backgroundColor: category.softColor }]}>
        <View style={[styles.categoryIcon, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={20} color={category.color} /></View>
        <View style={styles.categoryCopy}><Text style={[styles.categoryText, categoryId === category.id && { color: category.color }]}>{category.name}</Text><Text style={styles.categoryDescription}>{category.description}</Text></View>
        {categoryId === category.id && <AppIcon name="check_circle" filled size={20} color={category.color} />}
      </Pressable>)}</View></>}

    <Text style={styles.label}>What kind of post is this?</Text><View style={styles.types}>{postTypes.map(([id, label]) => <Pressable key={id} onPress={() => setType(id)} style={[styles.type, type === id && styles.typeActive]}><Text style={[styles.typeText, type === id && styles.typeTextActive]}>{label}</Text></Pressable>)}</View>
    <Text style={styles.label}>{t('moodQuestion')}</Text><View style={styles.moods}>{moods.map(([icon, label]) => <Pressable key={label} onPress={() => setMood(label)} style={[styles.mood, mood === label && styles.moodActive]}><AppIcon name={icon} size={19} color={mood === label ? colors.white : colors.ocean600} /><Text style={[styles.moodText, mood === label && styles.moodTextActive]}>{label}</Text></Pressable>)}</View>

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
