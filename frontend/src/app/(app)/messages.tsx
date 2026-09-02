import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { usePlatformData } from '@/features/platform/data-provider';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

function timeLabel(value: string | null) {
  if (!value) return '';
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export default function Messages() {
  const { channels, conversations, loading } = usePlatformData();
  const [query, setQuery] = useState('');
  const shown = useMemo(() => {
    const term = query.trim().toLowerCase();
    return conversations.filter(item => !term || `${item.name} ${item.subtitle ?? ''} ${item.lastMessage ?? ''}`.toLowerCase().includes(term));
  }, [conversations, query]);
  const unread = conversations.reduce((sum, item) => sum + item.unread, 0);

  return <SafeAreaView style={styles.safe}><ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
    <View style={styles.header}><View><Text style={styles.title}>Messages</Text><Text style={styles.subtitle}>Private conversations and support spaces</Text></View></View>
    <View style={styles.search}><AppIcon name="search" color={colors.muted} /><TextInput value={query} onChangeText={setQuery} placeholder="Search conversations" placeholderTextColor={colors.muted} style={styles.input} /></View>
    <View style={styles.quickRow}>
      <Pressable onPress={() => router.push('/channels')} style={styles.quick}><View style={[styles.quickIcon, { backgroundColor: colors.ocean100 }]}><AppIcon name="groups" color={colors.ocean700} /></View><View style={{ flex: 1 }}><Text style={styles.quickTitle}>Groups & channels</Text><Text style={styles.quickText}>{channels.length} support spaces available</Text></View><AppIcon name="chevron_right" color={colors.muted} /></Pressable>
      <Pressable onPress={() => router.push('/support')} style={styles.quick}><View style={[styles.quickIcon, { backgroundColor: colors.skySoft }]}><AppIcon name="support_agent" color={colors.sky} /></View><View style={{ flex: 1 }}><Text style={styles.quickTitle}>Find a listener</Text><Text style={styles.quickText}>Connect with a trained peer listener</Text></View><AppIcon name="chevron_right" color={colors.muted} /></Pressable>
    </View>
    <View style={styles.sectionHead}><Text style={styles.sectionTitle}>Recent conversations</Text>{unread > 0 && <Text style={styles.request}>{unread} unread</Text>}</View>
    {shown.map(conversation => <Pressable key={conversation.id} onPress={() => router.push({ pathname: '/chat/[id]', params: { id: conversation.id } })} style={styles.row}>
      <View><Avatar name={conversation.name} size={52} verified={conversation.verified} />{conversation.online && <View style={styles.online} />}</View>
      <View style={{ flex: 1 }}><View style={styles.nameRow}><Text style={styles.name}>{conversation.name}</Text><Text style={styles.time}>{timeLabel(conversation.lastMessageAt)}</Text></View><Text style={styles.kind}>{conversation.subtitle}</Text><Text style={[styles.last, conversation.unread > 0 && styles.lastUnread]} numberOfLines={1}>{conversation.lastMessage ?? 'No messages yet'}</Text></View>
      {conversation.unread > 0 && <View style={styles.unread}><Text style={styles.unreadText}>{conversation.unread}</Text></View>}
    </Pressable>)}
    {!loading && shown.length === 0 && <View style={styles.empty}><AppIcon name="forum" size={34} color={colors.ocean500} /><Text style={styles.emptyTitle}>No conversations yet</Text><Text style={styles.emptyText}>Join a support group, find a listener, or message a verified professional.</Text></View>}
  </ScrollView></SafeAreaView>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.foam }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, paddingBottom: spacing.xxxl, gap: spacing.md },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, title: { fontSize: typography.h1, fontWeight: '900', color: colors.ink }, subtitle: { fontSize: 12, color: colors.muted, marginTop: 3 },
  search: { flexDirection: 'row', height: 50, alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, paddingHorizontal: spacing.md }, input: { flex: 1, color: colors.ink, textAlign: 'left' },
  quickRow: { gap: spacing.sm }, quick: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, ...shadow.soft }, quickIcon: { width: 42, height: 42, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center' }, quickTitle: { color: colors.ink, fontWeight: '900' }, quickText: { fontSize: 11, color: colors.muted, marginTop: 3 },
  sectionHead: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: spacing.sm }, sectionTitle: { fontSize: typography.h3, fontWeight: '900', color: colors.ink }, request: { fontSize: 11, color: colors.ocean600, fontWeight: '800' },
  row: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md }, online: { position: 'absolute', right: 0, bottom: 0, width: 13, height: 13, borderRadius: 7, backgroundColor: colors.ocean500, borderWidth: 2, borderColor: colors.white }, nameRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, name: { fontWeight: '900', color: colors.ink }, time: { fontSize: 10, color: colors.muted }, kind: { fontSize: 10, color: colors.ocean600, marginTop: 2 }, last: { fontSize: 12, color: colors.muted, marginTop: 5 }, lastUnread: { color: colors.inkSoft, fontWeight: '700' }, unread: { minWidth: 20, height: 20, borderRadius: 10, backgroundColor: colors.ocean600, alignItems: 'center', justifyContent: 'center' }, unreadText: { fontSize: 10, color: colors.white, fontWeight: '900' },
  empty: { alignItems: 'center', gap: spacing.sm, padding: spacing.xl, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border }, emptyTitle: { color: colors.ink, fontWeight: '900' }, emptyText: { color: colors.muted, fontSize: 11, textAlign: 'center', lineHeight: 17 },
});
