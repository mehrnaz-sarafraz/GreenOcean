import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';

import { colors, radius, spacing } from '@/theme/tokens';

type Props = { label: string; onPress: () => void; loading?: boolean; disabled?: boolean; variant?: 'primary' | 'secondary' };

export function AppButton({ label, onPress, loading = false, disabled = false, variant = 'primary' }: Props) {
  return (
    <Pressable
      accessibilityRole="button"
      disabled={loading || disabled}
      onPress={onPress}
      style={({ pressed }) => [styles.base, styles[variant], pressed && styles.pressed, (loading || disabled) && styles.disabled]}>
      {loading ? <ActivityIndicator color={variant === 'primary' ? colors.white : colors.ocean700} /> : (
        <Text style={[styles.label, variant === 'secondary' && styles.secondaryLabel]}>{label}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: { minHeight: 54, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center', paddingHorizontal: spacing.lg },
  primary: { backgroundColor: colors.ocean600 },
  secondary: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.ocean300 },
  pressed: { opacity: 0.84 },
  disabled: { opacity: 0.6 },
  label: { color: colors.white, fontSize: 16, fontWeight: '700' },
  secondaryLabel: { color: colors.ocean700 },
});
