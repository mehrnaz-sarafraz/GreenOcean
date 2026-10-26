import { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';

import { colors, radius, spacing } from '@/theme/tokens';

type Props = { label: string; onPress: () => void; loading?: boolean; disabled?: boolean; selected?: boolean; variant?: 'primary' | 'secondary' };

export function AppButton({ label, onPress, loading = false, disabled = false, selected, variant = 'primary' }: Props) {
  const [focused,setFocused]=useState(false);
  return (
    <Pressable
      accessibilityRole="button"
      disabled={loading || disabled}
      onPress={onPress}
      onFocus={()=>setFocused(true)}
      onBlur={()=>setFocused(false)}
      accessibilityState={{disabled:loading||disabled,busy:loading,selected}}
      style={({ pressed }) => [styles.base, styles[variant], focused && styles.focused, pressed && styles.pressed, (loading || disabled) && styles.disabled]}>
      {loading ? <ActivityIndicator color={variant === 'primary' ? colors.white : colors.ocean700} /> : (
        <Text style={[styles.label, focused && styles.focusedLabel, variant === 'secondary' && styles.secondaryLabel]}>{label}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: { minHeight: 54, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center', paddingHorizontal: spacing.lg },
  primary: { backgroundColor: colors.ocean600 },
  secondary: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.ocean300 },
  focused: { borderWidth: 3, borderColor: colors.ocean950, backgroundColor: colors.sunSoft },
  focusedLabel: { color: colors.ocean950 },
  pressed: { opacity: 0.84 },
  disabled: { opacity: 0.6 },
  label: { color: colors.white, fontSize: 16, fontWeight: '700' },
  secondaryLabel: { color: colors.ocean700 },
});
