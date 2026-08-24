import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { Screen } from '@/components/screen';
import { PostCard } from '@/features/content/post-card';
import { PageResponse, PostItem } from '@/features/content/types';
import { UserSearchItem } from '@/features/search/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

type SearchMode = 'posts' | 'people';

export default function SearchScreen() {
  const { t, isRtl } = useLanguage(); const [query, setQuery] = useState(''); const [mode, setMode] = useState<SearchMode>('posts');
  const [posts, setPosts] = useState<PostItem[]>([]); const [people, setPeople] = useState<UserSearchItem[]>([]); const [loading, setLoading] = useState(false); const [error, setError] = useState('');
  async function search() {
    if (query.trim().length < 2) return; setLoading(true); setError('');
    try {
      if (mode === 'posts') setPosts((await apiRequest<PageResponse<PostItem>>(`/api/v1/search/posts?q=${encodeURIComponent(query)}`)).items);
      else setPeople((await apiRequest<PageResponse<UserSearchItem>>(`/api/v1/search/users?q=${encodeURIComponent(query)}`)).items);
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }
  async function toggleFollow(person: UserSearchItem) {
    const next = !person.following; setPeople((items) => items.map((item) => item.userId === person.userId ? { ...item, following: next, followerCount: item.followerCount + (next ? 1 : -1) } : item));
    try { await apiRequest(`/api/v1/social/follows/${person.userId}`, { method: next ? 'PUT' : 'DELETE' }); }
    catch { setPeople((items) => items.map((item) => item.userId === person.userId ? person : item)); }
  }
  return (
    <Screen scroll style={styles.content}>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('search')}</Text>
      <TextInput value={query} onChangeText={setQuery} onSubmitEditing={search} style={[styles.input, { textAlign: isRtl ? 'right' : 'left' }]} placeholder={t('search')} placeholderTextColor={colors.muted} />
      <View style={[styles.modes, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>{(['posts', 'people'] as SearchMode[]).map((item) => <Pressable key={item} onPress={() => setMode(item)} style={[styles.mode, mode === item && styles.modeActive]}><Text style={mode === item ? styles.modeTextActive : styles.modeText}>{item === 'posts' ? t('posts') : t('people')}</Text></Pressable>)}</View>
      <AppButton label={t('search')} loading={loading} onPress={search} />
      {!!error && <Text style={styles.error}>{error}</Text>}
      {mode === 'posts' ? posts.map((post) => <PostCard key={post.id} post={post} />) : people.map((person) => (
        <View key={person.userId} style={[styles.person, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
          <View style={styles.personInfo}><Text style={[styles.personName, { textAlign: isRtl ? 'right' : 'left' }]}>{person.displayName}</Text><Text style={[styles.personUsername, { textAlign: isRtl ? 'right' : 'left' }]}>@{person.username} · {person.followerCount}</Text></View>
          <Pressable onPress={() => toggleFollow(person)} style={[styles.follow, person.following && styles.following]}><Text style={person.following ? styles.followingText : styles.followText}>{person.following ? t('following') : t('follow')}</Text></Pressable>
        </View>
      ))}
    </Screen>
  );
}
const styles = StyleSheet.create({ content: { gap: spacing.md }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '800', marginTop: spacing.md }, input: { minHeight: 52, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.white, color: colors.ink, paddingHorizontal: spacing.md, fontSize: 16 }, modes: { gap: spacing.sm }, mode: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radius.pill, borderWidth: 1, borderColor: colors.border }, modeActive: { backgroundColor: colors.ocean600 }, modeText: { color: colors.muted }, modeTextActive: { color: colors.white, fontWeight: '700' }, error: { color: colors.danger }, person: { alignItems: 'center', gap: spacing.md, padding: spacing.md, borderRadius: radius.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border }, personInfo: { flex: 1 }, personName: { color: colors.ink, fontWeight: '800' }, personUsername: { color: colors.muted, marginTop: 3 }, follow: { borderRadius: radius.pill, backgroundColor: colors.ocean600, paddingHorizontal: spacing.md, paddingVertical: spacing.sm }, following: { backgroundColor: colors.ocean100 }, followText: { color: colors.white, fontWeight: '700' }, followingText: { color: colors.ocean700, fontWeight: '700' } });
