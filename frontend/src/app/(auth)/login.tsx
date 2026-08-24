import { router } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, spacing } from '@/theme/tokens';

export default function LoginScreen() {
  const { signIn } = useAuth(); const { t, isRtl } = useLanguage();
  const [email, setEmail] = useState(''); const [password, setPassword] = useState('');
  const [error, setError] = useState(''); const [loading, setLoading] = useState(false);
  async function submit() {
    if (!email.trim() || !password) return setError(t('requiredFields'));
    setLoading(true); setError('');
    try { await signIn({ email: email.trim(), password, deviceName: 'GreenOcean Expo app' }); router.replace('/(app)'); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }
  return (
    <Screen scroll style={styles.content}>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('loginTitle')}</Text>
      <View style={styles.form}>
        <AppField label={t('email')} value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" />
        <AppField label={t('password')} value={password} onChangeText={setPassword} secureTextEntry />
        {!!error && <Text style={[styles.error, { textAlign: isRtl ? 'right' : 'left' }]}>{error}</Text>}
        <AppButton label={t('signIn')} loading={loading} onPress={submit} />
        <AppButton label={t('createAccount')} variant="secondary" onPress={() => router.push('/(auth)/register')} />
      </View>
    </Screen>
  );
}
const styles = StyleSheet.create({ content: { justifyContent: 'center', gap: spacing.xl }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '800' }, form: { gap: spacing.md }, error: { color: colors.danger } });
