import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Linking, Share, StyleSheet, Switch, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { ActionRow, EmptyState, Notice, Pill, ProductCard, ProductHeader, ProductPage, ProductSection } from '@/components/product-ui';
import { useProduct } from '@/features/product/product-provider';
import { mockCommunities, mockPosts, professionals } from '@/mocks/data';
import { mediaPicks, professionalArticles } from '@/mocks/discovery';
import { colors, radius, spacing } from '@/theme/tokens';

type Params = { tool?: string; targetType?: string; targetId?: string; slug?: string };
const nav = (tool: string, params: Record<string, string> = {}) => router.push({ pathname: '/tools/[tool]' as never, params: { tool, ...params } } as never);
const id = (prefix: string) => `${prefix}-${Date.now()}`;

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
  wellbeing: { title: 'Wellbeing hub', subtitle: 'Small tools for reflection, routine, and progress—not diagnosis.', eyebrow: 'YOUR PRIVATE SPACE' },
  'check-ins': { title: 'Mood check-ins', subtitle: 'Notice patterns without judging the day.' },
  goals: { title: 'Gentle goals', subtitle: 'Turn care into small, realistic steps.' },
  journal: { title: 'Private journal', subtitle: 'A private place to name what is happening.' },
  assessments: { title: 'Wellbeing reflection', subtitle: 'A short, non-diagnostic check to help you decide what support may fit.' },
  'growth-paths': { title: 'Guided growth paths', subtitle: 'Short, evidence-informed learning journeys you can take at your pace.' },
  companion: { title: 'Reflection companion', subtitle: 'A guided writing tool—not a therapist or crisis service.' },
  'search-history': { title: 'Search history', subtitle: 'Review or clear recent searches stored on this device.' },
  'session-management': { title: 'Active sessions', subtitle: 'Review where your account is signed in.' },
  'data-export': { title: 'Download your data', subtitle: 'Prepare a portable copy of your GreenOcean activity.' },
};

export default function ProductToolScreen() {
  const params = useLocalSearchParams<Params>();
  const screen = Array.isArray(params.tool) ? params.tool[0] : params.tool || '';
  const copy = pageCopy[screen];
  if (!copy) return <ProductPage><ProductHeader title="Page not found" subtitle="This GreenOcean tool does not exist." /><EmptyState icon="travel_explore" title="Nothing at this address" body="Return to the wellbeing hub or your profile to keep exploring." /><AppButton label="Go to wellbeing hub" onPress={() => nav('wellbeing')} /></ProductPage>;
  return <ProductPage><ProductHeader {...copy} />{renderScreen(screen, params)}</ProductPage>;
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
    case 'urgent-help': return <UrgentHelp />;
    case 'message-requests': return <MessageRequests />;
    case 'new-message': return <NewMessage />;
    case 'community-create': return <CommunityCreate />;
    case 'community-rules': return <CommunityRules slug={String(params.slug || 'better-sleep')} />;
    case 'community-members': return <CommunityMembers />;
    case 'community-events': return <CommunityEvents />;
    case 'wellbeing': return <WellbeingHub />;
    case 'check-ins': return <CheckIns />;
    case 'goals': return <Goals />;
    case 'journal': return <Journal />;
    case 'assessments': return <Assessment />;
    case 'growth-paths': return <GrowthPaths />;
    case 'companion': return <Companion />;
    case 'search-history': return <SearchHistory />;
    case 'session-management': return <Sessions />;
    case 'data-export': return <DataExport />;
    default: return null;
  }
}

function ProfileEdit() {
  const { state, update } = useProduct(); const [profile, setProfile] = useState(state.profile); const [saved, setSaved] = useState(false);
  const set = (key: keyof typeof profile, value: string | string[] | boolean) => setProfile(current => ({ ...current, [key]: value }));
  return <><ProductSection title="Public details"><ProductCard><Field label="Display name" value={profile.displayName} onChangeText={value => set('displayName', value)} /><Field label="Username" value={profile.username} onChangeText={value => set('username', value.replace(/\s/g, ''))} /><Field label="Bio" value={profile.bio} onChangeText={value => set('bio', value)} multiline /><View style={styles.two}><View style={styles.flex}><Field label="City" value={profile.city} onChangeText={value => set('city', value)} /></View><View style={styles.flex}><Field label="Country" value={profile.country} onChangeText={value => set('country', value)} /></View></View><Field label="Languages (comma separated)" value={profile.languages.join(', ')} onChangeText={value => set('languages', value.split(',').map(item => item.trim()).filter(Boolean))} /><Toggle label="Private profile" detail="Only approved followers can see your posts." value={profile.privateProfile} onChange={value => set('privateProfile', value)} /></ProductCard></ProductSection>{saved && <Success text="Profile changes saved on this device." />}<AppButton label="Save profile" onPress={() => { update(current => ({ ...current, profile })); setSaved(true); }} /></>;
}

