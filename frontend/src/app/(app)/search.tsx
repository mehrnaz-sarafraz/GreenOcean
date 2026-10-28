import {AppButton} from '@/components/app-button';
import {useLanguage} from '@/localization/language-provider';
import { router, useFocusEffect } from 'expo-router';
import { useCallback, useRef, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppIcon } from '@/components/app-icon';
import { Avatar } from '@/components/avatar';
import { Screen } from '@/components/screen';
import { PageResponse, PostItem, SupportCategoryGroup } from '@/features/content/types';
import { PostCard } from '@/features/content/post-card';
import { UserSearchItem } from '@/features/search/types';
import { apiRequest } from '@/lib/api/client';
import { usePlatformData } from '@/features/platform/data-provider';
import { optionsIn } from '@/features/platform/catalog';
import { colors, layout, radius, shadow, spacing, typography } from '@/theme/tokens';

export default function ExploreScreen() {
  const { categories, communities, professionals, articles: professionalArticles, options } = usePlatformData();
  const groups = optionsIn(options, 'CATEGORY_GROUP').map(option => ({ id: option.key as SupportCategoryGroup, title: option.label, subtitle: option.description || '' }));
  const {language}=useLanguage();const tr=useCallback((en:string,fa:string)=>language==='fa'?fa:en,[language]);
  const searchGeneration=useRef(0);
  const [resultPage,setResultPage]=useState(0);const [morePeople,setMorePeople]=useState(false);const [moreStories,setMoreStories]=useState(false);
  const [query, setQuery] = useState('');
  const [people, setPeople] = useState<UserSearchItem[]>([]);
  const [storyResults, setStoryResults] = useState<PostItem[]>([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');
  const normalizedQuery = query.trim().toLowerCase();
  const visibleCategories = categories.filter(category => `${category.name} ${category.group}`.toLowerCase().includes(normalizedQuery));
  const visibleProfessionals = professionals.filter(professional => [professional.displayName, professional.title, professional.city, professional.country, professional.gender, ...professional.specialties, ...professional.languages].join(' ').toLowerCase().includes(normalizedQuery));
  const visibleCommunities = communities.filter(community => `${community.name} ${community.description}`.toLowerCase().includes(normalizedQuery));
  const showKnowledge = !normalizedQuery || 'knowledge articles science verified professionals mental health'.includes(normalizedQuery);
  const showMedia = !normalizedQuery || 'movies series documentaries psychology watch reflect'.includes(normalizedQuery);
  const hasResults = showKnowledge || showMedia || visibleCategories.length > 0 || visibleProfessionals.length > 0 || visibleCommunities.length > 0 || people.length > 0 || storyResults.length > 0;
  const rememberSearch = async () => {
    const term = query.trim();
    if (term.length < 2) return;
    try { await apiRequest('/api/v1/search/history', { method: 'POST', body: JSON.stringify({ term }) }); }
    catch (caught) { setSearchError(caught instanceof Error ? caught.message : tr("Could not save this search.","ذخیرهٔ جست‌وجو انجام نشد.")); }
  };

  useFocusEffect(useCallback(() => {
    let active=true;searchGeneration.current++;setPeople([]);setStoryResults([]);setResultPage(0);setMorePeople(false);setMoreStories(false);
    const timer = setTimeout(() => {
      if (query.trim().length < 2) {
        setPeople([]); setStoryResults([]); setSearchError(''); setSearching(false);
        return;
      }
      setSearching(true); setSearchError('');
      const encoded = encodeURIComponent(query.trim());
      Promise.all([
        apiRequest<PageResponse<UserSearchItem>>(`/api/v1/search/users?q=${encoded}&size=12`),
        apiRequest<PageResponse<PostItem>>(`/api/v1/search/posts?q=${encoded}&size=12`),
      ]).then(([users, stories]) => { if(!active)return;setPeople(users.items); setStoryResults(stories.items);setMorePeople(users.hasNext);setMoreStories(stories.hasNext); })
        .catch(caught => {if(active){setPeople([]);setStoryResults([]);setSearchError(caught instanceof Error ? caught.message : tr("Search is temporarily unavailable.","جست‌وجو موقتاً در دسترس نیست."));}})
        .finally(() => {if(active)setSearching(false);});
    }, 300);
    return () => {active=false;searchGeneration.current++;clearTimeout(timer);setPeople([]);setStoryResults([]);};
  }, [query,tr]));

  async function more(){if(searching)return;setSearching(true);const generation=searchGeneration.current;try{const encoded=encodeURIComponent(query.trim());const [users,stories]=await Promise.all([apiRequest<PageResponse<UserSearchItem>>(`/api/v1/search/users?q=${encoded}&size=12&page=${resultPage+1}`),apiRequest<PageResponse<PostItem>>(`/api/v1/search/posts?q=${encoded}&size=12&page=${resultPage+1}`)]);if(generation!==searchGeneration.current)return;setPeople(current=>[...current,...users.items.filter(p=>!current.some(old=>old.userId===p.userId))]);setStoryResults(current=>[...current,...stories.items.filter(p=>!current.some(old=>old.id===p.id))]);setMorePeople(users.hasNext);setMoreStories(stories.hasNext);setResultPage(resultPage+1);}catch(e){setPeople([]);setStoryResults([]);setSearchError(e instanceof Error?e.message:tr("Unavailable","در دسترس نیست"));}finally{setSearching(false);}}
  return <Screen scroll style={styles.screen}><View style={[styles.content,{direction:language==='fa'?'rtl':'ltr'}]}>
    <Text style={styles.title}>{tr("Explore","کاوش")}</Text>
    <Text style={styles.subtitle}>{tr("Find stories, professional knowledge, support spaces, and thoughtful things to watch.","پست‌ها، دانش تخصصی، فضاهای حمایتی و محتوای دیدنی را پیدا کنید.")}</Text>
    <View style={styles.search}>
      <AppIcon name="search" color={colors.muted} />
      <TextInput accessibilityLabel={tr("Search GreenOcean","جست‌وجو در گرین‌اوشن")} value={query} onChangeText={setQuery} onSubmitEditing={() => void rememberSearch()} returnKeyType="search" placeholder={tr("Search topics, communities, or professionals","جست‌وجوی موضوع، گروه یا متخصص")} placeholderTextColor={colors.muted} style={[styles.input,{textAlign:language==='fa'?'right':'left',writingDirection:language==='fa'?'rtl':'ltr'}]} />
      {!!query && <Pressable accessibilityRole="button" accessibilityLabel={tr("Clear search","پاک‌کردن جست‌وجو")} onPress={() => setQuery('')} style={styles.clearButton}><AppIcon name="close" size={18} color={colors.muted} /></Pressable>}
    </View>

    {normalizedQuery.length >= 2 && <>
      <Section title={tr('People','اعضا')} subtitle={searching ? tr('Searching members…','در حال جست‌وجوی اعضا…') : `${people.length} ${tr('matching members','عضو یافت‌شده')}`} />
      {people.map(person => <Pressable accessibilityRole="button" key={person.userId} onPress={() => router.push({ pathname: '/user/[username]' as never, params: { username: person.username } } as never)} style={styles.person}>
        <Avatar name={person.displayName} uri={person.avatarUrl} size={50} /><View style={styles.flex}><Text style={styles.proName}>{person.displayName}</Text><Text style={styles.proTitle}>@{person.username}{person.profilePrivate ? tr(' · Private profile',' · پروفایل خصوصی') : ''}</Text>{!!person.bio && <Text numberOfLines={2} style={styles.personBio}>{person.bio}</Text>}</View><View style={styles.personCount}><Text style={styles.personCountValue}>{person.followerCount}</Text><Text style={styles.personCountLabel}>{tr("followers","دنبال‌کننده")}</Text></View>
      </Pressable>)}
      <Section title={tr('Stories','پست‌ها')} subtitle={searching ? tr('Searching stories…','در حال جست‌وجوی پست‌ها…') : `${storyResults.length} ${tr('matching stories','پست یافت‌شده')}`} />
      {storyResults.map(post => <PostCard key={post.id} post={post} />)}
      {(morePeople||moreStories)&&<AppButton label={tr('More search results','نتایج جست‌وجوی بیشتر')} disabled={searching} variant="secondary" onPress={()=>void more()}/>}
      {!!searchError && <Text accessibilityRole="alert" style={styles.searchError}>{searchError}</Text>}
    </>}

    {(showKnowledge || showMedia) && <View style={styles.hubGrid}>
      {showKnowledge && <Pressable accessibilityRole="button" onPress={() => router.push('/articles')} style={[styles.hub, { backgroundColor: colors.ocean950 }]}>
        <View style={styles.hubIcon}><AppIcon name="science" color={colors.ocean700} /></View><Text style={styles.hubLabel}>{tr("KNOWLEDGE HUB","دانشنامه")}</Text><Text style={styles.hubTitle}>{tr("Articles by verified professionals","مقاله‌های متخصصان تأییدشده")}</Text><Text style={styles.hubMeta}>{professionalArticles.length} {tr('published articles','مقالهٔ منتشرشده')}</Text><View style={styles.hubArrow}><AppIcon name="arrow_forward" color={colors.ocean950} /></View>
      </Pressable>}
      {showMedia && <Pressable accessibilityRole="button" onPress={() => router.push('/media')} style={[styles.hub, { backgroundColor: '#392C59' }]}>
        <View style={[styles.hubIcon, { backgroundColor: colors.lavenderSoft }]}><AppIcon name="movie" color={colors.lavender} /></View><Text style={[styles.hubLabel, { color: '#C8B9F1' }]}>{tr("WATCH & REFLECT","تماشا و تأمل")}</Text><Text style={styles.hubTitle}>{tr("Psychology movies, series & documentaries","فیلم، سریال و مستند روان‌شناسی")}</Text><Text style={[styles.hubMeta, { color: '#D8D0ED' }]}>{tr("Expert picks · Content notes · Discussion prompts","گزیده‌ها · توضیحات محتوا · پیشنهادهای گفت‌وگو")}</Text><View style={styles.hubArrow}><AppIcon name="arrow_forward" color="#392C59" /></View>
      </Pressable>}
    </View>}

    {groups.map(group => {
      const groupCategories = visibleCategories.filter(category => category.group === group.id);
      return groupCategories.length > 0 && <View key={group.id} style={styles.groupBlock}>
        <Section title={group.title} subtitle={group.subtitle} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.horizontal}>{groupCategories.map(category => <Pressable accessibilityRole="button" key={category.id} style={[styles.topic, { backgroundColor: category.softColor }]}><AppIcon name={category.icon} size={25} color={category.color} /><Text style={styles.topicText}>{category.name}</Text><Text style={styles.topicCount}>{tr("Explore stories","دیدن پست‌ها")}</Text></Pressable>)}</ScrollView>
      </View>;
    })}

    {visibleProfessionals.length > 0 && <Section title={tr("Verified professionals","متخصصان تأییدشده")} subtitle={tr("Compare fit, expertise, location, and trust score","مقایسهٔ تناسب، تخصص، موقعیت و امتیاز اعتماد")} onPress={() => router.push('/professionals')} />}
    {visibleProfessionals.map(professional => <Pressable accessibilityRole="button" key={professional.id} onPress={() => router.push({ pathname: '/profile', params: { professional: professional.id } })} style={styles.pro}>
      <Avatar name={professional.displayName} size={58} verified /><View style={styles.flex}><Text style={styles.proName}>{professional.displayName}</Text><Text style={styles.proTitle}>{professional.title} · {professional.city}, {professional.country}</Text><View style={styles.tags}>{professional.specialties.slice(0, 3).map(specialty => <Text key={specialty} style={styles.tag}>{specialty}</Text>)}</View><Text style={styles.languages}>{professional.languages.join(' · ')} · {professional.gender}</Text></View><View style={styles.score}><Text style={styles.scoreValue}>{professional.greenOceanScore}</Text><Text style={styles.scoreLabel}>{tr("GO SCORE","امتیاز گرین‌اوشن")}</Text><View style={styles.rating}><AppIcon name="star" filled size={13} color={colors.sun} /><Text style={styles.ratingText}>{professional.rating}</Text></View></View>
    </Pressable>)}

    {visibleCommunities.length > 0 && <>
      <Section title={tr("Support circles","گروه‌های حمایت")} subtitle={tr("Moderated communities for shared experiences","گروه‌های دارای نظارت برای تجربه‌های مشترک")} />
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.horizontal}>{visibleCommunities.slice(0, 3).map((community, index) => <Pressable accessibilityRole="button" key={community.id} onPress={() => router.push({ pathname: '/community/[slug]', params: { slug: community.slug } })} style={styles.circle}><View style={[styles.circleIcon, { backgroundColor: [colors.ocean100, colors.lavenderSoft, colors.coralSoft][index] }]}><AppIcon name={['bedtime', 'psychology', 'diversity_1'][index]} color={colors.ocean700} /></View><Text style={styles.circleName}>{community.name}</Text><Text style={styles.circleDescription}>{community.description}</Text><Text style={styles.circleCount}>{community.memberCount.toLocaleString(language)} {tr('members','عضو')}</Text></Pressable>)}</ScrollView>
    </>}

    {!hasResults && <View style={styles.empty}><View style={styles.emptyIcon}><AppIcon name="search_off" color={colors.ocean700} /></View><Text style={styles.emptyTitle}>{tr("No matches yet","نتیجه‌ای پیدا نشد")}</Text><Text style={styles.emptyBody}>{tr("Try a broader topic, condition, city, language, or professional name.","موضوع، شهر، زبان یا نام متخصص دیگری را امتحان کنید.")}</Text><Pressable accessibilityRole="button" onPress={() => setQuery('')}><Text style={styles.emptyAction}>{tr("Clear search","پاک‌کردن جست‌وجو")}</Text></Pressable></View>}
  </View></Screen>;
}

function Section({ title, subtitle, onPress }: { title: string; subtitle: string; onPress?: () => void }) {
  const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const content = <><View><Text style={styles.sectionTitle}>{title}</Text><Text style={styles.sectionSubtitle}>{subtitle}</Text></View>{onPress && <View style={styles.sectionAction}><Text style={styles.sectionActionText}>{tr("View all","دیدن همه")}</Text><AppIcon name="arrow_forward" color={colors.ocean600} /></View>}</>;
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
  person: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, padding: spacing.md },
  personBio: { color: colors.inkSoft, fontSize: 10, lineHeight: 15, marginTop: 4 }, personCount: { alignItems: 'center', minWidth: 54 }, personCountValue: { color: colors.ocean700, fontSize: 16, fontWeight: '900' }, personCountLabel: { color: colors.muted, fontSize: 8 }, searchError: { color: colors.danger, fontSize: 12 },
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
