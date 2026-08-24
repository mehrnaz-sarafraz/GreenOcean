import { router } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

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
    try { await register({ ...form, birthYear: Number(form.birthYear), countryCode: form.countryCode.toUpperCase() }); router.replace('/(app)'); }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
    finally { setLoading(false); }
  }
  return (
    <Screen scroll style={styles.content}>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('registerTitle')}</Text>
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
const styles = StyleSheet.create({ content: { gap: spacing.xl, paddingTop: spacing.xxl }, title: { color: colors.ocean900, fontSize: 34, fontWeight: '800' }, form: { gap: spacing.md }, error: { color: colors.danger } });
