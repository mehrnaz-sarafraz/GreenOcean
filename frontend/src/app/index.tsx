import { Redirect } from 'expo-router';
import { ActivityIndicator, StyleSheet, View } from 'react-native';

import { useAuth } from '@/features/auth/auth-provider';
import { colors } from '@/theme/tokens';

export default function Index() {
  const { status } = useAuth();
  if (status === 'loading') {
    return <View style={styles.loading}><ActivityIndicator color={colors.ocean500} size="large" /></View>;
  }
  return <Redirect href={status === 'authenticated' ? '/(app)' : '/(auth)/welcome'} />;
}

const styles = StyleSheet.create({ loading: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.foam } });
