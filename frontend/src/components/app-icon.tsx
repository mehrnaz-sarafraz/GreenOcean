import { Text, TextStyle } from 'react-native';
type Props = { name: string; size?: number; color?: string; filled?: boolean; style?: TextStyle };
export function AppIcon({ name, size = 24, color = '#152522', filled = false, style }: Props) {
  return <Text accessibilityElementsHidden style={[{ fontFamily: filled ? 'MaterialSymbols_600SemiBold' : 'MaterialSymbols_400Regular', fontSize: size, lineHeight: size + 2, color }, style]}>{name}</Text>;
}