function Saved() {
  const { state, toggleList } = useProduct();
  const savedArticles = professionalArticles.filter(item => state.savedArticles.includes(item.id)); const savedMedia = mediaPicks.filter(item => state.savedMedia.includes(item.id)); const savedPosts = [...mockPosts.map(item => ({ id: item.id, body: item.body })), ...state.posts.map(item => ({ id: item.id, body: item.body }))].filter(item => state.savedPosts.includes(item.id));
  return <><Notice title="Your private collection" body="Saved items are visible only to you in this prototype and stay available after refresh." /><ProductSection title="Posts" subtitle={`${savedPosts.length} saved`}>{savedPosts.length ? savedPosts.map(item => <ActionRow key={item.id} icon="bookmark" title={item.body.slice(0, 72)} subtitle="Saved community story" onPress={() => router.push({ pathname: '/post/[id]', params: { id: item.id } })} />) : <EmptyState icon="bookmark_border" title="No saved posts yet" body="Bookmark a story from the feed to keep it here." />}</ProductSection><ProductSection title="Articles" subtitle={`${savedArticles.length} saved`}>{savedArticles.length ? savedArticles.map(item => <ActionRow key={item.id} icon="article" title={item.title} subtitle={item.summary} onPress={() => router.push({ pathname: '/article/[id]', params: { id: item.id } })} />) : <EmptyState icon="bookmark_border" title="No saved articles yet" body="Open Knowledge and bookmark anything you want to revisit." />}</ProductSection><ProductSection title="Media" subtitle={`${savedMedia.length} saved`}>{savedMedia.map(item => <ActionRow key={item.id} icon="movie" title={item.title} subtitle={item.description} value="Saved" onPress={() => toggleList('savedMedia', item.id)} />)}{!savedMedia.length && <EmptyState icon="subscriptions" title="No saved media yet" body="Save a film, series, podcast, or book from the Media guide." />}</ProductSection></>;
}

function Connections() {
  const { state, toggleList } = useProduct(); const people = ['jamie.grows', 'alex.notes', 'priya.daybyday'];
  return <><ProductSection title="People you follow">{people.map(username => <ActionRow key={username} icon="person" title={`@${username}`} subtitle="Lived-experience community member" value={state.followingUsernames.includes(username) ? 'Following' : 'Follow'} onPress={() => toggleList('followingUsernames', username)} />)}</ProductSection><ProductSection title="Professionals">{professionals.map(pro => <ActionRow key={pro.id} icon="verified" title={pro.displayName} subtitle={`${pro.title} · ${pro.country}`} value={state.followedProfessionals.includes(pro.id) ? 'Following' : 'Follow'} onPress={() => toggleList('followedProfessionals', pro.id)} />)}</ProductSection></>;
}

function NotificationSettings() {
  const { state, update } = useProduct(); const set = (key: keyof typeof state.notifications, value: boolean) => update(current => ({ ...current, notifications: { ...current.notifications, [key]: value } }));
  return <ProductCard>{([['messages','Direct messages','Private conversations and requests'],['replies','Replies & mentions','Activity on your posts and comments'],['follows','New followers','People and professionals who connect'],['groups','Community activity','Events, announcements, and moderation'],['articles','Knowledge updates','New articles from followed professionals'],['quietHours','Quiet hours','Pause non-urgent notifications overnight']] as const).map(([key,label,detail]) => <Toggle key={key} label={label} detail={detail} value={state.notifications[key]} onChange={value => set(key, value)} />)}</ProductCard>;
}

function AccountSecurity() {
  const [current, setCurrent] = useState(''); const [next, setNext] = useState(''); const [confirm, setConfirm] = useState(''); const [message, setMessage] = useState('');
  const save = () => setMessage(next.length < 8 ? 'Use at least 8 characters.' : next !== confirm ? 'New passwords do not match.' : 'Password updated in demo mode. A production build will confirm this with the server.');
  return <><Notice icon="shield" title="Security baseline" body="Use a unique password. Production sessions will also support email verification, rate limiting, and optional multi-factor authentication." /><ProductCard><Field label="Current password" value={current} onChangeText={setCurrent} secure /><Field label="New password" value={next} onChangeText={setNext} secure /><Field label="Confirm new password" value={confirm} onChangeText={setConfirm} secure /><AppButton label="Update password" onPress={save} />{!!message && (message.startsWith('Password') ? <Success text={message} /> : <ErrorText text={message} />)}</ProductCard><ActionRow icon="devices" title="Active sessions" subtitle="Review devices and sign out remotely" onPress={() => nav('session-management')} /><ActionRow icon="download" title="Download my data" subtitle="Request a portable account archive" onPress={() => nav('data-export')} /></>;
}

