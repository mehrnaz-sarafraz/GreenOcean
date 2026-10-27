import { router, Redirect } from 'expo-router';
import { useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { PublicPage, PublicText } from '@/features/public/public-page';
import { publicCopy } from '@/localization/public-copy';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, spacing } from '@/theme/tokens';

export default function LoginScreen() {
  const { signIn, status } = useAuth(); const { t, isRtl, language } = useLanguage();
  const submitting=useRef(false);
  const [email, setEmail] = useState(''); const [password, setPassword] = useState('');
  const [error, setError] = useState(''); const [loading, setLoading] = useState(false);
  async function submit() {
    if(submitting.current)return;
    if (!email.trim() || !password) return setError(t('requiredFields'));
    submitting.current=true;setLoading(true); setError('');
    try { await signIn({ email: email.trim(), password, deviceName: 'GreenOcean app' });setPassword(''); router.replace('/(app)'); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { submitting.current=false;setLoading(false); }
  }
  if(status==='authenticated')return <Redirect href="/(app)"/>;
  if(status==='loading')return <PublicPage><PublicText>{publicCopy[language].checking}</PublicText></PublicPage>;
  return (
    <Screen scroll style={styles.content}>
      <Pressable accessibilityRole="button" accessibilityLabel={publicCopy[language].back} style={{minWidth:48,minHeight:48,justifyContent:'center'}} onPress={() => router.canGoBack()?router.back():router.replace('/(auth)/welcome')}><Text style={styles.back}>←</Text></Pressable>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('loginTitle')}</Text>
      <Text style={[styles.subtitle, { textAlign: isRtl ? 'right' : 'left' }]}>{t('loginSubtitle')}</Text>
      <View style={styles.form}>
        <AppField label={t('email')} value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" autoComplete="username" autoCorrect={false} style={{writingDirection:'ltr',textAlign:'left'}} />
        <AppField label={t('password')} value={password} onChangeText={setPassword} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr />
        <Pressable accessibilityRole="button" accessibilityLabel={publicCopy[language].forgot} style={{minHeight:48,justifyContent:'center'}} onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'forgot-password' } } as never)}><Text style={styles.forgot}>{publicCopy[language].forgot}</Text></Pressable>
        {!!error && <Text accessibilityRole="alert" style={[styles.error, { textAlign: isRtl ? 'right' : 'left' }]}>{error}</Text>}
        <AppButton label={t('signIn')} loading={loading} onPress={submit} />
        <AppButton label={t('createAccount')} variant="secondary" onPress={() => router.push('/(auth)/register')} />
      </View>
    </Screen>
  );
}
const styles = StyleSheet.create({ content: { justifyContent: 'center', gap: spacing.md, maxWidth: 560, width: '100%', alignSelf: 'center' }, back: { color: colors.ocean700, fontSize: 25, fontWeight: '800' }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '900', marginTop: spacing.lg }, subtitle: { color: colors.muted, fontSize: 15, marginBottom: spacing.md }, form: { gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: 24, padding: spacing.lg }, forgot: { color: colors.ocean700, fontSize: 11, fontWeight: '800', textAlign: 'right' }, error: { color: colors.danger } });
