import {useLanguage} from '@/localization/language-provider';
import {GrowthRewardsScreen} from '@/features/wellbeing/growth-rewards';
import {LearningScreen} from '@/features/wellbeing/learning';
import {QuestionEditorial} from '@/features/wellbeing/question-editorial';
import {GrowthLibraryScreen,EditorialWorkspace,EducationPermissions} from '@/features/wellbeing/growth-library';
import {PrivateSpace} from '@/features/wellbeing/private-space';
import {ConnectionsPanel} from '@/features/content/connections-panel';
import { AccountSecurityPanel,SessionsPanel } from '@/features/auth/account-security';
import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import { Share, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { ActionRow, EmptyState, Notice, Pill, ProductCard, ProductHeader, ProductPage, ProductSection } from '@/components/product-ui';
import { usePlatformData } from '@/features/platform/data-provider';
import { optionsIn } from '@/features/platform/catalog';
import { CommunityDetails } from '@/features/community/types';
import { AccountArchive, Consultation, Listener, SearchHistoryItem, SessionItem, SocialConnection, UserPreferences } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { useAuth } from '@/features/auth/auth-provider';
import { UrgentHelpContent } from '@/features/safety/urgent-help';
import { colors, radius, spacing } from '@/theme/tokens';

type Params = { tool?: string; targetType?: string; targetId?: string; slug?: string };
const nav = (tool: string, params: Record<string, string> = {}) => router.push({ pathname: '/tools/[tool]' as never, params: { tool, ...params } } as never);

const pageCopy: Record<string, { title: string; subtitle: string; eyebrow?: string }> = {
  'profile-edit': { title: 'Edit profile', subtitle: 'Choose what people can learn about you.', eyebrow: 'YOUR IDENTITY' },
  saved: { title: 'Saved library', subtitle: 'Posts, articles, and media you kept for later.' },
  followers: { title: 'Your connections', subtitle: 'Manage people and professionals you follow.' },
  'notification-settings': { title: 'Notification settings', subtitle: 'Make GreenOcean supportive, not noisy.' },
  'account-security': { title: 'Account & security', subtitle: 'Password, sessions, and sign-in protection.' },
  'privacy-data': { title: 'Privacy & your data', subtitle: 'Control visibility and access a copy of your data.' },
  blocked: { title: 'Blocked accounts', subtitle: 'Blocked people cannot follow or message you.' },
  'safety-center': { title: 'Safety center', subtitle: 'Reporting, boundaries, urgent resources, and safer participation.' },
  'my-reports': { title: 'My reports', subtitle: 'Track reports you submitted and the moderation outcome.' },
  report: { title: 'Report something', subtitle: 'Tell the safety team what happened. Reports are confidential.' },
  'urgent-help': { title: 'Urgent help', subtitle: 'Immediate, country-aware routes to crisis and emergency support.', eyebrow: 'NOT A CRISIS SERVICE' },
  'message-requests': { title: 'Message requests', subtitle: 'Choose who can start a private conversation with you.' },
  'new-message': { title: 'New message', subtitle: 'Start a private conversation with a connection or listener.' },
  'community-create': { title: 'Create a community', subtitle: 'Build a moderated peer space around a shared experience.' },
  'community-rules': { title: 'Community rules', subtitle: 'Clear expectations help people share more safely.' },
  'community-members': { title: 'Members & moderators', subtitle: 'See who helps care for this community.' },
  'community-events': { title: 'Community events', subtitle: 'Join structured, moderated conversations and workshops.' },
  'daily-reflection': {title:'Daily reflection / تأمل روزانه',subtitle:'Three optional reviewed questions. Private answers, no points.'},
  wellbeing: { title: 'Wellbeing hub', subtitle: 'Small tools for reflection, routine, and progress—not diagnosis.', eyebrow: 'YOUR PRIVATE SPACE' },
  'check-ins': { title: 'Mood check-ins', subtitle: 'Notice patterns without judging the day.' },
  goals: { title: 'Gentle goals', subtitle: 'Turn care into small, realistic steps.' },
  journal: { title: 'Private journal', subtitle: 'A private place to name what is happening.' },
  assessments: { title: 'Wellbeing reflection', subtitle: 'A short, non-diagnostic check to help you decide what support may fit.' },
  'growth-paths': { title: 'Guided growth paths', subtitle: 'Short, evidence-informed learning journeys you can take at your pace.' },
  companion: { title: 'Reflection companion', subtitle: 'A guided writing tool—not a therapist or crisis service.' },
  'search-history': { title: 'Search history', subtitle: 'Review or clear recent searches synchronized with your account.' },
  'session-management': { title: 'Active sessions', subtitle: 'Review where your account is signed in.' },
  'data-export': { title: 'Download your data', subtitle: 'Prepare a portable copy of your GreenOcean activity.' },
  consultations: { title: 'Consultation requests', subtitle: 'Track preferred times and confirmations from professional practices.' },
};

export default function ProductToolScreen() {
  const {language}=useLanguage();
  const params = useLocalSearchParams<Params>();
  const screen = Array.isArray(params.tool) ? params.tool[0] : params.tool || '';
  if(screen==='growth-rewards')return <GrowthRewardsScreen/>;
  if(screen==='growth-paths')return <LearningScreen/>;
  if(screen==='question-editorial')return <QuestionEditorial/>;
  if(screen==='growth-library')return <GrowthLibraryScreen/>;
  if(screen==='educational-editorial')return <EditorialWorkspace/>;
  if(screen==='educational-permissions')return <EducationPermissions/>;
  const privateFa:Record<string,{title:string;subtitle:string}>={wellbeing:{title:'فضای خصوصی بهزیستی',subtitle:'ابزارهای اختیاری تأمل و یادگیری؛ بدون تشخیص.'},journal:{title:'دفترچه خصوصی',subtitle:'نوشتن و مدیریت متن‌های خصوصی؛ بدون انتشار.'},'check-ins':{title:'ثبت خلق‌وخو',subtitle:'مشاهدهٔ توصیفی حالت‌های ثبت‌شده؛ بدون قضاوت.'},goals:{title:'هدف‌های کوچک',subtitle:'گام‌های واقعی و قابل تغییر، با سرعت دلخواه تو.'},companion:{title:'همراه نوشتن هدایت‌شده',subtitle:'پرسش‌های ثابت؛ بدون درمان یا پاسخ به بحران.'},assessments:{title:'تأمل دربارهٔ بهزیستی',subtitle:'مشاهده‌های شخصی و اختیاری؛ بدون سنجش بالینی معتبر.'},'daily-reflection':{title:'تأمل روزانه',subtitle:'سه پرسش اختیاری؛ پاسخ خصوصی بدون امتیاز.'}};
  const copy = language==='fa'&&privateFa[screen]?privateFa[screen]:pageCopy[screen];
  if (!copy) return <ProductPage><ProductHeader title="Page not found" subtitle="This GreenOcean tool does not exist." /><EmptyState icon="travel_explore" title="Nothing at this address" body="Return to the wellbeing hub or your profile to keep exploring." /><AppButton label="Go to wellbeing hub" onPress={() => nav('wellbeing')} /></ProductPage>;
  return <ProductPage><ProductHeader {...copy} backLabel={language==='fa'?'بازگشت':'Go back'} />{renderScreen(screen, params)}</ProductPage>;
}

function renderScreen(screen: string, params: Params) {
  switch (screen) {
    case 'profile-edit': return <ProfileEdit />;
    case 'saved': return <Saved />;
    case 'followers': return <Connections />;
    case 'notification-settings': return <NotificationSettings />;
    case 'account-security': return <AccountSecurity />;
    case 'privacy-data': return <PrivacyData />;
    case 'blocked': return <Blocked />;
    case 'safety-center': return <SafetyCenter />;
    case 'my-reports': return <MyReports />;
    case 'report': return <ReportForm targetType={String(params.targetType || 'content')} targetId={String(params.targetId || 'unknown')} />;
    case 'urgent-help': return <UrgentHelpContent />;
    case 'message-requests': return <MessageRequests />;
    case 'new-message': return <NewMessage />;
    case 'community-create': return <CommunityCreate />;
    case 'community-rules': return <CommunityRules slug={String(params.slug || 'better-sleep')} />;
    case 'community-members': return <CommunityMembers slug={String(params.slug || 'better-sleep')} />;
    case 'community-events': return <CommunityEvents slug={String(params.slug || 'better-sleep')} />;
    case 'daily-reflection': return <PrivateSpace section="daily-reflection" />;
    case 'wellbeing': return <WellbeingHub />;
    case 'check-ins': return <PrivateSpace section="check-ins" />;
    case 'goals': return <PrivateSpace section="goals" />;
    case 'journal': return <PrivateSpace section="journal" />;
    case 'assessments': return <Assessment />;
    case 'companion': return <PrivateSpace section="companion" />;
    case 'search-history': return <SearchHistory />;
    case 'session-management': return <Sessions />;
    case 'data-export': return <DataExport />;
    case 'consultations': return <Consultations />;
    default: return null;
  }
}

function ProfileEdit() {
  const { profile: source, refresh } = usePlatformData();
  const [profile, setProfile] = useState({ displayName: '', username: '', bio: '', city: '', countryCode: '', avatarUrl: '', showLocation: false, profilePrivate: true });
  const edited=useRef(false);const [initialized,setInitialized]=useState(false);
  const [message, setMessage] = useState(''); const [busy, setBusy] = useState(false);
  useEffect(() => {
    const timer = setTimeout(() => {
      if (source && !edited.current) {setProfile({ displayName: source.displayName, username: source.username, bio: source.bio || '', city: source.city || '', countryCode: source.countryCode || '', avatarUrl: source.avatarUrl || '', showLocation: source.showLocation, profilePrivate: source.profilePrivate });setInitialized(true);}
    }, 0);
    return () => clearTimeout(timer);
  }, [source]);
  const set = (key: keyof typeof profile, value: string | boolean) => {edited.current=true;setProfile(current => ({ ...current, [key]: value }));};
  const save = async () => { setBusy(true); setMessage(''); try { await apiRequest('/api/v1/profiles/me', { method: 'PATCH', body: JSON.stringify(profile) }); edited.current=false;await refresh(); setMessage('Profile changes saved to your account.'); } catch (caught) { setMessage(caught instanceof Error ? caught.message : 'Could not save profile'); } finally { setBusy(false); } };
  if(!initialized)return <Text accessibilityLiveRegion="polite">Loading profile…</Text>;
  return <><ProductSection title="Social details"><ProductCard><Field label="Display name" value={profile.displayName} onChangeText={value => set('displayName', value)} /><Field label="Username" value={profile.username} onChangeText={value => set('username', value.replace(/\s/g, ''))} /><Field label="Bio" value={profile.bio} onChangeText={value => set('bio', value)} multiline /><Field label="Avatar HTTPS URL (optional)" value={profile.avatarUrl} onChangeText={value=>set('avatarUrl',value)}/><AppButton variant="secondary" label="Remove avatar" onPress={()=>set('avatarUrl','')}/><Text style={styles.body}>Your avatar is discoverable to eligible members. Use a trusted image host; an external host may receive image requests. Location here is separate from your private country for service eligibility.</Text><Toggle label="Show social location" detail="Only visible to people allowed to view your profile details." value={profile.showLocation} onChange={value=>set('showLocation',value)}/><View style={styles.two}><View style={styles.flex}><Field label="City" value={profile.city} onChangeText={value => set('city', value)} /></View><View style={styles.flex}><Field label="Country code" value={profile.countryCode} onChangeText={value => set('countryCode', value.toUpperCase().slice(0, 2))} /></View></View><Toggle label="Private profile" detail="Approve followers to share profile details. Each post has its own audience." value={profile.profilePrivate} onChange={value => set('profilePrivate', value)} /></ProductCard></ProductSection>{!!message && (message.startsWith('Profile') ? <Success text={message} /> : <ErrorText text={message} />)}<AppButton label="Save profile" loading={busy} disabled={!source} onPress={() => void save()} /></>;
}

function Saved() {
  const { articles, media, posts, refresh } = usePlatformData();
  const savedArticles = articles.filter(item => item.saved); const savedMedia = media.filter(item => item.saved); const savedPosts = posts.filter(item => item.bookmarked);
  const remove = async (path: string) => { await apiRequest(path, { method: 'DELETE' }); await refresh(); };
  return <><Notice title="Your private collection" body="Saved items are private and synchronized with your GreenOcean account." /><ProductSection title="Posts" subtitle={`${savedPosts.length} saved`}>{savedPosts.length ? savedPosts.map(item => <ActionRow key={item.id} icon="bookmark" title={item.body.slice(0, 72)} subtitle="Saved community story" value="Remove" onPress={() => void remove(`/api/v1/posts/${item.id}/bookmark`)} />) : <EmptyState icon="bookmark_border" title="No saved posts yet" body="Bookmark a story from the feed to keep it here." />}</ProductSection><ProductSection title="Articles" subtitle={`${savedArticles.length} saved`}>{savedArticles.length ? savedArticles.map(item => <ActionRow key={item.id} icon="article" title={item.title} subtitle={item.summary} value="Remove" onPress={() => void remove(`/api/v1/articles/${item.id}/save`)} />) : <EmptyState icon="bookmark_border" title="No saved articles yet" body="Open Knowledge and save anything you want to revisit." />}</ProductSection><ProductSection title="Media" subtitle={`${savedMedia.length} saved`}>{savedMedia.map(item => <ActionRow key={item.id} icon="movie" title={item.title} subtitle={item.description} value="Remove" onPress={() => void remove(`/api/v1/media-recommendations/${item.id}/save`)} />)}{!savedMedia.length && <EmptyState icon="subscriptions" title="No saved media yet" body="Save a movie, series, or documentary from the Media guide." />}</ProductSection></>;
}

function Connections() {return <ConnectionsPanel/>;}

function NotificationSettings() {
  const { preferences, setPreferences } = usePlatformData();
  const fields = [['notifyMessages','Direct messages','Private conversations and requests'],['notifyReplies','Replies & mentions','Activity on your posts and comments'],['notifyFollows','New followers','People and professionals who connect'],['notifyGroups','Community activity','Events, announcements, and moderation'],['notifyArticles','Knowledge updates','New articles from followed professionals'],['quietHoursEnabled','Quiet hours','Pause non-urgent notifications overnight']] as const;
  const set = async (key: typeof fields[number][0], value: boolean) => { if (!preferences) return; const next = { ...preferences, [key]: value }; setPreferences(next); try { setPreferences(await apiRequest<UserPreferences>('/api/v1/preferences/me', { method: 'PATCH', body: JSON.stringify({ [key]: value }) })); } catch { setPreferences(preferences); } };
  return <ProductCard>{preferences ? fields.map(([key,label,detail]) => <Toggle key={key} label={label} detail={detail} value={preferences[key]} onChange={value => void set(key, value)} />) : <EmptyState icon="notifications" title="Preferences unavailable" body="Notification settings could not be loaded." />}</ProductCard>;
}

function AccountSecurity(){return <AccountSecurityPanel/>;}

function PrivacyData() {
  const { profile, refresh } = usePlatformData(); const [blocked, setBlocked] = useState<SocialConnection[]>([]);
  useEffect(() => { apiRequest<SocialConnection[]>('/api/v1/social/blocks').then(setBlocked).catch(() => undefined); }, []);
  const [privacyBusy,setPrivacyBusy]=useState(false);const [privacyError,setPrivacyError]=useState('');
  const setPrivate = async (value: boolean) => {if(!profile||privacyBusy)return;setPrivacyBusy(true);setPrivacyError('');try{await apiRequest('/api/v1/profiles/me', { method: 'PATCH', body: JSON.stringify({ profilePrivate: value }) }); await refresh();}catch{setPrivacyError('Privacy changes were not saved. Your previous setting remains active.');}finally{setPrivacyBusy(false);} };
  if(!profile)return <Text>Loading saved profile privacy…</Text>;
  return <>{!!privacyError&&<ErrorText text={privacyError}/>}<Text>{privacyBusy?'Saving…':'Saved profile privacy'}</Text><ProductCard><Toggle label="Private profile" detail="Approve followers to share profile details. Each post keeps its own audience." value={profile?.profilePrivate ?? true} onChange={value => void setPrivate(value)} /><ActionRow icon="block" title="Blocked accounts" subtitle={`${blocked.length} accounts blocked`} onPress={() => nav('blocked')} /><ActionRow icon="manage_search" title="Search history" subtitle="Synchronized with your account" onPress={() => nav('search-history')} /></ProductCard><ProductSection title="Data rights"><ProductCard><Text style={styles.body}>Generate a current JSON archive containing your profile, preferences, activity, saved items, wellbeing records, connections, reports, and search history.</Text><AppButton label="Open data export" onPress={() => nav('data-export')} /></ProductCard></ProductSection></>;
}

function Blocked() {
  const [accounts, setAccounts] = useState<SocialConnection[]>([]);
  const load = async () => setAccounts(await apiRequest<SocialConnection[]>('/api/v1/social/blocks'));
  useEffect(() => {
    const timer = setTimeout(() => void load(), 0);
    return () => clearTimeout(timer);
  }, []);
  const unblock = async (userId: string) => { await apiRequest(`/api/v1/social/blocks/${userId}`, { method: 'DELETE' }); await load(); };
  return <>{accounts.length ? accounts.map(account => <ActionRow key={account.userId} icon="block" title={account.displayName} subtitle={`@${account.username} · cannot message, mention, or follow you`} value="Unblock" onPress={() => void unblock(account.userId)} />) : <EmptyState icon="verified_user" title="No blocked accounts" body="Accounts you block will appear here." />}</>;
}

function SafetyCenter() {
  return <><Notice icon="health_and_safety" title="Safety is a product feature" body="Control what you see, report harmful behavior, and reach urgent help without hunting through menus." /><ProductSection title="Your controls"><ActionRow icon="tune" title="Content & contact controls" subtitle="Sensitive content, muted words, and messages" onPress={() => router.push('/safety-settings')} /><ActionRow icon="block" title="Blocked accounts" subtitle="Review and unblock accounts" onPress={() => nav('blocked')} /><ActionRow icon="flag" title="My reports" subtitle="See status and moderation outcomes" onPress={() => nav('my-reports')} /></ProductSection><ProductSection title="Get help"><ActionRow icon="emergency" title="Urgent help" subtitle="Country-aware emergency and crisis routes" danger onPress={() => nav('urgent-help')} /><ActionRow icon="support_agent" title="Talk with support" subtitle="Listener, peer, or verified professional" onPress={() => router.push('/support')} /><ActionRow icon="gavel" title="Community guidelines" subtitle="What GreenOcean permits and why" onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'guidelines' } } as never)} /></ProductSection></>;
}

