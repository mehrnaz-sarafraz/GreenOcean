import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';

import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

type Props = TextInputProps & { label: string };

export function AppField({ label, ...inputProps }: Props) {
  const { isRtl } = useLanguage();
  return (
    <View style={styles.container}>
      <Text style={[styles.label, { textAlign: isRtl ? 'right' : 'left' }]}>{label}</Text>
      <TextInput
        {...inputProps}
        placeholderTextColor={colors.muted}
        style={[styles.input, { textAlign: isRtl ? 'right' : 'left' }, inputProps.style]}
      />
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
