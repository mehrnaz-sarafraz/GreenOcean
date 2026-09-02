import { router } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { useAuth } from '@/features/auth/auth-provider';
import { AdminDashboard } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

type Section = 'OVERVIEW' | 'REPORTS' | 'VERIFICATION' | 'MEMBERS' | 'AUDIT';
const navigation: [Section, string, string][] = [
  ['OVERVIEW', 'Overview', 'dashboard'], ['REPORTS', 'Reports', 'flag'],
  ['VERIFICATION', 'Verification', 'verified'], ['MEMBERS', 'Members', 'group'], ['AUDIT', 'Audit log', 'history'],
];

export default function Admin() {
  const { user } = useAuth();
  const allowed = user?.roles.some(role => role === 'ADMIN' || role === 'MODERATOR');
  const [section, setSection] = useState<Section>('OVERVIEW');
  const [dashboard, setDashboard] = useState<AdminDashboard | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    if (!allowed) return;
    setLoading(true); setError('');
    try { setDashboard(await apiRequest<AdminDashboard>('/api/v1/admin/dashboard')); }
    catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not load moderation data'); }
    finally { setLoading(false); }
  }, [allowed]);
  useEffect(() => { void load(); }, [load]);

  async function reportAction(id: string, action: 'APPROVE' | 'REVIEW' | 'REMOVE') {
    await apiRequest(`/api/v1/admin/reports/${id}`, { method: 'PATCH', body: JSON.stringify({ action }) });
    await load();
  }
  async function verificationAction(id: string, action: 'APPROVE_VERIFICATION' | 'REJECT_VERIFICATION' | 'REQUEST_INFO') {
    await apiRequest(`/api/v1/admin/verifications/${id}`, { method: 'PATCH', body: JSON.stringify({ action }) });
    await load();
  }

  if (!allowed) return <SafeAreaView style={styles.safe}><View style={styles.restricted}><AppIcon name="lock" size={42} color={colors.ocean600} /><Text style={styles.pageTitle}>Restricted area</Text><Text style={styles.pageSubtitle}>Only moderators and administrators can open trust and safety operations.</Text><Pressable onPress={() => router.back()} style={styles.primary}><Text style={styles.primaryText}>Go back</Text></Pressable></View></SafeAreaView>;

  return <SafeAreaView style={styles.safe}><View style={styles.shell}>
    <View style={styles.top}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><View style={{ flex: 1 }}><Text style={styles.title}>GreenOcean Admin</Text><Text style={styles.subtitle}>Trust, safety, and community operations</Text></View><Pressable onPress={() => void load()}><AppIcon name="refresh" color={colors.ocean700} /></Pressable></View>
    <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.nav}>{navigation.map(([id, label, icon]) => <Pressable key={id} onPress={() => setSection(id)} style={[styles.navItem, section === id && styles.navActive]}><AppIcon name={icon} size={18} color={section === id ? colors.white : colors.muted} /><Text style={[styles.navText, section === id && styles.navTextActive]}>{label}</Text>{id === 'REPORTS' && !!dashboard?.stats.openReports && <View style={styles.badge}><Text style={styles.badgeText}>{dashboard.stats.openReports}</Text></View>}</Pressable>)}</ScrollView>
    <ScrollView contentContainerStyle={styles.page} showsVerticalScrollIndicator={false}>
      {loading && !dashboard && <Text style={styles.state}>Loading operations…</Text>}
      {!!error && <Text style={styles.error}>{error}</Text>}
      {dashboard && section === 'OVERVIEW' && <Overview data={dashboard} go={setSection} />}
      {dashboard && section === 'REPORTS' && <Reports data={dashboard} act={reportAction} />}
      {dashboard && section === 'VERIFICATION' && <Verifications data={dashboard} act={verificationAction} />}
      {dashboard && section === 'MEMBERS' && <Members data={dashboard} />}
      {dashboard && section === 'AUDIT' && <Audit data={dashboard} />}
    </ScrollView>
  </View></SafeAreaView>;
}

