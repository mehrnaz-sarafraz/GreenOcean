import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { usePlatformData } from '@/features/platform/data-provider';
import { SupportChannel } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

export default function Channels() {
  const { channels, setChannels, refresh } = usePlatformData();
  const [mode, setMode] = useState<'ALL' | 'GROUP' | 'ANNOUNCEMENT'>('ALL');
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState('');
  const shown = channels.filter(channel => mode === 'ALL' || channel.type === mode);

  async function toggle(channel: SupportChannel) {
    if (pending) return;
    setPending(channel.id); setError('');
    try {
      if (channel.joined) {
        await apiRequest(`/api/v1/support-channels/${channel.id}/membership`, { method: 'DELETE' });
        setChannels(items => items.map(item => item.id === channel.id ? { ...item, joined: false, memberCount: Math.max(0, item.memberCount - 1) } : item));
      } else {
        const updated = await apiRequest<SupportChannel>(`/api/v1/support-channels/${channel.id}/membership`, { method: 'PUT' });
        setChannels(items => items.map(item => item.id === channel.id ? updated : item));
      }
      await refresh();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not update channel membership');
    } finally {
      setPending(null);
    }
  }

  return <SafeAreaView style={styles.safe}><ScrollView contentContainerStyle={styles.content}>
    <View style={styles.header}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><View style={{ flex: 1 }}><Text style={styles.title}>Groups & channels</Text><Text style={styles.subtitle}>Live support, guided conversations, and trusted updates</Text></View></View>
    <View style={styles.safety}><AppIcon name="verified_user" color={colors.ocean600} /><View style={{ flex: 1 }}><Text style={styles.safetyTitle}>Moderated support spaces</Text><Text style={styles.safetyText}>Group chats are monitored and follow GreenOcean’s peer-support guidelines.</Text></View></View>
    <View style={styles.filters}>{([['ALL', 'All'], ['GROUP', 'Live groups'], ['ANNOUNCEMENT', 'Channels']] as const).map(([id, label]) => <Pressable key={id} onPress={() => setMode(id)} style={[styles.filter, mode === id && styles.filterActive]}><Text style={[styles.filterText, mode === id && styles.filterTextActive]}>{label}</Text></Pressable>)}</View>
    {!!error && <Text style={styles.error}>{error}</Text>}
    {shown.map(channel => <View key={channel.id} style={styles.card}>
      <View style={styles.cardTop}><View style={[styles.icon, { backgroundColor: channel.type === 'GROUP' ? colors.ocean100 : colors.skySoft }]}><AppIcon name={channel.icon} color={channel.type === 'GROUP' ? colors.ocean700 : colors.sky} /></View><View style={{ flex: 1 }}><View style={styles.nameRow}><Text style={styles.name}>{channel.name}</Text>{channel.moderated && <AppIcon name="verified" filled size={16} color={colors.ocean500} />}</View><Text style={styles.category}>{channel.category} · {channel.type === 'GROUP' ? 'Group chat' : 'Announcement channel'}</Text></View></View>
      <Text style={styles.description}>{channel.description}</Text>
      <View style={styles.stats}><View style={styles.stat}><AppIcon name="group" size={17} color={colors.muted} /><Text style={styles.statText}>{channel.memberCount.toLocaleString()} members</Text></View>{channel.onlineCount > 0 && <View style={styles.stat}><View style={styles.onlineDot} /><Text style={styles.statText}>{channel.onlineCount} online</Text></View>}</View>
      {channel.nextEvent && <View style={styles.event}><AppIcon name="event" size={18} color={colors.lavender} /><Text style={styles.eventText}>{channel.nextEvent}</Text></View>}
      <View style={styles.actions}><Pressable disabled={pending === channel.id} onPress={() => void toggle(channel)} style={[styles.join, channel.joined && styles.joined]}><Text style={[styles.joinText, channel.joined && styles.joinedText]}>{pending === channel.id ? 'Saving…' : channel.joined ? 'Joined' : 'Join'}</Text></Pressable>{channel.joined && channel.conversationId && <Pressable onPress={() => router.push({ pathname: '/chat/[id]', params: { id: channel.conversationId! } })} style={styles.open}><Text style={styles.openText}>{channel.type === 'GROUP' ? 'Open chat' : 'Read updates'}</Text><AppIcon name="arrow_forward" size={17} color={colors.white} /></Pressable>}</View>
    </View>)}
  </ScrollView></SafeAreaView>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.foam }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, paddingBottom: spacing.xxxl, gap: spacing.md },
  header: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, title: { color: colors.ink, fontSize: typography.h1, fontWeight: '900' }, subtitle: { color: colors.muted, fontSize: 10, marginTop: 3 },
  safety: { flexDirection: 'row', gap: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.md, padding: spacing.md }, safetyTitle: { color: colors.ink, fontWeight: '900', fontSize: 11 }, safetyText: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: 3 },
  filters: { flexDirection: 'row', gap: spacing.sm }, filter: { borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, paddingHorizontal: spacing.md, paddingVertical: 8 }, filterActive: { backgroundColor: colors.ocean900, borderColor: colors.ocean900 }, filterText: { color: colors.muted, fontSize: 10, fontWeight: '800' }, filterTextActive: { color: colors.white }, error: { color: colors.danger, fontSize: 11 },
  card: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.sm, ...shadow.soft }, cardTop: { flexDirection: 'row', alignItems: 'center', gap: spacing.md }, icon: { width: 48, height: 48, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center' }, nameRow: { flexDirection: 'row', alignItems: 'center', gap: 5 }, name: { color: colors.ink, fontSize: 15, fontWeight: '900' }, category: { color: colors.ocean600, fontSize: 9, marginTop: 3 }, description: { color: colors.inkSoft, fontSize: 11, lineHeight: 17 }, stats: { flexDirection: 'row', gap: spacing.md }, stat: { flexDirection: 'row', alignItems: 'center', gap: 5 }, statText: { color: colors.muted, fontSize: 9 }, onlineDot: { width: 7, height: 7, borderRadius: 4, backgroundColor: colors.ocean500 }, event: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.lavenderSoft, borderRadius: radius.sm, padding: spacing.sm }, eventText: { color: colors.lavender, fontSize: 9, fontWeight: '800' },
  actions: { flexDirection: 'row', gap: spacing.sm, marginTop: spacing.xs }, join: { minHeight: 40, flex: 1, alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: colors.ocean400, borderRadius: radius.pill }, joined: { backgroundColor: colors.ocean50 }, joinText: { color: colors.ocean700, fontSize: 10, fontWeight: '900' }, joinedText: { color: colors.ocean600 }, open: { minHeight: 40, flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 5, backgroundColor: colors.ocean600, borderRadius: radius.pill }, openText: { color: colors.white, fontSize: 10, fontWeight: '900' },
});
