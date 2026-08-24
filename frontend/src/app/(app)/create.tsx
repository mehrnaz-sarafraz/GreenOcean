import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Switch, Text, TextInput, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { Screen } from '@/components/screen';
import { PostItem, PostVisibility } from '@/features/content/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function CreatePostScreen() {
  const { communityId, communityName } = useLocalSearchParams<{ communityId?: string; communityName?: string }>();
  const isCommunityPost = typeof communityId === 'string';
  const { t, isRtl } = useLanguage(); const [body, setBody] = useState(''); const [warning, setWarning] = useState('');
  const [visibility, setVisibility] = useState<PostVisibility>(isCommunityPost ? 'COMMUNITY' : 'PUBLIC'); const [anonymous, setAnonymous] = useState(false);
  const [loading, setLoading] = useState(false); const [error, setError] = useState('');
  async function publish() {
    if (!body.trim()) return setError(t('requiredFields')); setLoading(true); setError('');
    try {
      const post = await apiRequest<PostItem>('/api/v1/posts', { method: 'POST', body: JSON.stringify({ body, anonymous, visibility: isCommunityPost ? 'COMMUNITY' : visibility, communityId: isCommunityPost ? communityId : null, contentWarning: warning || null }) });
      setBody(''); setWarning(''); setAnonymous(false); router.push({ pathname: '/(app)/post/[id]', params: { id: post.id } });
    } catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }
  return (
    <Screen scroll style={styles.content}>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('createPost')}</Text>
      {isCommunityPost && <Text style={[styles.communityName, { textAlign: isRtl ? 'right' : 'left' }]}>{communityName}</Text>}
      <TextInput multiline value={body} onChangeText={setBody} placeholder={t('sharePrompt')} placeholderTextColor={colors.muted}
        style={[styles.editor, { textAlign: isRtl ? 'right' : 'left' }]} maxLength={10000} />
      <AppField label={t('contentWarning')} value={warning} onChangeText={setWarning} maxLength={120} />
      {!isCommunityPost && <View style={[styles.choices, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}> 
        {(['PUBLIC', 'FOLLOWERS'] as PostVisibility[]).map((item) => <Pressable key={item} onPress={() => setVisibility(item)} style={[styles.choice, visibility === item && styles.choiceActive]}><Text style={visibility === item ? styles.choiceTextActive : styles.choiceText}>{item === 'PUBLIC' ? t('public') : t('followers')}</Text></Pressable>)}
      </View>}
      <View style={[styles.switchRow, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}><Text style={styles.switchLabel}>{t('postAnonymously')}</Text><Switch value={anonymous} onValueChange={setAnonymous} trackColor={{ true: colors.ocean500 }} /></View>
      {!!error && <Text style={styles.error}>{error}</Text>}
      <AppButton label={t('publish')} loading={loading} onPress={publish} />
    </Screen>
  );
}
const styles = StyleSheet.create({ content: { gap: spacing.lg }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '800', marginTop: spacing.md }, communityName: { color: colors.ocean600, fontWeight: '700' }, editor: { minHeight: 190, textAlignVertical: 'top', backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.lg, color: colors.ink, fontSize: 18, lineHeight: 28 }, choices: { gap: spacing.sm }, choice: { paddingHorizontal: spacing.md, paddingVertical: spacing.sm, borderRadius: radius.pill, borderWidth: 1, borderColor: colors.border }, choiceActive: { backgroundColor: colors.ocean600, borderColor: colors.ocean600 }, choiceText: { color: colors.muted }, choiceTextActive: { color: colors.white, fontWeight: '700' }, switchRow: { alignItems: 'center', justifyContent: 'space-between' }, switchLabel: { color: colors.ink, fontWeight: '600' }, error: { color: colors.danger } });
