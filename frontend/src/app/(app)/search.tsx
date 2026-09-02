import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { SupportCategoryGroup } from '@/features/content/types';
import { usePlatformData } from '@/features/platform/data-provider';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

const groups: { id: SupportCategoryGroup; title: string; subtitle: string }[] = [
  { id: 'EMOTION', title: 'Emotions', subtitle: 'How people are feeling today' },
  { id: 'CONDITION', title: 'Conditions', subtitle: 'Symptoms, diagnoses, and recovery' },
  { id: 'LIFE_EXPERIENCE', title: 'Life experiences', subtitle: 'What people have lived through' },
];

export default function ExploreScreen() {
  const { categories, communities, professionals, articles: professionalArticles } = usePlatformData();
  const [query, setQuery] = useState('');
  const normalizedQuery = query.trim().toLowerCase();
  const visibleCategories = categories.filter(category => `${category.name} ${category.group}`.toLowerCase().includes(normalizedQuery));
  const visibleProfessionals = professionals.filter(professional => [professional.displayName, professional.title, professional.city, professional.country, professional.gender, ...professional.specialties, ...professional.languages].join(' ').toLowerCase().includes(normalizedQuery));
  const visibleCommunities = communities.filter(community => `${community.name} ${community.description}`.toLowerCase().includes(normalizedQuery));
  const showKnowledge = !normalizedQuery || 'knowledge articles science verified professionals mental health'.includes(normalizedQuery);
  const showMedia = !normalizedQuery || 'movies series documentaries psychology watch reflect'.includes(normalizedQuery);
  const hasResults = showKnowledge || showMedia || visibleCategories.length > 0 || visibleProfessionals.length > 0 || visibleCommunities.length > 0;

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <Text style={styles.title}>Explore</Text>
    <Text style={styles.subtitle}>Find stories, professional knowledge, support spaces, and thoughtful things to watch.</Text>
    <View style={styles.search}>
      <AppIcon name="search" color={colors.muted} />
      <TextInput accessibilityLabel="Search GreenOcean" value={query} onChangeText={setQuery} placeholder="Search topics, communities, or professionals" placeholderTextColor={colors.muted} style={styles.input} />
      {!!query && <Pressable accessibilityRole="button" accessibilityLabel="Clear search" onPress={() => setQuery('')} style={styles.clearButton}><AppIcon name="close" size={18} color={colors.muted} /></Pressable>}
    </View>

    {(showKnowledge || showMedia) && <View style={styles.hubGrid}>
      {showKnowledge && <Pressable accessibilityRole="button" onPress={() => router.push('/articles')} style={[styles.hub, { backgroundColor: colors.ocean950 }]}>
        <View style={styles.hubIcon}><AppIcon name="science" color={colors.ocean700} /></View><Text style={styles.hubLabel}>KNOWLEDGE HUB</Text><Text style={styles.hubTitle}>Articles by verified professionals</Text><Text style={styles.hubMeta}>{professionalArticles.length} new reads · Clinician reviewed</Text><View style={styles.hubArrow}><AppIcon name="arrow_forward" color={colors.ocean950} /></View>
      </Pressable>}
      {showMedia && <Pressable accessibilityRole="button" onPress={() => router.push('/media')} style={[styles.hub, { backgroundColor: '#392C59' }]}>
        <View style={[styles.hubIcon, { backgroundColor: colors.lavenderSoft }]}><AppIcon name="movie" color={colors.lavender} /></View><Text style={[styles.hubLabel, { color: '#C8B9F1' }]}>WATCH & REFLECT</Text><Text style={styles.hubTitle}>Psychology movies, series & documentaries</Text><Text style={[styles.hubMeta, { color: '#D8D0ED' }]}>Expert picks · Content notes · Discussion prompts</Text><View style={styles.hubArrow}><AppIcon name="arrow_forward" color="#392C59" /></View>
      </Pressable>}
    </View>}

    {groups.map(group => {
      const groupCategories = visibleCategories.filter(category => category.group === group.id);
      return groupCategories.length > 0 && <View key={group.id} style={styles.groupBlock}>
        <Section title={group.title} subtitle={group.subtitle} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.horizontal}>{groupCategories.map(category => <Pressable accessibilityRole="button" key={category.id} style={[styles.topic, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={25} color={category.color} /><Text style={styles.topicText}>{category.name}</Text><Text style={styles.topicCount}>{category.postCount.toLocaleString()} stories</Text></Pressable>)}</ScrollView>
      </View>;
    })}

    {visibleProfessionals.length > 0 && <Section title="Verified professionals" subtitle="Compare fit, expertise, location, and trust score" onPress={() => router.push('/professionals')} />}
    {visibleProfessionals.map(professional => <Pressable accessibilityRole="button" key={professional.id} onPress={() => router.push({ pathname: '/profile', params: { professional: professional.id } })} style={styles.pro}>
      <Avatar name={professional.displayName} size={58} verified /><View style={styles.flex}><Text style={styles.proName}>{professional.displayName}</Text><Text style={styles.proTitle}>{professional.title} · {professional.city}, {professional.country}</Text><View style={styles.tags}>{professional.specialties.slice(0, 3).map(specialty => <Text key={specialty} style={styles.tag}>{specialty}</Text>)}</View><Text style={styles.languages}>{professional.languages.join(' · ')} · {professional.gender}</Text></View><View style={styles.score}><Text style={styles.scoreValue}>{professional.greenOceanScore}</Text><Text style={styles.scoreLabel}>GO SCORE</Text><View style={styles.rating}><AppIcon name="star" filled size={13} color={colors.sun} /><Text style={styles.ratingText}>{professional.rating}</Text></View></View>
    </Pressable>)}

    {visibleCommunities.length > 0 && <>
      <Section title="Support circles" subtitle="Moderated communities for shared experiences" />
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.horizontal}>{visibleCommunities.slice(0, 3).map((community, index) => <Pressable accessibilityRole="button" key={community.id} onPress={() => router.push({ pathname: '/community/[slug]', params: { slug: community.slug } })} style={styles.circle}><View style={[styles.circleIcon, { backgroundColor: [colors.ocean100, colors.lavenderSoft, colors.coralSoft][index] }]}><AppIcon name={['bedtime', 'psychology', 'diversity_1'][index]} color={colors.ocean700} /></View><Text style={styles.circleName}>{community.name}</Text><Text style={styles.circleDescription}>{community.description}</Text><Text style={styles.circleCount}>{community.memberCount.toLocaleString()} members</Text></Pressable>)}</ScrollView>
    </>}

    {!hasResults && <View style={styles.empty}><View style={styles.emptyIcon}><AppIcon name="search_off" color={colors.ocean700} /></View><Text style={styles.emptyTitle}>No matches yet</Text><Text style={styles.emptyBody}>Try a broader topic, condition, city, language, or professional name.</Text><Pressable accessibilityRole="button" onPress={() => setQuery('')}><Text style={styles.emptyAction}>Clear search</Text></Pressable></View>}
  </View></Screen>;
}