function PrivacyData() {
  const { state, update } = useProduct(); const [requested, setRequested] = useState(false);
  return <><ProductCard><Toggle label="Private profile" detail="Approve followers before they see profile activity." value={state.profile.privateProfile} onChange={value => update(current => ({ ...current, profile: { ...current.profile, privateProfile: value } }))} /><ActionRow icon="block" title="Blocked accounts" subtitle={`${state.blockedAccounts.length} accounts blocked`} onPress={() => nav('blocked')} /><ActionRow icon="manage_search" title="Search history" subtitle="Stored locally in this prototype" onPress={() => nav('search-history')} /></ProductCard><ProductSection title="Data rights"><ProductCard><Text style={styles.body}>You can access, correct, export, or request deletion of your account data. Production requests require identity confirmation before delivery.</Text><AppButton label={requested ? 'Export requested' : 'Request data export'} onPress={() => setRequested(true)} /></ProductCard></ProductSection>{requested && <Success text="Your demo request is queued. The real service will email a secure download link when the backend is connected." />}</>;
}

function Blocked() {
  const { state, toggleList } = useProduct();
  return <>{state.blockedAccounts.length ? state.blockedAccounts.map(account => <ActionRow key={account} icon="block" title={`@${account}`} subtitle="Cannot message, mention, or follow you" value="Unblock" onPress={() => toggleList('blockedAccounts', account)} />) : <EmptyState icon="verified_user" title="No blocked accounts" body="Accounts you block will appear here." />}</>;
}

function SafetyCenter() {
  return <><Notice icon="health_and_safety" title="Safety is a product feature" body="Control what you see, report harmful behavior, and reach urgent help without hunting through menus." /><ProductSection title="Your controls"><ActionRow icon="tune" title="Content & contact controls" subtitle="Sensitive content, muted words, and messages" onPress={() => router.push('/safety-settings')} /><ActionRow icon="block" title="Blocked accounts" subtitle="Review and unblock accounts" onPress={() => nav('blocked')} /><ActionRow icon="flag" title="My reports" subtitle="See status and moderation outcomes" onPress={() => nav('my-reports')} /></ProductSection><ProductSection title="Get help"><ActionRow icon="emergency" title="Urgent help" subtitle="Country-aware emergency and crisis routes" danger onPress={() => nav('urgent-help')} /><ActionRow icon="support_agent" title="Talk with support" subtitle="Listener, peer, or verified professional" onPress={() => router.push('/support')} /><ActionRow icon="gavel" title="Community guidelines" subtitle="What GreenOcean permits and why" onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'guidelines' } } as never)} /></ProductSection></>;
}

function MyReports() {
  const { state } = useProduct();
  return <>{state.reports.length ? state.reports.map(report => <ProductCard key={report.id}><View style={styles.between}><Text style={styles.cardTitle}>{report.reason}</Text><Pill label={report.status} active={report.status === 'RESOLVED'} /></View><Text style={styles.body}>{report.targetType} · {report.targetId}</Text><Text style={styles.meta}>{new Date(report.createdAt).toLocaleString()}</Text>{report.outcome && <Notice title="Outcome" body={report.outcome} />}</ProductCard>) : <EmptyState icon="flag" title="No reports submitted" body="If you report content or behavior, its status and outcome will appear here." />}</>;
}

function ReportForm({ targetType, targetId }: { targetType: string; targetId: string }) {
  const { addReport } = useProduct(); const [reason, setReason] = useState('Harassment or bullying'); const [details, setDetails] = useState(''); const [done, setDone] = useState(false);
  const reasons = ['Harassment or bullying', 'Dangerous or misleading advice', 'Hate or discrimination', 'Spam or scam', 'Self-harm risk', 'Privacy violation'];
  if (done) return <><Success text="Report submitted. You can track its status in My reports." /><AppButton label="View my reports" onPress={() => nav('my-reports')} /></>;
  return <><Notice title="Report context" body={`${targetType}: ${targetId}. The reported account is not told who submitted the report.`} /><ProductCard><Text style={styles.label}>What is happening?</Text><View style={styles.wrap}>{reasons.map(item => <Pill key={item} label={item} active={reason === item} onPress={() => setReason(item)} />)}</View><Field label="Details for the safety team" value={details} onChangeText={setDetails} multiline placeholder="What happened, and when? Avoid adding unrelated personal information." /><AppButton label="Submit confidential report" onPress={() => { addReport(targetType, targetId, reason, details.trim()); setDone(true); }} /></ProductCard></>;
}

