import {AppButton} from '@/components/app-button';
import { router } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { usePlatformData } from '@/features/platform/data-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function Communities() {
  const { t, isRtl } = useLanguage();
  const { communities } = usePlatformData();


  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={[styles.header, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}><Pressable onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Text style={styles.title}>{t('communities')}</Text><AppIcon name="groups" color={colors.ocean600} /></View>
    <Text style={[styles.subtitle, { textAlign: isRtl ? 'right' : 'left' }]}>{t('discoverCommunities')}</Text>
    <AppButton label={isRtl?'گروه‌ها و پیشنهادهای من':'My groups and proposals'} onPress={()=>router.push('/group-workspace' as never)}/>
    {communities.map((community, index) => <Pressable key={community.id} onPress={() => router.push({ pathname: '/community/[slug]', params: { slug: community.slug } })} style={[styles.card, { flexDirection: isRtl ? 'row-reverse' : 'row' }]}>
      <View style={[styles.icon, { backgroundColor: [colors.ocean100, colors.skySoft, colors.coralSoft, colors.lavenderSoft][index % 4] }]}><AppIcon name={['bedtime', 'psychology', 'diversity_1', 'lock'][index % 4]} color={colors.ocean700} /></View>
      <View style={{ flex: 1 }}><Text style={[styles.name, { textAlign: isRtl ? 'right' : 'left' }]}>{community.name}</Text><Text style={[styles.desc, { textAlign: isRtl ? 'right' : 'left' }]} numberOfLines={2}>{community.description}</Text><Text style={[styles.meta, { textAlign: isRtl ? 'right' : 'left' }]}>{isRtl?'عضویت خصوصی است':'Membership is private'}</Text></View>
      <Pressable onPress={event => { event.stopPropagation(); router.push({pathname:'/community/[slug]',params:{slug:community.slug}}); }} style={[styles.join, community.member && styles.joined]}><Text style={[styles.joinText, community.member && styles.joinedText]}>{community.member ? t('joined') : (isRtl?'مشاهدهٔ قوانین':'Review group rules')}</Text></Pressable>
    </Pressable>)}
    {communities.length === 0 && <Text style={styles.empty}>No support circles are available yet.</Text>}
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  header: { alignItems: 'center', gap: spacing.md }, title: { fontSize: typography.h2, fontWeight: '900', color: colors.ink, flex: 1 }, subtitle: { color: colors.muted, fontSize: 15 },
  card: { alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md },
  icon: { width: 52, height: 52, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center' }, name: { fontWeight: '900', color: colors.ink },
  desc: { fontSize: 12, color: colors.muted, lineHeight: 18, marginTop: 3 }, meta: { fontSize: 10, color: colors.ocean600, fontWeight: '700', marginTop: 5 },
  join: { paddingHorizontal: spacing.md, paddingVertical: 7, borderRadius: radius.pill, backgroundColor: colors.ocean600 }, joined: { backgroundColor: colors.ocean100 },
  joinText: { color: colors.white, fontSize: 11, fontWeight: '800' }, joinedText: { color: colors.ocean700 }, empty: { textAlign: 'center', color: colors.muted, padding: spacing.xl },
});
