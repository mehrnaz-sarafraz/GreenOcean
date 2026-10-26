import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';
import { Ref } from 'react';

import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

type Props = TextInputProps & { label: string; error?: string; ltr?: boolean; ref?: Ref<TextInput> };

export function AppField({ label, error, ltr, ref, ...inputProps }: Props) {
  const { isRtl } = useLanguage();
  return (
    <View style={styles.container}>
      <Text style={[styles.label, { textAlign: isRtl ? 'right' : 'left' }]}>{label}</Text>
      <TextInput
        ref={ref}
        accessibilityLabel={label}
        accessibilityHint={error}
        {...inputProps}
        placeholderTextColor={colors.muted}
        style={[styles.input, { textAlign: ltr ? 'left' : isRtl ? 'right' : 'left', writingDirection: ltr ? 'ltr' : 'auto' }, error ? { borderColor: colors.danger } : undefined, inputProps.style]}
      />
      {!!error && <Text accessibilityRole="alert" style={{ color: colors.danger }}>{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { gap: spacing.sm },
  label: { color: colors.ink, fontWeight: '600' },
  input: {
    minHeight: 52,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.md,
    backgroundColor: colors.white,
    color: colors.ink,
    paddingHorizontal: spacing.md,
    fontSize: 16,
  },
});