function UrgentHelp() {
  const [region, setRegion] = useState('United States');
  const resources = region === 'United Kingdom' ? [{ title: 'Samaritans', detail: 'Call 116 123, 24/7', url: 'tel:116123' }, { title: 'NHS urgent mental health help', detail: 'Find local urgent support', url: 'https://www.nhs.uk/nhs-services/mental-health-services/where-to-get-urgent-help-for-mental-health/' }] : region === 'Canada' ? [{ title: '9-8-8 Suicide Crisis Helpline', detail: 'Call or text 9-8-8, 24/7', url: 'tel:988' }, { title: 'Talk Suicide Canada', detail: 'Official crisis support information', url: 'https://988.ca/' }] : [{ title: '988 Suicide & Crisis Lifeline', detail: 'Call or text 988, 24/7 in the U.S.', url: 'tel:988' }, { title: '988 Lifeline', detail: 'Chat and accessibility options', url: 'https://988lifeline.org/' }];
  return <><Notice tone="warning" icon="emergency" title="If someone is in immediate danger" body="Call the local emergency number now or go to the nearest emergency department. GreenOcean cannot monitor this page or dispatch help." /><Text style={styles.label}>Choose your location</Text><View style={styles.wrap}>{['United States','Canada','United Kingdom'].map(item => <Pill key={item} label={item} active={region === item} onPress={() => setRegion(item)} />)}</View>{resources.map(item => <ActionRow key={item.title} icon="call" title={item.title} subtitle={item.detail} onPress={() => void Linking.openURL(item.url)} />)}<ProductCard tone="soft"><Text style={styles.cardTitle}>Elsewhere in the world</Text><Text style={styles.body}>Use your local emergency number or a verified national crisis service. The production app must localize and clinically review this directory before launch in each country.</Text><AppButton label="Browse Befrienders Worldwide" variant="secondary" onPress={() => void Linking.openURL('https://befrienders.org/')} /></ProductCard></>;
}

function MessageRequests() {
  const { state, update } = useProduct();
  return <><ProductCard><Toggle label="Allow message requests" detail="People you do not follow can request a conversation." value={state.safety.messageRequests} onChange={value => update(current => ({ ...current, safety: { ...current.safety, messageRequests: value } }))} /><Toggle label="Professionals only" detail="Limit new requests to verified professionals." value={state.safety.professionalsOnly} onChange={value => update(current => ({ ...current, safety: { ...current.safety, professionalsOnly: value } }))} /></ProductCard><ProductSection title="Pending requests"><ProductCard><View style={styles.between}><View style={styles.flex}><Text style={styles.cardTitle}>Avery · trained listener</Text><Text style={styles.body}>Responding to your request about work stress.</Text></View><Pill label="Review" active onPress={() => router.push('/chat/jamie')} /></View></ProductCard></ProductSection></>;
}

function NewMessage() {
  const [query, setQuery] = useState(''); const people = ['Jamie · peer connection', 'Dr. Maya Bennett · psychologist', 'Avery · trained listener'].filter(item => item.toLowerCase().includes(query.toLowerCase()));
  return <><Field label="Find a person" value={query} onChangeText={setQuery} placeholder="Search connections" />{people.map((person, index) => <ActionRow key={person} icon={index === 1 ? 'verified' : 'person'} title={person.split(' · ')[0]} subtitle={person.split(' · ')[1]} onPress={() => router.push('/chat/jamie')} />)}<Notice title="Private conversation boundaries" body="Do not share financial details, passwords, or identifying medical documents in chat. Report pressure, threats, or unsafe advice." /></>;
}

