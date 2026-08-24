import { useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { Screen } from '@/components/screen';
import { PostCard } from '@/features/content/post-card';
import { CommentItem, PageResponse, PostItem } from '@/features/content/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function PostDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>(); const { t, isRtl } = useLanguage();
  const [post, setPost] = useState<PostItem | null>(null); const [comments, setComments] = useState<CommentItem[]>([]);
  const [body, setBody] = useState(''); const [replyTo, setReplyTo] = useState<string | null>(null); const [loading, setLoading] = useState(false); const [error, setError] = useState('');
  useEffect(() => {
    if (!id) return; let active = true;
    Promise.all([apiRequest<PostItem>(`/api/v1/posts/${id}`), apiRequest<PageResponse<CommentItem>>(`/api/v1/posts/${id}/comments?page=0&size=100`)])
      .then(([postResult, commentsResult]) => { if (active) { setPost(postResult); setComments(commentsResult.items); } })
      .catch((exception) => { if (active) setError(exception instanceof Error ? exception.message : t('genericError')); });
    return () => { active = false; };
  }, [id, t]);
  async function sendComment() {
    if (!body.trim() || !id) return; setLoading(true);
    try { const comment = await apiRequest<CommentItem>(`/api/v1/posts/${id}/comments`, { method: 'POST', body: JSON.stringify({ body, parentCommentId: replyTo, anonymous: false }) }); setComments((current) => [...current, comment]); setBody(''); setReplyTo(null); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }
  return (
    <Screen scroll style={styles.content}>
      {!post && !error && <ActivityIndicator color={colors.ocean500} size="large" />}
      {!!error && <Text style={styles.error}>{error}</Text>}
      {post && <PostCard post={post} />}
      <Text style={[styles.heading, { textAlign: isRtl ? 'right' : 'left' }]}>{t('comments')}</Text>
      {comments.map((comment) => (
        <View key={comment.id} style={[styles.comment, comment.parentCommentId && styles.reply]}>
          <Text style={[styles.commentAuthor, { textAlign: isRtl ? 'right' : 'left' }]}>{comment.author?.displayName ?? t('anonymous')}</Text>
          <Text style={[styles.commentBody, { textAlign: isRtl ? 'right' : 'left' }]}>{comment.body}</Text>
          <Pressable onPress={() => setReplyTo(comment.id)}><Text style={styles.replyButton}>{t('reply')}</Text></Pressable>
        </View>
      ))}
      {!!replyTo && <Pressable onPress={() => setReplyTo(null)}><Text style={styles.replying}>{t('reply')} ×</Text></Pressable>}
      <TextInput multiline value={body} onChangeText={setBody} placeholder={t('writeComment')} placeholderTextColor={colors.muted}
        style={[styles.commentInput, { textAlign: isRtl ? 'right' : 'left' }]} />
      <AppButton label={t('send')} loading={loading} onPress={sendComment} />
    </Screen>
  );
}
const styles = StyleSheet.create({ content: { gap: spacing.md, paddingTop: spacing.lg }, error: { color: colors.danger }, heading: { color: colors.ocean900, fontSize: 24, fontWeight: '800', marginTop: spacing.md }, comment: { backgroundColor: colors.white, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.sm }, reply: { marginStart: spacing.xl, borderStartWidth: 3, borderStartColor: colors.ocean300 }, commentAuthor: { color: colors.ocean700, fontWeight: '800' }, commentBody: { color: colors.ink, lineHeight: 23 }, replyButton: { color: colors.ocean600, fontWeight: '700' }, replying: { color: colors.ocean600 }, commentInput: { minHeight: 100, textAlignVertical: 'top', backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.md, padding: spacing.md, color: colors.ink } });
