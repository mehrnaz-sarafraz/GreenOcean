import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { PostCard } from '@/features/content/post-card';
import { usePlatformData } from '@/features/platform/data-provider';
import { Professional } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

export default function ProfileScreen() {
  const { professional: professionalId } = useLocalSearchParams<{ professional?: string }>();
  const { t } = useLanguage();
  const { user, signOut } = useAuth();
  const { profile, profileStats, posts, preferences, professionals } = usePlatformData();  const professional = professionals.find(item => item.id === professionalId);
  if (professional) return <ProfessionalProfile professional={professional} />;

  const recentStories = posts.filter(post => post.author?.userId === profile?.userId).slice(0, 4);
  const location = [profile?.city, profile?.countryCode].filter(Boolean).join(', ');
  const canModerate = user?.roles.some(role => role === 'ADMIN' || role === 'MODERATOR');

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.cover}><View style={styles.orb} /><View style={styles.coverMark}><AppIcon name="waves" size={42} color="#ffffff18" /></View></View>
    <View style={styles.identity}><Avatar name={profile?.displayName ?? 'GreenOcean member'} uri={profile?.avatarUrl} size={86} /><AppButton label={t('editProfile')} variant="secondary" onPress={() => router.push('/edit-profile')} /></View>
    <Text style={styles.name}>{profile?.displayName ?? 'GreenOcean member'}</Text>
    <Text style={styles.handle}>@{profile?.username ?? 'member'}{location ? ` · ${location}` : ''}</Text>
    <Text style={styles.bio}>{profile?.bio || 'Add a short introduction so people know how to support you.'}</Text>
    <View style={styles.badges}><View style={styles.badge}><AppIcon name="calendar_month" size={16} color={colors.ocean600} /><Text style={styles.badgeText}>Member since {profile?.createdAt ? new Date(profile.createdAt).getFullYear() : '—'}</Text></View></View>
    <View style={styles.counts}><Count value={profileStats?.followers ?? 0} label={t('followers')} /><Count value={profileStats?.following ?? 0} label={t('following')} /><Count value={profileStats?.stories ?? 0} label={t('stories')} /><Count value={profileStats?.helpfulReactions ?? 0} label="Helpful reactions" /></View>

    <Text style={styles.sectionTitle}>Recent stories</Text>
    {recentStories.map(post => <PostCard key={post.id} post={post} />)}
    {recentStories.length === 0 && <View style={styles.empty}><AppIcon name="edit_note" color={colors.ocean500} /><Text style={styles.emptyText}>Your published stories will appear here.</Text></View>}

    <Text style={styles.sectionTitle}>Profile details</Text>
    <View style={styles.infoCard}>
      <Info icon="location_on" title="Location" text={profile?.showLocation && location ? location : 'Hidden'} />
      <Info icon="interests" title="Support topics" text={preferences?.supportTopics.join(' · ') || 'Not selected yet'} />
      <Info icon="lock" title="Visibility" text={profile?.profilePrivate ? 'Private profile' : 'Public profile'} />
    </View>

    <Text style={styles.sectionTitle}>Account & safety</Text>
    <View style={styles.settings}>
      <Setting icon="chat_bubble" title="Messages" subtitle="Manage conversations and requests" onPress={() => router.push('/messages')} />
      <Setting icon="groups" title="Groups & channels" subtitle="Your joined support spaces" onPress={() => router.push('/channels')} />
      <Setting icon="shield" title="Privacy and safety" subtitle="Muted words and content controls" onPress={() => router.push('/safety-settings')} />
      {canModerate && <Setting icon="admin_panel_settings" title="Admin panel" subtitle="Trust and safety operations" accent onPress={() => router.push('/admin')} />}
      <Setting icon="logout" title="Sign out" subtitle="End the session on this device" onPress={() => void signOut()} />
    </View>
  </View></Screen>;
}