function CommunityCreate() {
  const { state, patch, toggleList } = useProduct(); const [name, setName] = useState(''); const [description, setDescription] = useState(''); const [privateCommunity, setPrivate] = useState(false); const [error, setError] = useState(''); const [created, setCreated] = useState('');
  const create = () => { if (name.trim().length < 3 || description.trim().length < 20) return setError('Add a clear name and a description of at least 20 characters.'); const slug = name.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''); patch('localCommunities', [{ id: id('community'), slug, name: name.trim(), description: description.trim(), privateCommunity, createdAt: new Date().toISOString() }, ...state.localCommunities]); if (!state.joinedCommunities.includes(slug)) toggleList('joinedCommunities', slug); setCreated(slug); };
  if (created) return <><Success text="Community created with you as owner and moderator." /><AppButton label="Open my community" onPress={() => router.replace({ pathname: '/community/[slug]', params: { slug: created } })} /></>;
  return <><Notice title="Moderation commitment" body="Creators define rules, appoint moderators, respond to reports, and keep promotional or clinical claims out of peer spaces." /><ProductCard><Field label="Community name" value={name} onChangeText={setName} placeholder="Example: Gentle Mornings" /><Field label="Purpose and who it is for" value={description} onChangeText={setDescription} multiline /><Toggle label="Private community" detail="People request access before reading or posting." value={privateCommunity} onChange={setPrivate} />{!!error && <ErrorText text={error} />}<AppButton label="Create community" onPress={create} /></ProductCard></>;
}

function CommunityRules({ slug }: { slug: string }) {
  const community = mockCommunities.find(item => item.slug === slug); const rules = ['Share lived experience; do not diagnose other members.', 'No graphic instructions, threats, harassment, or hate.', 'Do not promote medication changes or products as treatment.', 'Protect privacy: no names, screenshots, or identifying details.', 'Use content notes for potentially distressing experiences.'];
  return <><Notice title={community?.name || 'Community rules'} body="Moderators may add context-specific rules, but GreenOcean-wide safety standards always apply." />{rules.map((rule, index) => <ProductCard key={rule}><View style={styles.number}><Text style={styles.numberText}>{index + 1}</Text></View><Text style={[styles.body, styles.flex]}>{rule}</Text></ProductCard>)}<ActionRow icon="flag" title="Report a rules concern" subtitle="Send it confidentially to moderators" onPress={() => nav('report', { targetType: 'community', targetId: slug })} /></>;
}

function CommunityMembers() {
  return <><ProductSection title="Moderators"><ActionRow icon="shield" title="Morgan Lee" subtitle="Lead moderator · safety trained" /><ActionRow icon="shield" title="Dr. Maya Bennett" subtitle="Professional adviser · verified" /></ProductSection><ProductSection title="Members"><ActionRow icon="person" title="Jamie" subtitle="Member since March 2025" /><ActionRow icon="person" title="Priya" subtitle="Peer welcome volunteer" /><ActionRow icon="person" title="Alex" subtitle="Member since January 2026" /></ProductSection></>;
}

function CommunityEvents() {
  const [joined, setJoined] = useState<string[]>([]); const events = [{ id:'e1', title:'Gentle sleep reset', detail:'Today · 7:00 PM · text circle' },{ id:'e2', title:'Ask a psychologist: worry loops', detail:'Thursday · 6:30 PM · moderated Q&A' },{ id:'e3', title:'Quiet co-journaling', detail:'Saturday · 10:00 AM · 30 minutes' }];
  return <>{events.map(event => <ProductCard key={event.id}><Text style={styles.cardTitle}>{event.title}</Text><Text style={styles.body}>{event.detail}</Text><AppButton label={joined.includes(event.id) ? 'Added to my events' : 'Join event'} variant={joined.includes(event.id) ? 'secondary' : 'primary'} onPress={() => setJoined(current => current.includes(event.id) ? current.filter(item => item !== event.id) : [...current, event.id])} /></ProductCard>)}</>;
}

function WellbeingHub() {
  return <><Notice title="Private by design" body="These tools are for reflection and skill practice. They do not diagnose conditions or replace professional care." /><ProductSection title="Today"><ActionRow icon="mood" title="Mood check-in" subtitle="Name the moment in under a minute" onPress={() => nav('check-ins')} /><ActionRow icon="flag" title="Gentle goals" subtitle="Small steps and progress" onPress={() => nav('goals')} /><ActionRow icon="book" title="Private journal" subtitle="Write without publishing" onPress={() => nav('journal')} /></ProductSection><ProductSection title="Learn and reflect"><ActionRow icon="quiz" title="Wellbeing reflection" subtitle="A non-diagnostic support check" onPress={() => nav('assessments')} /><ActionRow icon="route" title="Guided growth paths" subtitle="Short skill-building journeys" onPress={() => nav('growth-paths')} /><ActionRow icon="auto_awesome" title="Reflection companion" subtitle="Guided prompts with clear boundaries" onPress={() => nav('companion')} /></ProductSection></>;
}