function Overview({ data, go }: { data: AdminDashboard; go: (section: Section) => void }) {
  const stats = data.stats;
  return <><View><Text style={styles.pageTitle}>Operations overview</Text><Text style={styles.pageSubtitle}>Live counts calculated from GreenOcean’s database.</Text></View>
    {stats.criticalReports > 0 && <Pressable onPress={() => go('REPORTS')} style={styles.alert}><AppIcon name="emergency" color={colors.white} /><View style={{ flex: 1 }}><Text style={styles.alertTitle}>{stats.criticalReports} critical reports require review</Text><Text style={styles.alertText}>Open the queue and review the full context.</Text></View><AppIcon name="arrow_forward" color={colors.white} /></Pressable>}
    <View style={styles.kpis}><Kpi label="Members" value={stats.members} icon="group" /><Kpi label="Active today" value={stats.activeToday} icon="bolt" /><Kpi label="Posts today" value={stats.postsToday} icon="article" /><Kpi label="Open reports" value={stats.openReports} icon="flag" /><Kpi label="Verified professionals" value={stats.verifiedProfessionals} icon="verified" /><Kpi label="Pending verification" value={stats.pendingVerifications} icon="pending_actions" /></View>
    <Panel title="Reports by reason">{data.reasonBreakdown.length ? data.reasonBreakdown.map(item => <View key={item.name} style={styles.reason}><View style={[styles.reasonDot, { backgroundColor: item.color }]} /><Text style={styles.reasonName}>{item.name}</Text><Text style={styles.reasonValue}>{item.value}%</Text></View>) : <Text style={styles.empty}>No reports in the last 30 days.</Text>}</Panel>
  </>;
}

function Reports({ data, act }: { data: AdminDashboard; act: (id: string, action: 'APPROVE' | 'REVIEW' | 'REMOVE') => Promise<void> }) {
  return <><Heading title="Reports queue" subtitle="User reports and automated safety signals." />{data.reports.map(report => <View key={report.id} style={styles.card}><View style={styles.cardTop}><Text style={[styles.severity, report.severity === 'CRITICAL' && styles.critical]}>{report.severity}</Text><Text style={styles.time}>{new Date(report.createdAt).toLocaleString()}</Text></View><Text style={styles.cardTitle}>{report.reason}</Text><Text style={styles.cardText}>{report.summary || 'No additional description supplied.'}</Text><Text style={styles.meta}>@{report.reportedUser} · {report.targetType} · {report.status}</Text><View style={styles.tags}>{report.signals.map(signal => <Text key={signal} style={styles.tag}>{signal}</Text>)}</View><View style={styles.actions}><Action label="Dismiss" onPress={() => void act(report.id, 'APPROVE')} /><Action label="Review" primary onPress={() => void act(report.id, 'REVIEW')} /><Action label="Remove" danger onPress={() => void act(report.id, 'REMOVE')} /></View></View>)}</>;
}

function Verifications({ data, act }: { data: AdminDashboard; act: (id: string, action: 'APPROVE_VERIFICATION' | 'REJECT_VERIFICATION' | 'REQUEST_INFO') => Promise<void> }) {
  return <><Heading title="Professional verification" subtitle="Review submitted identity, education, and license evidence." />{data.verificationQueue.map(item => <View key={item.id} style={styles.card}><View style={styles.member}><Avatar name={item.name} size={44} /><View style={{ flex: 1 }}><Text style={styles.cardTitle}>{item.name}</Text><Text style={styles.meta}>{item.profession || 'Profession not supplied'} · {item.country || 'Country not supplied'}</Text></View><Text style={styles.severity}>{item.status}</Text></View><View style={styles.tags}>{item.documents.map(document => <Text key={document} style={styles.tag}>{document}</Text>)}</View>{item.status === 'PENDING' && <View style={styles.actions}><Action label="Request info" onPress={() => void act(item.id, 'REQUEST_INFO')} /><Action label="Reject" danger onPress={() => void act(item.id, 'REJECT_VERIFICATION')} /><Action label="Approve" primary onPress={() => void act(item.id, 'APPROVE_VERIFICATION')} /></View>}</View>)}</>;
}

