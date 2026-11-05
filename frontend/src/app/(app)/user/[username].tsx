import {useLanguage} from '@/localization/language-provider';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { EmptyState } from '@/components/product-ui';
import { Screen } from '@/components/screen';
import { PageResponse, PostItem } from '@/features/content/types';
import { PostCard } from '@/features/content/post-card';
import { FollowAction } from '@/features/platform/types';
import { PublicProfile } from '@/features/profile/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function PublicProfileScreen() {
  const { username = '' } = useLocalSearchParams<{ username: string }>();
  const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const [messageAllowed,setMessageAllowed]=useState(false);
  const [profile, setProfile] = useState<PublicProfile | null>(null);
  const [posts, setPosts] = useState<PostItem[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async (isCurrent: () => boolean = () => true) => {
    setError('');
    try {
      const next = await apiRequest<PublicProfile>(`/api/v1/profiles/${encodeURIComponent(username)}`);
      if (!isCurrent()) return;
      setProfile(next);
      const permission=await apiRequest<{allowed:boolean}>(`/api/v1/conversations/users/${next.userId}/permission`).catch(()=>({allowed:false}));if(!isCurrent())return;setMessageAllowed(permission.allowed);
      if (next.canSeeActivity) {
        const page = await apiRequest<PageResponse<PostItem>>(`/api/v1/profiles/${encodeURIComponent(username)}/posts?size=50`);
        if (!isCurrent()) return;
        setPosts(page.items);
      } else setPosts([]);
    } catch (caught) {
      if (!isCurrent()) return;
      setProfile(null); setPosts([]);
      setError(caught instanceof Error ? caught.message : 'Could not load this profile.');
    }
  }, [username]);

  useFocusEffect(useCallback(() => {
    let active=true;
    setProfile(null);setPosts([]);setMessageAllowed(false);
    void load(()=>active);
    return () => {active=false;setProfile(null);setPosts([]);setMessageAllowed(false);};
  }, [load]));

  async function toggleFollow() {
    if (!profile) return;
    setBusy(true); setError('');
    try {
      if (profile.following) await apiRequest(`/api/v1/social/follows/${profile.userId}`, { method: 'DELETE' });
      else if (profile.followRequested) await apiRequest(`/api/v1/social/follow-requests/outgoing/${profile.userId}`, { method: 'DELETE' });
      else await apiRequest<FollowAction>(`/api/v1/social/follows/${profile.userId}`, { method: 'PUT' });
      await load();
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not update follow status.'); }
    finally { setBusy(false); }
  }

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <Pressable accessibilityRole="button" accessibilityLabel="Go back" onPress={() => router.back()} style={styles.back}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable>
    {!!error && !profile && <EmptyState icon="person_off" title="Profile unavailable" body={error} />}
    {profile && <>
      <View style={styles.cover}><View style={styles.glow} /></View>
      <View style={styles.identity}><Avatar name={profile.displayName} uri={profile.avatarUrl} size={86} /><View style={styles.follow}><AppButton loading={busy} label={profile.following ? 'Following' : profile.followRequested ? 'Request sent' : 'Follow'} variant={profile.following || profile.followRequested ? 'secondary' : 'primary'} onPress={() => void toggleFollow()} /></View></View>
      {messageAllowed?<AppButton label={tr('Request a conversation','درخواست گفتگو')} disabled={busy} onPress={()=>{setBusy(true);void apiRequest<{id:string;status:string}>(`/api/v1/conversations/users/${profile.userId}`,{method:'POST'}).then(r=>{if(r.status==='PENDING')router.push('/message-requests' as never);else router.push({pathname:'/chat/[id]',params:{id:r.id}});}).catch(e=>setError(e.message)).finally(()=>setBusy(false));}}/>:<Text>{tr('New message requests are unavailable.','درخواست پیام تازه در دسترس نیست.')}</Text>}
      <AppButton label={tr('Block account','مسدودکردن حساب')} variant="secondary" disabled={busy} onPress={()=>{setBusy(true);void apiRequest(`/api/v1/social/blocks/${profile.userId}`,{method:'PUT'}).then(()=>{setProfile(null);setPosts([]);setMessageAllowed(false);}).catch(e=>setError(e.message)).finally(()=>setBusy(false));}}/>
      <AppButton label={tr('Report account','گزارش حساب')} variant="secondary" onPress={()=>router.push({pathname:'/tools/[tool]',params:{tool:'report',targetType:'USER',targetId:profile.userId}} as never)}/>
      <Text style={styles.name}>{profile.displayName}</Text><Text style={styles.handle}>@{profile.username}</Text>
      <Text style={styles.bio}>{profile.bio || 'This member has not added a bio yet.'}</Text>
      <View style={styles.counts}><Count value={profile.followerCount} label="Followers" /><Count value={profile.followingCount} label="Following" /><Count value={profile.postCount} label="Stories" /></View>
      {(profile.city || profile.countryCode) && <View style={styles.meta}><AppIcon name="location_on" size={18} color={colors.muted} /><Text style={styles.metaText}>{[profile.city, profile.countryCode].filter(Boolean).join(', ')}</Text></View>}
      <View style={styles.meta}><AppIcon name="calendar_month" size={18} color={colors.muted} /><Text style={styles.metaText}>Member since {new Date(profile.memberSince).getFullYear()}</Text></View>
      {!!error && <Text style={styles.error}>{error}</Text>}
      {!profile.canSeeActivity ? <View style={styles.privateCard}><AppIcon name="lock" size={30} color={colors.ocean700} /><Text style={styles.privateTitle}>This profile is private</Text><Text style={styles.privateBody}>{profile.followRequested ? 'Your follow request is waiting for approval.' : 'Follow this member to request access to their stories.'}</Text></View> : <><Text style={styles.sectionTitle}>Stories</Text>{posts.map(post => <PostCard key={post.id} post={post} />)}{!posts.length && <EmptyState icon="article" title="No visible stories" body="Stories shared by this member will appear here." />}</>}
    </>}
  </View></Screen>;
}