function CheckIns() {
  const { state, patch } = useProduct(); const [mood, setMood] = useState('Calm'); const [note, setNote] = useState(''); const [done, setDone] = useState(false); const moods = ['Calm','Hopeful','Low','Anxious','Overwhelmed'];
  const add = () => { patch('checkIns', [{ id: id('checkin'), mood, note: note.trim(), createdAt: new Date().toISOString() }, ...state.checkIns]); setNote(''); setDone(true); };
  return <><ProductCard tone="soft"><Text style={styles.label}>How are you feeling?</Text><View style={styles.wrap}>{moods.map(item => <Pill key={item} label={item} active={mood === item} onPress={() => setMood(item)} />)}</View><Field label="A note for yourself (optional)" value={note} onChangeText={setNote} multiline /><AppButton label="Save check-in" onPress={add} /></ProductCard>{done && <Success text="Check-in saved privately on this device." />}<ProductSection title="Recent check-ins">{state.checkIns.length ? state.checkIns.map(item => <ProductCard key={item.id}><View style={styles.between}><Text style={styles.cardTitle}>{item.mood}</Text><Text style={styles.meta}>{new Date(item.createdAt).toLocaleDateString()}</Text></View>{!!item.note && <Text style={styles.body}>{item.note}</Text>}</ProductCard>) : <EmptyState icon="mood" title="Your first check-in starts here" body="There is no good or bad score—just a moment worth noticing." />}</ProductSection></>;
}

function Goals() {
  const { state, patch } = useProduct(); const [title, setTitle] = useState(''); const [cadence, setCadence] = useState('Daily');
  const add = () => { if (!title.trim()) return; patch('goals', [{ id: id('goal'), title: title.trim(), cadence, progress: 0, completed: false }, ...state.goals]); setTitle(''); };
  const progress = (goalId: string) => patch('goals', state.goals.map(goal => goal.id === goalId ? { ...goal, progress: goal.progress + 1, completed: goal.progress >= 6 } : goal));
  return <><ProductCard><Field label="A small, specific step" value={title} onChangeText={setTitle} placeholder="Example: Step outside for five minutes" /><View style={styles.wrap}>{['Daily','3× weekly','Weekly'].map(item => <Pill key={item} label={item} active={cadence === item} onPress={() => setCadence(item)} />)}</View><AppButton label="Add goal" onPress={add} /></ProductCard>{state.goals.map(goal => <ProductCard key={goal.id}><View style={styles.between}><View style={styles.flex}><Text style={styles.cardTitle}>{goal.title}</Text><Text style={styles.body}>{goal.cadence} · {goal.progress} check-ins</Text></View><Pill label={goal.completed ? 'Completed' : '+ Done today'} active={goal.completed} onPress={() => !goal.completed && progress(goal.id)} /></View></ProductCard>)}</>;
}

function Journal() {
  const { state, patch } = useProduct(); const [title, setTitle] = useState(''); const [body, setBody] = useState(''); const [mood, setMood] = useState('Reflective');
  const add = () => { if (!body.trim()) return; patch('journal', [{ id: id('journal'), title: title.trim() || 'Untitled reflection', body: body.trim(), mood, createdAt: new Date().toISOString() }, ...state.journal]); setTitle(''); setBody(''); };
  return <><Notice icon="lock" title="Private—not a post" body="Journal entries stay in your private local data. Use Create if you decide to share something with the community." /><ProductCard><Field label="Title" value={title} onChangeText={setTitle} /><Field label="What feels important to name?" value={body} onChangeText={setBody} multiline /><View style={styles.wrap}>{['Reflective','Heavy','Grateful','Unclear'].map(item => <Pill key={item} label={item} active={mood === item} onPress={() => setMood(item)} />)}</View><AppButton label="Save privately" onPress={add} /></ProductCard>{state.journal.map(entry => <ProductCard key={entry.id}><View style={styles.between}><Text style={styles.cardTitle}>{entry.title}</Text><Pill label={entry.mood} /></View><Text style={styles.body}>{entry.body}</Text><Text style={styles.meta}>{new Date(entry.createdAt).toLocaleString()}</Text></ProductCard>)}</>;
}

