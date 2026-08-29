import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { apiRequest } from '@/lib/api/client';
import { isMockMode } from '@/lib/data-mode';
import { categories, MockPost } from '@/mocks/data';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, shadow, spacing } from '@/theme/tokens';
import { PostItem } from './types';

const categoryGroupNames = { EMOTION: 'EMOTION', CONDITION: 'CONDITION', LIFE_EXPERIENCE: 'LIFE EXPERIENCE' } as const;

function relativeTime(value: string) {
  const minutes = Math.max(1, Math.floor((Date.now() - new Date(value).getTime()) / 60000));
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60); return `${hours}h`;
}
export function PostCard({ post: initialPost, elevated = true }: { post: PostItem | MockPost; elevated?: boolean }) {
  const { t } = useLanguage(); const [post, setPost] = useState(initialPost as MockPost);
  const category = post.category ?? categories[0];
  const postType = post.postType ?? 'EXPERIENCE';
  async function toggleLike() { const next = !post.liked; setPost(c => ({ ...c, liked: next, likeCount: c.likeCount + (next ? 1 : -1) })); if (!isMockMode) try { await apiRequest(`/api/v1/posts/${post.id}/like`, { method: next ? 'PUT' : 'DELETE' }); } catch { setPost(c => ({ ...c, liked: !next, likeCount: c.likeCount + (next ? -1 : 1) })); } }
  async function toggleBookmark() { const next = !post.bookmarked; setPost(c => ({ ...c, bookmarked: next })); if (!isMockMode) try { await apiRequest(`/api/v1/posts/${post.id}/bookmark`, { method: next ? 'PUT' : 'DELETE' }); } catch { setPost(c => ({ ...c, bookmarked: !next })); } }
  const authorName = post.author?.displayName ?? t('anonymous');
  return <Pressable onPress={() => router.push({ pathname: '/(app)/post/[id]', params: { id: post.id } })} style={[styles.card, elevated && shadow.soft]}>
    <View style={styles.authorRow}>
      <Avatar name={authorName} uri={post.author?.avatarUrl} />
      <View style={styles.authorText}><View style={styles.nameLine}><Text style={styles.name}>{authorName}</Text>{post.anonymous && <View style={styles.anonymousBadge}><AppIcon name="visibility_off" size={13} color={colors.muted} /><Text style={styles.badgeText}>{t('anonymous')}</Text></View>}</View>
        <Text style={styles.meta}>{post.author ? `@${post.author.username} · ` : ''}{relativeTime(post.createdAt)}</Text></View>
      <AppIcon name="more_horiz" color={colors.muted} />
    </View>
    <View style={styles.chips}><View style={styles.groupChip}><Text style={styles.groupText}>{categoryGroupNames[category.group]}</Text></View><View style={[styles.categoryChip, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={15} color={category.color} /><Text style={[styles.categoryText, { color: category.color }]}>{category.name}</Text></View><View style={styles.typeChip}><Text style={styles.typeText}>{postType === 'QUESTION' ? t('question') : postType === 'REFLECTION' ? t('reflection') : t('experience')}</Text></View>{post.mood && <View style={[styles.chip, styles.moodChip]}><Text style={styles.moodText}>● {post.mood}</Text></View>}</View>
    {!!post.contentWarning && <View style={styles.warning}><AppIcon name="shield" size={18} color={colors.coral} /><Text style={styles.warningText}>{post.contentWarning}</Text></View>}
    <Text style={styles.body}>{post.body}</Text>
    {post.professionalReply && <View style={styles.professionalBox}>
      <View style={styles.profHeader}><Avatar name={post.professionalReply.professional.displayName} size={34} verified /><View style={{ flex: 1 }}><Text style={styles.profLabel}>{t('professionalResponse')}</Text><Text style={styles.profName}>{post.professionalReply.professional.displayName} · {post.professionalReply.professional.title}</Text></View></View>
      <Text style={styles.profBody} numberOfLines={3}>{post.professionalReply.body}</Text>
    </View>}
    <View style={styles.actions}>
      <Pressable onPress={e => { e.stopPropagation(); void toggleLike(); }} style={styles.action}><AppIcon name="favorite" filled={post.liked} size={22} color={post.liked ? colors.coral : colors.muted} /><Text style={[styles.actionText, post.liked && { color: colors.coral }]}>{post.likeCount}</Text></Pressable>
      <View style={styles.action}><AppIcon name="chat_bubble" size={21} color={colors.muted} /><Text style={styles.actionText}>{post.commentCount}</Text></View>
      <View style={styles.action}><AppIcon name="ios_share" size={22} color={colors.muted} /></View>
      <Pressable onPress={e => { e.stopPropagation(); void toggleBookmark(); }} style={[styles.action, styles.save]}><AppIcon name="bookmark" filled={post.bookmarked} size={22} color={post.bookmarked ? colors.ocean600 : colors.muted} /></Pressable>
    </View>
  </Pressable>;
}
const styles = StyleSheet.create({ card: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.md }, authorRow: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, authorText: { flex: 1 }, nameLine: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, name: { color: colors.ink, fontWeight: '800', fontSize: 15 }, meta: { color: colors.muted, fontSize: 12, marginTop: 2, textAlign:'left' }, anonymousBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, backgroundColor: colors.foam, borderRadius: radius.pill, paddingHorizontal: 7, paddingVertical: 3 }, badgeText: { color: colors.muted, fontSize: 10, fontWeight: '700' }, chips: { flexDirection:'row', gap: spacing.sm, flexWrap: 'wrap', alignItems:'center' }, chip: { backgroundColor: colors.ocean50, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 5 }, groupChip:{borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5,backgroundColor:colors.ocean900},groupText:{fontSize:8,color:colors.white,fontWeight:'900',letterSpacing:.6},categoryChip:{flexDirection:'row',alignItems:'center',gap:4,borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5},categoryText:{fontSize:11,fontWeight:'900'},typeChip:{borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5,backgroundColor:colors.foam,borderWidth:1,borderColor:colors.border},typeText:{fontSize:10,color:colors.muted,fontWeight:'800'}, moodChip: { backgroundColor: colors.sunSoft }, moodText: { color: '#9A6B22', fontSize: 12, fontWeight: '700' }, warning: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.coralSoft, padding: spacing.sm, borderRadius: radius.sm }, warningText: { color: colors.danger, fontSize: 13 }, body: { color: colors.ink, fontSize: 16, lineHeight: 26, textAlign:'left', writingDirection:'ltr' }, professionalBox: { backgroundColor: colors.ocean50, borderRadius: radius.md, borderStartWidth: 3, borderStartColor: colors.ocean400, padding: spacing.md, gap: spacing.sm }, profHeader: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, profLabel: { color: colors.ocean600, fontSize: 10, fontWeight: '900', textTransform: 'uppercase',textAlign:'left' }, profName: { color: colors.inkSoft, fontSize: 12, fontWeight: '700', marginTop: 2,textAlign:'left' }, profBody: { color: colors.inkSoft, fontSize: 14, lineHeight: 22,textAlign:'left' }, actions: { flexDirection:'row', borderTopWidth: 1, borderTopColor: colors.border, paddingTop: spacing.sm, gap: spacing.lg, alignItems: 'center' }, action: { flexDirection: 'row', alignItems: 'center', gap: 5, minHeight: 30 }, actionText: { color: colors.muted, fontSize: 12, fontWeight: '700' }, save: { marginStart: 'auto' } });
