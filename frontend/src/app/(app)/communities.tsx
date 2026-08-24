import { router } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Switch, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { Screen } from '@/components/screen';
import { Community } from '@/features/community/types';
import { PageResponse } from '@/features/content/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function CommunitiesScreen() {
  const { t, isRtl } = useLanguage();
  const [communities, setCommunities] = useState<Community[]>([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [description, setDescription] = useState('');
  const [privateCommunity, setPrivateCommunity] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const response = await apiRequest<PageResponse<Community>>(`/api/v1/communities?q=${encodeURIComponent(query)}`);
      setCommunities(response.items);
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }, [query, t]);

  useEffect(() => {
    let active = true;
    apiRequest<PageResponse<Community>>(`/api/v1/communities?q=${encodeURIComponent(query)}`)
      .then((response) => { if (active) { setCommunities(response.items); setLoading(false); } })
      .catch((exception) => { if (active) { setError(exception instanceof Error ? exception.message : t('genericError')); setLoading(false); } });
    return () => { active = false; };
  }, [query, t]);

  async function create() {
    if (!name.trim() || !slug.trim()) return setError(t('requiredFields'));
    setCreating(true); setError('');
    try {
      const community = await apiRequest<Community>('/api/v1/communities', {
        method: 'POST', body: JSON.stringify({ name, slug, description: description || null, privateCommunity }),
      });
      setCommunities((current) => [community, ...current]);
      setName(''); setSlug(''); setDescription(''); setPrivateCommunity(false);
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setCreating(false); }
  }

  async function toggleMembership(community: Community) {
    try {
      if (community.member) {
        await apiRequest(`/api/v1/communities/${community.id}/membership`, { method: 'DELETE' });
        setCommunities((items) => items.map((item) => item.id === community.id
          ? { ...item, member: false, membershipRole: null, memberCount: Math.max(0, item.memberCount - 1) } : item));
      } else {
        const updated = await apiRequest<Community>(`/api/v1/communities/${community.id}/membership`, { method: 'PUT' });
        setCommunities((items) => items.map((item) => item.id === community.id ? updated : item));
      }
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
  }

  return (
    <Screen style={styles.screen}>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('communities')}</Text>
      <Text style={[styles.subtitle, { textAlign: isRtl ? 'right' : 'left' }]}>{t('discoverCommunities')}</Text>
      <AppField label={t('search')} value={query} onChangeText={setQuery} returnKeyType="search" />
      <View style={styles.createPanel}>
        <AppField label={t('communityName')} value={name} onChangeText={setName} maxLength={80} />
        <AppField label={t('communitySlug')} value={slug} onChangeText={(value) => setSlug(value.toLowerCase())} autoCapitalize="none" maxLength={100} />
        <AppField label={t('description')} value={description} onChangeText={setDescription} maxLength={500} />
        <View style={[styles.switchRow, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
          <Text style={styles.switchText}>{t('privateCommunity')}</Text>
          <Switch value={privateCommunity} onValueChange={setPrivateCommunity} trackColor={{ true: colors.ocean500 }} />
        </View>
        <AppButton label={t('createCommunity')} loading={creating} onPress={create} />
      </View>
      {!!error && <Text style={styles.error}>{error}</Text>}
      <FlatList
        data={communities}
        keyExtractor={(item) => item.id}
        refreshing={loading}
        onRefresh={() => void load()}
        contentContainerStyle={styles.list}
        ListEmptyComponent={!loading ? <Text style={styles.empty}>{t('noCommunities')}</Text> : null}
        renderItem={({ item }) => (
          <Pressable onPress={() => router.push({ pathname: '/community/[slug]', params: { slug: item.slug } })} style={styles.card}>
            <View style={[styles.row, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
              <View style={styles.cardText}>
                <Text style={[styles.name, { textAlign: isRtl ? 'right' : 'left' }]}>{item.name}</Text>
                <Text style={[styles.meta, { textAlign: isRtl ? 'right' : 'left' }]}>{item.memberCount} {t('members')} · {item.privateCommunity ? '◉' : '○'}</Text>
              </View>
              <Pressable onPress={(event) => { event.stopPropagation(); void toggleMembership(item); }}
                disabled={item.membershipRole === 'OWNER'} style={[styles.join, item.member && styles.joined]}>
                <Text style={item.member ? styles.joinedText : styles.joinText}>{item.member ? t('joined') : t('join')}</Text>
              </Pressable>
            </View>
            {!!item.description && <Text style={[styles.description, { textAlign: isRtl ? 'right' : 'left' }]}>{item.description}</Text>}
          </Pressable>
        )}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, gap: spacing.md }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 16 }, createPanel: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.md },
  switchRow: { alignItems: 'center', justifyContent: 'space-between' }, switchText: { color: colors.ink, fontWeight: '600' },
  list: { gap: spacing.md, paddingBottom: spacing.xxl }, card: { backgroundColor: colors.white, padding: spacing.lg, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, gap: spacing.sm },
  row: { alignItems: 'center', gap: spacing.md }, cardText: { flex: 1 }, name: { color: colors.ink, fontSize: 18, fontWeight: '800' },
  meta: { color: colors.muted, marginTop: spacing.xs }, description: { color: colors.muted, lineHeight: 22 },
  join: { borderRadius: radius.pill, borderWidth: 1, borderColor: colors.ocean600, paddingHorizontal: spacing.md, paddingVertical: spacing.sm },
  joined: { backgroundColor: colors.ocean100, borderColor: colors.ocean300 }, joinText: { color: colors.ocean700, fontWeight: '700' }, joinedText: { color: colors.ocean700, fontWeight: '700' },
  error: { color: colors.danger }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.xl },
});
