import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, shadow, spacing } from '@/theme/tokens';
import { PostItem } from './types';

const categoryGroupNames = { EMOTION: 'EMOTION', CONDITION: 'CONDITION', LIFE_EXPERIENCE: 'LIFE EXPERIENCE' } as const;

function relativeTime(value: string) {
  const minutes = Math.max(1, Math.floor((Date.now() - new Date(value).getTime()) / 60000));
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60); return `${hours}h`;
}
export function PostCard({ post: initialPost, elevated = true }: { post: PostItem; elevated?: boolean }) {
  const { t } = useLanguage(); const [post, setPost] = useState(initialPost);
  const [sensitiveRevealed, setSensitiveRevealed] = useState(!initialPost.contentWarning);
  const category = post.category;
  const postType = post.postType ?? 'EXPERIENCE';
  async function toggleLike() { const next = !post.liked; setPost(c => ({ ...c, liked: next, likeCount: c.likeCount + (next ? 1 : -1) })); try { await apiRequest(`/api/v1/posts/${post.id}/like`, { method: next ? 'PUT' : 'DELETE' }); } catch { setPost(c => ({ ...c, liked: !next, likeCount: c.likeCount + (next ? -1 : 1) })); } }
  async function toggleBookmark() { const next = !post.bookmarked; setPost(c => ({ ...c, bookmarked: next })); try { await apiRequest(`/api/v1/posts/${post.id}/bookmark`, { method: next ? 'PUT' : 'DELETE' }); } catch { setPost(c => ({ ...c, bookmarked: !next })); } }
  const authorName = post.author?.displayName ?? t('anonymous');
  const openPost = () => router.push({ pathname: '/(app)/post/[id]', params: { id: post.id } });
  return <View style={[styles.card, elevated && shadow.soft]}>
    <Pressable accessibilityRole="button" accessibilityLabel={`Open post by ${authorName}`} onPress={openPost} style={styles.authorRow}>
      <Avatar name={authorName} uri={post.author?.avatarUrl} />
      <View style={styles.authorText}><View style={styles.nameLine}><Text style={styles.name}>{authorName}</Text>{post.anonymous && <View style={styles.anonymousBadge}><AppIcon name="visibility_off" size={13} color={colors.muted} /><Text style={styles.badgeText}>{t('anonymous')}</Text></View>}</View>
        <Text style={styles.meta}>{post.author ? `@${post.author.username} · ` : ''}{relativeTime(post.createdAt)}</Text></View>
      <AppIcon name="more_horiz" color={colors.muted} />
    </Pressable>
    <View style={styles.chips}>{category && <><View style={styles.groupChip}><Text style={styles.groupText}>{categoryGroupNames[category.group]}</Text></View><View style={[styles.categoryChip, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={15} color={category.color} /><Text style={[styles.categoryText, { color: category.color }]}>{category.name}</Text></View></>}<View style={styles.typeChip}><Text style={styles.typeText}>{postType === 'QUESTION' ? t('question') : postType === 'REFLECTION' ? t('reflection') : t('experience')}</Text></View>{post.mood && <View style={[styles.chip, styles.moodChip]}><Text style={styles.moodText}>● {post.mood}</Text></View>}</View>
    {!!post.contentWarning && !sensitiveRevealed && <View style={styles.warningGate}>
      <View style={styles.warningIcon}><AppIcon name="visibility_off" size={22} color={colors.coral} /></View>
      <View style={styles.warningCopy}><Text style={styles.warningTitle}>Sensitive story hidden</Text><Text style={styles.warningText}>{post.contentWarning}. Your safety settings keep this content covered until you choose to see it.</Text></View>
      <Pressable accessibilityRole="button" accessibilityLabel="Show sensitive story" onPress={event => { event.stopPropagation(); setSensitiveRevealed(true); }} style={styles.revealButton}><Text style={styles.revealButtonText}>Show story</Text></Pressable>
    </View>}
    {!!post.contentWarning && sensitiveRevealed && <View style={styles.warning}><AppIcon name="shield" size={18} color={colors.coral} /><Text style={[styles.warningText, styles.warningGrow]}>{post.contentWarning}</Text><Pressable accessibilityRole="button" accessibilityLabel="Hide sensitive story" onPress={event => { event.stopPropagation(); setSensitiveRevealed(false); }}><Text style={styles.warningAction}>Hide</Text></Pressable></View>}
    {sensitiveRevealed && <Pressable accessibilityRole="button" accessibilityLabel={`Read full post by ${authorName}`} onPress={openPost} style={styles.storyContent}><Text style={styles.body}>{post.body}</Text>
    {!!post.professionalReply && <View style={styles.professionalBox}>
      <View style={styles.profHeader}><Avatar name={post.professionalReply.professional.displayName} size={34} verified /><View style={{ flex: 1 }}><Text style={styles.profLabel}>{t('professionalResponse')}</Text><Text style={styles.profName}>{post.professionalReply.professional.displayName} · {post.professionalReply.professional.title}</Text></View></View>
      <Text style={styles.profBody} numberOfLines={3}>{post.professionalReply.body}</Text>
    </View>}</Pressable>}
    <View style={styles.actions}>
      <Pressable accessibilityRole="button" accessibilityLabel={post.liked ? 'Unlike post' : 'Like post'} accessibilityState={{ selected: post.liked }} onPress={e => { e.stopPropagation(); void toggleLike(); }} style={styles.action}><AppIcon name="favorite" filled={post.liked} size={22} color={post.liked ? colors.coral : colors.muted} /><Text style={[styles.actionText, post.liked && { color: colors.coral }]}>{post.likeCount}</Text></Pressable>
      <Pressable accessibilityRole="button" accessibilityLabel="Open comments" onPress={openPost} style={styles.action}><AppIcon name="chat_bubble" size={21} color={colors.muted} /><Text style={styles.actionText}>{post.commentCount}</Text></Pressable>
      <Pressable accessibilityRole="button" accessibilityLabel={post.bookmarked ? 'Remove bookmark' : 'Bookmark post'} accessibilityState={{ selected: post.bookmarked }} onPress={e => { e.stopPropagation(); void toggleBookmark(); }} style={[styles.action, styles.save]}><AppIcon name="bookmark" filled={post.bookmarked} size={22} color={post.bookmarked ? colors.ocean600 : colors.muted} /></Pressable>
    </View>
  </View>;
}
const styles = StyleSheet.create({ card: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.md }, authorRow: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, authorText: { flex: 1 }, nameLine: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, name: { color: colors.ink, fontWeight: '800', fontSize: 15 }, meta: { color: colors.muted, fontSize: 12, marginTop: 2, textAlign:'left' }, anonymousBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, backgroundColor: colors.foam, borderRadius: radius.pill, paddingHorizontal: 7, paddingVertical: 3 }, badgeText: { color: colors.muted, fontSize: 10, fontWeight: '700' }, chips: { flexDirection:'row', gap: spacing.sm, flexWrap: 'wrap', alignItems:'center' }, chip: { backgroundColor: colors.ocean50, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 5 }, groupChip:{borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5,backgroundColor:colors.ocean900},groupText:{fontSize:8,color:colors.white,fontWeight:'900',letterSpacing:.6},categoryChip:{flexDirection:'row',alignItems:'center',gap:4,borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5},categoryText:{fontSize:11,fontWeight:'900'},typeChip:{borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5,backgroundColor:colors.foam,borderWidth:1,borderColor:colors.border},typeText:{fontSize:10,color:colors.muted,fontWeight:'800'}, moodChip: { backgroundColor: colors.sunSoft }, moodText: { color: '#9A6B22', fontSize: 12, fontWeight: '700' }, warning: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.coralSoft, padding: spacing.sm, borderRadius: radius.sm }, warningGate: { flexDirection: 'row', alignItems: 'center', flexWrap: 'wrap', gap: spacing.sm, backgroundColor: colors.coralSoft, borderWidth: 1, borderColor: '#F7C8BE', padding: spacing.md, borderRadius: radius.md }, warningIcon: { width: 42, height: 42, borderRadius: 21, backgroundColor: colors.white, alignItems: 'center', justifyContent: 'center' }, warningCopy: { flex: 1, minWidth: 190 }, warningTitle: { color: colors.ink, fontSize: 14, fontWeight: '900', marginBottom: 3 }, warningText: { color: colors.danger, fontSize: 12, lineHeight: 18 }, warningGrow: { flex: 1 }, warningAction: { color: colors.danger, fontSize: 12, fontWeight: '900', padding: 5 }, revealButton: { minHeight: 38, borderRadius: radius.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: '#F2B5A9', paddingHorizontal: spacing.md, alignItems: 'center', justifyContent: 'center' }, revealButtonText: { color: colors.danger, fontSize: 12, fontWeight: '900' }, storyContent: { gap: spacing.md }, body: { color: colors.ink, fontSize: 16, lineHeight: 26, textAlign:'left', writingDirection:'ltr' }, professionalBox: { backgroundColor: colors.ocean50, borderRadius: radius.md, borderStartWidth: 3, borderStartColor: colors.ocean400, padding: spacing.md, gap: spacing.sm }, profHeader: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, profLabel: { color: colors.ocean600, fontSize: 10, fontWeight: '900', textTransform: 'uppercase',textAlign:'left' }, profName: { color: colors.inkSoft, fontSize: 12, fontWeight: '700', marginTop: 2,textAlign:'left' }, profBody: { color: colors.inkSoft, fontSize: 14, lineHeight: 22,textAlign:'left' }, actions: { flexDirection:'row', borderTopWidth: 1, borderTopColor: colors.border, paddingTop: spacing.sm, gap: spacing.lg, alignItems: 'center' }, action: { flexDirection: 'row', alignItems: 'center', gap: 5, minHeight: 30 }, actionText: { color: colors.muted, fontSize: 12, fontWeight: '700' }, save: { marginStart: 'auto' } });