function Assessment() {
  const questions = ['I have felt able to pause when stress rises.', 'I have felt connected to at least one person.', 'My sleep has felt restorative enough.', 'Daily responsibilities have felt manageable.', 'I have had moments of interest or hope.']; const [answers, setAnswers] = useState<number[]>(Array(5).fill(0)); const [done, setDone] = useState(false); const score = answers.reduce((sum, value) => sum + value, 0);
  if (done) return <><Notice title="Your reflection summary" body={score >= 15 ? 'Several areas feel supported right now. Keep the routines and relationships that help.' : score >= 8 ? 'Some areas may need more support. A small goal, peer conversation, or professional directory may help.' : 'You reported strain across several areas. Consider reaching out to someone you trust or a verified professional. If you may be in danger, use urgent help now.'} tone={score < 8 ? 'warning' : 'ocean'} /><ProductCard><Text style={styles.bigScore}>{score}/20</Text><Text style={styles.body}>This is a GreenOcean reflection score, not a validated clinical scale, diagnosis, or risk assessment.</Text></ProductCard><ActionRow icon="support_agent" title="Choose support now" onPress={() => router.push('/support')} /><ActionRow icon="emergency" title="Urgent help" danger onPress={() => nav('urgent-help')} /><AppButton label="Take again" variant="secondary" onPress={() => setDone(false)} /></>;
  return <><Notice title="Important boundary" body="Answer for the past 7 days. This tool cannot detect or diagnose a mental health condition." />{questions.map((question, index) => <ProductCard key={question}><Text style={styles.cardTitle}>{index + 1}. {question}</Text><View style={styles.wrap}>{['Not at all','Some days','Often','Most days','Nearly always'].map((label, value) => <Pill key={label} label={label} active={answers[index] === value} onPress={() => setAnswers(current => current.map((item, i) => i === index ? value : item))} />)}</View></ProductCard>)}<AppButton label="See reflection" onPress={() => setDone(true)} /></>;
}

function GrowthPaths() {
  const { state, toggleList } = useProduct(); const paths = [{ id:'grounding', title:'Grounding through anxious moments', detail:'5 lessons · 28 minutes' },{ id:'sleep', title:'Build a gentler sleep rhythm', detail:'7 lessons · 42 minutes' },{ id:'boundaries', title:'Boundaries without guilt', detail:'6 lessons · 35 minutes' }];
  return <>{paths.map(path => { const complete = state.completedPaths.includes(path.id); return <ProductCard key={path.id}><View style={styles.between}><View style={styles.flex}><Text style={styles.cardTitle}>{path.title}</Text><Text style={styles.body}>{path.detail}</Text></View><Pill label={complete ? 'Completed' : 'Start'} active={complete} onPress={() => toggleList('completedPaths', path.id)} /></View><Text style={styles.meta}>{complete ? 'Saved to your progress' : 'Reflection · practice · community prompt'}</Text></ProductCard>; })}<Notice title="Evidence and review" body="Production learning paths need named authors, citations, review dates, and professional editorial approval." /></>;
}

function Companion() {
  const prompts = ['What happened just before this feeling showed up?', 'What would you say to a friend in the same moment?', 'What is one need underneath this reaction?', 'What is a small next step that does not require fixing everything?']; const [index, setIndex] = useState(0); const [answer, setAnswer] = useState(''); const [notes, setNotes] = useState<string[]>([]);
  return <><Notice title="Clear boundary" body="This companion follows fixed reflection prompts. It does not understand emergencies, provide therapy, or make clinical recommendations." /><ProductCard tone="soft"><Text style={styles.eyebrow}>PROMPT {index + 1} OF {prompts.length}</Text><Text style={styles.prompt}>{prompts[index]}</Text><Field label="Your private reflection" value={answer} onChangeText={setAnswer} multiline /><AppButton label={index === prompts.length - 1 ? 'Finish reflection' : 'Save and continue'} onPress={() => { setNotes(current => [...current, answer]); setAnswer(''); setIndex(current => current === prompts.length - 1 ? 0 : current + 1); }} /></ProductCard>{notes.length > 0 && <Success text={`${notes.length} reflection ${notes.length === 1 ? 'step' : 'steps'} completed in this session.`} />}<ActionRow icon="emergency" title="I need urgent help" danger onPress={() => nav('urgent-help')} /></>;
}

function SearchHistory() {
  const { state, patch } = useProduct();
  return <>{state.searchHistory.length ? <><ProductCard>{state.searchHistory.map(term => <View key={term} style={styles.history}><AppIcon name="history" color={colors.muted} /><Text style={styles.body}>{term}</Text></View>)}</ProductCard><AppButton label="Clear search history" variant="secondary" onPress={() => patch('searchHistory', [])} /></> : <EmptyState icon="manage_search" title="No recent searches" body="Searches made after this update will appear here and stay on this device." />}</>;
}

