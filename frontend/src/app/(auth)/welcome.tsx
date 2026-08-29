import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, spacing, typography } from '@/theme/tokens';

const values = [
  { icon: 'forum', title: 'Share without judgment', body: 'Tell your story anonymously and find people who truly understand.' },
  { icon: 'support_agent', title: 'Talk when you need to', body: 'Connect with a trained listener or join a moderated support circle.' },
  { icon: 'psychology', title: 'Learn from trusted experts', body: 'Read verified insights and discover professionals who fit your needs.' },
] as const;

export default function Welcome() {
  const { t } = useLanguage();

  return <LinearGradient colors={[colors.ocean950, colors.ocean800, colors.ocean600]} style={styles.root}>
    <View style={styles.orb1} /><View style={styles.orb2} />
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
        <View style={styles.top}>
          <View style={styles.logo}><AppIcon name="waves" color={colors.white} /><Text style={styles.brand}>GreenOcean</Text></View>
          <View style={styles.safetyBadge}><AppIcon name="health_and_safety" size={17} color={colors.ocean300} /><Text style={styles.safetyBadgeText}>Safety first</Text></View>
        </View>

        <View style={styles.hero}>
          <View style={styles.eyebrow}><AppIcon name="verified_user" size={18} color={colors.ocean300} /><Text style={styles.eyebrowText}>{t('noJudgement')} · {t('realSupport')}</Text></View>
          <Text style={styles.title}>{t('welcomeTitle')}</Text>
          <Text style={styles.subtitle}>{t('welcomeSubtitle')}</Text>

          <View style={styles.valueGrid}>
            {values.map(value => <View key={value.title} style={styles.valueCard}>
              <View style={styles.valueIcon}><AppIcon name={value.icon} size={21} color={colors.ocean800} /></View>
              <Text style={styles.valueTitle}>{value.title}</Text>
              <Text style={styles.valueBody}>{value.body}</Text>
            </View>)}
          </View>

          <View style={styles.people}>
            <View style={styles.face}><Text>J</Text></View>
            <View style={[styles.face, styles.faceOverlap, { backgroundColor: colors.sunSoft }]}><Text>S</Text></View>
            <View style={[styles.face, styles.faceOverlap, { backgroundColor: colors.coralSoft }]}><Text>A</Text></View>
            <Text style={styles.peopleText}>{t('verifiedProfessionals')}</Text>
          </View>
        </View>

        <View style={styles.actions}>
          <AppButton label={t('getStarted')} onPress={() => router.push('/(auth)/register')} />
          <AppButton label={t('signIn')} variant="secondary" onPress={() => router.push('/(auth)/login')} />
          <Pressable accessibilityRole="button" accessibilityLabel="Explore the GreenOcean demo" onPress={() => router.replace('/(app)')}><Text style={styles.demo}>{t('exploreDemo')} →</Text></Pressable>
          <Text style={styles.safety}>{t('safetyNote')}</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  </LinearGradient>;
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  safe: { flex: 1, width: '100%' },
  scrollContent: { flexGrow: 1, width: '100%', maxWidth: 920, alignSelf: 'center', padding: spacing.lg },
  top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  logo: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  brand: { color: colors.white, fontSize: 20, fontWeight: '900' },
  safetyBadge: { flexDirection: 'row', alignItems: 'center', gap: 5, borderWidth: 1, borderColor: '#ffffff30', borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 6 },
  safetyBadgeText: { color: colors.ocean200, fontSize: 10, fontWeight: '800' },
  hero: { flex: 1, justifyContent: 'center', gap: spacing.lg, paddingVertical: spacing.xxl },
  eyebrow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  eyebrowText: { color: colors.ocean200, fontSize: 12, fontWeight: '800' },
  title: { fontSize: typography.display, color: colors.white, fontWeight: '900', lineHeight: 49, textAlign: 'left', maxWidth: 680 },
  subtitle: { fontSize: 16, color: colors.ocean100, lineHeight: 26, maxWidth: 600, textAlign: 'left' },
  valueGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  valueCard: { flex: 1, minWidth: 190, minHeight: 150, borderWidth: 1, borderColor: '#ffffff26', backgroundColor: '#ffffff0D', borderRadius: radius.lg, padding: spacing.md, gap: spacing.sm },
  valueIcon: { width: 38, height: 38, borderRadius: radius.sm, backgroundColor: colors.ocean200, alignItems: 'center', justifyContent: 'center' },
  valueTitle: { color: colors.white, fontSize: 13, fontWeight: '900' },
  valueBody: { color: colors.ocean200, fontSize: 11, lineHeight: 17 },
  people: { flexDirection: 'row', alignItems: 'center' },
  face: { width: 36, height: 36, borderRadius: 18, backgroundColor: colors.ocean100, borderWidth: 2, borderColor: colors.ocean800, alignItems: 'center', justifyContent: 'center' },
  faceOverlap: { marginStart: -10 },
  peopleText: { color: colors.ocean200, fontSize: 12, fontWeight: '700', marginStart: spacing.sm },
  actions: { width: '100%', maxWidth: 560, alignSelf: 'center', gap: spacing.sm },
  demo: { color: colors.ocean200, fontWeight: '800', textAlign: 'center', padding: spacing.sm },
  safety: { color: '#ffffff80', fontSize: 9, lineHeight: 14, textAlign: 'center' },
  orb1: { position: 'absolute', width: 350, height: 350, borderRadius: 175, backgroundColor: '#86D9C510', right: -100, top: 70 },
  orb2: { position: 'absolute', width: 260, height: 260, borderRadius: 130, backgroundColor: '#F4B8600D', left: -100, bottom: 70 },
});
