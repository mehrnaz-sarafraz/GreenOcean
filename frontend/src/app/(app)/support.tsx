import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { EmptyState } from '@/components/product-ui';
import { Listener } from '@/features/platform/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

export default function SupportNow() {
  const { channels } = usePlatformData();const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const [listeners, setListeners] = useState<Listener[]>([]);
  useEffect(() => {
    const timer = setTimeout(() => apiRequest<Listener[]>('/api/v1/listeners').then(setListeners).catch(() => undefined), 0);
    return () => clearTimeout(timer);
  }, []);
  const onlineListeners = listeners.filter(listener => listener.online);
  const onlinePeers = channels.reduce((total, channel) => total + channel.onlineCount, 0);
  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.top}><Pressable onPress={() => router.back()} style={styles.back}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><View style={styles.private}><AppIcon name="lock" size={15} color={colors.ocean600} /><Text style={styles.privateText}>Private by default</Text></View></View>
    <View><Text style={styles.eyebrow}>SUPPORT, WITHOUT THE GUESSWORK</Text><Text style={styles.title}>What would feel helpful right now?</Text><Text style={styles.subtitle}>Choose the kind of connection you want. You can change your mind at any time.</Text></View>

    <View style={styles.availability}><View style={styles.pulse} /><Text style={styles.availabilityText}>{onlineListeners.length} trained listeners and {onlinePeers} channel participants online now</Text></View>

    <Pressable onPress={() => router.push({ pathname: '/listener/[action]' as never, params: { action: 'match' } } as never)} style={[styles.path, styles.listenerPath]}>
      <View style={styles.pathTop}><View style={[styles.pathIcon, { backgroundColor: colors.ocean200 }]}><AppIcon name="hearing" size={28} color={colors.ocean800} /></View><View style={styles.recommended}><Text style={styles.recommendedText}>TRAINED · NON-CLINICAL</Text></View></View>
      <Text style={styles.pathTitle}>Talk with a trained listener</Text><Text style={styles.pathText}>A private, one-to-one conversation with someone trained to listen without judging or giving medical advice.</Text>
      <View style={styles.pathMeta}><Meta icon="schedule" text={onlineListeners.length ? `${onlineListeners.length} online now` : 'Check current availability'} /><Meta icon="payments" text="Free" /><Meta icon="lock" text="Private conversation" /></View>
      <View style={styles.pathButton}><Text style={styles.pathButtonText}>Find my listener</Text><AppIcon name="arrow_forward" color={colors.white} /></View>
    </Pressable>

    <View style={styles.pathGrid}>
      <Pressable onPress={() => router.push('/channels')} style={styles.smallPath}><View style={[styles.pathIcon, { backgroundColor: colors.skySoft }]}><AppIcon name="groups" color={colors.sky} /></View><Text style={styles.smallTitle}>Join people who understand</Text><Text style={styles.smallText}>Enter a moderated group based on a feeling, condition, or lived experience.</Text><Meta icon="forum" text="Live and asynchronous groups" /><View style={styles.inlineAction}><Text style={styles.inlineActionText}>Browse groups</Text><AppIcon name="arrow_forward" size={17} color={colors.ocean700} /></View></Pressable>
      <Pressable onPress={() => router.push('/professionals')} style={styles.smallPath}><View style={[styles.pathIcon, { backgroundColor: colors.lavenderSoft }]}><AppIcon name="psychology" color={colors.lavender} /></View><Text style={styles.smallTitle}>Find a verified professional</Text><Text style={styles.smallText}>Compare specialty, language, location, availability, and consultation style.</Text><Meta icon="verified" text="Credentials checked" /><View style={styles.inlineAction}><Text style={styles.inlineActionText}>Find a professional</Text><AppIcon name="arrow_forward" size={17} color={colors.ocean700} /></View></Pressable>
    </View>

    {listeners.length ? <View style={styles.listenerPreview}><View style={styles.faces}>{listeners.slice(0, 3).map(listener => <Avatar key={listener.id} name={listener.displayName} uri={listener.avatarUrl || undefined} size={38} />)}</View><View style={{ flex: 1 }}><Text style={styles.previewTitle}>{tr('Explore listener profiles','کشف پروفایل شنوندگان')}</Text><Text style={styles.previewText}>{tr('Review profiles before requesting a match. Onboarding topics do not automatically match you.','پیش از درخواست تطبیق، پروفایل‌ها را بررسی کنید. موضوعات معرفی اولیه شما را خودکار به شنونده‌ای متصل نمی‌کنند.')}</Text></View></View> : <EmptyState icon="support_agent" title={tr('Listener availability is not confirmed','دسترسی به شنونده تأیید نشده است')} body={tr('Matching depends on your region and current capacity. Explore permitted resources or check again later.','تطبیق به منطقه و ظرفیت فعلی وابسته است. منابع مجاز را ببینید یا بعداً دوباره بررسی کنید.')} />}

    <View style={styles.crisis}><View style={styles.crisisIcon}><AppIcon name="emergency" color={colors.white} /></View><View style={{ flex: 1 }}><Text style={styles.crisisTitle}>Are you in immediate danger?</Text><Text style={styles.crisisText}>GreenOcean is not a crisis service. Contact your local emergency services or a crisis line in your country now.</Text></View><Pressable onPress={() => router.push({ pathname: '/tools/[tool]' as never, params: { tool: 'urgent-help' } } as never)} style={styles.crisisButton}><Text style={styles.crisisButtonText}>Find urgent help</Text></Pressable></View>
    <Text style={styles.boundary}>Peer listeners and community members do not provide diagnosis, treatment, or emergency intervention.</Text>
  </View></Screen>;
}

