import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function Notifications() {
  const { t, isRtl } = useLanguage();
  const { notifications, setNotifications } = usePlatformData();
  const message = (type: string) => type === 'PROFESSIONAL_REPLY' ? t('notificationProfessional') : type === 'LIKE' ? t('notificationLike') : type === 'FOLLOW' ? t('notificationFollow') : type === 'COMMENT' ? 'commented on your story' : 'replied to your conversation';

  async function read(id: string) {
    await apiRequest(`/api/v1/notifications/${id}/read`, { method: 'PUT' });
    setNotifications(items => items.map(item => item.id === id ? { ...item, read: true } : item));
  }
  async function readAll() {
    await apiRequest('/api/v1/notifications/read-all', { method: 'PUT' });
    setNotifications(items => items.map(item => ({ ...item, read: true })));
  }

  return <SafeAreaView style={styles.safe}><FlatList data={notifications} keyExtractor={item => item.id} contentContainerStyle={styles.list}
    ListHeaderComponent={<View style={[styles.header, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}><Text style={styles.title}>{t('notifications')}</Text><Pressable onPress={() => void readAll()}><Text style={styles.read}>{t('markAllRead')}</Text></Pressable></View>}
    renderItem={({ item }) => <Pressable onPress={() => void read(item.id)} style={[styles.item, !item.read && styles.unread, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}><Avatar name={item.actor?.displayName ?? t('anonymous')} uri={item.actor?.avatarUrl} size={46} verified={item.type === 'PROFESSIONAL_REPLY'} /><View style={{ flex: 1 }}><Text style={[styles.message, { textAlign: isRtl ? 'right' : 'left' }]}><Text style={styles.actor}>{item.actor?.displayName ?? 'Someone'} </Text>{message(item.type)}</Text><Text style={[styles.time, { textAlign: isRtl ? 'right' : 'left' }]}>{new Date(item.createdAt).toLocaleString(isRtl ? 'fa-IR' : 'en-US')}</Text></View>{!item.read && <View style={styles.dot} />}<AppIcon name="chevron_right" color={colors.borderStrong} /></Pressable>}
    ItemSeparatorComponent={() => <View style={{ height: spacing.sm }} />}
    ListEmptyComponent={<Text style={styles.empty}>You have no notifications yet.</Text>} />
  </SafeAreaView>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.foam }, list: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, paddingBottom: spacing.xxxl },
  header: { alignItems: 'center', justifyContent: 'space-between', marginBottom: spacing.lg }, title: { fontSize: typography.h1, fontWeight: '900', color: colors.ink }, read: { fontSize: 12, color: colors.ocean600, fontWeight: '800' },
  item: { alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md }, unread: { backgroundColor: colors.ocean50, borderColor: colors.ocean200 },
  message: { fontSize: 14, color: colors.inkSoft, lineHeight: 21 }, actor: { fontWeight: '900', color: colors.ink }, time: { fontSize: 11, color: colors.muted, marginTop: 4 }, dot: { width: 8, height: 8, borderRadius: 4, backgroundColor: colors.ocean500 }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.xl },
});
