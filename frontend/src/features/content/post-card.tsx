import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

import { PostItem } from './types';

export function PostCard({ post: initialPost }: { post: PostItem }) {
  const { t, isRtl } = useLanguage(); const [post, setPost] = useState(initialPost);
  async function toggleLike() {
    const next = !post.liked;
    setPost((current) => ({ ...current, liked: next, likeCount: current.likeCount + (next ? 1 : -1) }));
    try { await apiRequest(`/api/v1/posts/${post.id}/like`, { method: next ? 'PUT' : 'DELETE' }); }
    catch { setPost((current) => ({ ...current, liked: !next, likeCount: current.likeCount + (next ? -1 : 1) })); }
  }
  async function toggleBookmark() {
    const next = !post.bookmarked; setPost((current) => ({ ...current, bookmarked: next }));
    try { await apiRequest(`/api/v1/posts/${post.id}/bookmark`, { method: next ? 'PUT' : 'DELETE' }); }
    catch { setPost((current) => ({ ...current, bookmarked: !next })); }
  }
  return (
    <Pressable onPress={() => router.push({ pathname: '/(app)/post/[id]', params: { id: post.id } })} style={styles.card}>
      <View style={[styles.authorRow, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
        <View style={styles.avatar}><Text style={styles.avatarText}>{post.author?.displayName.slice(0, 1).toUpperCase() ?? '◌'}</Text></View>
        <View style={styles.authorText}>
          <Text style={[styles.name, { textAlign: isRtl ? 'right' : 'left' }]}>{post.author?.displayName ?? t('anonymous')}</Text>
          {!!post.author && <Text style={[styles.username, { textAlign: isRtl ? 'right' : 'left' }]}>@{post.author.username}</Text>}
        </View>
      </View>
      {!!post.contentWarning && <Text style={styles.warning}>{post.contentWarning}</Text>}
      <Text style={[styles.body, { textAlign: isRtl ? 'right' : 'left' }]}>{post.body}</Text>
      <View style={[styles.actions, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
        <Pressable onPress={(event) => { event.stopPropagation(); void toggleLike(); }}><Text style={[styles.action, post.liked && styles.active]}>♡ {post.likeCount} · {t('like')}</Text></Pressable>
        <Text style={styles.action}>◌ {post.commentCount} · {t('comments')}</Text>
        <Pressable onPress={(event) => { event.stopPropagation(); void toggleBookmark(); }}><Text style={[styles.action, post.bookmarked && styles.active]}>⌑ {t('bookmark')}</Text></Pressable>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.lg, gap: spacing.md },
  authorRow: { alignItems: 'center', gap: spacing.md }, avatar: { width: 44, height: 44, borderRadius: radius.pill, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: colors.ocean700, fontWeight: '800' }, authorText: { flex: 1 }, name: { color: colors.ink, fontWeight: '800' }, username: { color: colors.muted, marginTop: 2 },
  warning: { color: colors.danger, backgroundColor: '#FEF3F2', padding: spacing.sm, borderRadius: radius.sm }, body: { color: colors.ink, fontSize: 17, lineHeight: 27 },
  actions: { justifyContent: 'space-between', gap: spacing.sm, flexWrap: 'wrap' }, action: { color: colors.muted, fontWeight: '600' }, active: { color: colors.ocean600 },
});
