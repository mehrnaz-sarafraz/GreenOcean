import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppButton } from '@/components/app-button';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing } from '@/theme/tokens';

export default function WelcomeScreen() {
  const { language, setLanguage, t, isRtl } = useLanguage();
  const values = [t('noJudgement'), t('realSupport'), t('verifiedProfessionals')];
  return (
    <LinearGradient colors={[colors.ocean950, colors.ocean800, colors.ocean600]} style={styles.root}>
      <View style={[styles.orb, styles.orbOne]} />
      <View style={[styles.orb, styles.orbTwo]} />
      <SafeAreaView style={styles.safeArea}>
        <View style={[styles.topBar, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
          <Text style={styles.brand}>GreenOcean</Text>
          <Pressable onPress={() => setLanguage(language === 'en' ? 'fa' : 'en')} style={styles.languageButton}>
            <Text style={styles.languageText}>{language === 'en' ? 'فارسی' : 'EN'}</Text>
          </Pressable>
        </View>
        <View style={styles.hero}>
          <Text style={[styles.title, { textAlign: isRtl ? 'right' : 'left' }]}>{t('welcomeTitle')}</Text>
          <View style={styles.values}>
            {values.map((value) => (
              <View key={value} style={[styles.valueRow, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
                <View style={styles.dot} />
                <Text style={styles.valueText}>{value}</Text>
              </View>
            ))}
          </View>
        </View>
        <View style={styles.actions}>
          <AppButton label={t('getStarted')} onPress={() => router.push('/(auth)/register')} />
          <AppButton label={t('signIn')} variant="secondary" onPress={() => router.push('/(auth)/login')} />
        </View>
      </SafeAreaView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 }, safeArea: { flex: 1, padding: spacing.lg },
  topBar: { alignItems: 'center', justifyContent: 'space-between' },
  brand: { color: colors.white, fontSize: 22, fontWeight: '800', letterSpacing: 0.4 },
  languageButton: { borderWidth: 1, borderColor: '#FFFFFF55', borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: spacing.sm },
  languageText: { color: colors.white, fontWeight: '700' },
  hero: { flex: 1, justifyContent: 'center', gap: spacing.xl },
  title: { color: colors.white, fontSize: 37, lineHeight: 48, fontWeight: '800', maxWidth: 620 },
  values: { gap: spacing.md }, valueRow: { alignItems: 'center', gap: spacing.md },
  dot: { width: 9, height: 9, borderRadius: radius.pill, backgroundColor: colors.ocean300 },
  valueText: { color: colors.ocean100, fontSize: 17, fontWeight: '600' }, actions: { gap: spacing.md },
  orb: { position: 'absolute', borderRadius: radius.pill, backgroundColor: '#73D5BE18' },
  orbOne: { width: 330, height: 330, top: -80, right: -120 }, orbTwo: { width: 240, height: 240, bottom: 100, left: -130 },
});
