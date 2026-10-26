import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useFonts } from '@expo-google-fonts/material-symbols/useFonts';
import { MaterialSymbols_400Regular } from '@expo-google-fonts/material-symbols/400Regular';
import { MaterialSymbols_600SemiBold } from '@expo-google-fonts/material-symbols/600SemiBold';

import { AuthProvider, useAuth } from '@/features/auth/auth-provider';
import { DataProvider } from '@/features/platform/data-provider';
import { LanguageProvider } from '@/localization/language-provider';
import { colors } from '@/theme/tokens';

function AccountData({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  return <DataProvider key={user?.userId ?? 'visitor'}>{children}</DataProvider>;
}

export default function RootLayout() {
  useFonts({ MaterialSymbols_400Regular, MaterialSymbols_600SemiBold });
  // Public urgent information remains usable if decorative icon fonts fail to load.
  return (
    <LanguageProvider>
      <AuthProvider>
        <AccountData>
          <StatusBar style="dark" />
          <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.foam } }} />
        </AccountData>
      </AuthProvider>
    </LanguageProvider>
  );
}
