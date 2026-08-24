import { useEffect, useState } from 'react';
import { ActivityIndicator, FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppButton } from '@/components/app-button';
import { PostCard } from '@/features/content/post-card';
import { PageResponse, PostItem } from '@/features/content/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, spacing } from '@/theme/tokens';

export default function FeedScreen() {
  const { t, isRtl } = useLanguage();
  const [posts, setPosts] = useState<PostItem[]>([]); const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false); const [loading, setLoading] = useState(true); const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    apiRequest<PageResponse<PostItem>>('/api/v1/posts/feed?page=0&size=20')
      .then((result) => { if (active) { setPosts(result.items); setHasNext(result.hasNext); setLoading(false); } })
      .catch((exception) => { if (active) { setError(exception instanceof Error ? exception.message : t('genericError')); setLoading(false); } });
    return () => { active = false; };
  }, [t]);

  async function refresh() {
    setRefreshing(true);
    try { const result = await apiRequest<PageResponse<PostItem>>('/api/v1/posts/feed?page=0&size=20'); setPosts(result.items); setPage(0); setHasNext(result.hasNext); setError(''); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setRefreshing(false); }
  }
  async function loadMore() {
    if (!hasNext || loading) return; setLoading(true);
    try { const nextPage = page + 1; const result = await apiRequest<PageResponse<PostItem>>(`/api/v1/posts/feed?page=${nextPage}&size=20`); setPosts((current) => [...current, ...result.items]); setPage(nextPage); setHasNext(result.hasNext); }
    finally { setLoading(false); }
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('feed')}</Text>
      {!!error && <View style={styles.errorBox}><Text style={styles.error}>{error}</Text><AppButton label={t('retry')} onPress={refresh} /></View>}
      {loading && posts.length === 0 ? <ActivityIndicator style={styles.loader} color={colors.ocean500} size="large" /> : (
        <FlatList data={posts} keyExtractor={(item) => item.id} renderItem={({ item }) => <PostCard post={item} />}
          contentContainerStyle={styles.list} ItemSeparatorComponent={() => <View style={styles.separator} />}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={refresh} tintColor={colors.ocean500} />}
          ListEmptyComponent={<Text style={[styles.empty, { textAlign: isRtl ? 'right' : 'left' }]}>{t('feedEmpty')}</Text>}
          ListFooterComponent={hasNext ? <AppButton label={t('loadMore')} variant="secondary" onPress={loadMore} loading={loading} /> : null} />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.foam, paddingHorizontal: spacing.lg }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '800', marginVertical: spacing.md },
  list: { paddingBottom: spacing.xxl }, separator: { height: spacing.md }, loader: { marginTop: spacing.xxl }, empty: { color: colors.muted, fontSize: 17, marginTop: spacing.xxl },
  errorBox: { gap: spacing.md, marginBottom: spacing.md }, error: { color: colors.danger },
});
