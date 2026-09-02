import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Switch, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { usePlatformData } from '@/features/platform/data-provider';
import { OwnProfile } from '@/features/profile/types';
import { apiRequest } from '@/lib/api/client';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

export default function EditProfile() {
  const { profile, setProfile } = usePlatformData();
  const [username, setUsername] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [bio, setBio] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [city, setCity] = useState('');
  const [birthYear, setBirthYear] = useState('');
  const [profilePrivate, setProfilePrivate] = useState(false);
  const [showLocation, setShowLocation] = useState(true);
  const [showBirthYear, setShowBirthYear] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!profile) return;
    setUsername(profile.username); setDisplayName(profile.displayName); setBio(profile.bio ?? '');
    setCountryCode(profile.countryCode ?? ''); setCity(profile.city ?? '');
    setBirthYear(profile.birthYear?.toString() ?? ''); setProfilePrivate(profile.profilePrivate);
    setShowLocation(profile.showLocation); setShowBirthYear(profile.showBirthYear);
  }, [profile]);

  async function save() {
    const year = birthYear.trim() ? Number(birthYear) : null;
    if (!username.trim() || !displayName.trim() || (year !== null && !Number.isInteger(year))) {
      setError('Username, display name, and birth year must be valid.'); return;
    }
    setSaving(true); setError('');
    try {
      const updated = await apiRequest<OwnProfile>('/api/v1/profiles/me', {
        method: 'PATCH',
        body: JSON.stringify({
          username: username.trim(), displayName: displayName.trim(), bio: bio.trim() || null,
          countryCode: countryCode.trim().toUpperCase(), city: city.trim() || null, birthYear: year,
          profilePrivate, showLocation, showBirthYear,
        }),
      });
      setProfile(updated);
      router.back();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not save your profile');
    } finally { setSaving(false); }
  }

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.top}><Pressable onPress={() => router.back()} style={styles.back}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Text style={styles.title}>Edit profile</Text><View style={{ width: 40 }} /></View>
    <AppField label="Username" value={username} onChangeText={setUsername} autoCapitalize="none" />
    <AppField label="Display name" value={displayName} onChangeText={setDisplayName} />
    <AppField label="Bio" value={bio} onChangeText={setBio} multiline style={styles.bio} />
    <View style={styles.row}><View style={{ flex: 1 }}><AppField label="Country code" value={countryCode} onChangeText={setCountryCode} autoCapitalize="characters" maxLength={2} /></View><View style={{ flex: 2 }}><AppField label="City" value={city} onChangeText={setCity} /></View></View>
    <AppField label="Birth year" value={birthYear} onChangeText={setBirthYear} keyboardType="number-pad" maxLength={4} />
    <View style={styles.settings}>
      <Toggle title="Private profile" value={profilePrivate} onChange={setProfilePrivate} />
      <Toggle title="Show location" value={showLocation} onChange={setShowLocation} />
      <Toggle title="Show birth year" value={showBirthYear} onChange={setShowBirthYear} />
    </View>
    {!!error && <Text style={styles.error}>{error}</Text>}
    <AppButton label="Save profile" loading={saving} onPress={() => void save()} />
  </View></Screen>;
}

function Toggle({ title, value, onChange }: { title: string; value: boolean; onChange: (value: boolean) => void }) {
  return <View style={styles.toggle}><Text style={styles.toggleText}>{title}</Text><Switch value={value} onValueChange={onChange} trackColor={{ true: colors.ocean500 }} /></View>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.md }, top: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, back: { width: 40, height: 40, borderRadius: radius.pill, backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center' }, title: { color: colors.ink, fontSize: typography.h2, fontWeight: '900' }, bio: { minHeight: 120, paddingTop: spacing.md, textAlignVertical: 'top' }, row: { flexDirection: 'row', gap: spacing.sm }, settings: { backgroundColor: colors.white, borderWidth: 1, borderColor: colors.border, borderRadius: radius.lg, overflow: 'hidden' }, toggle: { minHeight: 56, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: spacing.md, borderBottomWidth: 1, borderBottomColor: colors.border }, toggleText: { color: colors.ink, fontWeight: '800' }, error: { color: colors.danger, fontSize: 11, textAlign: 'center' },
});
