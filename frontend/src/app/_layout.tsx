import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';

import { AuthProvider } from '@/features/auth/auth-provider';
import { LanguageProvider } from '@/localization/language-provider';
import { colors } from '@/theme/tokens';

export default function RootLayout() {
  return (
    <LanguageProvider>
      <AuthProvider>
        <StatusBar style="light" />
        <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.foam } }} />
      </AuthProvider>
    </LanguageProvider>
  );
}
