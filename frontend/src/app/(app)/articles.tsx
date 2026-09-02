import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { usePlatformData } from '@/features/platform/data-provider';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';

const filters = ['All', 'Anxiety', 'Sleep', 'Relationships'];

export default function Articles() {
  const { user } = useAuth();
  const { professionals, articles: professionalArticles } = usePlatformData();
  const [filter, setFilter] = useState('All');
  const shown = useMemo(
    () => professionalArticles.filter(article => filter === 'All' || article.topic === filter),
    [filter, professionalArticles]
  );  
  const featured = professionalArticles.find(article => article.pinned) ?? professionalArticles[0];
  const featuredAuthor = professionals.find(item => item.id === featured?.authorId);
  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.top}><Pressable onPress={() => router.back()} style={styles.back}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable>{user?.roles.includes('PROFESSIONAL') && <Pressable onPress={() => router.push('/article-editor')} style={styles.studio}><AppIcon name="edit_note" size={18} color={colors.white} /><Text style={styles.studioText}>Professional studio</Text></Pressable>}</View>
    <View><Text style={styles.eyebrow}>GREENOCEAN KNOWLEDGE HUB</Text><Text style={styles.title}>Clear science for real life</Text><Text style={styles.subtitle}>Educational articles written by verified mental-health professionals and reviewed for clarity, boundaries, and safety.</Text></View>

    {featured && featuredAuthor && <Pressable onPress={() => router.push({ pathname: '/article/[id]', params: { id: featured.id } })} style={styles.hero}>
      <View style={styles.heroGlow} /><View style={styles.pinned}><AppIcon name="push_pin" filled size={16} color={colors.ocean950} /><Text style={styles.pinnedText}>PINNED ARTICLE</Text></View><Text style={styles.heroTopic}>{featured.evidenceLevel} · {featured.readTime}</Text><Text style={styles.heroTitle}>{featured.title}</Text><Text style={styles.heroSummary}>{featured.summary}</Text><View style={styles.author}><Avatar name={featuredAuthor.displayName} size={42} verified /><View style={{ flex: 1 }}><Text style={styles.authorName}>{featuredAuthor.displayName}</Text><Text style={styles.authorRole}>{featuredAuthor.title} · Score {featuredAuthor.greenOceanScore}</Text></View><View style={styles.read}><Text style={styles.readText}>Read article</Text><AppIcon name="arrow_forward" color={colors.ocean950} /></View></View>
    </Pressable>}

    <View style={styles.boundary}><AppIcon name="health_and_safety" color={colors.ocean600} /><Text style={styles.boundaryText}>Knowledge Hub content is educational. It cannot diagnose you or replace care from a qualified professional who knows your situation.</Text></View>
    <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filters}>{filters.map(item => <Pressable key={item} onPress={() => setFilter(item)} style={[styles.filter, filter === item && styles.filterActive]}><Text style={[styles.filterText, filter === item && styles.filterTextActive]}>{item}</Text></Pressable>)}</ScrollView>
    <View style={styles.sectionHead}><Text style={styles.sectionTitle}>{filter === 'All' ? 'Latest professional articles' : filter}</Text><Text style={styles.result}>{shown.length} articles</Text></View>
    {shown.map(article => {
      const author = professionals.find(item => item.id === article.authorId);
      if (!author) return null;
      return <Pressable key={article.id} onPress={() => router.push({ pathname: '/article/[id]', params: { id: article.id } })} style={styles.card}>
        <View style={styles.cardTop}><View style={styles.evidence}><AppIcon name="science" size={15} color={colors.ocean600} /><Text style={styles.evidenceText}>{article.evidenceLevel}</Text></View>{article.pinned && <AppIcon name="push_pin" filled size={18} color={colors.ocean600} />}</View>
        <Text style={styles.topic}>{article.topic} · {article.readTime}</Text><Text style={styles.cardTitle}>{article.title}</Text><Text style={styles.cardSummary}>{article.summary}</Text>
        <View style={styles.cardAuthor}><Avatar name={author.displayName} uri={author.avatarUrl} size={38} verified /><View style={{ flex: 1 }}><Text style={styles.cardAuthorName}>{author.displayName}</Text><Text style={styles.cardMeta}>{article.publishedAt ? new Date(article.publishedAt).toLocaleDateString() : 'In review'} · {article.helpfulCount} helpful</Text></View><AppIcon name="chevron_right" color={colors.ocean700} /></View>
      </Pressable>;
    })}
    {!professionalArticles.length && <Text style={styles.result}>No published articles are available yet.</Text>}
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md }, top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, back: { width: 40, height: 40, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' }, studio: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.ocean700, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: spacing.sm }, studioText: { color: colors.white, fontSize: 10, fontWeight: '900' },
  eyebrow: { color: colors.ocean600, fontSize: 10, fontWeight: '900', letterSpacing: 1.3 }, title: { fontSize: typography.h1, lineHeight: 38, fontWeight: '900', color: colors.ink, marginTop: 5 }, subtitle: { color: colors.muted, fontSize: 13, lineHeight: 20, marginTop: spacing.sm },
  hero: { backgroundColor: colors.ocean950, borderRadius: radius.xl, padding: spacing.lg, gap: spacing.sm, overflow: 'hidden', ...shadow.floating }, heroGlow: { position: 'absolute', width: 320, height: 320, borderRadius: 160, backgroundColor: colors.ocean700, right: -100, top: -160 }, pinned: { alignSelf: 'flex-start', flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.ocean300, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 5 }, pinnedText: { color: colors.ocean950, fontSize: 8, fontWeight: '900', letterSpacing: .8 }, heroTopic: { color: colors.ocean300, fontSize: 9, fontWeight: '900', marginTop: spacing.xs }, heroTitle: { color: colors.white, fontSize: 25, lineHeight: 33, fontWeight: '900' }, heroSummary: { color: colors.ocean100, fontSize: 12, lineHeight: 19 }, author: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginTop: spacing.sm }, authorName: { color: colors.white, fontSize: 12, fontWeight: '900' }, authorRole: { color: colors.ocean300, fontSize: 9, marginTop: 2 }, read: { flexDirection: 'row', alignItems: 'center', gap: 5, backgroundColor: colors.white, borderRadius: radius.pill, paddingHorizontal: spacing.sm, paddingVertical: 7 }, readText: { color: colors.ocean950, fontSize: 9, fontWeight: '900' },
  boundary: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.ocean50, borderRadius: radius.md, padding: spacing.md }, boundaryText: { flex: 1, color: colors.muted, fontSize: 10, lineHeight: 16 }, filters: { gap: spacing.sm }, filter: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: 9 }, filterActive: { backgroundColor: colors.ocean900, borderColor: colors.ocean900 }, filterText: { color: colors.muted, fontSize: 11, fontWeight: '800' }, filterTextActive: { color: colors.white },
  sectionHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }, sectionTitle: { color: colors.ink, fontSize: typography.h3, fontWeight: '900' }, result: { color: colors.muted, fontSize: 10 }, card: { backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.lg, gap: spacing.sm, ...shadow.soft }, cardTop: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, evidence: { flexDirection: 'row', alignItems: 'center', gap: 4 }, evidenceText: { color: colors.ocean600, fontSize: 8, fontWeight: '900' }, topic: { color: colors.muted, fontSize: 10, fontWeight: '800' }, cardTitle: { color: colors.ink, fontSize: 19, lineHeight: 26, fontWeight: '900' }, cardSummary: { color: colors.inkSoft, fontSize: 12, lineHeight: 19 }, cardAuthor: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, borderTopWidth: 1, borderTopColor: colors.border, paddingTop: spacing.sm, marginTop: spacing.xs }, cardAuthorName: { color: colors.ink, fontSize: 11, fontWeight: '900' }, cardMeta: { color: colors.muted, fontSize: 9, marginTop: 2 },
});
