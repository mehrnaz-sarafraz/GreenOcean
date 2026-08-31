import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { PostCard } from '@/features/content/post-card';
import { PageResponse, PostItem } from '@/features/content/types';
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
  const [error, setError] = useState('');

  useEffect(() => {
    if (!community?.member) { setPosts([]); return; }
    apiRequest<PageResponse<PostItem>>(`/api/v1/communities/${community.id}/posts?size=50`)
      .then(result => setPosts(result.items)).catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load this circle'));
  }, [community?.id, community?.member]);

  async function join() {
    if (!community) return;
    const updated = await apiRequest<typeof community>(`/api/v1/communities/${community.id}/membership`, { method: 'PUT' });
    setCommunities(items => items.map(item => item.id === community.id ? updated : item));
  }

  if (!community) return <Screen style={styles.screen}><View style={styles.content}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Text style={styles.empty}>Community not found.</Text></View></Screen>;

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable>
    <View style={styles.hero}><View style={styles.heroIcon}><AppIcon name="diversity_1" size={34} color={colors.ocean700} /></View><Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{community.name}</Text><Text style={[styles.meta, { textAlign: isRtl ? 'right' : 'left' }]}>{community.memberCount.toLocaleString()} {t('members')}</Text><Text style={[styles.desc, { textAlign: isRtl ? 'right' : 'left' }]}>{community.description}</Text><AppButton label={community.member ? t('shareHere') : t('join')} onPress={() => community.member ? router.push('/create') : void join()} /></View>
    <Text style={[styles.section, { textAlign: isRtl ? 'right' : 'left' }]}>{t('communityPosts')}</Text>
    {!community.member && <Text style={styles.empty}>Join this circle to read and share community posts.</Text>}
    {!!error && <Text style={styles.error}>{error}</Text>}
    {posts.map(post => <PostCard key={post.id} post={post} />)}
    {community.member && posts.length === 0 && !error && <Text style={styles.empty}>No stories have been shared here yet.</Text>}
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  hero: { backgroundColor: colors.ocean900, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.sm }, heroIcon: { width: 58, height: 58, borderRadius: radius.md, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: typography.h1, color: colors.white, fontWeight: '900' }, meta: { color: colors.ocean300, fontSize: 12 }, desc: { color: colors.ocean100, lineHeight: 22, marginBottom: spacing.sm },
  section: { fontSize: typography.h3, fontWeight: '900', color: colors.ink, marginTop: spacing.sm }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.lg }, error: { color: colors.danger, fontWeight: '700' },
});
