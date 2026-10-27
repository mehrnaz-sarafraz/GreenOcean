import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import {useForegroundPoll} from '@/features/live-support/foreground';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { PageResponse } from '@/features/content/types';
import { NotificationItem } from '@/features/notification/types';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function Notifications() {
  const { t, isRtl,language } = useLanguage();
  const { notifications, setNotifications } = usePlatformData();
  const message = (type: string) => type === 'PROFESSIONAL_REPLY' ? t('notificationProfessional') : type === 'LIKE' ? t('notificationLike') : type === 'FOLLOW' ? t('notificationFollow') : type === 'FOLLOW_REQUEST' ? (language==='fa'?'درخواست دنبال‌کردن داده است':'requested to follow you') : type==='FOLLOW_ACCEPTED'?(language==='fa'?'درخواست دنبال‌کردن را پذیرفت':'accepted your follow request'):type==='MESSAGE_REQUEST'?(language==='fa'?'درخواست پیام فرستاد':'requested a private conversation') : type === 'COMMENT' ? 'commented on your story' : type === 'SYSTEM_NOTICE' ? '' : 'replied to your conversation';
  useForegroundPoll(async isCurrent=>{try{const r=await apiRequest<PageResponse<NotificationItem>>('/api/v1/notifications?size=50');if(isCurrent())setNotifications(r.items);}catch{if(isCurrent())setNotifications([]);}},10000,()=>setNotifications([]));

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
    renderItem={({ item }) => <Pressable onPress={() => {void read(item.id);if(item.type==='SYSTEM_NOTICE'&&item.title==='Content review updated')router.push('/content-decisions');else if(item.type==='MESSAGE_REQUEST')router.push('/message-requests' as never);else if(item.type==='FOLLOW_REQUEST')router.push('/tools/followers' as never);else if(item.postId)router.push({pathname:'/post/[id]',params:{id:item.postId}});}} style={[styles.item, !item.read && styles.unread, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>{item.type === 'SYSTEM_NOTICE' ? <View style={styles.noticeIcon}><AppIcon name="campaign" color={colors.ocean700} /></View> : <Avatar name={item.actor?.displayName ?? t('anonymous')} uri={item.actor?.avatarUrl} size={46} verified={item.type === 'PROFESSIONAL_REPLY'} />}<View style={{ flex: 1 }}>{item.type === 'SYSTEM_NOTICE' ? <><Text style={[styles.actor, { textAlign: isRtl ? 'right' : 'left' }]}>{item.title==='Content review updated'?(language==='fa'?'نتیجهٔ بررسی محتوا به‌روز شد':item.title):item.title}</Text><Text style={[styles.message, { textAlign: isRtl ? 'right' : 'left' }]}>{item.title==='Content review updated'?(language==='fa'?'برای مشاهدهٔ نتیجه، تصمیم‌های محتوای خود را باز کنید.':item.body):item.body}</Text></> : <Text style={[styles.message, { textAlign: isRtl ? 'right' : 'left' }]}><Text style={styles.actor}>{item.actor?.displayName ?? 'Someone'} </Text>{message(item.type)}</Text>}<Text style={[styles.time, { textAlign: isRtl ? 'right' : 'left' }]}>{new Date(item.createdAt).toLocaleString(isRtl ? 'fa-IR' : 'en-US')}</Text></View>{!item.read && <View style={styles.dot} />}<AppIcon name="chevron_right" color={colors.borderStrong} /></Pressable>}
    ItemSeparatorComponent={() => <View style={{ height: spacing.sm }} />}
    ListEmptyComponent={<Text style={styles.empty}>You have no notifications yet.</Text>} />
  </SafeAreaView>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.foam }, list: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, paddingBottom: spacing.xxxl },
  header: { alignItems: 'center', justifyContent: 'space-between', marginBottom: spacing.lg }, title: { fontSize: typography.h1, fontWeight: '900', color: colors.ink }, read: { fontSize: 12, color: colors.ocean600, fontWeight: '800' },
  item: { alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md }, unread: { backgroundColor: colors.ocean50, borderColor: colors.ocean200 },
  message: { fontSize: 14, color: colors.inkSoft, lineHeight: 21 }, actor: { fontWeight: '900', color: colors.ink }, noticeIcon: { width: 46, height: 46, borderRadius: 23, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, time: { fontSize: 11, color: colors.muted, marginTop: 4 }, dot: { width: 8, height: 8, borderRadius: 4, backgroundColor: colors.ocean500 }, empty: { color: colors.muted, textAlign: 'center', padding: spacing.xl },
});
