import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

const topics = ['Anxiety', 'Depression', 'Sleep', 'Relationships', 'Trauma', 'ADHD'];

export default function ArticleEditor() {
  const [topic, setTopic] = useState('Anxiety');
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [body, setBody] = useState('');
  const [references, setReferences] = useState('');
  const [pinned, setPinned] = useState(true);
  const [error, setError] = useState('');
  function publish() { if (!title.trim() || !summary.trim() || !body.trim()) { setError('Add a title, summary, and article body before publishing.'); return; } router.replace('/articles'); }
  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.top}><Pressable onPress={() => router.back()} style={styles.back}><AppIcon name="close" color={colors.ocean700} /></Pressable><View style={{ alignItems: 'center' }}><Text style={styles.title}>Professional article studio</Text><Text style={styles.subtitle}>Educational content by verified professionals</Text></View><Pressable style={styles.draft}><Text style={styles.draftText}>Save draft</Text></Pressable></View>
    <View style={styles.author}><Avatar name="Dr. Maya Bennett" size={48} verified /><View style={{ flex: 1 }}><Text style={styles.authorName}>Dr. Maya Bennett</Text><Text style={styles.authorMeta}>Clinical Psychologist · Verified author</Text></View><View style={styles.score}><Text style={styles.scoreValue}>96</Text><Text style={styles.scoreLabel}>SCORE</Text></View></View>
    <View style={styles.notice}><AppIcon name="science" color={colors.ocean600} /><View style={{ flex: 1 }}><Text style={styles.noticeTitle}>Professional publishing standards</Text><Text style={styles.noticeText}>Use clear sources, separate education from personal medical advice, and include content notes where needed. Articles enter editorial review before publication.</Text></View></View>
    <FieldLabel step="1" title="Article topic" required /><View style={styles.topics}>{topics.map(item => <Pressable key={item} onPress={() => setTopic(item)} style={[styles.topic, topic === item && styles.topicActive]}><Text style={[styles.topicText, topic === item && styles.topicTextActive]}>{item}</Text></Pressable>)}</View>
    <FieldLabel step="2" title="Title" required /><TextInput value={title} onChangeText={setTitle} placeholder="Write a clear, specific title" placeholderTextColor={colors.muted} style={styles.input} />
    <FieldLabel step="3" title="Short summary" required /><TextInput value={summary} onChangeText={setSummary} multiline placeholder="What will readers understand or be able to try after reading?" placeholderTextColor={colors.muted} style={[styles.input, styles.summaryInput]} />
    <FieldLabel step="4" title="Article body" required /><TextInput value={body} onChangeText={setBody} multiline placeholder="Use short sections, accessible language, and practical examples…" placeholderTextColor={colors.muted} style={[styles.input, styles.bodyInput]} />
    <FieldLabel step="5" title="Sources and review notes" /><TextInput value={references} onChangeText={setReferences} multiline placeholder="Add research links, clinical guidelines, DOI references, or editorial notes" placeholderTextColor={colors.muted} style={[styles.input, styles.summaryInput]} />
    <View style={styles.pinSetting}><View style={styles.pinIcon}><AppIcon name="push_pin" filled color={colors.ocean700} /></View><View style={{ flex: 1 }}><Text style={styles.pinTitle}>Pin to professional profile</Text><Text style={styles.pinText}>Keep this article at the top of your profile and feature it in the Knowledge Hub.</Text></View><Switch value={pinned} onValueChange={setPinned} trackColor={{ true: colors.ocean500 }} /></View>
    {!!error && <Text style={styles.error}>{error}</Text>}<AppButton label="Submit for editorial review" onPress={publish} />
    <Text style={styles.footerNote}>Publishing does not make the article medical advice. GreenOcean may add safety notes, request revisions, or remove content that does not meet professional standards.</Text>
  </View></Screen>;
}

function FieldLabel({ step, title, required }: { step: string; title: string; required?: boolean }) { return <View><Text style={styles.step}>STEP {step}</Text><Text style={styles.label}>{title}{required && <Text style={styles.required}> *</Text>}</Text></View>; }
const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md }, top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, back: { width: 40, height: 40, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' }, title: { color: colors.ink, fontSize: typography.h3, fontWeight: '900' }, subtitle: { color: colors.muted, fontSize: 9, marginTop: 2 }, draft: { paddingHorizontal: spacing.sm, paddingVertical: 7 }, draftText: { color: colors.ocean600, fontSize: 10, fontWeight: '900' },
  author: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.ocean950, borderRadius: radius.lg, padding: spacing.md }, authorName: { color: colors.white, fontWeight: '900' }, authorMeta: { color: colors.ocean300, fontSize: 9, marginTop: 3 }, score: { width: 46, height: 46, borderRadius: 23, backgroundColor: colors.white, alignItems: 'center', justifyContent: 'center' }, scoreValue: { color: colors.ocean800, fontWeight: '900', fontSize: 15 }, scoreLabel: { color: colors.muted, fontWeight: '900', fontSize: 6 }, notice: { flexDirection: 'row', gap: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.lg, padding: spacing.md }, noticeTitle: { color: colors.ink, fontSize: 11, fontWeight: '900' }, noticeText: { color: colors.muted, fontSize: 10, lineHeight: 16, marginTop: 3 },
  step: { color: colors.ocean600, fontSize: 9, fontWeight: '900', letterSpacing: 1 }, label: { color: colors.ink, fontSize: 14, fontWeight: '900', marginTop: 3 }, required: { color: colors.coral }, topics: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, topic: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 9 }, topicActive: { backgroundColor: colors.ocean900, borderColor: colors.ocean900 }, topicText: { color: colors.muted, fontSize: 11, fontWeight: '800' }, topicTextActive: { color: colors.white }, input: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.md, padding: spacing.md, color: colors.ink, fontSize: 14, textAlign: 'left', writingDirection: 'ltr' }, summaryInput: { minHeight: 90, textAlignVertical: 'top' }, bodyInput: { minHeight: 250, textAlignVertical: 'top', lineHeight: 23 }, pinSetting: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md }, pinIcon: { width: 42, height: 42, borderRadius: radius.sm, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, pinTitle: { color: colors.ink, fontSize: 12, fontWeight: '900' }, pinText: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: 3 }, error: { color: colors.danger, fontSize: 11, fontWeight: '800' }, footerNote: { color: colors.muted, fontSize: 9, lineHeight: 15, textAlign: 'center', paddingHorizontal: spacing.lg },
});
