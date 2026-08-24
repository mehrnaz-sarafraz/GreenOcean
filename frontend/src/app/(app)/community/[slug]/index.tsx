import { router, useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { Screen } from '@/components/screen';
import { Community } from '@/features/community/types';
import { PageResponse, PostItem } from '@/features/content/types';
import { PostCard } from '@/features/content/post-card';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function CommunityDetailScreen() {
  const { slug } = useLocalSearchParams<{ slug: string }>();
  const { t, isRtl } = useLanguage();
  const [community, setCommunity] = useState<Community | null>(null);
  const [posts, setPosts] = useState<PostItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!slug) return;
    setLoading(true); setError('');
    try {
      const item = await apiRequest<Community>(`/api/v1/communities/slug/${encodeURIComponent(slug)}`);
      setCommunity(item);
      if (item.member) {
        const response = await apiRequest<PageResponse<PostItem>>(`/api/v1/communities/${item.id}/posts`);
        setPosts(response.items);
      } else setPosts([]);
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }, [slug, t]);

  useEffect(() => {
    if (!slug) return;
    let active = true;
    apiRequest<Community>(`/api/v1/communities/slug/${encodeURIComponent(slug)}`)
      .then(async (item) => {
        const response = item.member
          ? await apiRequest<PageResponse<PostItem>>(`/api/v1/communities/${item.id}/posts`)
          : { items: [] as PostItem[] };
        if (active) { setCommunity(item); setPosts(response.items); setLoading(false); }
      })
      .catch((exception) => { if (active) { setError(exception instanceof Error ? exception.message : t('genericError')); setLoading(false); } });
    return () => { active = false; };
  }, [slug, t]);

  async function join() {
    if (!community) return;
    try {
      const updated = await apiRequest<Community>(`/api/v1/communities/${community.id}/membership`, { method: 'PUT' });
      setCommunity(updated); await load();
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
  }

  async function leave() {
    if (!community) return;
    try {
      await apiRequest(`/api/v1/communities/${community.id}/membership`, { method: 'DELETE' });
      setCommunity({ ...community, member: false, membershipRole: null, memberCount: Math.max(0, community.memberCount - 1) });
      setPosts([]);
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
  }

  return (
    <Screen scroll style={styles.screen}>
      <Pressable onPress={() => router.back()}><Text style={styles.back}>‹</Text></Pressable>
      {!!community && <View style={styles.hero}>
        <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{community.name}</Text>
        <Text style={[styles.meta, { textAlign: isRtl ? 'right' : 'left' }]}>{community.memberCount} {t('members')}</Text>
        {!!community.description && <Text style={[styles.description, { textAlign: isRtl ? 'right' : 'left' }]}>{community.description}</Text>}
        {!community.member && community.privateCommunity && <Text style={styles.privateMessage}>{t('privateInviteOnly')}</Text>}
        {!community.member && !community.privateCommunity && <AppButton label={t('join')} onPress={() => void join()} />}
        {community.member && <View style={styles.actions}>
          <AppButton label={t('shareHere')} onPress={() => router.push({ pathname: '/create', params: { communityId: community.id, communityName: community.name } })} />
          {community.membershipRole !== 'OWNER' && <AppButton label={t('leave')} variant="secondary" onPress={() => void leave()} />}
        </View>}
      </View>}
      {!!error && <Text style={styles.error}>{error}</Text>}
      {loading && <Text style={styles.meta}>…</Text>}
      {!!community?.member && <Text style={[styles.sectionTitle, { textAlign: isRtl ? 'right' : 'left' }]}>{t('communityPosts')}</Text>}
      {community?.member && posts.map((post) => <PostCard key={post.id} post={post} />)}
      {community?.member && !loading && posts.length === 0 && <Text style={styles.empty}>{t('noCommunityPosts')}</Text>}
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { gap: spacing.lg }, back: { color: colors.ocean700, fontSize: 42 }, hero: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.lg, gap: spacing.md },
  title: { color: colors.ocean900, fontSize: 32, fontWeight: '800' }, meta: { color: colors.muted }, description: { color: colors.ink, lineHeight: 23 },
  privateMessage: { color: colors.muted, backgroundColor: colors.ocean100, padding: spacing.md, borderRadius: radius.md }, actions: { gap: spacing.sm },
  sectionTitle: { color: colors.ocean900, fontSize: 22, fontWeight: '800' }, error: { color: colors.danger }, empty: { color: colors.muted, textAlign: 'center' },
});
