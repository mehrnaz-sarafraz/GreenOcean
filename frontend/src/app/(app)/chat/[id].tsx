import { LiveChat } from '@/features/live-support/chat';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { EmptyState } from '@/components/product-ui';
import { usePlatformData } from '@/features/platform/data-provider';
import { ChatMessage } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, spacing } from '@/theme/tokens';

export default function ChatRouter() {
 const {id,liveRequestId}=useLocalSearchParams<{id:string;liveRequestId?:string}>();
 const {conversations}=usePlatformData();const conversation=conversations.find(item=>item.id===id);
 if(liveRequestId||conversation?.serviceKind==='LISTENER')return <LiveChat key={id} id={id}/>;
 return <Chat key={id}/>;
}
function Chat() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { conversations, setConversations } = usePlatformData();
  const conversation = conversations.find(item => item.id === id);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [text, setText] = useState('');
  const [menu, setMenu] = useState(false);
  const [loading, setLoading] = useState(true);
  const title = useMemo(() => conversation?.name || 'Conversation', [conversation?.name]);

  useEffect(() => {
    if (!id) return;
    const loadMessages = (showLoading: boolean) => {
      if (showLoading) setLoading(true);
      return apiRequest<ChatMessage[]>('/api/v1/conversations/' + id + '/messages').then(items => {
        setMessages(items);
        setConversations(current => current.map(item => item.id === id ? { ...item, unread: 0 } : item));
      })
        .catch(()=>{setMessages([]);})
        .finally(() => { if (showLoading) setLoading(false); });
    };
    const timer = setTimeout(() => void loadMessages(true), 0);
    const interval = setInterval(() => void loadMessages(false), 5000);
    return () => { clearTimeout(timer); clearInterval(interval); };
  }, [id, setConversations]);

  async function send() {
    const body = text.trim();
    if (!body || !id) return;
    setText('');
    try {
      const message = await apiRequest<ChatMessage>('/api/v1/conversations/' + id + '/messages', { method: 'POST', body: JSON.stringify({ body }) });
      setMessages(current => [...current, message]);
      setConversations(current => current.map(item => item.id === id ? { ...item, lastMessage: message.body, lastMessageAt: message.createdAt } : item));
    } catch {
      setText(body);
    }
  }

  if (!conversation && !loading) return <SafeAreaView style={styles.safe}><View style={styles.missing}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><EmptyState icon="chat_error" title="Conversation not found" body="It may have been removed, left, or the link is incomplete." /></View></SafeAreaView>;
  return <SafeAreaView style={styles.safe}><View style={styles.page}><View style={styles.header}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Avatar name={title} size={42} verified={conversation?.verified} /><View style={styles.flex}><Text style={styles.name}>{title}</Text><Text style={styles.status}>{conversation?.online ? 'Online · ' : ''}{conversation?.subtitle || conversation?.kind}</Text></View><Pressable onPress={() => setMenu(value => !value)}><AppIcon name="more_vert" color={colors.muted} /></Pressable></View>
    {menu && <View style={styles.menu}><Pressable onPress={() => router.push({ pathname: '/tools/[tool]' as never, params: { tool: 'report', targetType: 'conversation', targetId: id } } as never)} style={styles.menuItem}><AppIcon name="flag" color={colors.danger} /><Text style={styles.menuText}>Report conversation</Text></Pressable></View>}
    <View style={styles.safety}><AppIcon name="shield" size={17} color={colors.ocean600} /><Text style={styles.safetyText}>Keep personal information private. You can block or report any conversation.</Text></View>
    <ScrollView contentContainerStyle={styles.messages} showsVerticalScrollIndicator={false}>{messages.map(message => message.system ? <View key={message.id} style={styles.system}><AppIcon name="info" size={16} color={colors.ocean600} /><Text style={styles.systemText}>{message.body}</Text></View> : <View key={message.id} style={[styles.messageRow, message.mine && styles.messageRowMine]}>{!message.mine && <Avatar name={message.senderName || 'Member'} size={30} verified={conversation?.verified} />}<View style={[styles.bubble, message.mine && styles.bubbleMine]}>{!message.mine && conversation?.kind === 'GROUP' && <Text style={styles.sender}>{message.senderName || 'Member'}</Text>}<Text style={[styles.body, message.mine && styles.bodyMine]}>{message.body}</Text><Text style={[styles.time, message.mine && styles.timeMine]}>{new Date(message.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</Text></View></View>)}</ScrollView>
    <View style={styles.composer}><TextInput value={text} onChangeText={setText} onSubmitEditing={send} placeholder="Write a supportive message…" placeholderTextColor={colors.muted} style={styles.input} /><Pressable onPress={send} style={styles.send}><AppIcon name="arrow_upward" size={20} color={colors.white} /></Pressable></View>
  </View></SafeAreaView>;
}
const styles=StyleSheet.create({safe:{flex:1,backgroundColor:colors.foam},page:{flex:1,width:'100%',maxWidth:layout.maxContent,alignSelf:'center',backgroundColor:colors.white},missing:{width:'100%',maxWidth:layout.maxContent,alignSelf:'center',padding:spacing.md,gap:spacing.lg},flex:{flex:1},header:{height:68,flexDirection:'row',alignItems:'center',gap:spacing.sm,paddingHorizontal:spacing.md,borderBottomWidth:1,borderBottomColor:colors.border},name:{fontSize:14,fontWeight:'900',color:colors.ink},status:{fontSize:9,color:colors.muted,marginTop:2},menu:{position:'absolute',zIndex:5,right:spacing.md,top:62,width:220,backgroundColor:colors.white,borderWidth:1,borderColor:colors.border,borderRadius:radius.md,padding:spacing.sm},menuItem:{flexDirection:'row',alignItems:'center',gap:spacing.sm,padding:spacing.sm},menuText:{color:colors.ink,fontSize:10,fontWeight:'800'},safety:{flexDirection:'row',alignItems:'center',gap:spacing.sm,backgroundColor:colors.ocean50,padding:spacing.sm,paddingHorizontal:spacing.md},safetyText:{flex:1,fontSize:9,color:colors.muted,lineHeight:14},messages:{flexGrow:1,padding:spacing.md,gap:spacing.sm},messageRow:{flexDirection:'row',alignItems:'flex-end',gap:spacing.sm},messageRowMine:{justifyContent:'flex-end'},bubble:{maxWidth:'78%',backgroundColor:colors.foam,borderRadius:radius.lg,padding:spacing.md},bubbleMine:{backgroundColor:colors.ocean600},sender:{fontSize:9,color:colors.ocean600,fontWeight:'900',marginBottom:4},body:{fontSize:12,lineHeight:19,color:colors.ink},bodyMine:{color:colors.white},time:{fontSize:8,color:colors.muted,marginTop:5},timeMine:{color:colors.ocean100},system:{flexDirection:'row',alignItems:'center',gap:spacing.sm,backgroundColor:colors.ocean50,borderRadius:radius.md,padding:spacing.sm},systemText:{flex:1,fontSize:9,color:colors.muted,lineHeight:14},composer:{minHeight:66,flexDirection:'row',alignItems:'center',gap:spacing.sm,padding:spacing.sm,borderTopWidth:1,borderTopColor:colors.border},input:{flex:1,minHeight:44,borderRadius:radius.pill,backgroundColor:colors.foam,paddingHorizontal:spacing.md,color:colors.ink,textAlign:'left'},send:{width:42,height:42,borderRadius:21,backgroundColor:colors.ocean600,alignItems:'center',justifyContent:'center'}});
