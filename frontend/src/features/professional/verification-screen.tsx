import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { MediaAttachment, MediaAttachmentPicker } from '@/features/media/media-attachment-picker';
import { ApiError, apiRequest } from '@/lib/api/client';
import { colors, layout, radius, spacing, typography } from '@/theme/tokens';

type Status = { id: string; status: string; profession: string; country: string; submittedAt: string; reviewedAt: string | null; reviewerNote: string | null; informationRequest: string | null };

export function VerificationScreen() {
  const [profession, setProfession] = useState('');
  const [country, setCountry] = useState('');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [specialization, setSpecialization] = useState('');
  const [documents, setDocuments] = useState<MediaAttachment[]>([]);
  const [status, setStatus] = useState<Status | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { apiRequest<Status>('/api/v1/professional-verifications/mine').then(setStatus).catch(caught => {
    if (!(caught instanceof ApiError && caught.status === 404)) setError(caught instanceof Error ? caught.message : 'Could not load verification status');
  }); }, []);

  async function submit() {
    if (!profession.trim() || !country.trim() || !licenseNumber.trim() || !documents.length) return setError('Complete the required fields and upload at least one credential.');
    setBusy(true); setError('');
    try {
      const result = await apiRequest<Status>('/api/v1/professional-verifications', { method: 'POST', body: JSON.stringify({
        professionalType: 'OTHER', profession: profession.trim(), country: country.trim(), licenseNumber: licenseNumber.trim(),
        specialization: specialization.trim() || null, documentIds: documents.map(item => item.id),
      }) });
      setStatus(result); setDocuments([]);
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'Could not submit verification'); }
    finally { setBusy(false); }
  }

  return <Screen scroll style={styles.screen}><View style={styles.content}>
    <View style={styles.header}><Pressable accessibilityLabel="Go back" onPress={() => router.back()}><AppIcon name="arrow_back" color={colors.ocean700} /></Pressable><Text style={styles.title}>Professional verification</Text><View style={{ width: 24 }} /></View>
    <View style={styles.notice}><AppIcon name="verified_user" color={colors.ocean600} /><Text style={styles.noticeText}>Credentials are private and visible only to authorised reviewers. Do not upload unrelated medical or identity information.</Text></View>
    {status && <View accessibilityRole="summary" style={styles.status}><Text style={styles.statusTitle}>Latest submission · {status.status}</Text><Text style={styles.statusText}>Submitted {new Date(status.submittedAt).toLocaleDateString()} · {status.profession}, {status.country}</Text>{status.informationRequest && <Text style={styles.request}>{status.informationRequest}</Text>}</View>}
    {status?.status !== 'PENDING' && <View style={styles.form}>
      <AppField label="Profession" value={profession} onChangeText={setProfession} autoCapitalize="words" />
      <AppField label="Country of licence" value={country} onChangeText={setCountry} autoCapitalize="words" />
      <AppField label="Licence / registration number" value={licenseNumber} onChangeText={setLicenseNumber} />
      <AppField label="Specialisation (optional)" value={specialization} onChangeText={setSpecialization} />
      <Text style={styles.label}>Credential documents</Text>
      <MediaAttachmentPicker value={documents} onChange={setDocuments} onError={setError} purpose="VERIFICATION" disabled={busy} />
      <AppButton label="Submit for review" loading={busy} disabled={documents.some(item => item.uploading)} onPress={() => void submit()} />
    </View>}
    {!!error && <Text accessibilityRole="alert" style={styles.error}>{error}</Text>}
  </View></Screen>;
}

const styles = StyleSheet.create({
  screen: { padding: 0 }, content: { width: '100%', maxWidth: layout.maxContent, alignSelf: 'center', padding: spacing.md, gap: spacing.lg },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, title: { fontSize: typography.h2, fontWeight: '900', color: colors.ink },
  notice: { flexDirection: 'row', gap: spacing.sm, padding: spacing.md, borderRadius: radius.md, backgroundColor: colors.ocean50 }, noticeText: { flex: 1, color: colors.inkSoft, fontSize: 11, lineHeight: 17 },
  status: { padding: spacing.md, borderWidth: 1, borderColor: colors.ocean200, borderRadius: radius.lg, backgroundColor: colors.white }, statusTitle: { color: colors.ink, fontWeight: '900' }, statusText: { color: colors.muted, marginTop: 5, fontSize: 11 }, request: { color: colors.coral, marginTop: spacing.sm, fontWeight: '700' },
  form: { gap: spacing.md }, label: { color: colors.ink, fontWeight: '800' }, error: { color: colors.danger, fontWeight: '700' },
});
