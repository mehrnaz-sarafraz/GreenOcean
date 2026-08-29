import { Redirect, Tabs } from 'expo-router';
import { Platform, StyleSheet, View } from 'react-native';
import { AppIcon } from '@/components/app-icon';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { colors, radius, shadow } from '@/theme/tokens';

const icon = (name: string, focused: boolean) => <AppIcon name={name} filled={focused} size={25} color={focused ? colors.ocean700 : colors.muted} />;
export default function AppLayout() {
  const { status } = useAuth(); const { t } = useLanguage();
  if (status === 'unauthenticated') return <Redirect href="/(auth)/welcome" />;
  return <Tabs screenOptions={{ headerShown: false, tabBarActiveTintColor: colors.ocean700, tabBarInactiveTintColor: colors.muted, tabBarLabelStyle: styles.label, tabBarStyle: styles.bar, tabBarItemStyle: styles.item }}>
    <Tabs.Screen name="index" options={{ title: t('feed'), tabBarIcon: ({ focused }) => icon('home', focused) }} />
    <Tabs.Screen name="search" options={{ title: t('search'), tabBarIcon: ({ focused }) => icon('explore', focused) }} />
    <Tabs.Screen name="create" options={{ title: '', tabBarIcon: () => <View style={styles.create}><AppIcon name="add" size={29} color={colors.white} /></View> }} />
    <Tabs.Screen name="messages" options={{ title: 'Messages', tabBarIcon: ({ focused }) => icon('chat_bubble', focused) }} />
    <Tabs.Screen name="profile" options={{ title: t('profile'), tabBarIcon: ({ focused }) => icon('person', focused) }} />
    <Tabs.Screen name="notifications" options={{ href: null }} /><Tabs.Screen name="communities" options={{ href: null }} /><Tabs.Screen name="channels" options={{ href: null }} /><Tabs.Screen name="articles" options={{ href: null }} /><Tabs.Screen name="media" options={{ href: null }} /><Tabs.Screen name="article-editor" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="article/[id]" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="admin" options={{ href: null, tabBarStyle: { display: 'none' } }} /><Tabs.Screen name="post/[id]" options={{ href: null }} /><Tabs.Screen name="community/[slug]" options={{ href: null }} /><Tabs.Screen name="chat/[id]" options={{ href: null, tabBarStyle: { display: 'none' } }} />
  </Tabs>;
}
const styles = StyleSheet.create({ bar: { height: Platform.OS === 'ios' ? 88 : 70, paddingTop: 8, backgroundColor: colors.white, borderTopColor: colors.border, ...shadow.soft }, item: { paddingVertical: 2 }, label: { fontSize: 10, fontWeight: '700', paddingBottom: 4 }, create: { width: 48, height: 48, marginTop: -15, borderRadius: radius.pill, backgroundColor: colors.ocean600, alignItems: 'center', justifyContent: 'center', ...shadow.floating } });