function MyReports() {
  const [reports, setReports] = useState<{ id: string; reason: string; status: string; targetType: string; targetId: string; createdAt: string; resolutionNote: string | null }[]>([]);
  useEffect(() => { apiRequest<typeof reports>('/api/v1/reports/me').then(setReports).catch(() => undefined); }, []);
  return <>{reports.length ? reports.map(report => <ProductCard key={report.id}><View style={styles.between}><Text style={styles.cardTitle}>{report.reason.replaceAll('_', ' ')}</Text><Pill label={report.status} active={report.status === 'RESOLVED'} /></View><Text style={styles.body}>{report.targetType} · {report.targetId}</Text><Text style={styles.meta}>{new Date(report.createdAt).toLocaleString()}</Text>{report.resolutionNote && <Notice title="Outcome" body={report.resolutionNote} />}</ProductCard>) : <EmptyState icon="flag" title="No reports submitted" body="If you report content or behavior, its status and outcome will appear here." />}</>;
}

function ReportForm({ targetType, targetId }: { targetType: string; targetId: string }) {
  const [reason, setReason] = useState('HARASSMENT'); const [details, setDetails] = useState(''); const [done, setDone] = useState(false); const [error, setError] = useState('');
  const { options } = usePlatformData(); const reasons = optionsIn(options, 'REPORT_REASON');
  const type = targetType.toUpperCase() === 'CONTENT' ? 'POST' : targetType.toUpperCase();
  const submit = async () => { setError(''); try { await apiRequest('/api/v1/reports', { method: 'POST', body: JSON.stringify({ targetType: type, targetId, reason, description: details }) }); setDone(true); } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not submit report'); } };
  if (done) return <><Success text="Report submitted. You can track its status in My reports." /><AppButton label="View my reports" onPress={() => nav('my-reports')} /></>;
  return <><Notice title="Report context" body={`${type}: ${targetId}. The reported account is not told who submitted the report.`} /><ProductCard><Text style={styles.label}>What is happening?</Text><View style={styles.wrap}>{reasons.map(option => <Pill key={option.key} label={option.label} active={reason === option.key} onPress={() => setReason(option.key)} />)}</View><Field label="Details for the safety team" value={details} onChangeText={setDetails} multiline placeholder="What happened, and when? Avoid adding unrelated personal information." />{!!error && <ErrorText text={error} />}<AppButton label="Submit confidential report" disabled={!targetId || targetId === 'unknown'} onPress={() => void submit()} /></ProductCard></>;
}

function MessageRequests() {
  const { preferences, setPreferences } = usePlatformData();
  const set = async (key: 'allowMessageRequests' | 'professionalsOnlyMessages', value: boolean) => { if (!preferences) return; const previous = preferences; setPreferences({ ...previous, [key]: value }); try { setPreferences(await apiRequest<UserPreferences>('/api/v1/preferences/me', { method: 'PATCH', body: JSON.stringify({ [key]: value }) })); } catch { setPreferences(previous); } };
  return <><ProductCard>{preferences ? <><Toggle label="Allow message requests" detail="People you do not follow can request a conversation." value={preferences.allowMessageRequests} onChange={value => void set('allowMessageRequests', value)} /><Toggle label="Professionals only" detail="Limit new requests to verified professionals." value={preferences.professionalsOnlyMessages} onChange={value => void set('professionalsOnlyMessages', value)} /></> : <EmptyState icon="mail" title="Preferences unavailable" body="Message controls could not be loaded." />}</ProductCard><Notice title="Requests are never invented" body="A request appears only after another account starts a real conversation permitted by these settings." /></>;
}

function NewMessage() {
  const { professionals } = usePlatformData();
  const [query, setQuery] = useState('');
  const [connections, setConnections] = useState<SocialConnection[]>([]);
  const [listeners, setListeners] = useState<Listener[]>([]);
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');
  useEffect(() => {
    Promise.all([
      apiRequest<SocialConnection[]>('/api/v1/social/following'),
      apiRequest<Listener[]>('/api/v1/listeners'),
    ]).then(([people, availableListeners]) => { setConnections(people); setListeners(availableListeners); })
      .catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load message contacts'));
  }, []);
  const normalized = query.trim().toLowerCase();
  const matches = (value: string) => !normalized || value.toLowerCase().includes(normalized);
  const start = async (kind: 'connection' | 'professional' | 'listener', id: string) => {
    setBusy(`${kind}-${id}`); setError('');
    try {
      const result = kind === 'listener'
        ? await apiRequest<{ conversationId: string }>(`/api/v1/listeners/match?listenerId=${id}`, { method: 'POST' })
        : await apiRequest<{ id: string; status?:string }>(kind === 'professional' ? `/api/v1/conversations/professionals/${id}` : `/api/v1/conversations/users/${id}`, { method: 'POST' });
      if('status' in result&&result.status==='PENDING'){router.replace('/message-requests' as never);return;}
      router.replace({ pathname: '/chat/[id]', params: { id: 'conversationId' in result ? result.conversationId : result.id } });
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not start conversation');
    } finally { setBusy(''); }
  };
  const visibleConnections = connections.filter(person => matches(`${person.displayName} ${person.username}`));
  const visibleProfessionals = professionals.filter(person => matches(`${person.displayName} ${person.title} ${person.specialties.join(' ')}`));
  const visibleListeners = listeners.filter(person => matches(`${person.displayName} ${person.username} ${person.topics.join(' ')}`));
  return <><Field label="Find a person" value={query} onChangeText={setQuery} placeholder="Search connections, professionals, or listeners" />{!!error && <ErrorText text={error} />}<ProductSection title="Connections" subtitle={`${visibleConnections.length} available`}>{visibleConnections.map(person => <ActionRow key={person.userId} icon="person" title={person.displayName} subtitle={`@${person.username}${busy === `connection-${person.userId}` ? ' · opening…' : ''}`} onPress={() => void start('connection', person.userId)} />)}{!visibleConnections.length && <EmptyState icon="group" title="No matching connections" body="Follow someone from their profile to find them here." />}</ProductSection><ProductSection title="Verified professionals" subtitle={`${visibleProfessionals.length} available`}>{visibleProfessionals.map(person => <ActionRow key={person.id} icon="verified" title={person.displayName} subtitle={`${person.title}${busy === `professional-${person.id}` ? ' · opening…' : ''}`} onPress={() => void start('professional', person.id)} />)}</ProductSection><ProductSection title="Trained peer listeners" subtitle={`${visibleListeners.length} available`}>{visibleListeners.map(person => <ActionRow key={person.id} icon="support_agent" title={person.displayName} subtitle={`${person.displayTitle} · ${person.online ? 'online' : 'offline'}${busy === `listener-${person.id}` ? ' · opening…' : ''}`} onPress={() => void start('listener', person.id)} />)}</ProductSection><Notice title="Private conversation boundaries" body="Do not share financial details, passwords, or identifying medical documents in chat. Report pressure, threats, or unsafe advice." /></>;
}

function CommunityCreate() {
  const { refresh } = usePlatformData(); const [name, setName] = useState(''); const [description, setDescription] = useState(''); const [privateCommunity, setPrivate] = useState(false); const [error, setError] = useState(''); const [created, setCreated] = useState('');
  const create = async () => { if (name.trim().length < 3 || description.trim().length < 20) return setError('Add a clear name and a description of at least 20 characters.'); const slug = name.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''); setError(''); try { await apiRequest('/api/v1/communities', { method: 'POST', body: JSON.stringify({ name: name.trim(), slug, description: description.trim(), privateCommunity }) }); await refresh(); setCreated(slug); } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not create community'); } };
  if (created) return <><Success text="Community created with you as owner and moderator." /><AppButton label="Open my community" onPress={() => router.replace({ pathname: '/community/[slug]', params: { slug: created } })} /></>;
  return <><Notice title="Moderation commitment" body="Creators define rules, appoint moderators, respond to reports, and keep promotional or clinical claims out of peer spaces." /><ProductCard><Field label="Community name" value={name} onChangeText={setName} placeholder="Example: Gentle Mornings" /><Field label="Purpose and who it is for" value={description} onChangeText={setDescription} multiline /><Toggle label="Private community" detail="People request access before reading or posting." value={privateCommunity} onChange={setPrivate} />{!!error && <ErrorText text={error} />}<AppButton label="Create community" onPress={() => void create()} /></ProductCard></>;
}

function CommunityRules({ slug }: { slug: string }) {
  const { community, details, setDetails, error } = useCommunityDetails(slug);
  const [draft, setDraft] = useState(''); const [message, setMessage] = useState('');
  useEffect(() => {
    const timer = setTimeout(() => {
      if (details) setDraft(details.rules.map(rule => rule.text).join('\n'));
    }, 0);
    return () => clearTimeout(timer);
  }, [details]);
  const canManage = community?.membershipRole === 'OWNER' || community?.membershipRole === 'MODERATOR';
  const save = async () => {
    if (!community) return;
    const rules = draft.split('\n').map(rule => rule.trim()).filter(Boolean);
    if (!rules.length) return setMessage('Add at least one rule.');
    try {
      setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/rules`, { method: 'PUT', body: JSON.stringify({ rules }) }));
      setMessage('Community rules updated.');
    } catch (caught) { setMessage(caught instanceof Error ? caught.message : 'Could not update rules.'); }
  };
  return <><Notice title={community?.name || 'Community rules'} body="Community-specific rules and GreenOcean-wide safety standards both apply." />{!!error && <ErrorText text={error} />}{canManage ? <ProductCard><Field label="One rule per line" value={draft} onChangeText={setDraft} multiline placeholder="Write clear, specific participation rules." /><Text style={styles.meta}>{draft.split('\n').filter(item => item.trim()).length}/20 rules</Text><AppButton label="Save rules" onPress={() => void save()} />{!!message && (message.startsWith('Community') ? <Success text={message} /> : <ErrorText text={message} />)}</ProductCard> : details?.rules.map(rule => <ProductCard key={rule.id}><View style={styles.number}><Text style={styles.numberText}>{rule.position}</Text></View><Text style={[styles.body, styles.flex]}>{rule.text}</Text></ProductCard>)}{community && !community.member && <EmptyState icon="lock" title="Join to view the complete guidelines" body="Members can review the current community rules before posting." />}<ActionRow icon="flag" title="Report a rules concern" subtitle="Send it confidentially to moderators" onPress={() => community && nav('report', { targetType: 'community', targetId: community.id })} /></>;
}

function CommunityMembers({ slug }: { slug: string }) {
  const { community, details, setDetails, error } = useCommunityDetails(slug);
  const [requests, setRequests] = useState<{ userId: string; displayName: string; username: string; requestedAt: string }[]>([]);
  const canManage = community?.membershipRole === 'OWNER' || community?.membershipRole === 'MODERATOR';
  const communityId = community?.id;
  useEffect(() => { if (communityId && canManage) apiRequest<typeof requests>(`/api/v1/communities/${communityId}/join-requests`).then(setRequests).catch(() => undefined); }, [communityId, canManage]);
  const resolve = async (userId: string, approve: boolean) => {
    if (!community) return;
    await apiRequest(`/api/v1/communities/${community.id}/join-requests/${userId}${approve ? '/approve' : ''}`, { method: approve ? 'POST' : 'DELETE' });
    setRequests(items => items.filter(item => item.userId !== userId));
    if (approve) setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/details`));
  };
  const setRole = async (userId: string, role: 'MEMBER' | 'MODERATOR') => { if (community) setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/members/${userId}/role`, { method: 'PATCH', body: JSON.stringify({ role }) })); };
  const remove = async (userId: string) => { if (community) setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/members/${userId}`, { method: 'DELETE' })); };
  const moderators = details?.members.filter(member => member.role !== 'MEMBER') || [];
  const members = details?.members.filter(member => member.role === 'MEMBER') || [];
  return <>{!!error && <ErrorText text={error} />}{canManage && <ProductSection title="Access requests" subtitle={`${requests.length} waiting`}>{requests.map(person => <ProductCard key={person.userId}><ActionRow icon="person_add" title={person.displayName} subtitle={`@${person.username}`} /><View style={styles.two}><View style={styles.flex}><AppButton label="Approve" onPress={() => void resolve(person.userId, true)} /></View><View style={styles.flex}><AppButton label="Decline" variant="secondary" onPress={() => void resolve(person.userId, false)} /></View></View></ProductCard>)}{!requests.length && <EmptyState icon="how_to_reg" title="No access requests" body="Private-circle requests will appear here." />}</ProductSection>}<ProductSection title="Moderators" subtitle={`${moderators.length} community leads`}>{moderators.map(person => <ProductCard key={person.userId}><ActionRow icon="shield" title={person.displayName} subtitle={`@${person.username} · ${person.role.toLowerCase()}`} />{community?.membershipRole === 'OWNER' && person.role === 'MODERATOR' && <View style={styles.two}><View style={styles.flex}><AppButton label="Make member" variant="secondary" onPress={() => void setRole(person.userId, 'MEMBER')} /></View><View style={styles.flex}><AppButton label="Remove" variant="secondary" onPress={() => void remove(person.userId)} /></View></View>}</ProductCard>)}{!moderators.length && !error && <EmptyState icon="shield" title="No moderators to show" body="Community leadership will appear here." />}</ProductSection><ProductSection title="Members" subtitle={`${members.length} visible members`}>{members.map(person => <ProductCard key={person.userId}><ActionRow icon="person" title={person.displayName} subtitle={`@${person.username} · joined ${new Date(person.joinedAt).toLocaleDateString()}`} />{canManage && <View style={styles.two}>{community?.membershipRole === 'OWNER' && <View style={styles.flex}><AppButton label="Make moderator" variant="secondary" onPress={() => void setRole(person.userId, 'MODERATOR')} /></View>}<View style={styles.flex}><AppButton label="Remove" variant="secondary" onPress={() => void remove(person.userId)} /></View></View>}</ProductCard>)}{!members.length && !error && <EmptyState icon="group" title="No members to show" body="Join this community to see its member directory." />}</ProductSection></>;
}