function Meta({ icon, text }: { icon: string; text: string }) { return <View style={styles.meta}><AppIcon name={icon} size={15} color={colors.muted} /><Text style={styles.metaText}>{text}</Text></View>; }
const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md }, top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, back: { width: 40, height: 40, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' }, private: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.ocean50, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 6 }, privateText: { color: colors.ocean600, fontSize: 9, fontWeight: '900' },
  eyebrow: { color: colors.ocean600, fontSize: 9, fontWeight: '900', letterSpacing: 1.3 }, title: { color: colors.ink, fontSize: typography.h1, lineHeight: 39, fontWeight: '900', marginTop: 5 }, subtitle: { color: colors.muted, fontSize: 13, lineHeight: 20, marginTop: spacing.sm }, availability: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, alignSelf: 'flex-start', backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 8 }, pulse: { width: 9, height: 9, borderRadius: 5, backgroundColor: colors.ocean500 }, availabilityText: { color: colors.inkSoft, fontSize: 10, fontWeight: '800' },
  path: { borderRadius: radius.xl, padding: spacing.lg, gap: spacing.md, ...shadow.floating }, listenerPath: { backgroundColor: colors.ocean950 }, pathTop: { flexDirection: 'row', justifyContent: 'space-between' }, pathIcon: { width: 52, height: 52, borderRadius: radius.md, alignItems: 'center', justifyContent: 'center' }, recommended: { alignSelf: 'flex-start', backgroundColor: colors.ocean300, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 6 }, recommendedText: { color: colors.ocean950, fontSize: 8, fontWeight: '900', letterSpacing: .8 }, pathTitle: { color: colors.white, fontSize: 23, fontWeight: '900' }, pathText: { color: colors.ocean100, fontSize: 12, lineHeight: 19 }, pathMeta: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.md }, pathButton: { alignSelf: 'flex-start', flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.ocean500, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 10 }, pathButtonText: { color: colors.white, fontSize: 11, fontWeight: '900' },
  pathGrid: { gap: spacing.sm }, smallPath: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.sm, ...shadow.soft }, smallTitle: { color: colors.ink, fontSize: 17, fontWeight: '900' }, smallText: { color: colors.muted, fontSize: 11, lineHeight: 18 }, meta: { flexDirection: 'row', alignItems: 'center', gap: 5 }, metaText: { color: colors.muted, fontSize: 9, fontWeight: '700' }, inlineAction: { flexDirection: 'row', alignItems: 'center', gap: 5, marginTop: spacing.xs }, inlineActionText: { color: colors.ocean700, fontSize: 11, fontWeight: '900' },
  listenerPreview: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.ocean50, borderRadius: radius.lg, padding: spacing.md }, faces: { flexDirection: 'row' }, previewTitle: { color: colors.ink, fontSize: 11, fontWeight: '900' }, previewText: { color: colors.muted, fontSize: 9, lineHeight: 14, marginTop: 3 }, crisis: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: spacing.md, backgroundColor: colors.coralSoft, borderWidth: 1, borderColor: '#F8B4A8', borderRadius: radius.lg, padding: spacing.md }, crisisIcon: { width: 42, height: 42, borderRadius: radius.sm, backgroundColor: colors.danger, alignItems: 'center', justifyContent: 'center' }, crisisTitle: { color: colors.danger, fontSize: 12, fontWeight: '900' }, crisisText: { color: colors.inkSoft, fontSize: 9, lineHeight: 14, marginTop: 3 }, crisisButton: { backgroundColor: colors.danger, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 9 }, crisisButtonText: { color: colors.white, fontSize: 9, fontWeight: '900' }, boundary: { color: colors.muted, fontSize: 9, lineHeight: 15, textAlign: 'center', paddingHorizontal: spacing.lg },
});
