import { Redirect, Tabs } from 'expo-router';

import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors } from '@/theme/tokens';

export default function AppLayout() {
  const { status } = useAuth(); const { t } = useLanguage();
  if (status === 'unauthenticated') return <Redirect href="/(auth)/welcome" />;
  return (
    <Tabs screenOptions={{ headerShown: false, tabBarActiveTintColor: colors.ocean600, tabBarInactiveTintColor: colors.muted }}>
      <Tabs.Screen name="index" options={{ title: t('homeTitle') }} />
      <Tabs.Screen name="profile" options={{ title: t('profile') }} />
    </Tabs>
  );
}
