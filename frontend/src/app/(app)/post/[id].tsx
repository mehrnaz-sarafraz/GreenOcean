import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { PostCard } from '@/features/content/post-card';
import { CommentItem, PageResponse, PostItem } from '@/features/content/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function PostDetail() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useLanguage();
  const { posts } = usePlatformData();
  const [post, setPost] = useState<PostItem | null>(posts.find(item => item.id === id) ?? null);
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [body, setBody] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    async function load() {
      if (!id) return;
      setLoading(true); setError('');
      try {
        const [loadedPost, loadedComments] = await Promise.all([
          apiRequest<PostItem>(`/api/v1/posts/${id}`),
          apiRequest<PageResponse<CommentItem>>(`/api/v1/posts/${id}/comments?size=100`),
        ]);
        if (active) { setPost(loadedPost); setComments(loadedComments.items); }
      } catch (caught) {
        if (active) setError(caught instanceof Error ? caught.message : 'Could not load this story');
      } finally {
        if (active) setLoading(false);
      }
    }
    void load();
    return () => { active = false; };
  }, [id]);

  async function send() {
    const text = body.trim();
    if (!text || !id) return;
    setSending(true); setError('');
    try {
      const comment = await apiRequest<CommentItem>(`/api/v1/posts/${id}/comments`, {
        method: 'POST', body: JSON.stringify({ body: text, parentCommentId: null, anonymous: false }),
      });
      setComments(current => [...current, comment]);
      setBody('');
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not send the comment');
    } finally {
      setSending(false);
    }
  }

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable>
    {loading && !post && <ActivityIndicator color={colors.ocean600} size="large" />}
    {post && <PostCard post={post} />}
    {!!error && <Text style={styles.error}>{error}</Text>}
    <Text style={styles.title}>{t('comments')}</Text>
    {comments.map(comment => <View key={comment.id} style={styles.comment}>
      <Avatar name={comment.author?.displayName ?? t('anonymous')} uri={comment.author?.avatarUrl} size={38} />
      <View style={{ flex: 1 }}><Text style={styles.commentName}>{comment.author?.displayName ?? t('anonymous')}</Text>
        <Text style={styles.commentBody}>{comment.body}</Text><Text style={styles.reply}>{comment.likeCount} {t('like')}</Text></View>
    </View>)}
    {!loading && comments.length === 0 && <Text style={styles.empty}>No comments yet. Add the first supportive response.</Text>}
    <TextInput value={body} onChangeText={setBody} multiline placeholder={t('writeComment')} placeholderTextColor={colors.muted} style={styles.input} />
    <AppButton label={sending ? 'Sending…' : t('send')} disabled={sending || !body.trim()} onPress={() => void send()} />
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  title: { fontSize: typography.h3, fontWeight: '900', color: colors.ink, textAlign: 'left' },
  comment: { flexDirection: 'row', gap: spacing.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md },
  commentName: { fontWeight: '900', color: colors.ink, textAlign: 'left' }, commentBody: { color: colors.inkSoft, lineHeight: 21, marginTop: 4, textAlign: 'left' },
  reply: { fontSize: 11, color: colors.ocean600, fontWeight: '700', marginTop: 7, textAlign: 'left' },
  input: { minHeight: 100, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, color: colors.ink, textAlignVertical: 'top', textAlign: 'left' },
  error: { color: colors.danger, fontWeight: '700' }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.md },
});
