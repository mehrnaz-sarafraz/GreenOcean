import { Image, StyleSheet, Text, View } from 'react-native';
import { colors, radius } from '@/theme/tokens';
export function Avatar({ name, uri, size = 44, verified = false }: { name: string; uri?: string | null; size?: number; verified?: boolean }) {
  return <View style={{ width: size, height: size }}>
    {uri ? <Image source={{ uri }} style={{ width: size, height: size, borderRadius: radius.pill }} /> : <View style={[styles.fallback, { width: size, height: size }]}><Text style={[styles.letter, { fontSize: size * .36 }]}>{name.slice(0, 1).toUpperCase()}</Text></View>}
    {verified && <View style={styles.check}><Text style={styles.checkText}>✓</Text></View>}
  </View>;
}
const styles = StyleSheet.create({ fallback: { borderRadius: radius.pill, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: colors.ocean200 }, letter: { color: colors.ocean700, fontWeight: '800' }, check: { position: 'absolute', right: -2, bottom: -2, width: 17, height: 17, borderRadius: 9, backgroundColor: colors.ocean500, borderWidth: 2, borderColor: colors.white, alignItems: 'center', justifyContent: 'center' }, checkText: { color: colors.white, fontSize: 9, fontWeight: '900' } });
