import { Redirect, Tabs, router } from 'expo-router';
import { useEffect, useState } from 'react';
import { apiRequest } from '@/lib/api/client';
import { publicCopy } from '@/localization/public-copy';
import { AppButton } from '@/components/app-button';
import { Platform, StyleSheet, Text, View } from 'react-native';
import { AppIcon } from '@/components/app-icon';
import { useAuth } from '@/features/auth/auth-provider';
import { usePlatformData } from '@/features/platform/data-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, shadow } from '@/theme/tokens';
import { needsOnboarding } from '@/features/platform/onboarding-policy';

const icon = (name: string, focused: boolean) => <AppIcon name={name} filled={focused} size={25} color={focused ? colors.ocean700 : colors.muted} />;
export default function AppLayout() {
  const { status, user } = useAuth(); const { t, language } = useLanguage();
  const { preferences } = usePlatformData();
  const [allowed, setAllowed] = useState<boolean|null>(null);
  const [error, setError] = useState('');
  async function checkAccess() {
    try { const result = await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity'); setAllowed(result.services.COMMUNITY); setError(''); }
    catch { setError('Could not check account access. Please retry.'); }
  }
  useEffect(() => {
    if (status !== 'authenticated') return;
    let active = true;
    apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity')
      .then(result => { if (active) { setAllowed(result.services.COMMUNITY); setError(''); } })
      .catch(() => { if (active) setError('Could not check account access. Please retry.'); });
    return () => { active = false; };
  }, [status,user?.userId]);
  if(status==='loading')return <View style={{padding:24,gap:16}}><Text>{publicCopy[language].checking}</Text><AppButton label={publicCopy[language].urgent} onPress={()=>router.push('/(auth)/urgent-help')}/></View>;
  if (status === 'authenticated' && allowed === false) return <Redirect href="/(auth)/activate" />;
  if (status === 'authenticated' && allowed === null) return <View style={{padding:24}}><Text>{error || 'Checking account access…'}</Text>{!!error && <AppButton label="Retry" onPress={()=>void checkAccess()} />}</View>;
  if (status === 'unauthenticated') return <Redirect href="/(auth)/welcome" />;
  if (status === 'authenticated' && preferences && needsOnboarding(preferences)) return <Redirect href="/(auth)/onboarding" />;
  return <Tabs screenOptions={{ headerShown: false, tabBarActiveTintColor: colors.ocean700, tabBarInactiveTintColor: colors.muted, tabBarLabelStyle: styles.label, tabBarStyle: styles.bar, tabBarItemStyle: styles.item }}>
    <Tabs.Screen name="index" options={{ title: t('feed'), tabBarIcon: ({ focused }) => icon('home', focused) }} />
    <Tabs.Screen name="search" options={{ title: t('search'), tabBarIcon: ({ focused }) => icon('explore', focused) }} />
    <Tabs.Screen name="create" options={{ title: '', tabBarIcon: () => <View style={styles.create}><AppIcon name="add" size={29} color={colors.white} /></View> }} />
    <Tabs.Screen name="messages" options={{ title: 'Messages', tabBarIcon: ({ focused }) => icon('chat_bubble', focused) }} />
    <Tabs.Screen name="profile" options={{ title: t('profile'), tabBarIcon: ({ focused }) => icon('person', focused) }} />
    <Tabs.Screen name="privacy" options={{href:null}} /><Tabs.Screen name="notifications" options={{ href: null }} /><Tabs.Screen name="communities" options={{ href: null }} /><Tabs.Screen name="channels" options={{ href: null }} /><Tabs.Screen name="support" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="professionals" options={{ href: null }} /><Tabs.Screen name="safety-settings" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="articles" options={{ href: null }} /><Tabs.Screen name="media" options={{ href: null }} /><Tabs.Screen name="article-editor" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="article/[id]" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="admin" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="post/[id]" options={{ href: null }} /><Tabs.Screen name="community/[slug]" options={{ href: null }} /><Tabs.Screen name="chat/[id]" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="tools/[tool]" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="professional/[id]/[action]" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="listener/[action]" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="admin-tools/[section]" options={{ href: null, tabBarStyle: { display: 'none' } }} />
    <Tabs.Screen name="user/[username]" options={{ href: null, tabBarStyle: { display: 'none' } }} />
    <Tabs.Screen name="community-review" options={{ href: null, tabBarStyle: { display: 'none' } }} />
    <Tabs.Screen name="content-decisions" options={{ href: null }} />
    <Tabs.Screen name="group-workspace" options={{href:null}} /><Tabs.Screen name="group-review" options={{href:null}} /><Tabs.Screen name="group-safety-review" options={{href:null}} />
    <Tabs.Screen name="professional-rankings" options={{href:null}}/><Tabs.Screen name="clinic-reputation" options={{href:null}}/><Tabs.Screen name="clinic-reputation-operations" options={{href:null}}/><Tabs.Screen name="clinical-governance" options={{href:null,tabBarStyle:{display:'none'}}}/><Tabs.Screen name="clinical/[appointment]" options={{href:null,tabBarStyle:{display:'none'}}}/><Tabs.Screen name="clinic" options={{href:null}} />
    <Tabs.Screen name="clinic-followup" options={{href:null}}/><Tabs.Screen name="clinic-operations" options={{href:null}}/><Tabs.Screen name="clinic-finance-operations" options={{href:null}} />
    <Tabs.Screen name="my-posts" options={{ href: null }} />
    <Tabs.Screen name="message-requests" options={{ href: null }} />
  </Tabs>;
}
const styles = StyleSheet.create({ bar: { height: Platform.OS === 'ios' ? 88 : 70, paddingTop: 8, backgroundColor: colors.white, borderTopColor: colors.border, ...shadow.soft }, item: { paddingVertical: 2 }, label: { fontSize: 10, fontWeight: '700', paddingBottom: 4 }, create: { width: 48, height: 48, marginTop: -15, borderRadius: radius.pill, backgroundColor: colors.ocean600, alignItems: 'center', justifyContent: 'center', ...shadow.floating } });
