import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, shadow, spacing } from '@/theme/tokens';
import { usePlatformData } from '@/features/platform/data-provider';
import { PostItem } from './types';

const categoryGroupNames = { EMOTION: 'EMOTION', CONDITION: 'CONDITION', LIFE_EXPERIENCE: 'LIFE EXPERIENCE' } as const;

function relativeTime(value: string) {
  const minutes = Math.max(1, Math.floor((Date.now() - new Date(value).getTime()) / 60000));
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60); return `${hours}h`;
}
export function PostCard({ post: initialPost, elevated = true }: { post: PostItem; elevated?: boolean }) {
  const { t,language } = useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const [override,setOverride]=useState<{source:PostItem;value:PostItem}|null>(null);
  const post=override?.source===initialPost?override.value:initialPost;
  const setPost=(update:(value:PostItem)=>PostItem)=>setOverride(current=>({source:initialPost,value:update(current?.source===initialPost?current.value:initialPost)}));
  const {preferences}=usePlatformData();
  const [reveal,setReveal]=useState<{source:PostItem;shown:boolean}|null>(null);
  const revealed=reveal?.source===initialPost?reveal.shown:null;
  const setSensitiveRevealed=(shown:boolean)=>setReveal({source:initialPost,shown});
  const sensitiveRevealed=!post.contentWarning||(revealed??preferences?.blurSensitiveContent===false);
  const category = post.category;
  const postType = post.postType ?? 'EXPERIENCE';
  async function toggleLike() { const next = !post.liked; setPost(c => ({ ...c, liked: next, likeCount: c.likeCount + (next ? 1 : -1) })); try { await apiRequest(`/api/v1/posts/${post.id}/like`, { method: next ? 'PUT' : 'DELETE' }); } catch { setPost(c => ({ ...c, liked: !next, likeCount: c.likeCount + (next ? -1 : 1) })); } }
  async function toggleBookmark() { const next = !post.bookmarked; setPost(c => ({ ...c, bookmarked: next })); try { await apiRequest(`/api/v1/posts/${post.id}/bookmark`, { method: next ? 'PUT' : 'DELETE' }); } catch { setPost(c => ({ ...c, bookmarked: !next })); } }
  const authorName = post.author?.displayName ?? t('anonymous');
  const openPost = () => router.push({ pathname: '/(app)/post/[id]', params: { id: post.id } });
  const openAuthor = () => post.author
    ? router.push({ pathname: '/user/[username]' as never, params: { username: post.author.username } } as never)
    : openPost();
  return <View style={[styles.card, elevated && shadow.soft,{direction:language==='fa'?'rtl':'ltr'}]}>
    <Pressable accessibilityRole="button" accessibilityLabel={post.author?tr(`Open ${authorName}'s profile`,`نمایش پروفایل ${authorName}`):tr('Read anonymous post','خواندن پست ناشناس')} onPress={openAuthor} style={styles.authorRow}>
      <Avatar name={authorName} uri={post.author?.avatarUrl} />
      <View style={styles.authorText}><View style={styles.nameLine}><Text style={styles.name}>{authorName}</Text>{post.anonymous && <View style={styles.anonymousBadge}><AppIcon name="visibility_off" size={13} color={colors.muted} /><Text style={styles.badgeText}>{t('anonymous')}</Text></View>}</View>
        <Text style={styles.meta}>{post.author ? `@${post.author.username} · ` : ''}{relativeTime(post.createdAt).replace('m',tr('m',' دقیقه')).replace('h',tr('h',' ساعت'))} · {post.visibility==='PUBLIC'?tr('Eligible members','اعضای مجاز'):post.visibility==='COMMUNITY'?tr('Community members','اعضای گروه'):tr('Approved followers','دنبال‌کنندگان تأییدشده')}</Text></View>
      <AppIcon name="more_horiz" color={colors.muted} />
    </Pressable>
    <View style={styles.chips}>{category && <><View style={styles.groupChip}><Text style={styles.groupText}>{language==='fa'?{EMOTION:'احساس',CONDITION:'وضعیت',LIFE_EXPERIENCE:'تجربهٔ زندگی'}[category.group]:categoryGroupNames[category.group]}</Text></View><View style={[styles.categoryChip, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={15} color={category.color} /><Text style={[styles.categoryText, { color: category.color }]}>{category.name}</Text></View></>}<View style={styles.typeChip}><Text style={styles.typeText}>{postType === 'QUESTION' ? t('question') : postType === 'REFLECTION' ? t('reflection') : t('experience')}</Text></View>{post.mood && <View style={[styles.chip, styles.moodChip]}><Text style={styles.moodText}>● {post.mood}</Text></View>}</View>
    {!!post.contentWarning && !sensitiveRevealed && <View style={styles.warningGate}>
      <View style={styles.warningIcon}><AppIcon name="visibility_off" size={22} color={colors.coral} /></View>
      <View style={styles.warningCopy}><Text style={styles.warningTitle}>{tr("Sensitive story hidden","پست حساس پنهان است")}</Text><Text style={styles.warningText}>{post.contentWarning}. {tr('Your safety settings keep this content covered until you choose to see it.','این محتوا طبق تنظیمات ایمنی تا انتخاب شما پنهان می‌ماند.')}</Text></View>
      <Pressable accessibilityRole="button" accessibilityLabel={tr("Show sensitive story","نمایش پست حساس")} onPress={event => { event.stopPropagation(); setSensitiveRevealed(true); }} style={styles.revealButton}><Text style={styles.revealButtonText}>{tr("Show story","نمایش پست")}</Text></Pressable>
    </View>}
    {!!post.contentWarning && sensitiveRevealed && <View style={styles.warning}><AppIcon name="shield" size={18} color={colors.coral} /><Text style={[styles.warningText, styles.warningGrow]}>{post.contentWarning}</Text><Pressable accessibilityRole="button" accessibilityLabel={tr("Hide sensitive story","پنهان‌کردن پست حساس")} onPress={event => { event.stopPropagation(); setSensitiveRevealed(false); }}><Text style={styles.warningAction}>{tr("Hide","پنهان‌کردن")}</Text></Pressable></View>}
    {sensitiveRevealed && <Pressable accessibilityRole="button" accessibilityLabel={tr(`Read full post by ${authorName}`,`خواندن پست کامل ${authorName}`)} onPress={openPost} style={styles.storyContent}><Text style={[styles.body,{textAlign:language==='fa'?'right':'left',writingDirection:language==='fa'?'rtl':'ltr'}]}>{post.body}</Text>
    {!!post.professionalReply && <View style={styles.professionalBox}>
      <View style={styles.profHeader}><Avatar name={post.professionalReply.professional.displayName} size={34} verified /><View style={{ flex: 1 }}><Text style={styles.profLabel}>{t('professionalResponse')}</Text><Text style={styles.profName}>{post.professionalReply.professional.displayName} · {post.professionalReply.professional.title}</Text></View></View>
      <Text style={styles.profBody} numberOfLines={3}>{post.professionalReply.body}</Text>
    </View>}</Pressable>}
    <View style={styles.actions}>
      <Pressable accessibilityRole="button" accessibilityLabel={post.liked ? tr('Unlike post','لغو پسندیدن پست') : tr('Like post','پسندیدن پست')} accessibilityState={{ selected: post.liked }} onPress={e => { e.stopPropagation(); void toggleLike(); }} style={styles.action}><AppIcon name="favorite" filled={post.liked} size={22} color={post.liked ? colors.coral : colors.muted} /><Text style={[styles.actionText, post.liked && { color: colors.coral }]}>{post.likeCount}</Text></Pressable>
      <View style={styles.action}><AppIcon name="chat_bubble" size={21} color={colors.muted} /><Text style={styles.actionText}>{post.commentCount}</Text></View>
      <View style={styles.action}><AppIcon name="ios_share" size={22} color={colors.muted} /></View>
      <Pressable accessibilityRole="button" accessibilityLabel={post.bookmarked ? tr('Remove bookmark','حذف نشانک') : tr('Bookmark post','ذخیرهٔ پست')} accessibilityState={{ selected: post.bookmarked }} onPress={e => { e.stopPropagation(); void toggleBookmark(); }} style={[styles.action, styles.save]}><AppIcon name="bookmark" filled={post.bookmarked} size={22} color={post.bookmarked ? colors.ocean600 : colors.muted} /></Pressable>
    </View>
  </View>;
}
const styles = StyleSheet.create({ card: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.md }, authorRow: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, authorText: { flex: 1 }, nameLine: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, name: { color: colors.ink, fontWeight: '800', fontSize: 15 }, meta: { color: colors.muted, fontSize: 12, marginTop: 2, textAlign:'left' }, anonymousBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, backgroundColor: colors.foam, borderRadius: radius.pill, paddingHorizontal: 7, paddingVertical: 3 }, badgeText: { color: colors.muted, fontSize: 10, fontWeight: '700' }, chips: { flexDirection:'row', gap: spacing.sm, flexWrap: 'wrap', alignItems:'center' }, chip: { backgroundColor: colors.ocean50, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 5 }, groupChip:{borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5,backgroundColor:colors.ocean900},groupText:{fontSize:8,color:colors.white,fontWeight:'900',letterSpacing:.6},categoryChip:{flexDirection:'row',alignItems:'center',gap:4,borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5},categoryText:{fontSize:11,fontWeight:'900'},typeChip:{borderRadius:radius.pill,paddingHorizontal:spacing.sm,paddingVertical:5,backgroundColor:colors.foam,borderWidth:1,borderColor:colors.border},typeText:{fontSize:10,color:colors.muted,fontWeight:'800'}, moodChip: { backgroundColor: colors.sunSoft }, moodText: { color: '#9A6B22', fontSize: 12, fontWeight: '700' }, warning: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.coralSoft, padding: spacing.sm, borderRadius: radius.sm }, warningGate: { flexDirection: 'row', alignItems: 'center', flexWrap: 'wrap', gap: spacing.sm, backgroundColor: colors.coralSoft, borderWidth: 1, borderColor: '#F7C8BE', padding: spacing.md, borderRadius: radius.md }, warningIcon: { width: 42, height: 42, borderRadius: 21, backgroundColor: colors.white, alignItems: 'center', justifyContent: 'center' }, warningCopy: { flex: 1, minWidth: 190 }, warningTitle: { color: colors.ink, fontSize: 14, fontWeight: '900', marginBottom: 3 }, warningText: { color: colors.danger, fontSize: 12, lineHeight: 18 }, warningGrow: { flex: 1 }, warningAction: { color: colors.danger, fontSize: 12, fontWeight: '900', padding: 5 }, revealButton: { minHeight: 38, borderRadius: radius.sm, backgroundColor: colors.white, borderWidth: 1, borderColor: '#F2B5A9', paddingHorizontal: spacing.md, alignItems: 'center', justifyContent: 'center' }, revealButtonText: { color: colors.danger, fontSize: 12, fontWeight: '900' }, storyContent: { gap: spacing.md }, body: { color: colors.ink, fontSize: 16, lineHeight: 26, textAlign:'left', writingDirection:'ltr' }, professionalBox: { backgroundColor: colors.ocean50, borderRadius: radius.md, borderStartWidth: 3, borderStartColor: colors.ocean400, padding: spacing.md, gap: spacing.sm }, profHeader: { flexDirection:'row', alignItems: 'center', gap: spacing.sm }, profLabel: { color: colors.ocean600, fontSize: 10, fontWeight: '900', textTransform: 'uppercase',textAlign:'left' }, profName: { color: colors.inkSoft, fontSize: 12, fontWeight: '700', marginTop: 2,textAlign:'left' }, profBody: { color: colors.inkSoft, fontSize: 14, lineHeight: 22,textAlign:'left' }, actions: { flexDirection:'row', borderTopWidth: 1, borderTopColor: colors.border, paddingTop: spacing.sm, gap: spacing.lg, alignItems: 'center' }, action: { flexDirection: 'row', alignItems: 'center', gap: 5, minHeight: 30 }, actionText: { color: colors.muted, fontSize: 12, fontWeight: '700' }, save: { marginStart: 'auto' } });