function CommunityEvents({ slug }: { slug: string }) {
  const { community, details, setDetails, error } = useCommunityDetails(slug);
  const [title, setTitle] = useState(''); const [startsAt, setStartsAt] = useState(() => new Date(Date.now() + 86400000).toISOString().slice(0, 16)); const [duration, setDuration] = useState('60'); const [message, setMessage] = useState('');
  const canManage = community?.membershipRole === 'OWNER' || community?.membershipRole === 'MODERATOR';
  const toggle = async (eventId: string, joined: boolean) => { if (community) setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/events/${eventId}/attendance`, { method: joined ? 'DELETE' : 'PUT' })); };
  const create = async () => {
    if (!community) return;
    const start = new Date(startsAt); const minutes = Number(duration);
    if (!title.trim() || Number.isNaN(start.getTime()) || minutes < 15) return setMessage('Add a title, valid start time, and a duration of at least 15 minutes.');
    try {
      const end = new Date(start.getTime() + minutes * 60000);
      setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/events`, { method: 'POST', body: JSON.stringify({ title: title.trim(), format: 'TEXT_CIRCLE', startsAt: start.toISOString(), endsAt: end.toISOString(), capacity: 100 }) }));
      setTitle(''); setMessage('Event scheduled.');
    } catch (caught) { setMessage(caught instanceof Error ? caught.message : 'Could not schedule event.'); }
  };
  const cancel = async (eventId: string) => { if (community) setDetails(await apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/events/${eventId}`, { method: 'DELETE' })); };
  return <>{!!error && <ErrorText text={error} />}{canManage && <ProductSection title="Schedule an event"><ProductCard><Field label="Event title" value={title} onChangeText={setTitle} /><Field label="Start (YYYY-MM-DDTHH:mm)" value={startsAt} onChangeText={setStartsAt} /><Field label="Duration in minutes" value={duration} onChangeText={setDuration} /><AppButton label="Schedule text circle" onPress={() => void create()} />{!!message && (message.startsWith('Event') ? <Success text={message} /> : <ErrorText text={message} />)}</ProductCard></ProductSection>}{details?.events.map(event => <ProductCard key={event.id}><Text style={styles.cardTitle}>{event.title}</Text><Text style={styles.body}>{new Date(event.startsAt).toLocaleString()} · {event.format.replaceAll('_', ' ')}</Text>{!!event.description && <Text style={styles.body}>{event.description}</Text>}<Text style={styles.meta}>Hosted by {event.hostName || 'Community team'} · {event.attendeeCount}{event.capacity ? `/${event.capacity}` : ''} attending</Text><View style={styles.two}><View style={styles.flex}><AppButton label={event.joined ? 'Leave event' : 'Join event'} variant={event.joined ? 'secondary' : 'primary'} onPress={() => void toggle(event.id, event.joined)} /></View>{canManage && <View style={styles.flex}><AppButton label="Cancel event" variant="secondary" onPress={() => void cancel(event.id)} /></View>}</View></ProductCard>)}{!details?.events.length && !error && <EmptyState icon="event" title="No upcoming events" body="Scheduled community circles and workshops will appear here." />}</>;
}

function useCommunityDetails(slug: string) {
  const { communities } = usePlatformData();
  const community = communities.find(item => item.slug === slug);
  const [details, setDetails] = useState<CommunityDetails | null>(null);
  const [error, setError] = useState('');
  useEffect(() => {
    const timer = setTimeout(() => {
      if (!community?.member) { setDetails(null); return; }
      setError('');
      apiRequest<CommunityDetails>(`/api/v1/communities/${community.id}/details`)
        .then(setDetails)
        .catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load community details'));
    }, 0);
    return () => clearTimeout(timer);
  }, [community?.id, community?.member]);
  return { community, details, setDetails, error };
}

function WellbeingHub() {
  const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const rows=[['growth-rewards','Growth Points','امتیاز رشد','Private educational milestones; no wellbeing score','گام‌های آموزشی خصوصی؛ بدون امتیاز بهزیستی'],['growth-library','Growth Library / کتابخانه رشد','کتابخانه رشد','Reviewed educational resources','منابع آموزشی بازبینی‌شده'],['daily-reflection','Daily reflection / تأمل روزانه','تأمل روزانه','Three optional questions; skip freely','سه پرسش اختیاری؛ با امکان رد کردن'],['assessments','Wellbeing reflection','تأمل دربارهٔ بهزیستی','A non-diagnostic support check','مشاهدهٔ شخصی بدون تشخیص'],['growth-paths','Guided growth paths','مسیرهای یادگیری','Short skill-building journeys','درس‌های واقعی با سرعت دلخواه'],['companion','Reflection companion','همراه نوشتن هدایت‌شده','Guided prompts with clear boundaries','پرسش‌های ثابت با مرزهای روشن']];
  return <><Notice title={tr('Private by design','خصوصی از ابتدا')} body={tr('These tools are for reflection and skill practice. They do not diagnose conditions or replace professional care.','این ابزارها برای تأمل و تمرین مهارت‌اند؛ تشخیص نمی‌دهند و جای مراقبت حرفه‌ای را نمی‌گیرند.')}/><ProductSection title={tr('Today','امروز')}><ActionRow icon="mood" title={tr('Mood check-in','ثبت خلق‌وخو')} subtitle={tr('Name the moment in under a minute','ثبت اختیاری حالت کنونی')} onPress={()=>nav('check-ins')}/><ActionRow icon="flag" title={tr('Gentle goals','هدف‌های کوچک')} subtitle={tr('Small steps and progress','گام‌های واقعی و قابل تغییر')} onPress={()=>nav('goals')}/><ActionRow icon="book" title={tr('Private journal','دفترچه خصوصی')} subtitle={tr('Write without publishing','نوشتن بدون انتشار')} onPress={()=>nav('journal')}/></ProductSection><ProductSection title={tr('Learn and reflect','یادگیری و تأمل')}>{rows.map(([tool,en,fa,detailEn,detailFa])=><ActionRow key={tool} icon="book" title={tr(en,fa)} subtitle={tr(detailEn,detailFa)} onPress={()=>nav(tool)}/>)}</ProductSection></>;
}

function Assessment() {
  const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const translations:Record<string,string>={PAUSE_UNDER_STRESS:'توانسته‌ام هنگام افزایش فشار مکث کنم.',SOCIAL_CONNECTION:'با دست‌کم یک نفر احساس ارتباط داشته‌ام.',RESTORATIVE_SLEEP:'خوابم به اندازهٔ کافی نیروبخش بوده است.',MANAGEABLE_RESPONSIBILITIES:'مسئولیت‌های روزانه قابل انجام بوده‌اند.',INTEREST_OR_HOPE:'لحظه‌هایی از علاقه یا امید داشته‌ام.',NOT_AT_ALL:'اصلاً',SOME_DAYS:'بعضی روزها',OFTEN:'اغلب',MOST_DAYS:'بیشتر روزها',NEARLY_ALWAYS:'تقریباً همیشه'};
  const { options } = usePlatformData(); const questions = optionsIn(options, 'ASSESSMENT_QUESTION'); const responses = optionsIn(options, 'ASSESSMENT_RESPONSE'); const [answers, setAnswers] = useState<Record<string, number>>({}); const [done, setDone] = useState(false); const [saving, setSaving] = useState(false); const [saveError, setSaveError] = useState(''); const orderedAnswers = questions.map(question => answers[question.key] ?? 0);
  const finish = async () => { setSaving(true); setSaveError(''); try { await apiRequest('/api/v1/wellbeing/reflections', { method: 'POST', body: JSON.stringify({ answers: orderedAnswers }) }); setDone(true); } catch (caught) { setSaveError(caught instanceof Error ? caught.message : 'Could not save this reflection.'); } finally { setSaving(false); } };
  if (done) return <><Notice title={tr('Private reflection saved','تأمل خصوصی ذخیره شد')} body={tr('These are your own observations. No diagnostic result, risk estimate or treatment recommendation is inferred from them.','این‌ها مشاهده‌های شخصی تو هستند؛ نتیجهٔ تشخیصی، برآورد خطر یا پیشنهاد درمان از آن‌ها استنباط نمی‌شود.')} /><Text>{tr('This optional tool is not a validated clinical measurement. Historical responses remain private and unchanged.','این ابزار اختیاری سنجش بالینی معتبر نیست. پاسخ‌های تاریخی خصوصی و بدون تغییر می‌مانند.')}</Text><ActionRow icon="emergency" title={tr('Urgent help','کمک فوری')} danger onPress={() => nav('urgent-help')} /><AppButton label={tr('Take again','تکرار اختیاری')} variant="secondary" onPress={() => setDone(false)} /></>;
  return <><Notice title={tr('Important boundary','مرز مهم')} body={tr('Answer for the past 7 days. This tool cannot detect or diagnose a mental health condition.','به هفت روز گذشته فکر کن. این ابزار نمی‌تواند مشکل سلامت روان را شناسایی یا تشخیص دهد.')} />{questions.map((question, index) => <ProductCard key={question.key}><Text style={styles.cardTitle}>{index + 1}. {language==='fa'?(translations[question.key]||question.label):question.label}</Text><View style={styles.wrap}>{responses.map(response => <Pill key={response.key} label={language==='fa'?(translations[response.key]||response.label):response.label} active={(answers[question.key] ?? 0) === response.sortOrder} onPress={() => setAnswers(current => ({ ...current, [question.key]: response.sortOrder }))} />)}</View></ProductCard>)}{!!saveError && <ErrorText text={saveError} />}<AppButton label={tr('Save and see reflection','ذخیره و مشاهدهٔ تأمل')} disabled={!questions.length || !responses.length} loading={saving} onPress={() => void finish()} /></>;
}

function SearchHistory() {
  const [history, setHistory] = useState<SearchHistoryItem[]>([]); const [error, setError] = useState('');
  const load = async () => { try { setHistory(await apiRequest<SearchHistoryItem[]>('/api/v1/search/history')); } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not load search history.'); } };
  useEffect(() => { const timer = setTimeout(() => void load(), 0); return () => clearTimeout(timer); }, []);
  const clear = async () => { await apiRequest('/api/v1/search/history', { method: 'DELETE' }); setHistory([]); };
  return <>{history.length ? <><ProductCard>{history.map(item => <View key={item.id} style={styles.history}><AppIcon name="history" color={colors.muted} /><View style={styles.flex}><Text style={styles.body}>{item.term}</Text><Text style={styles.meta}>{new Date(item.searchedAt).toLocaleString()}</Text></View></View>)}</ProductCard><AppButton label="Clear search history" variant="secondary" onPress={() => void clear()} /></> : <EmptyState icon="manage_search" title="No recent searches" body="Submitted searches will appear here and stay synced with your account." />}{!!error && <ErrorText text={error} />}</>;
}

function Sessions(){return <SessionsPanel/>;}

function DataExport() {
  const [archive, setArchive] = useState<AccountArchive | null>(null); const [busy, setBusy] = useState(false); const [error, setError] = useState('');
  const prepare = async () => { setBusy(true); setError(''); try { setArchive(await apiRequest<AccountArchive>('/api/v1/account/archive')); } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not generate archive'); } finally { setBusy(false); } };
  const summary = archive ? `${archive.activity.posts.length} posts, ${archive.activity.comments.length} comments, ${archive.wellbeing.journalEntries.length} journal entries, ${archive.searchHistory.length} recent searches, ${archive.reports.length} reports, ${archive.activity.savedItems.length} saved items` : '';
  return <><ProductCard><Text style={styles.cardTitle}>What your archive includes</Text><Text style={styles.body}>Profile, preferences, posts, comments, community memberships, saved items, search history, private wellbeing data, connections, blocks, and report history.</Text>{!!summary && <Text style={styles.meta}>{summary}</Text>}<AppButton label={archive ? 'Share JSON archive' : 'Generate archive'} loading={busy} onPress={() => archive ? void Share.share({ message: JSON.stringify(archive, null, 2) }) : void prepare()} /></ProductCard>{archive && <Success text={`Archive generated from your account at ${new Date(archive.generatedAt).toLocaleString()}.`} />}{!!error && <ErrorText text={error} />}</>;
}

function Consultations() {
  const [items, setItems] = useState<Consultation[]>([]); const [error, setError] = useState('');
  const load = async () => { try { setItems(await apiRequest<Consultation[]>('/api/v1/consultations')); } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not load consultation requests.'); } };
  useEffect(() => { const timer = setTimeout(() => void load(), 0); return () => clearTimeout(timer); }, []);
  const cancel = async (id: string) => { await apiRequest(`/api/v1/consultations/${id}`, { method: 'DELETE' }); await load(); };
  return <>{items.map(item => <ProductCard key={item.id}><View style={styles.between}><View style={styles.flex}><Text style={styles.cardTitle}>{item.professionalName}</Text><Text style={styles.body}>{new Date(item.scheduledAt).toLocaleString()} · {item.consultationMode}</Text>{item.note && <Text style={styles.meta}>{item.note}</Text>}</View><Pill label={item.status} active={item.status === 'CONFIRMED'} /></View>{(item.status === 'REQUESTED' || item.status === 'CONFIRMED') && <AppButton label="Cancel request" variant="secondary" onPress={() => void cancel(item.id)} />}</ProductCard>)}{!items.length && !error && <EmptyState icon="event_available" title="No consultation requests" body="Requests you send from a professional profile will appear here." />}{!!error && <ErrorText text={error} />}</>;
}

function Field({ label, value, onChangeText, multiline = false, placeholder, secure = false }: { label: string; value: string; onChangeText: (value: string) => void; multiline?: boolean; placeholder?: string; secure?: boolean }) { return <View style={styles.field}><Text style={styles.fieldLabel}>{label}</Text><TextInput accessibilityLabel={label} value={value} onChangeText={onChangeText} multiline={multiline} secureTextEntry={secure} placeholder={placeholder} placeholderTextColor={colors.muted} style={[styles.input, multiline && styles.multiline]} /></View>; }
function Toggle({ label, detail, value, onChange }: { label: string; detail: string; value: boolean; onChange: (value: boolean) => void }) { return <View style={styles.toggle}><View style={styles.flex}><Text style={styles.cardTitle}>{label}</Text><Text style={styles.body}>{detail}</Text></View><Switch accessibilityLabel={label} value={value} onValueChange={onChange} trackColor={{ true: colors.ocean500 }} /></View>; }
function Success({ text }: { text: string }) { return <View style={styles.success}><AppIcon name="check_circle" filled color={colors.ocean600} /><Text style={[styles.body, styles.flex]}>{text}</Text></View>; }
function ErrorText({ text }: { text: string }) { return <Text style={styles.error}>{text}</Text>; }

const styles = StyleSheet.create({
  flex: { flex: 1 }, two: { flexDirection: 'row', gap: spacing.sm, flexWrap: 'wrap' }, between: { flexDirection: 'row', gap: spacing.md, alignItems: 'center', justifyContent: 'space-between' }, wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, field: { gap: 6 }, fieldLabel: { color: colors.ink, fontSize: 11, fontWeight: '900' }, input: { minHeight: 48, borderWidth: 1, borderColor: colors.borderStrong, borderRadius: radius.md, backgroundColor: colors.white, paddingHorizontal: spacing.md, color: colors.ink, textAlign: 'left', writingDirection: 'ltr' }, multiline: { minHeight: 112, paddingTop: spacing.md, textAlignVertical: 'top' }, toggle: { minHeight: 64, flexDirection: 'row', alignItems: 'center', gap: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border, paddingVertical: spacing.sm }, label: { color: colors.ink, fontSize: 12, fontWeight: '900' }, cardTitle: { color: colors.ink, fontSize: 12, lineHeight: 18, fontWeight: '900' }, body: { color: colors.inkSoft, fontSize: 10, lineHeight: 17 }, meta: { color: colors.muted, fontSize: 9, lineHeight: 14 }, success: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, borderRadius: radius.md, backgroundColor: colors.ocean100, borderWidth: 1, borderColor: colors.ocean200 }, error: { color: colors.danger, fontSize: 11, fontWeight: '800' }, number: { width: 28, height: 28, borderRadius: 14, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, numberText: { color: colors.ocean700, fontWeight: '900' }, bigScore: { color: colors.ocean700, fontSize: 36, fontWeight: '900', textAlign: 'center' }, eyebrow: { color: colors.ocean600, fontSize: 9, fontWeight: '900', letterSpacing: 1 }, prompt: { color: colors.ink, fontSize: 20, lineHeight: 29, fontWeight: '900' }, history: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.sm },
});