function Section({ title, subtitle, onPress }: { title: string; subtitle: string; onPress?: () => void }) {
  const content = <><View><Text style={styles.sectionTitle}>{title}</Text><Text style={styles.sectionSubtitle}>{subtitle}</Text></View>{onPress && <View style={styles.sectionAction}><Text style={styles.sectionActionText}>View all</Text><AppIcon name="arrow_forward" color={colors.ocean600} /></View>}</>;
  return onPress ? <Pressable accessibilityRole="button" onPress={onPress} style={styles.section}>{content}</Pressable> : <View style={styles.section}>{content}</View>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 },
  content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md },
  title: { fontSize: typography.h1, fontWeight: '900', color: colors.ink },
  subtitle: { color: colors.muted, fontSize: 12, lineHeight: 18 },
  search: { height: 52, flexDirection: 'row', alignItems: 'center', gap: spacing.sm, backgroundColor: colors.white, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, paddingHorizontal: spacing.md },
  input: { flex: 1, color: colors.ink, fontSize: 15, textAlign: 'left' },
  clearButton: { width: 34, height: 34, borderRadius: 17, backgroundColor: colors.foam, alignItems: 'center', justifyContent: 'center' },
  hubGrid: { gap: spacing.sm },
  hub: { minHeight: 185, borderRadius: radius.xl, padding: spacing.lg, overflow: 'hidden' },
  hubIcon: { width: 44, height: 44, borderRadius: radius.sm, backgroundColor: colors.ocean200, alignItems: 'center', justifyContent: 'center' },
  hubLabel: { color: colors.ocean300, fontSize: 8, fontWeight: '900', letterSpacing: 1, marginTop: spacing.md },
  hubTitle: { color: colors.white, fontSize: 18, lineHeight: 24, fontWeight: '900', marginTop: 4, maxWidth: 470 },
  hubMeta: { color: colors.ocean200, fontSize: 9, marginTop: 5 },
  hubArrow: { position: 'absolute', right: spacing.lg, bottom: spacing.lg, width: 36, height: 36, borderRadius: 18, backgroundColor: colors.white, alignItems: 'center', justifyContent: 'center' },
  groupBlock: { gap: spacing.sm },
  section: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: spacing.sm, minHeight: 44 },
  sectionTitle: { fontSize: typography.h3, fontWeight: '900', color: colors.ink },
  sectionSubtitle: { color: colors.muted, fontSize: 9, marginTop: 3 },
  sectionAction: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  sectionActionText: { color: colors.ocean700, fontSize: 11, fontWeight: '900' },
  horizontal: { gap: spacing.sm },
  topic: { width: 150, minHeight: 125, borderRadius: radius.lg, padding: spacing.md, justifyContent: 'space-between' },
  topicText: { fontWeight: '900', color: colors.ink, fontSize: 12 },
  topicCount: { fontSize: 9, color: colors.muted },
  pro: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md, ...shadow.soft },
  flex: { flex: 1 },
  proName: { fontWeight: '900', color: colors.ink },
  proTitle: { fontSize: 10, color: colors.muted, marginTop: 3 },
  tags: { flexDirection: 'row', flexWrap: 'wrap', gap: 5, marginTop: 7 },
  tag: { fontSize: 8, color: colors.ocean700, backgroundColor: colors.ocean50, paddingHorizontal: 7, paddingVertical: 3, borderRadius: radius.pill },
  languages: { color: colors.muted, fontSize: 8, marginTop: 7 },
  score: { width: 62, height: 72, borderRadius: radius.md, backgroundColor: colors.ocean50, alignItems: 'center', justifyContent: 'center' },
  scoreValue: { color: colors.ocean800, fontSize: 19, fontWeight: '900' },
  scoreLabel: { color: colors.muted, fontSize: 6, fontWeight: '900' },
  rating: { flexDirection: 'row', alignItems: 'center', gap: 2, marginTop: 4 },
  ratingText: { color: colors.inkSoft, fontSize: 9, fontWeight: '900' },
  circle: { width: 190, backgroundColor: colors.white, borderRadius: radius.lg, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.sm },
  circleIcon: { width: 40, height: 40, borderRadius: radius.sm, alignItems: 'center', justifyContent: 'center' },
  circleName: { fontWeight: '900', color: colors.ink },
  circleDescription: { color: colors.muted, fontSize: 9, lineHeight: 14 },
  circleCount: { fontSize: 10, color: colors.ocean600, fontWeight: '800' },
  empty: { minHeight: 260, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.xl, alignItems: 'center', justifyContent: 'center', padding: spacing.xl, gap: spacing.sm },
  emptyIcon: { width: 54, height: 54, borderRadius: 27, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' },
  emptyTitle: { color: colors.ink, fontSize: 18, fontWeight: '900' },
  emptyBody: { color: colors.muted, fontSize: 12, lineHeight: 18, textAlign: 'center', maxWidth: 360 },
  emptyAction: { color: colors.ocean700, fontSize: 12, fontWeight: '900', padding: spacing.sm },
});
