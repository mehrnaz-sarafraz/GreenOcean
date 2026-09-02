import { router } from 'expo-router';
import { useState } from 'react';
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
  const { profile } = usePlatformData();

  if (!profile) {
    return (
      <Screen style={styles.screen}>
        <View style={styles.loadingContent}>
          <Text style={styles.loadingText}>Loading profile…</Text>
        </View>
      </Screen>
    );
  }

  return (
    <EditProfileForm
      key={profile.userId}
      initialProfile={profile}
    />
  );
}

function EditProfileForm({
  initialProfile,
}: {
  initialProfile: OwnProfile;
}) {
  const { setProfile } = usePlatformData();

  const [username, setUsername] = useState(initialProfile.username);
  const [displayName, setDisplayName] = useState(initialProfile.displayName);
  const [bio, setBio] = useState(initialProfile.bio ?? '');
  const [countryCode, setCountryCode] = useState(initialProfile.countryCode ?? '');
  const [city, setCity] = useState(initialProfile.city ?? '');
  const [birthYear, setBirthYear] = useState(
    initialProfile.birthYear?.toString() ?? '',
  );

  const [profilePrivate, setProfilePrivate] = useState(
    initialProfile.profilePrivate,
  );
  const [showLocation, setShowLocation] = useState(
    initialProfile.showLocation,
  );
  const [showBirthYear, setShowBirthYear] = useState(
    initialProfile.showBirthYear,
  );

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function save() {
    const trimmedUsername = username.trim();
    const trimmedDisplayName = displayName.trim();
    const trimmedBio = bio.trim();
    const trimmedCountryCode = countryCode.trim().toUpperCase();
    const trimmedCity = city.trim();

    const year = birthYear.trim()
      ? Number(birthYear)
      : null;

    if (
      !trimmedUsername ||
      !trimmedDisplayName ||
      (year !== null && !Number.isInteger(year))
    ) {
      setError('Username, display name, and birth year must be valid.');
      return;
    }

    setSaving(true);
    setError('');

    try {
      const updated = await apiRequest<OwnProfile>(
        '/api/v1/profiles/me',
        {
          method: 'PATCH',
          body: JSON.stringify({
            username: trimmedUsername,
            displayName: trimmedDisplayName,
            bio: trimmedBio || null,
            countryCode: trimmedCountryCode,
            city: trimmedCity || null,
            birthYear: year,
            profilePrivate,
            showLocation,
            showBirthYear,
          }),
        },
      );

      setProfile(updated);
      router.back();
    } catch (caught) {
      setError(
        caught instanceof Error
          ? caught.message
          : 'Could not save your profile',
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <Screen scroll style={styles.screen}>
      <View style={styles.content}>
        <View style={styles.top}>
          <Pressable
            onPress={() => router.back()}
            style={styles.back}
            accessibilityRole="button"
            accessibilityLabel="Go back"
          >
            <AppIcon
              name="arrow_back"
              color={colors.ocean700}
            />
          </Pressable>

          <Text style={styles.title}>
            Edit profile
          </Text>

          <View style={styles.topSpacer} />
        </View>

        <AppField
          label="Username"
          value={username}
          onChangeText={setUsername}
          autoCapitalize="none"
        />

        <AppField
          label="Display name"
          value={displayName}
          onChangeText={setDisplayName}
        />

        <AppField
          label="Bio"
          value={bio}
          onChangeText={setBio}
          multiline
          style={styles.bio}
        />

        <View style={styles.row}>
          <View style={styles.countryField}>
            <AppField
              label="Country code"
              value={countryCode}
              onChangeText={setCountryCode}
              autoCapitalize="characters"
              maxLength={2}
            />
          </View>

          <View style={styles.cityField}>
            <AppField
              label="City"
              value={city}
              onChangeText={setCity}
            />
          </View>
        </View>

        <AppField
          label="Birth year"
          value={birthYear}
          onChangeText={setBirthYear}
          keyboardType="number-pad"
          maxLength={4}
        />

        <View style={styles.settings}>
          <Toggle
            title="Private profile"
            value={profilePrivate}
            onChange={setProfilePrivate}
          />

          <Toggle
            title="Show location"
            value={showLocation}
            onChange={setShowLocation}
          />

          <Toggle
            title="Show birth year"
            value={showBirthYear}
            onChange={setShowBirthYear}
          />
        </View>

        {!!error && (
          <Text style={styles.error}>
            {error}
          </Text>
        )}

        <AppButton
          label="Save profile"
          loading={saving}
          disabled={saving}
          onPress={() => void save()}
        />
      </View>
    </Screen>
  );
}

function Toggle({
  title,
  value,
  onChange,
}: {
  title: string;
  value: boolean;
  onChange: (value: boolean) => void;
}) {
  return (
    <View style={styles.toggle}>
      <Text style={styles.toggleText}>
        {title}
      </Text>

      <Switch
        value={value}
        onValueChange={onChange}
        trackColor={{
          false: colors.border,
          true: colors.ocean500,
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    padding: 0,
  },

  content: {
    width: '100%',
    maxWidth: layout.maxContent,
    alignSelf: 'center',
    padding: spacing.md,
    gap: spacing.md,
  },

  loadingContent: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.xl,
  },

  loadingText: {
    color: colors.muted,
    fontSize: 13,
  },

  top: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  topSpacer: {
    width: 40,
  },

  back: {
    width: 40,
    height: 40,
    borderRadius: radius.pill,
    backgroundColor: colors.white,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },

  title: {
    color: colors.ink,
    fontSize: typography.h2,
    fontWeight: '900',
  },

  bio: {
    minHeight: 120,
    paddingTop: spacing.md,
    textAlignVertical: 'top',
  },

  row: {
    flexDirection: 'row',
    gap: spacing.sm,
  },

  countryField: {
    flex: 1,
  },

  cityField: {
    flex: 2,
  },

  settings: {
    backgroundColor: colors.white,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: radius.lg,
    overflow: 'hidden',
  },

  toggle: {
    minHeight: 56,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },

  toggleText: {
    color: colors.ink,
    fontWeight: '800',
  },

  error: {
    color: colors.danger,
    fontSize: 11,
    textAlign: 'center',
  },
});