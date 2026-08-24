import { StyleSheet, Text, View } from 'react-native';

import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function HomeScreen() {
  const { user } = useAuth(); const { t, isRtl } = useLanguage();
  return (
    <Screen>
      <View style={styles.header}>
        <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('homeTitle')}</Text>
        <Text style={[styles.email, { textAlign: isRtl ? 'right' : 'left' }]}>{user?.email}</Text>
      </View>
      <View style={styles.emptyState}>
        <View style={styles.wave} />
        <Text style={[styles.message, { textAlign: isRtl ? 'right' : 'left' }]}>{t('homeMessage')}</Text>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  header: { gap: spacing.sm, marginTop: spacing.md }, title: { fontSize: 34, fontWeight: '800', color: colors.ocean900 }, email: { color: colors.muted },
  emptyState: { marginTop: spacing.xxl, borderRadius: radius.lg, padding: spacing.xl, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, gap: spacing.lg },
  wave: { width: 64, height: 12, borderRadius: radius.pill, backgroundColor: colors.ocean300 }, message: { color: colors.ink, fontSize: 18, lineHeight: 28 },
});