function Count({ value, label }: { value: number; label: string }) { return <View style={styles.count}><Text style={styles.countValue}>{value}</Text><Text style={styles.countLabel}>{label}</Text></View>; }

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  back: { width: 42, height: 42, borderRadius: 21, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' },
  cover: { height: 138, borderRadius: radius.xl, backgroundColor: colors.ocean950, overflow: 'hidden' }, glow: { position: 'absolute', width: 270, height: 270, borderRadius: 135, backgroundColor: colors.ocean600, right: -60, top: -140 },
  identity: { flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between', marginTop: -58, paddingHorizontal: spacing.md }, follow: { minWidth: 130 },
  name: { fontSize: typography.h2, color: colors.ink, fontWeight: '900' }, handle: { color: colors.muted }, bio: { color: colors.inkSoft, fontSize: 14, lineHeight: 22 },
  counts: { flexDirection: 'row', backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md }, count: { flex: 1, alignItems: 'center' }, countValue: { color: colors.ink, fontSize: 18, fontWeight: '900' }, countLabel: { color: colors.muted, fontSize: 10, marginTop: 3 },
  meta: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm }, metaText: { color: colors.muted, fontSize: 12 }, sectionTitle: { color: colors.ink, fontSize: typography.h3, fontWeight: '900', marginTop: spacing.sm },
  privateCard: { minHeight: 210, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.xl, alignItems: 'center', justifyContent: 'center', padding: spacing.xl, gap: spacing.sm }, privateTitle: { color: colors.ink, fontSize: 17, fontWeight: '900' }, privateBody: { color: colors.muted, fontSize: 12, lineHeight: 18, textAlign: 'center' }, error: { color: colors.danger, fontSize: 12 },
});
