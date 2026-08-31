import { router, useLocalSearchParams } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { EmptyState, Notice } from '@/components/product-ui';
import { Screen } from '@/components/screen';
import { PostCard } from '@/features/content/post-card';
import { useProduct } from '@/features/product/product-provider';
import { categories, MockPost, mockPosts } from '@/mocks/data';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function Detail() {
  const { id } = useLocalSearchParams<{ id: string }>(); const { state, addComment } = useProduct(); const [text, setText] = useState(''); const [error, setError] = useState('');
  const post = useMemo(() => {
    const mock = mockPosts.find(item => item.id === id); if (mock) return mock;
    const local = state.posts.find(item => item.id === id); if (!local) return null;
    const category = categories.find(item => item.id === local.categoryId) || categories[0];
    return { id: local.id, author: local.anonymous ? null : { userId: 'demo-user', username: state.profile.username, displayName: state.profile.displayName, avatarUrl: null }, communityId: local.communitySlug || null, body: local.body, anonymous: local.anonymous, visibility: local.audience, contentWarning: local.contentWarning || null, likeCount: 0, commentCount: state.comments.filter(item => item.postId === local.id).length, liked: false, bookmarked: false, createdAt: local.createdAt, mood: local.mood, categoryId: local.categoryId, category, postType: local.postType } satisfies MockPost;
  }, [id, state]);
  if (!post) return <Screen scroll style={styles.screen}><View style={styles.content}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><EmptyState icon="article" title="Post not found" body="It may have been removed, made private, or the link is incomplete." /><AppButton label="Return to feed" onPress={() => router.replace('/')} /></View></Screen>;
  const localComments = state.comments.filter(item => item.postId === post.id);
  const send = () => { if (text.trim().length < 2) return setError('Write a little more before sending.'); addComment(post.id, text.trim()); setText(''); setError(''); };
  return <Screen scroll style={styles.screen}><View style={styles.content}><View style={styles.top}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Pressable onPress={() => router.push({ pathname: '/tools/[tool]' as never, params: { tool: 'report', targetType: 'post', targetId: post.id } } as never)}><AppIcon name="flag" color={colors.muted} /></Pressable></View><PostCard post={post}/><Text style={styles.title}>Supportive replies</Text><Notice title="Respond to the person, not the diagnosis" body="Share what helped you, ask gentle questions, and leave medical decisions to qualified professionals." />{['Thank you for trusting this space with your experience.','I have had similar days. Those small, gentle steps really do matter.'].map((body,index) => <Comment key={body} name={index ? 'Alex' : 'Riley'} body={body} />)}{localComments.map(comment => <Comment key={comment.id} name={state.profile.displayName} body={comment.body} mine />)}<View style={styles.composer}><TextInput value={text} onChangeText={setText} multiline placeholder="Write a supportive reply…" placeholderTextColor={colors.muted} style={styles.input}/><Text style={styles.counter}>{text.length}/800</Text><AppButton label="Send reply" onPress={send}/>{!!error && <Text style={styles.error}>{error}</Text>}</View></View></Screen>;
}

function Comment({ name, body, mine = false }: { name: string; body: string; mine?: boolean }) { return <View style={styles.comment}><Avatar name={name} size={38}/><View style={styles.flex}><View style={styles.commentHead}><Text style={styles.commentName}>{name}</Text>{mine && <Text style={styles.you}>YOU</Text>}</View><Text style={styles.commentBody}>{body}</Text><View style={styles.actions}><Text style={styles.reply}>Reply</Text><Text style={styles.reply}>Helpful</Text><Text style={styles.reply}>Report</Text></View></View></View>; }
const styles = StyleSheet.create({ screen: { padding: 0 }, content: { width:'100%',maxWidth:layout.maxContent,alignSelf:'center',padding:spacing.md,gap:spacing.md }, top:{flexDirection:'row',alignItems:'center',justifyContent:'space-between'}, title:{fontSize:typography.h3,fontWeight:'900',color:colors.ink},comment:{flexDirection:'row',gap:spacing.sm,backgroundColor:colors.white,borderWidth:1,borderColor:colors.border,borderRadius:radius.lg,padding:spacing.md},flex:{flex:1},commentHead:{flexDirection:'row',alignItems:'center',gap:6},commentName:{fontWeight:'900',color:colors.ink,fontSize:11},you:{fontSize:7,color:colors.ocean700,backgroundColor:colors.ocean100,paddingHorizontal:5,paddingVertical:2,borderRadius:radius.pill,fontWeight:'900'},commentBody:{color:colors.inkSoft,fontSize:12,lineHeight:19,marginTop:4},actions:{flexDirection:'row',gap:spacing.md,marginTop:spacing.sm},reply:{color:colors.muted,fontSize:9,fontWeight:'800'},composer:{gap:spacing.sm,backgroundColor:colors.white,borderWidth:1,borderColor:colors.border,borderRadius:radius.lg,padding:spacing.md},input:{minHeight:100,borderWidth:1,borderColor:colors.borderStrong,borderRadius:radius.md,padding:spacing.md,color:colors.ink,textAlign:'left',writingDirection:'ltr',textAlignVertical:'top'},counter:{alignSelf:'flex-end',fontSize:9,color:colors.muted},error:{color:colors.danger,fontSize:10,fontWeight:'800'} });
