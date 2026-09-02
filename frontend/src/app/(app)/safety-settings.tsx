import { Linking, Pressable, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { router } from 'expo-router';
import { useState } from 'react';

import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { usePlatformData } from '@/features/platform/data-provider';
import { UserPreferences } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function SafetySettings() {
  const { preferences, profileStats, setPreferences } = usePlatformData();
  const [word, setWord] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function update(changes: Partial<UserPreferences>) {
    if (!preferences || saving) return;
    const previous = preferences;
    setPreferences({ ...preferences, ...changes }); setSaving(true); setError('');
    try {
      const saved = await apiRequest<UserPreferences>('/api/v1/preferences/me', { method: 'PATCH', body: JSON.stringify(changes) });
      setPreferences(saved);
    } catch (caught) {
      setPreferences(previous);
      setError(caught instanceof Error ? caught.message : 'Could not save safety settings');
    } finally { setSaving(false); }
  }

  function addWord() {
    const value = word.trim();
    if (!preferences || !value || preferences.mutedTerms.includes(value)) return;
    setWord('');
    void update({ mutedTerms: [...preferences.mutedTerms, value] });
  }

  if (!preferences) return <Screen style={styles.screen}><Text style={styles.state}>Loading safety settings…</Text></Screen>;

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.top}><Pressable onPress={() => router.back()} style={styles.back}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><View style={styles.saved}><AppIcon name={saving ? 'sync' : 'check_circle'} filled size={16} color={colors.ocean600} /><Text style={styles.savedText}>{saving ? 'Saving changes…' : 'Changes save automatically'}</Text></View></View>
    <View><Text style={styles.eyebrow}>YOUR SPACE, YOUR BOUNDARIES</Text><Text style={styles.title}>Privacy & safety</Text><Text style={styles.subtitle}>Choose what you see, who can reach you, and which topics need an extra pause before they appear.</Text></View>
    {!!error && <Text style={styles.error}>{error}</Text>}
    <View style={styles.score}><View style={styles.scoreRing}><Text style={styles.scoreValue}>{preferences.strongerContentControls ? 'Strong' : 'Standard'}</Text></View><View style={{ flex: 1 }}><Text style={styles.scoreTitle}>Your safety controls are active</Text><Text style={styles.scoreText}>These choices are stored in your GreenOcean account and follow you across devices.</Text></View></View>
    <Section title="Feed controls" subtitle="Personalise sensitive content without silencing supportive conversations">
      <Toggle icon="blur_on" title="Blur sensitive posts" subtitle="Show a content note before distressing text or media" value={preferences.blurSensitiveContent} onChange={value => void update({ blurSensitiveContent: value })} />
      <Toggle icon="medication" title="Reduce medication discussions" subtitle="Show fewer posts focused on medication experiences" value={preferences.reduceMedicationContent} onChange={value => void update({ reduceMedicationContent: value })} />
    </Section>
    <Section title="Message controls" subtitle="Decide how new conversations begin">
      <Toggle icon="mark_unread_chat_alt" title="Allow message requests" subtitle="People you do not follow can send a request" value={preferences.allowMessageRequests} onChange={value => void update({ allowMessageRequests: value })} />
      <Toggle icon="verified" title="Professionals only" subtitle="Only verified professionals can start a new request" value={preferences.professionalsOnlyMessages} onChange={value => void update({ professionalsOnlyMessages: value })} />
    </Section>
    <Section title="Muted words & topics" subtitle="Posts containing these phrases can be removed from personalised feeds">
      <View style={styles.addRow}><TextInput value={word} onChangeText={setWord} onSubmitEditing={addWord} placeholder="Add a word or phrase" placeholderTextColor={colors.muted} style={styles.input} /><Pressable onPress={addWord} style={styles.addButton}><AppIcon name="add" color={colors.white} /></Pressable></View>
      <View style={styles.chips}>{preferences.mutedTerms.map(item => <Pressable key={item} onPress={() => void update({ mutedTerms: preferences.mutedTerms.filter(value => value !== item) })} style={styles.chip}><Text style={styles.chipText}>{item}</Text><AppIcon name="close" size={14} color={colors.muted} /></Pressable>)}</View>
    </Section>
    <Section title="Blocked accounts" subtitle="Blocked people cannot view or contact you"><View style={styles.row}><View style={styles.rowIcon}><AppIcon name="block" color={colors.ocean700} /></View><View style={{ flex: 1 }}><Text style={styles.rowTitle}>Blocked accounts</Text><Text style={styles.rowText}>{profileStats?.blockedAccounts ?? 0} accounts currently blocked</Text></View></View></Section>
    <View style={styles.help}><AppIcon name="health_and_safety" color={colors.ocean600} /><View style={{ flex: 1 }}><Text style={styles.helpTitle}>Need urgent or crisis support?</Text><Text style={styles.helpText}>Open a directory of verified helplines for your country.</Text></View><Pressable onPress={() => void Linking.openURL('https://findahelpline.com/')} style={styles.helpButton}><Text style={styles.helpButtonText}>Find a helpline</Text></Pressable></View>
  </View></Screen>;
}