function Sessions() {
  const [signedOut, setSignedOut] = useState(false);
  return <><ProductCard><View style={styles.between}><View style={styles.flex}><Text style={styles.cardTitle}>Windows · Chrome</Text><Text style={styles.body}>Current session · Tehran · active now</Text></View><Pill label="Current" active /></View></ProductCard>{!signedOut ? <ProductCard><View style={styles.between}><View style={styles.flex}><Text style={styles.cardTitle}>Android · GreenOcean</Text><Text style={styles.body}>Portland · last active 4 days ago</Text></View><Pill label="Sign out" onPress={() => setSignedOut(true)} /></View></ProductCard> : <Success text="The other demo session was signed out." />}<Notice title="Location can be approximate" body="Session locations are inferred from network information and may not match a precise physical location." /></>;
}

function DataExport() {
  const { state } = useProduct(); const [ready, setReady] = useState(false); const summary = `${state.posts.length} posts, ${state.comments.length} comments, ${state.journal.length} journal entries, ${state.reports.length} reports`;
  return <><ProductCard><Text style={styles.cardTitle}>What your archive includes</Text><Text style={styles.body}>Profile, posts, comments, community membership, saved items, private wellbeing data, reports, and account settings.</Text><Text style={styles.meta}>{summary}</Text><AppButton label={ready ? 'Share export summary' : 'Prepare export'} onPress={() => ready ? void Share.share({ message: `GreenOcean demo export summary: ${summary}` }) : setReady(true)} /></ProductCard>{ready && <Success text="Demo export prepared. Production exports will use an encrypted, expiring download after identity verification." />}</>;
}

function Field({ label, value, onChangeText, multiline = false, placeholder, secure = false }: { label: string; value: string; onChangeText: (value: string) => void; multiline?: boolean; placeholder?: string; secure?: boolean }) { return <View style={styles.field}><Text style={styles.fieldLabel}>{label}</Text><TextInput value={value} onChangeText={onChangeText} multiline={multiline} secureTextEntry={secure} placeholder={placeholder} placeholderTextColor={colors.muted} style={[styles.input, multiline && styles.multiline]} /></View>; }
function Toggle({ label, detail, value, onChange }: { label: string; detail: string; value: boolean; onChange: (value: boolean) => void }) { return <View style={styles.toggle}><View style={styles.flex}><Text style={styles.cardTitle}>{label}</Text><Text style={styles.body}>{detail}</Text></View><Switch value={value} onValueChange={onChange} trackColor={{ true: colors.ocean500 }} /></View>; }
function Success({ text }: { text: string }) { return <View style={styles.success}><AppIcon name="check_circle" filled color={colors.ocean600} /><Text style={[styles.body, styles.flex]}>{text}</Text></View>; }
function ErrorText({ text }: { text: string }) { return <Text style={styles.error}>{text}</Text>; }

const styles = StyleSheet.create({
  flex: { flex: 1 }, two: { flexDirection: 'row', gap: spacing.sm, flexWrap: 'wrap' }, between: { flexDirection: 'row', gap: spacing.md, alignItems: 'center', justifyContent: 'space-between' }, wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, field: { gap: 6 }, fieldLabel: { color: colors.ink, fontSize: 11, fontWeight: '900' }, input: { minHeight: 48, borderWidth: 1, borderColor: colors.borderStrong, borderRadius: radius.md, backgroundColor: colors.white, paddingHorizontal: spacing.md, color: colors.ink, textAlign: 'left', writingDirection: 'ltr' }, multiline: { minHeight: 112, paddingTop: spacing.md, textAlignVertical: 'top' }, toggle: { minHeight: 64, flexDirection: 'row', alignItems: 'center', gap: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border, paddingVertical: spacing.sm }, label: { color: colors.ink, fontSize: 12, fontWeight: '900' }, cardTitle: { color: colors.ink, fontSize: 12, lineHeight: 18, fontWeight: '900' }, body: { color: colors.inkSoft, fontSize: 10, lineHeight: 17 }, meta: { color: colors.muted, fontSize: 9, lineHeight: 14 }, success: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, padding: spacing.md, borderRadius: radius.md, backgroundColor: colors.ocean100, borderWidth: 1, borderColor: colors.ocean200 }, error: { color: colors.danger, fontSize: 11, fontWeight: '800' }, number: { width: 28, height: 28, borderRadius: 14, backgroundColor: colors.ocean100, alignItems: 'center', justifyContent: 'center' }, numberText: { color: colors.ocean700, fontWeight: '900' }, bigScore: { color: colors.ocean700, fontSize: 36, fontWeight: '900', textAlign: 'center' }, eyebrow: { color: colors.ocean600, fontSize: 9, fontWeight: '900', letterSpacing: 1 }, prompt: { color: colors.ink, fontSize: 20, lineHeight: 29, fontWeight: '900' }, history: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.sm },
});
