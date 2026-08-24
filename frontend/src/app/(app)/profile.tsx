import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { OwnProfile } from '@/features/profile/types';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function ProfileScreen() {
  const { signOut } = useAuth(); const { t, isRtl } = useLanguage();
  const [profile, setProfile] = useState<OwnProfile | null>(null); const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    apiRequest<OwnProfile>('/api/v1/profiles/me')
      .then((result) => { if (active) { setProfile(result); setError(''); } })
      .catch((exception) => { if (active) setError(exception instanceof Error ? exception.message : t('genericError')); });
    return () => { active = false; };
  }, [t]);

  async function loadProfile() {
    try {
      setProfile(await apiRequest<OwnProfile>('/api/v1/profiles/me'));
      setError('');
    }
    catch (exception) { setError(exception instanceof Error ? exception.message : t('genericError')); }
  }
  async function logout() { await signOut(); router.replace('/(auth)/welcome'); }

  return (
    <Screen>
      <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('profile')}</Text>
      {!profile && !error && <ActivityIndicator style={styles.loader} color={colors.ocean500} />}
      {!!error && <View style={styles.card}><Text style={styles.error}>{error}</Text><AppButton label={t('retry')} onPress={loadProfile} /></View>}
      {profile && (
        <View style={styles.card}>
          <View style={styles.avatar}><Text style={styles.avatarText}>{profile.displayName.slice(0, 1).toUpperCase()}</Text></View>
          <Text style={[styles.name, { textAlign: isRtl ? 'right' : 'left' }]}>{profile.displayName}</Text>
          <Text style={[styles.username, { textAlign: isRtl ? 'right' : 'left' }]}>@{profile.username}</Text>
          {!!profile.bio && <Text style={[styles.bio, { textAlign: isRtl ? 'right' : 'left' }]}>{profile.bio}</Text>}
          <AppButton label={t('logout')} variant="secondary" onPress={logout} />
        </View>
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  title: { marginTop: spacing.md, color: colors.ocean900, fontSize: 34, fontWeight: '800' }, loader: { marginTop: spacing.xxl },
  card: { marginTop: spacing.xl, padding: spacing.xl, borderRadius: radius.lg, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, gap: spacing.md },
  avatar: { width: 72, height: 72, borderRadius: radius.pill, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: colors.ocean700, fontWeight: '800', fontSize: 28 }, name: { color: colors.ink, fontSize: 24, fontWeight: '800' },
  username: { color: colors.ocean600, fontWeight: '600' }, bio: { color: colors.ink, lineHeight: 24 }, error: { color: colors.danger },
});
