import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useFonts } from '@expo-google-fonts/material-symbols/useFonts';
import { MaterialSymbols_400Regular } from '@expo-google-fonts/material-symbols/400Regular';
import { MaterialSymbols_600SemiBold } from '@expo-google-fonts/material-symbols/600SemiBold';

import { AuthProvider } from '@/features/auth/auth-provider';
import { DataProvider } from '@/features/platform/data-provider';
import { LanguageProvider } from '@/localization/language-provider';
import { colors } from '@/theme/tokens';

export default function RootLayout() {
  const [fontsLoaded] = useFonts({ MaterialSymbols_400Regular, MaterialSymbols_600SemiBold });
  if (!fontsLoaded) return null;
  return (
    <LanguageProvider>
      <AuthProvider>
        <DataProvider>
          <StatusBar style="dark" />
          <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.foam } }} />
        </DataProvider>
      </AuthProvider>
    </LanguageProvider>
  );
}
