import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { PostCard } from '@/features/content/post-card';
import { PageResponse, PostItem } from '@/features/content/types';
import { CommunityDetails } from '@/features/community/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function CommunityDetail() {
  const { slug } = useLocalSearchParams<{ slug: string }>();
  const { t, isRtl } = useLanguage();
  const { communities, setCommunities } = usePlatformData();
  const community = communities.find(item => item.slug === slug);
  const [posts, setPosts] = useState<PostItem[]>([]);
  const [details, setDetails] = useState<CommunityDetails | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => {
      if (!community?.member) { setPosts([]); setDetails(null); return; }
      setError('');
      Promise.all([
        apiRequest<PageResponse<PostItem>>(`/api/v1/communities/${community.id}/posts?size=50`),
        apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/details`),
      ]).then(([postResult, detailResult]) => {
        setPosts(postResult.items);
        setDetails(detailResult);
      }).catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load this circle'));
    }, 0);
    return () => clearTimeout(timer);
  }, [community?.id, community?.member]);

  async function join() {
    if (!community) return;
    const updated = await apiRequest<typeof community>(`/api/v1/communities/${community.id}/membership`, { method: 'PUT' });
    setCommunities(items => items.map(item => item.id === community.id ? updated : item));
  }

  async function cancelRequest() {
    if (!community) return;
    await apiRequest(`/api/v1/communities/${community.id}/join-request`, { method: 'DELETE' });
    setCommunities(items => items.map(item => item.id === community.id ? { ...item, joinRequested: false } : item));
  }

  async function toggleEvent(eventId: string, joined: boolean) {
    if (!community) return;
    setError('');
    try {
      const next = await apiRequest<CommunityDetails>(
        `/api/v1/communities/${community.id}/events/${eventId}/attendance`,
        { method: joined ? 'DELETE' : 'PUT' },
      );
      setDetails(next);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not update event attendance');
    }
  }

  if (!community) return <Screen style={styles.screen}><View style={styles.content}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Text style={styles.empty}>Community not found.</Text></View></Screen>;

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable>
    <View style={styles.hero}><View style={styles.heroIcon}><AppIcon name="diversity_1" size={34} color={colors.ocean700} /></View><Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{community.name}</Text><Text style={[styles.meta, { textAlign: isRtl ? 'right' : 'left' }]}>{isRtl?'عضویت خصوصی است':'Membership is private'}</Text><Text style={[styles.desc, { textAlign: isRtl ? 'right' : 'left' }]}>{community.description}</Text><AppButton label={community.member ? t('shareHere') : community.joinRequested ? 'Request sent' : community.privateCommunity ? 'Request access' : t('join')} variant={community.joinRequested ? 'secondary' : 'primary'} onPress={() => community.member ? router.push('/create') : community.joinRequested ? void cancelRequest() : void join()} /></View>
    {!community.member && <Text style={styles.empty}>{community.joinRequested ? 'Your request is waiting for a community moderator.' : community.privateCommunity ? 'Request access to read and share in this private circle.' : 'Join this circle to read and share community posts.'}</Text>}
    {!!error && <Text style={styles.error}>{error}</Text>}
    {community.member && details && <>
      {(community.membershipRole === 'OWNER' || community.membershipRole === 'MODERATOR') && <View style={styles.manage}><Text style={styles.manageTitle}>Community management</Text><AppButton label="Rules" variant="secondary" onPress={() => router.push({ pathname: '/tools/[tool]' as never, params: { tool: 'community-rules', slug: community.slug } } as never)} /><AppButton label="Members & requests" variant="secondary" onPress={() => router.push({ pathname: '/tools/[tool]' as never, params: { tool: 'community-members', slug: community.slug } } as never)} /><AppButton label="Events" variant="secondary" onPress={() => router.push({ pathname: '/tools/[tool]' as never, params: { tool: 'community-events', slug: community.slug } } as never)} /></View>}
      <Text style={[styles.section, { textAlign: isRtl ? 'right' : 'left' }]}>Upcoming events</Text>
      {details.events.length ? details.events.map(event => <View key={event.id} style={styles.card}>
        <View style={styles.cardHead}><View style={styles.cardCopy}><Text style={styles.cardTitle}>{event.title}</Text><Text style={styles.meta}>{new Date(event.startsAt).toLocaleString()} · {event.format.replaceAll('_', ' ')}</Text></View><AppIcon name="event" color={colors.ocean600} /></View>
        {!!event.description && <Text style={styles.cardBody}>{event.description}</Text>}
        <Text style={styles.meta}>Hosted by {event.hostName || 'Community team'} · {event.attendeeCount}{event.capacity ? `/${event.capacity}` : ''} attending</Text>
        <AppButton label={event.joined ? 'Leave event' : 'Join event'} variant={event.joined ? 'secondary' : 'primary'} onPress={() => void toggleEvent(event.id, event.joined)} />
      </View>) : <Text style={styles.empty}>No upcoming events are scheduled.</Text>}

      <Text style={[styles.section, { textAlign: isRtl ? 'right' : 'left' }]}>Community guidelines</Text>
      <View style={styles.card}>{details.rules.map(rule => <View key={rule.id} style={styles.rule}><View style={styles.ruleNumber}><Text style={styles.ruleNumberText}>{rule.position}</Text></View><Text style={styles.ruleText}>{rule.text}</Text></View>)}</View>

      <View style={styles.sectionHead}><Text style={styles.section}>Members</Text><Text style={styles.meta}>{details.members.length} visible</Text></View>
      <View style={styles.card}>{details.members.slice(0, 12).map(member => <View key={member.userId} style={styles.member}><View style={styles.memberIcon}><AppIcon name={member.role === 'MEMBER' ? 'person' : 'shield'} size={18} color={colors.ocean700} /></View><View style={styles.cardCopy}><Text style={styles.memberName}>{member.displayName}</Text><Text style={styles.meta}>@{member.username} · {member.role.toLowerCase()}</Text></View></View>)}</View>
    </>}

    <Text style={[styles.section, { textAlign: isRtl ? 'right' : 'left' }]}>{t('communityPosts')}</Text>
    {posts.map(post => <PostCard key={post.id} post={post} />)}
    {community.member && posts.length === 0 && !error && <Text style={styles.empty}>No stories have been shared here yet.</Text>}
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  hero: { backgroundColor: colors.ocean900, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.sm }, heroIcon: { width: 58, height: 58, borderRadius: radius.md, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: typography.h1, color: colors.white, fontWeight: '900' }, meta: { color: colors.ocean300, fontSize: 12 }, desc: { color: colors.ocean100, lineHeight: 22, marginBottom: spacing.sm },
  section: { fontSize: typography.h3, fontWeight: '900', color: colors.ink, marginTop: spacing.sm }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.lg }, error: { color: colors.danger, fontWeight: '700' },
  sectionHead: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: spacing.sm },
  card: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, gap: spacing.sm },
  cardHead: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between', gap: spacing.sm },
  cardCopy: { flex: 1 }, cardTitle: { color: colors.ink, fontWeight: '900', fontSize: 16 }, cardBody: { color: colors.inkSoft, lineHeight: 21 },
  rule: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.sm }, ruleNumber: { width: 26, height: 26, borderRadius: 13, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, ruleNumberText: { color: colors.ocean700, fontWeight: '900', fontSize: 11 }, ruleText: { flex: 1, color: colors.inkSoft, lineHeight: 20 },
  member: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.xs }, memberIcon: { width: 36, height: 36, borderRadius: 18, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, memberName: { color: colors.ink, fontWeight: '800' },
  manage: { backgroundColor: colors.ocean50, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.ocean200, padding: spacing.md, gap: spacing.sm }, manageTitle: { color: colors.ink, fontSize: 15, fontWeight: '900' },
});
