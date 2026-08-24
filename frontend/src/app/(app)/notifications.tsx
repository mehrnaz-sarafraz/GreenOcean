import { router } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { Screen } from '@/components/screen';
import { PageResponse } from '@/features/content/types';
import { NotificationItem } from '@/features/notification/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { TranslationKey } from '@/localization/translations';
import { colors, radius, spacing } from '@/theme/tokens';

export default function NotificationsScreen() {
  const { t, isRtl } = useLanguage();
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try { setItems((await apiRequest<PageResponse<NotificationItem>>('/api/v1/notifications')).items); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }, [t]);

  useEffect(() => {
    let active = true;
    apiRequest<PageResponse<NotificationItem>>('/api/v1/notifications')
      .then((response) => { if (active) { setItems(response.items); setLoading(false); } })
      .catch((exception) => { if (active) { setError(exception instanceof Error ? exception.message : t('genericError')); setLoading(false); } });
    return () => { active = false; };
  }, [t]);

  async function open(item: NotificationItem) {
    if (!item.read) {
      setItems((current) => current.map((value) => value.id === item.id ? { ...value, read: true } : value));
      try { await apiRequest(`/api/v1/notifications/${item.id}/read`, { method: 'PUT' }); } catch { await load(); }
    }
    if (item.postId) router.push({ pathname: '/post/[id]', params: { id: item.postId } });
  }

  async function markAllRead() {
    setItems((current) => current.map((item) => ({ ...item, read: true })));
    try { await apiRequest('/api/v1/notifications/read-all', { method: 'PUT' }); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); await load(); }
  }

  function messageKey(item: NotificationItem): TranslationKey {
    if (item.type === 'LIKE') return item.commentId ? 'notificationCommentLike' : 'notificationLike';
    if (item.type === 'COMMENT') return 'notificationComment';
    if (item.type === 'REPLY') return 'notificationReply';
    return 'notificationFollow';
  }

  return (
    <Screen style={styles.screen}>
      <View style={[styles.header, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
        <Text style={styles.title}>{t('notifications')}</Text>
        <View style={styles.markAll}><AppButton label={t('markAllRead')} variant="secondary" onPress={() => void markAllRead()} /></View>
      </View>
      {!!error && <Text style={styles.error}>{error}</Text>}
      <FlatList
        data={items}
        keyExtractor={(item) => item.id}
        refreshing={loading}
        onRefresh={() => void load()}
        contentContainerStyle={styles.list}
        ListEmptyComponent={!loading ? <Text style={styles.empty}>{t('notificationEmpty')}</Text> : null}
        renderItem={({ item }) => (
          <Pressable onPress={() => void open(item)} style={[styles.card, !item.read && styles.unread]}>
            <View style={styles.dot}>{!item.read && <View style={styles.dotInner} />}</View>
            <View style={styles.cardContent}>
              <Text style={[styles.message, { textAlign: isRtl ? 'right' : 'left' }]}>
                <Text style={styles.actor}>{item.actor?.displayName ?? t('anonymous')} </Text>{t(messageKey(item))}
              </Text>
              <Text style={[styles.time, { textAlign: isRtl ? 'right' : 'left' }]}>{new Date(item.createdAt).toLocaleString()}</Text>
            </View>
          </Pressable>
        )}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, gap: spacing.lg }, header: { alignItems: 'center', justifyContent: 'space-between', gap: spacing.md },
  title: { color: colors.ocean900, fontSize: 34, fontWeight: '800' }, markAll: { maxWidth: 190 },
  list: { gap: spacing.sm, paddingBottom: spacing.xxl }, card: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md },
  unread: { backgroundColor: colors.ocean100, borderColor: colors.ocean300 }, dot: { width: 12, height: 12, alignItems: 'center', justifyContent: 'center' },
  dotInner: { width: 9, height: 9, borderRadius: radius.pill, backgroundColor: colors.ocean500 }, cardContent: { flex: 1, gap: spacing.xs },
  message: { color: colors.ink, lineHeight: 22 }, actor: { fontWeight: '800' }, time: { color: colors.muted, fontSize: 12 },
  error: { color: colors.danger }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.xl },
});
