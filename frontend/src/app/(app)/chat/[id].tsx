import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { usePlatformData } from '@/features/platform/data-provider';
import { ChatMessage } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, shadow, spacing } from '@/theme/tokens';

function timeLabel(value: string) {
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export default function Chat() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { conversations, setConversations } = usePlatformData();
  const conversation = conversations.find(item => item.id === id);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [text, setText] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    setLoading(true); setError('');
    apiRequest<ChatMessage[]>(`/api/v1/conversations/${id}/messages`)
      .then(result => {
        setMessages(result);
        setConversations(items => items.map(item => item.id === id ? { ...item, unread: 0 } : item));
      })
      .catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load this conversation'))
      .finally(() => setLoading(false));
  }, [id, setConversations]);

  async function send() {
    const body = text.trim();
    if (!id || !body || sending || conversation?.writable === false) return;
    setSending(true); setError('');
    try {
      const created = await apiRequest<ChatMessage>(`/api/v1/conversations/${id}/messages`, {
        method: 'POST',
        body: JSON.stringify({ body }),
      });
      setMessages(items => [...items, created]);
      setText('');
      setConversations(items => items.map(item => item.id === id ? { ...item, lastMessage: created.body, lastMessageAt: created.createdAt } : item));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not send the message');
    } finally {
      setSending(false);
    }
  }

  const title = conversation?.name ?? 'Conversation';
  return <SafeAreaView style={styles.safe}><View style={styles.page}>
    <View style={styles.header}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Avatar name={title} size={42} verified={conversation?.verified} /><View style={{ flex: 1 }}><Text style={styles.name}>{title}</Text><Text style={styles.status}>{conversation?.online ? 'Online · ' : ''}{conversation?.subtitle ?? 'Private support conversation'}</Text></View></View>
    <View style={styles.safety}><AppIcon name="shield" size={17} color={colors.ocean600} /><Text style={styles.safetyText}>Keep personal information private. You can block or report unsafe behaviour from a member profile.</Text></View>
    <ScrollView contentContainerStyle={styles.messages} showsVerticalScrollIndicator={false}>
      {loading && <Text style={styles.stateText}>Loading conversation…</Text>}
      {!!error && <Text style={styles.error}>{error}</Text>}
      {!loading && !error && messages.length === 0 && <Text style={styles.stateText}>No messages yet. Start with a kind hello.</Text>}
      {messages.map(message => message.system
        ? <View key={message.id} style={styles.system}><AppIcon name="info" size={16} color={colors.ocean600} /><Text style={styles.systemText}>{message.body}</Text></View>
        : <View key={message.id} style={[styles.messageRow, message.mine && styles.messageRowMine]}>{!message.mine && <Avatar name={message.senderName ?? 'Member'} size={30} verified={conversation?.verified} />}<View style={[styles.bubble, message.mine && styles.bubbleMine]}>{!message.mine && conversation?.kind === 'GROUP' && <Text style={styles.sender}>{message.senderName ?? 'Member'}</Text>}<Text style={[styles.body, message.mine && styles.bodyMine]}>{message.body}</Text><Text style={[styles.time, message.mine && styles.timeMine]}>{timeLabel(message.createdAt)}</Text></View></View>)}
    </ScrollView>
    {conversation?.writable === false
      ? <View style={styles.readOnly}><AppIcon name="campaign" color={colors.ocean600} /><Text style={styles.readOnlyText}>This is an announcement channel. Only moderators can publish here.</Text></View>
      : <View style={styles.composer}><TextInput value={text} onChangeText={setText} onSubmitEditing={() => void send()} editable={!sending} placeholder="Write a supportive message…" placeholderTextColor={colors.muted} style={styles.input} /><Pressable disabled={sending || !text.trim()} onPress={() => void send()} style={[styles.send, (sending || !text.trim()) && styles.sendDisabled]}><AppIcon name="arrow_upward" size={20} color={colors.white} /></Pressable></View>}
  </View></SafeAreaView>;
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.foam }, page: { flex: 1, width: '100%', maxWidth: layout.maxContent, alignSelf: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, backgroundColor: colors.white, borderBottomWidth: 1, borderBottomColor: colors.border }, name: { fontWeight: '900', color: colors.ink }, status: { fontSize: 10, color: colors.ocean600, marginTop: 2 },
  safety: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, margin: spacing.md, marginBottom: 0, padding: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.sm }, safetyText: { flex: 1, fontSize: 10, color: colors.inkSoft, lineHeight: 15 },
  messages: { flexGrow: 1, padding: spacing.md, gap: spacing.sm, justifyContent: 'flex-end' }, stateText: { color: colors.muted, textAlign: 'center', padding: spacing.lg }, error: { color: colors.danger, textAlign: 'center', padding: spacing.md },
  messageRow: { flexDirection: 'row', alignItems: 'flex-end', gap: spacing.sm, maxWidth: 540 }, messageRowMine: { alignSelf: 'flex-end', justifyContent: 'flex-end' }, bubble: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, borderBottomLeftRadius: radius.xs, padding: spacing.md, gap: 4, ...shadow.soft }, bubbleMine: { backgroundColor: colors.ocean700, borderColor: colors.ocean700, borderBottomLeftRadius: radius.lg, borderBottomRightRadius: radius.xs }, sender: { fontSize: 10, color: colors.ocean600, fontWeight: '900' }, body: { fontSize: 14, color: colors.ink, lineHeight: 21, textAlign: 'left' }, bodyMine: { color: colors.white }, time: { fontSize: 9, color: colors.muted, textAlign: 'right' }, timeMine: { color: colors.ocean200 }, system: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, alignSelf: 'center', maxWidth: 580, backgroundColor: colors.ocean50, borderRadius: radius.md, padding: spacing.sm }, systemText: { flex: 1, fontSize: 10, color: colors.muted, lineHeight: 15, textAlign: 'center' },
  composer: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, backgroundColor: colors.white, borderTopWidth: 1, borderTopColor: colors.border }, input: { flex: 1, minHeight: 46, maxHeight: 110, borderRadius: radius.pill, backgroundColor: colors.foam, borderWidth: 1, borderColor: colors.border, paddingHorizontal: spacing.md, color: colors.ink, textAlign: 'left' }, send: { width: 42, height: 42, borderRadius: 21, backgroundColor: colors.ocean600, alignItems: 'center', justifyContent: 'center' }, sendDisabled: { opacity: 0.45 },
  readOnly: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, backgroundColor: colors.ocean50, borderTopWidth: 1, borderTopColor: colors.border }, readOnlyText: { flex: 1, color: colors.inkSoft, fontSize: 11, lineHeight: 16 },
});