function ProfessionalProfile({ professional }: { professional: Professional }) {
  const { user } = useAuth();
  const { articles, setProfessionals, setConversations } = usePlatformData();
  const [following, setFollowing] = useState(professional.followed);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const pinned = articles.find(article => article.authorId === professional.id && article.pinned)
    ?? articles.find(article => article.authorId === professional.id);

  async function toggleFollow() {
    if (busy) return;
    const next = !following;
    setFollowing(next); setBusy(true); setError('');
    try {
      await apiRequest(`/api/v1/social/follows/${professional.id}`, { method: next ? 'PUT' : 'DELETE' });
      setProfessionals(items => items.map(item => item.id === professional.id ? { ...item, followed: next } : item));
    } catch (caught) {
      setFollowing(!next);
      setError(caught instanceof Error ? caught.message : 'Could not update follow status');
    } finally { setBusy(false); }
  }

  async function startConversation() {
    if (busy) return;
    setBusy(true); setError('');
    try {
      const result = await apiRequest<{ id: string }>(`/api/v1/conversations/professionals/${professional.id}`, { method: 'POST' });
      const conversations = await apiRequest<import('@/features/platform/types').Conversation[]>('/api/v1/conversations');
      setConversations(conversations);
      router.push({ pathname: '/chat/[id]', params: { id: result.id } });
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not start the conversation');
    } finally { setBusy(false); }
  }

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.top}><Pressable onPress={() => router.back()} style={styles.back}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable>{user?.roles.includes('PROFESSIONAL') && <Pressable onPress={() => router.push('/article-editor')} style={styles.studio}><AppIcon name="edit_note" size={18} color={colors.ocean700} /><Text style={styles.studioText}>Article studio</Text></Pressable>}</View>
    <View style={styles.proHero}><View style={styles.proIdentity}><Avatar name={professional.displayName} uri={professional.avatarUrl} size={82} verified /><View style={{ flex: 1 }}><Text style={styles.proName}>{professional.displayName}</Text><Text style={styles.proRole}>{professional.title}</Text><Text style={styles.proHandle}>@{professional.username}</Text></View><Score value={professional.greenOceanScore} /></View><Text style={styles.proBio}>{professional.bio}</Text><View style={styles.proStats}><Count value={professional.rating} label="Rating" light /><Count value={professional.reviewCount} label="Reviews" light /><Count value={professional.experienceYears} label="Years" light /></View><View style={styles.actions}><View style={{ flex: 1 }}><AppButton label="Message" loading={busy} variant="secondary" onPress={() => void startConversation()} /></View><View style={{ flex: 1 }}><AppButton label={following ? 'Following' : 'Follow'} loading={busy} onPress={() => void toggleFollow()} /></View></View></View>
    {!!error && <Text style={styles.error}>{error}</Text>}
    <View style={styles.availability}><View style={[styles.dot, !professional.acceptingNewClients && { backgroundColor: colors.sun }]} /><View style={{ flex: 1 }}><Text style={styles.availabilityTitle}>{professional.acceptingNewClients ? 'Accepting new clients' : 'Waitlist currently active'}</Text><Text style={styles.availabilityText}>{professional.consultationModes.join(' · ') || 'Contact for availability'}</Text></View></View>
    <Text style={styles.sectionTitle}>Professional profile</Text>
    <View style={styles.detailGrid}>
      <Detail icon="psychology" title="Specialties" value={professional.specialties.join(' · ')} />
      <Detail icon="apartment" title="Workplace" value={professional.workplace} />
      <Detail icon="medical_services" title="Clinic / practice" value={professional.clinicName} />
      <Detail icon="location_on" title="Location" value={[professional.city, professional.country].filter(Boolean).join(', ')} />
      <Detail icon="language" title="Languages" value={professional.languages.join(' · ')} />
      <Detail icon="person" title="Gender" value={professional.gender} />
    </View>
    <Text style={styles.sectionTitle}>Credentials</Text>
    <View style={styles.infoCard}>{professional.education.map(item => <Info key={item} icon="school" title="Education" text={item} />)}<Info icon="workspace_premium" title="License" text={professional.licenseNumber ? `${professional.licenseNumber} · Verified by GreenOcean` : 'Verified by GreenOcean'} /></View>
    {pinned && <><Text style={styles.sectionTitle}>Article by this professional</Text><Pressable onPress={() => router.push({ pathname: '/article/[id]', params: { id: pinned.id } })} style={styles.article}><Text style={styles.articleTopic}>{pinned.evidenceLevel} · {pinned.readTime}</Text><Text style={styles.articleTitle}>{pinned.title}</Text><Text style={styles.articleSummary}>{pinned.summary}</Text></Pressable></>}
    <View style={styles.boundary}><AppIcon name="health_and_safety" color={colors.ocean600} /><Text style={styles.boundaryText}>Public replies, articles, and messages provide general educational support. They do not establish a therapeutic relationship or replace local medical care.</Text></View>
  </View></Screen>;
}