function Section({ title, subtitle, children }: { title: string; subtitle: string; children: React.ReactNode }) { return <View style={styles.section}><View><Text style={styles.sectionTitle}>{title}</Text><Text style={styles.sectionSubtitle}>{subtitle}</Text></View><View style={styles.sectionCard}>{children}</View></View>; }
function Toggle({ icon, title, subtitle, value, onChange }: { icon: string; title: string; subtitle: string; value: boolean; onChange: (value: boolean) => void }) { return <View style={styles.toggle}><View style={styles.rowIcon}><AppIcon name={icon} color={colors.ocean700} /></View><View style={{ flex: 1 }}><Text style={styles.rowTitle}>{title}</Text><Text style={styles.rowText}>{subtitle}</Text></View><Switch value={value} onValueChange={onChange} trackColor={{ true: colors.ocean500 }} /></View>; }

const styles = StyleSheet.create({
  screen: { padding: 0 }, state: { color: colors.muted, textAlign: 'center', marginTop: spacing.xxl }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.lg }, top: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }, back: { width: 40, height: 40, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' }, saved: { flexDirection: 'row', alignItems: 'center', gap: 5 }, savedText: { color: colors.ocean600, fontSize: 9, fontWeight: '800' }, eyebrow: { color: colors.ocean600, fontSize: 9, fontWeight: '900', letterSpacing: 1.3 }, title: { color: colors.ink, fontSize: typography.h1, fontWeight: '900', marginTop: 4 }, subtitle: { color: colors.muted, fontSize: 12, lineHeight: 19, marginTop: spacing.sm }, error: { color: colors.danger, textAlign: 'center', fontSize: 11 },
  score: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.ocean950, borderRadius: radius.xl, padding: spacing.lg }, scoreRing: { width: 66, height: 66, borderRadius: 33, borderWidth: 5, borderColor: colors.ocean300, alignItems: 'center', justifyContent: 'center' }, scoreValue: { color: colors.white, fontSize: 10, fontWeight: '900' }, scoreTitle: { color: colors.white, fontSize: 14, fontWeight: '900' }, scoreText: { color: colors.ocean200, fontSize: 9, lineHeight: 15, marginTop: 4 },
  section: { gap: spacing.sm }, sectionTitle: { color: colors.ink, fontSize: typography.h3, fontWeight: '900' }, sectionSubtitle: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: 3 }, sectionCard: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, overflow: 'hidden' }, toggle: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border }, rowIcon: { width: 38, height: 38, borderRadius: radius.sm, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, rowTitle: { color: colors.ink, fontSize: 11, fontWeight: '900' }, rowText: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: 3 },
  addRow: { flexDirection: 'row', gap: spacing.sm, padding: spacing.md }, input: { flex: 1, minHeight: 46, borderRadius: radius.md, backgroundColor: colors.foam, borderWidth: 1, borderColor: colors.border, paddingHorizontal: spacing.md, color: colors.ink, fontSize: 12 }, addButton: { width: 46, height: 46, borderRadius: radius.md, backgroundColor: colors.ocean600, alignItems: 'center', justifyContent: 'center' }, chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm, padding: spacing.md, paddingTop: 0 }, chip: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.foam, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 6 }, chipText: { color: colors.inkSoft, fontSize: 9, fontWeight: '700' }, row: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md },
  help: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.lg, padding: spacing.md }, helpTitle: { color: colors.ink, fontSize: 11, fontWeight: '900' }, helpText: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: 3 }, helpButton: { borderWidth: 1, borderColor: colors.ocean300, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 8 }, helpButtonText: { color: colors.ocean700, fontSize: 9, fontWeight: '900' },
});
