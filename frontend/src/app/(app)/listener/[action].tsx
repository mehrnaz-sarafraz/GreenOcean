import {WaitingQueue} from '@/features/live-support/queue';
import {SupportBookings,ListenerCalendarScreen} from '@/features/live-support/planning';
import { LiveMember } from '@/features/live-support/member';
import { ListenerWorkspace } from '@/features/live-support/workspace';
import { router, useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { ActionRow, EmptyState, Notice, Pill, ProductCard, ProductHeader, ProductPage, ProductSection } from '@/components/product-ui';
import { Listener, ListenerApplication, ListenerTraining } from '@/features/platform/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { optionsIn } from '@/features/platform/catalog';
import { apiRequest } from '@/lib/api/client';
import { colors, radius, spacing } from '@/theme/tokens';

export default function ListenerFlow() {
  const { action } = useLocalSearchParams<{ action?: string }>(); const current = String(action || 'match');
  if (current === 'match') return <LiveMember />;
  if (current === 'queue') return <WaitingQueue />;
  if (current === 'bookings') return <SupportBookings />;
  if (current === 'calendar') return <ListenerCalendarScreen />;
  if (current === 'workspace') return <ListenerWorkspace />;
  if (current === 'apply') return <Apply />;
  if (current === 'training') return <Training />;
  if (current === 'profile') return <ListenerProfile />;
  return <ProductPage><ProductHeader title="Listener page not found" subtitle="This support workflow is unavailable." /></ProductPage>;
}


function ListenerCard({ listener }: { listener: Listener }) {
  return <ProductCard tone="ocean"><View style={styles.identity}><Avatar name={listener.displayName} uri={listener.avatarUrl || undefined} size={68} /><View style={styles.flex}><Text style={styles.lightTitle}>{listener.displayName}</Text><Text style={styles.light}>{listener.displayTitle} · {listener.listeningStyle}</Text><Text style={styles.light}>{listener.languages.join(' · ')} · {listener.completedSessions} conversations</Text></View><Pill label={listener.online ? 'Recent readiness evidence' : 'Not ready for live requests'} active={listener.online} /></View></ProductCard>;
}

function Apply() {
  const { options } = usePlatformData(); const availability = optionsIn(options, 'LISTENER_AVAILABILITY');
  const [why, setWhy] = useState(''); const [experience, setExperience] = useState('');
  const [hours, setHours] = useState(''); const selectedHours = hours || availability[0]?.label || ''; const [agreed, setAgreed] = useState(false);
  const [result, setResult] = useState<ListenerApplication | null>(null); const [busy, setBusy] = useState(false); const [error, setError] = useState('');
  const submit = async () => { if (!selectedHours) return; setBusy(true); setError(''); try { setResult(await apiRequest<ListenerApplication>('/api/v1/listener-applications', { method: 'POST', body: JSON.stringify({ motivation: why, experience, weeklyAvailability: selectedHours, acceptedCode: agreed }) })); } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not submit application'); } finally { setBusy(false); } };
  if (result) return <ProductPage><ProductHeader eyebrow="APPLICATION SUBMITTED" title="Training comes before matching" subtitle="Trust and safety can now review your listener application." /><Notice title={result.status} body={`Application ${result.id} was submitted ${new Date(result.submittedAt).toLocaleString()}.`} /><AppButton label="Open listener training" onPress={() => router.replace({ pathname: '/listener/[action]', params: { action: 'training' } })} /></ProductPage>;
  return <ProductPage><ProductHeader eyebrow="LISTENER APPLICATION" title="Why listening?" subtitle="Great listeners create space without taking control of someone else's choices." /><ProductCard><Field label="Why do you want to volunteer?" value={why} onChangeText={setWhy} multiline /><Field label="Relevant lived, volunteer, or community experience" value={experience} onChangeText={setExperience} multiline /><Text style={styles.label}>Weekly availability</Text><View style={styles.wrap}>{availability.map(item => <Pill key={item.key} label={item.label} active={selectedHours === item.label} onPress={() => setHours(item.label)} />)}</View><Text style={styles.title}>Safety boundaries</Text>{['No diagnosis or therapy','No medication direction','No off-platform contact','Escalate immediate safety concerns','Protect privacy and report abuse'].map(item => <View key={item} style={styles.rule}><AppIcon name="check_circle" filled size={18} color={colors.ocean600} /><Text style={styles.body}>{item}</Text></View>)}<Pressable onPress={() => setAgreed(value => !value)} style={styles.rule}><AppIcon name={agreed ? 'check_box' : 'check_box_outline_blank'} filled={agreed} color={colors.ocean600} /><Text style={styles.body}>I accept the Listener Code of Conduct.</Text></Pressable>{!!error && <Text style={styles.error}>{error}</Text>}<AppButton label="Submit application" loading={busy} disabled={why.trim().length < 40 || experience.trim().length < 20 || !selectedHours || !agreed} onPress={() => void submit()} /></ProductCard></ProductPage>;
}

function Training() {
  const [modules, setModules] = useState<ListenerTraining['modules']>([]); const [done, setDone] = useState<string[]>([]); const [applicationStatus, setApplicationStatus] = useState(''); const [activated, setActivated] = useState(false); const [error, setError] = useState('');
  const applyState = useCallback((state: ListenerTraining) => { setModules(state.modules); setDone(state.completedModules); setApplicationStatus(state.applicationStatus); setActivated(state.listenerActivated); }, []);
  useEffect(() => { apiRequest<ListenerTraining>('/api/v1/listener-training/me').then(applyState).catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load training progress.')); }, [applyState]);
  const complete = async (moduleKey: string) => { if (!done.includes(moduleKey)) applyState(await apiRequest<ListenerTraining>(`/api/v1/listener-training/me/modules/${moduleKey}`, { method: 'PUT' })); };
  return <ProductPage><ProductHeader eyebrow="LISTENER ACADEMY" title="Training & supervision" subtitle="Complete all modules before accepting a real conversation." />{!!error && <Text style={styles.error}>{error}</Text>}<ProductCard tone="soft"><Text style={styles.title}>{done.length} of {modules.length} complete</Text><View style={styles.track}><View style={[styles.progress, { width: `${modules.length ? done.length / modules.length * 100 : 0}%` }]} /></View><Text style={styles.meta}>Application: {applicationStatus || 'loading'} · Listener access: {activated ? 'active' : 'not active'}</Text></ProductCard>{modules.map((module, index) => <ActionRow key={module.key} icon={done.includes(module.key) ? 'check_circle' : 'school'} title={`${index + 1}. ${module.title}`} subtitle={module.description} value={done.includes(module.key) ? 'Complete' : 'Complete module'} onPress={() => void complete(module.key)} />)}{!!modules.length && done.length === modules.length && <Notice title="Coursework complete" body="A supervisor must still approve the application and practice conversation before the listener profile is activated." />}</ProductPage>;
}

function ListenerProfile() {
  const [listener, setListener] = useState<Listener | null>(null);
  useEffect(() => { apiRequest<Listener[]>('/api/v1/listeners').then(items => setListener(items[0] || null)).catch(() => undefined); }, []);
  return <ProductPage><ProductHeader title="Listener profile" subtitle="Transparent boundaries, availability, experience, and safety feedback." />{listener ? <><ListenerCard listener={listener} /><ProductSection title="Experience"><ActionRow icon="school" title="GreenOcean listener standards" subtitle="Boundaries, reflective listening, safety, and inclusion" /><ActionRow icon="forum" title={`${listener.completedSessions} support conversations`} subtitle="Peer support, not a clinical safety rating" /><ActionRow icon="psychology" title="Support topics" subtitle={listener.topics.join(', ')} /></ProductSection></> : <EmptyState icon="support_agent" title="No listener profile" body="No active listener is currently available." />}<Notice title="Not a professional profile" body="Peer listeners do not provide diagnosis, treatment, or emergency response." /><AppButton label="Request a listener match" onPress={() => router.push({ pathname: '/listener/[action]', params: { action: 'match' } })} /></ProductPage>;
}


function Field({ label, value, onChangeText, multiline }: { label: string; value: string; onChangeText: (value: string) => void; multiline?: boolean }) { return <View style={styles.field}><Text style={styles.label}>{label}</Text><TextInput value={value} onChangeText={onChangeText} multiline={multiline} style={[styles.input, multiline && styles.multiline]} /></View>; }
const styles = StyleSheet.create({ flex: { flex: 1 }, identity: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, lightTitle: { color: colors.white, fontSize: 20, fontWeight: '900' }, light: { color: colors.ocean200, fontSize: 10, lineHeight: 17, marginTop: 3 }, wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, link: { alignItems: 'center', padding: spacing.sm }, linkText: { color: colors.ocean700, fontSize: 11, fontWeight: '900' }, label: { color: colors.ink, fontSize: 11, fontWeight: '900' }, field: { gap: 6 }, input: { minHeight: 48, borderWidth: 1, borderColor: colors.borderStrong, borderRadius: radius.md, backgroundColor: colors.white, paddingHorizontal: spacing.md, color: colors.ink, textAlign: 'left', writingDirection: 'ltr' }, multiline: { minHeight: 100, paddingTop: spacing.md, textAlignVertical: 'top' }, title: { color: colors.ink, fontSize: 13, fontWeight: '900' }, body: { flex: 1, color: colors.inkSoft, fontSize: 10, lineHeight: 17 }, meta: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: spacing.sm }, rule: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: 5 }, error: { color: colors.danger, fontSize: 11, fontWeight: '800' }, track: { height: 8, backgroundColor: colors.white, borderRadius: 4, overflow: 'hidden' }, progress: { height: 8, backgroundColor: colors.ocean500 } });
