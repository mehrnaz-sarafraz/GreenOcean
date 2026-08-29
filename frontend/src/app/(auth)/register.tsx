import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, spacing } from '@/theme/tokens';

export default function RegisterScreen() {
  const { register } = useAuth(); const { t, isRtl } = useLanguage();
  const [form, setForm] = useState({ email: '', password: '', username: '', displayName: '', birthYear: '', countryCode: '', city: '' });
  const [error, setError] = useState(''); const [loading, setLoading] = useState(false);
  const setField = (field: keyof typeof form) => (value: string) => setForm((current) => ({ ...current, [field]: value }));
  async function submit() {
    if (Object.values(form).some((value) => !value.trim())) return setError(t('requiredFields'));
    setLoading(true); setError('');
    try { await register({ ...form, birthYear: Number(form.birthYear), countryCode: form.countryCode.toUpperCase() }); router.replace('/(auth)/onboarding'); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }
  return (
    <Screen scroll style={styles.content}>
      <Pressable onPress={() => router.back()}><Text style={styles.back}>←</Text></Pressable>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('registerTitle')}</Text>
      <Text style={[styles.subtitle, { textAlign: isRtl ? 'right' : 'left' }]}>{t('registerSubtitle')}</Text>
      <View style={styles.form}>
        <AppField label={t('email')} value={form.email} onChangeText={setField('email')} autoCapitalize="none" keyboardType="email-address" />
        <AppField label={t('password')} value={form.password} onChangeText={setField('password')} secureTextEntry />
        <AppField label={t('username')} value={form.username} onChangeText={setField('username')} autoCapitalize="none" />
        <AppField label={t('displayName')} value={form.displayName} onChangeText={setField('displayName')} />
        <AppField label={t('birthYear')} value={form.birthYear} onChangeText={setField('birthYear')} keyboardType="number-pad" />
        <AppField label={t('countryCode')} value={form.countryCode} onChangeText={setField('countryCode')} autoCapitalize="characters" maxLength={2} />
        <AppField label={t('city')} value={form.city} onChangeText={setField('city')} />
        {!!error && <Text style={[styles.error, { textAlign: isRtl ? 'right' : 'left' }]}>{error}</Text>}
        <AppButton label={t('createAccount')} loading={loading} onPress={submit} />
        <AppButton label={t('signIn')} variant="secondary" onPress={() => router.push('/(auth)/login')} />
      </View>
    </Screen>
  );
}
const styles = StyleSheet.create({ content: { gap: spacing.md, paddingTop: spacing.xl, maxWidth: 560, width: '100%', alignSelf: 'center' }, back: { color: colors.ocean700, fontSize: 25, fontWeight: '800' }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '900', marginTop: spacing.md }, subtitle: { color: colors.muted, fontSize: 15, marginBottom: spacing.md }, form: { gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: 24, padding: spacing.lg }, error: { color: colors.danger } });