function Count({ value, label, light = false }: { value: string | number; label: string; light?: boolean }) { return <View style={styles.count}><Text style={[styles.countValue, light && { color: colors.white }]}>{value}</Text><Text style={[styles.countLabel, light && { color: colors.ocean300 }]}>{label}</Text></View>; }
function Info({ icon, title, text }: { icon: string; title: string; text: string }) { return <View style={styles.info}><View style={styles.infoIcon}><AppIcon name={icon} color={colors.ocean600} /></View><View style={{ flex: 1 }}><Text style={styles.infoTitle}>{title}</Text><Text style={styles.infoText}>{text}</Text></View></View>; }
function Detail({ icon, title, value }: { icon: string; title: string; value: string | null }) { return <View style={styles.detail}><AppIcon name={icon} color={colors.ocean700} /><Text style={styles.detailTitle}>{title}</Text><Text style={styles.detailText}>{value || 'Not specified'}</Text></View>; }
function Setting({ icon, title, subtitle, onPress, accent }: { icon: string; title: string; subtitle: string; onPress: () => void; accent?: boolean }) { return <Pressable onPress={onPress} style={styles.setting}><View style={[styles.settingIcon, accent && { backgroundColor: colors.coralSoft }]}><AppIcon name={icon} color={accent ? colors.coral : colors.ocean700} /></View><View style={{ flex: 1 }}><Text style={styles.settingTitle}>{title}</Text><Text style={styles.settingSubtitle}>{subtitle}</Text></View><AppIcon name="chevron_right" color={colors.muted} /></Pressable>; }
function Score({ value }: { value: number }) { return <View style={styles.score}><Text style={styles.scoreValue}>{value}</Text><Text style={styles.scoreLabel}>GO SCORE</Text></View>; }

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  cover: { height: 150, borderRadius: radius.xl, backgroundColor: colors.ocean900, overflow: 'hidden' }, orb: { position: 'absolute', width: 240, height: 240, borderRadius: 120, backgroundColor: colors.ocean600, right: -40, top: -100 }, coverMark: { position: 'absolute', left: spacing.lg, bottom: spacing.lg }, identity: { marginTop: -58, paddingHorizontal: spacing.md, flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between' },
  name: { fontSize: typography.h2, fontWeight: '900', color: colors.ink }, handle: { color: colors.muted }, bio: { color: colors.inkSoft, lineHeight: 23 }, badges: { flexDirection: 'row' }, badge: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 6 }, badgeText: { fontSize: 10, color: colors.inkSoft, fontWeight: '700' },
  counts: { flexDirection: 'row', backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, justifyContent: 'space-around' }, count: { alignItems: 'center', flex: 1 }, countValue: { fontSize: 18, fontWeight: '900', color: colors.ink }, countLabel: { fontSize: 9, color: colors.muted, marginTop: 3, textAlign: 'center' },
  sectionTitle: { fontSize: typography.h3, fontWeight: '900', color: colors.ink, marginTop: spacing.sm }, empty: { alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.lg }, emptyText: { color: colors.muted, fontSize: 11 },
  infoCard: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.md }, info: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, infoIcon: { width: 38, height: 38, borderRadius: radius.sm, backgroundColor: colors.ocean50, alignItems: 'center', justifyContent: 'center' }, infoTitle: { color: colors.ink, fontSize: 11, fontWeight: '900' }, infoText: { color: colors.inkSoft, fontSize: 11, lineHeight: 17, marginTop: 2 },
  settings: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, overflow: 'hidden' }, setting: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border }, settingIcon: { width: 38, height: 38, borderRadius: radius.sm, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, settingTitle: { fontSize: 12, fontWeight: '900', color: colors.ink }, settingSubtitle: { fontSize: 9, color: colors.muted, marginTop: 3 },
  top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, back: { width: 40, height: 40, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' }, studio: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.ocean100, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: spacing.sm }, studioText: { color: colors.ocean700, fontSize: 11, fontWeight: '900' },
  proHero: { backgroundColor: colors.ocean950, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.md, ...shadow.floating }, proIdentity: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, proName: { fontSize: 21, fontWeight: '900', color: colors.white }, proRole: { color: colors.ocean200, marginTop: 3 }, proHandle: { color: colors.ocean300, fontSize: 10, marginTop: 3 }, proBio: { color: colors.white, lineHeight: 23 }, proStats: { flexDirection: 'row', borderTopWidth: 1, borderBottomWidth: 1, borderColor: '#ffffff22', paddingVertical: spacing.sm }, actions: { flexDirection: 'row', gap: spacing.sm }, score: { width: 72, height: 72, borderRadius: 36, backgroundColor: colors.white, alignItems: 'center', justifyContent: 'center' }, scoreValue: { fontSize: 22, color: colors.ocean800, fontWeight: '900' }, scoreLabel: { fontSize: 7, color: colors.muted, fontWeight: '900' }, error: { color: colors.danger, fontSize: 11, textAlign: 'center' },
  availability: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md }, dot: { width: 9, height: 9, borderRadius: 5, backgroundColor: colors.ocean500 }, availabilityTitle: { color: colors.ink, fontWeight: '900', fontSize: 12 }, availabilityText: { color: colors.muted, fontSize: 10, marginTop: 3 },
  detailGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, detail: { flexGrow: 1, flexBasis: 210, minHeight: 125, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, gap: spacing.sm }, detailTitle: { color: colors.muted, fontSize: 9, fontWeight: '900', textTransform: 'uppercase' }, detailText: { color: colors.ink, fontSize: 12, lineHeight: 18, fontWeight: '700' },
  article: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.ocean200, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.sm }, articleTopic: { color: colors.ocean600, fontSize: 9, fontWeight: '900' }, articleTitle: { color: colors.ink, fontSize: 19, lineHeight: 26, fontWeight: '900' }, articleSummary: { color: colors.inkSoft, fontSize: 12, lineHeight: 19 }, boundary: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.ocean50, borderRadius: radius.md, padding: spacing.md }, boundaryText: { flex: 1, fontSize: 10, color: colors.muted, lineHeight: 16 },
});