function Members({ data }: { data: AdminDashboard }) { return <><Heading title="Members" subtitle="Current account status and moderation context." />{data.members.map(item => <View key={item.id} style={styles.card}><View style={styles.member}><Avatar name={item.name} size={42} /><View style={{ flex: 1 }}><Text style={styles.cardTitle}>{item.name}</Text><Text style={styles.meta}>@{item.username} · {item.status}</Text></View><Text style={styles.meta}>{item.postCount} posts · {item.reportCount} reports</Text></View></View>)}</>; }
function Audit({ data }: { data: AdminDashboard }) { return <><Heading title="Audit log" subtitle="Immutable moderation activity from the backend." />{data.auditLog.map(item => <View key={item.id} style={styles.card}><Text style={styles.cardTitle}>{item.action.replaceAll('_', ' ')}</Text><Text style={styles.meta}>{item.actor} · {item.target}</Text><Text style={styles.time}>{new Date(item.createdAt).toLocaleString()}</Text></View>)}</>; }
function Heading({ title, subtitle }: { title: string; subtitle: string }) { return <View><Text style={styles.pageTitle}>{title}</Text><Text style={styles.pageSubtitle}>{subtitle}</Text></View>; }
function Kpi({ label, value, icon }: { label: string; value: number; icon: string }) { return <View style={styles.kpi}><AppIcon name={icon} color={colors.ocean600} /><Text style={styles.kpiValue}>{value.toLocaleString()}</Text><Text style={styles.kpiLabel}>{label}</Text></View>; }
function Panel({ title, children }: { title: string; children: React.ReactNode }) { return <View style={styles.panel}><Text style={styles.panelTitle}>{title}</Text>{children}</View>; }
function Action({ label, onPress, primary, danger }: { label: string; onPress: () => void; primary?: boolean; danger?: boolean }) { return <Pressable onPress={onPress} style={[styles.action, primary && styles.actionPrimary, danger && styles.actionDanger]}><Text style={[styles.actionText, (primary || danger) && styles.actionTextLight]}>{label}</Text></Pressable>; }

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.foam }, shell: { flex: 1, width: '100%', maxWidth: layout.maxContent, alignSelf: 'center' }, top: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.md, backgroundColor: colors.white, borderBottomWidth: 1, borderBottomColor: colors.border }, title: { color: colors.ink, fontSize: typography.h2, fontWeight: '900' }, subtitle: { color: colors.muted, fontSize: 10, marginTop: 2 },
  nav: { gap: spacing.sm, padding: spacing.md }, navItem: { flexDirection: 'row', alignItems: 'center', gap: 5, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, paddingHorizontal: spacing.md, paddingVertical: 9 }, navActive: { backgroundColor: colors.ocean900, borderColor: colors.ocean900 }, navText: { color: colors.muted, fontSize: 10, fontWeight: '800' }, navTextActive: { color: colors.white }, badge: { minWidth: 18, height: 18, borderRadius: 9, backgroundColor: colors.coral, alignItems: 'center', justifyContent: 'center' }, badgeText: { color: colors.white, fontSize: 8, fontWeight: '900' },
  page: { padding: spacing.md, paddingBottom: spacing.xxxl, gap: spacing.md }, pageTitle: { color: colors.ink, fontSize: typography.h2, fontWeight: '900' }, pageSubtitle: { color: colors.muted, fontSize: 11, marginTop: 3 }, state: { color: colors.muted, textAlign: 'center', padding: spacing.xl }, error: { color: colors.danger, textAlign: 'center' },
  alert: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.danger, borderRadius: radius.lg, padding: spacing.md }, alertTitle: { color: colors.white, fontWeight: '900' }, alertText: { color: '#ffffffcc', fontSize: 10, marginTop: 3 }, kpis: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, kpi: { flexGrow: 1, flexBasis: 180, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, gap: 5, ...shadow.soft }, kpiValue: { color: colors.ink, fontSize: 22, fontWeight: '900' }, kpiLabel: { color: colors.muted, fontSize: 10 },
  panel: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, gap: spacing.sm }, panelTitle: { color: colors.ink, fontSize: 14, fontWeight: '900' }, reason: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm }, reasonDot: { width: 8, height: 8, borderRadius: 4 }, reasonName: { flex: 1, color: colors.inkSoft, fontSize: 11 }, reasonValue: { color: colors.ink, fontWeight: '900' }, empty: { color: colors.muted, fontSize: 11 },
  card: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, gap: spacing.sm, ...shadow.soft }, cardTop: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, cardTitle: { color: colors.ink, fontWeight: '900' }, cardText: { color: colors.inkSoft, fontSize: 11, lineHeight: 17 }, meta: { color: colors.muted, fontSize: 9 }, time: { color: colors.muted, fontSize: 9 }, severity: { alignSelf: 'flex-start', color: colors.ocean700, backgroundColor: colors.ocean50, borderRadius: radius.pill, paddingHorizontal: 8, paddingVertical: 4, fontSize: 8, fontWeight: '900' }, critical: { color: colors.danger, backgroundColor: colors.coralSoft }, tags: { flexDirection: 'row', flexWrap: 'wrap', gap: 5 }, tag: { color: colors.muted, backgroundColor: colors.foam, borderRadius: radius.pill, paddingHorizontal: 7, paddingVertical: 4, fontSize: 8 }, actions: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, action: { minHeight: 38, flexGrow: 1, alignItems: 'center', justifyContent: 'center', borderRadius: radius.pill, borderWidth: 1, borderColor: colors.ocean300 }, actionPrimary: { backgroundColor: colors.ocean600, borderColor: colors.ocean600 }, actionDanger: { backgroundColor: colors.danger, borderColor: colors.danger }, actionText: { color: colors.ocean700, fontSize: 9, fontWeight: '900' }, actionTextLight: { color: colors.white }, member: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  restricted: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: spacing.md, padding: spacing.xl }, primary: { backgroundColor: colors.ocean600, borderRadius: radius.pill, paddingHorizontal: spacing.xl, paddingVertical: spacing.md }, primaryText: { color: colors.white, fontWeight: '900' },
});
