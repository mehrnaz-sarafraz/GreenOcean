import { StyleSheet, Text, TextInput, TextInputProps, View, Pressable } from 'react-native';
import { Ref, useState } from 'react';

import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

type Props = TextInputProps & { label: string; error?: string; ltr?: boolean; ref?: Ref<TextInput>; visibilityLabels?: {show:string;hide:string} };

export function AppField({ label, error, ltr, ref, visibilityLabels, ...inputProps }: Props) {
  const [visible,setVisible]=useState(false);
  const { isRtl } = useLanguage();
  return (
    <View style={styles.container}>
      <Text style={[styles.label, { textAlign: isRtl ? 'right' : 'left' }]}>{label}</Text>
      <TextInput
        ref={ref}
        accessibilityLabel={label}
        accessibilityHint={error}
        aria-invalid={!!error}
        {...inputProps}
        secureTextEntry={inputProps.secureTextEntry&&!visible}
        onBlur={event=>{setVisible(false);inputProps.onBlur?.(event);}}
        placeholderTextColor={colors.muted}
        style={[styles.input, { textAlign: ltr ? 'left' : isRtl ? 'right' : 'left', writingDirection: ltr ? 'ltr' : 'auto' }, error ? { borderColor: colors.danger } : undefined, inputProps.style]}
      />
      {inputProps.secureTextEntry&&<Pressable accessibilityRole="button" accessibilityLabel={visibilityLabels?(visible?visibilityLabels.hide:visibilityLabels.show):isRtl?(visible?'پنهان کردن '+label:'نمایش '+label):(visible?'Hide ':'Show ')+label} accessibilityState={{expanded:visible}} onPress={()=>setVisible(!visible)} onBlur={()=>setVisible(false)} style={{minHeight:48,justifyContent:'center'}}><Text style={{color:colors.ocean700}}>{isRtl?(visible?'پنهان کردن رمز':'نمایش رمز'):(visible?'Hide password':'Show password')}</Text></Pressable>}
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
