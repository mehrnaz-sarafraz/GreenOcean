import * as ImagePicker from 'expo-image-picker';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppIcon } from '@/components/app-icon';
import { apiRequest } from '@/lib/api/client';
import { colors, radius, spacing } from '@/theme/tokens';

export type MediaAttachment = { id: string; uri: string; uploading: boolean };

type Props = {
  value: MediaAttachment[];
  onChange: (value: MediaAttachment[]) => void;
  onError: (message: string) => void;
  disabled?: boolean;
  purpose?: 'POST' | 'VERIFICATION' | 'PROFILE';
};

type UploadResponse = { id: string };

export function MediaAttachmentPicker({ value, onChange, onError, disabled, purpose = 'POST' }: Props) {
  async function choose() {
    if (disabled || value.length >= 6) return;
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) return onError('Photo access is required to select an attachment.');
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'], allowsMultipleSelection: true, selectionLimit: 6 - value.length, quality: 0.86,
    });
    if (result.canceled) return;
    const assets = result.assets.slice(0, 6 - value.length);
    const placeholders = assets.map((asset, index) => ({ id: `upload-${Date.now()}-${index}`, uri: asset.uri, uploading: true }));
    onChange([...value, ...placeholders]);
    const uploaded = await Promise.all(assets.map(async asset => {
      const form = new FormData();
      if (asset.file) form.append('file', asset.file);
      else form.append('file', { uri: asset.uri, name: asset.fileName ?? 'attachment.jpg', type: asset.mimeType ?? 'image/jpeg' } as unknown as Blob);
      try {
        const created = await apiRequest<UploadResponse>(`/api/v1/media/uploads?purpose=${purpose}`, { method: 'POST', body: form, timeoutMs: 60_000 });
        return { id: created.id, uri: asset.uri, uploading: false };
      } catch (error) {
        onError(error instanceof Error ? error.message : 'Could not upload attachment');
        return null;
      }
    }));
    onChange([...value, ...uploaded.filter((item): item is MediaAttachment => item !== null)]);
  }

  async function remove(item: MediaAttachment) {
    onChange(value.filter(candidate => candidate.id !== item.id));
    if (!item.uploading) await apiRequest(`/api/v1/media/uploads/${item.id}`, { method: 'DELETE' }).catch(() => undefined);
  }

  return <View style={styles.container}>
    <View style={styles.list}>{value.map(item => <View key={item.id} style={styles.preview}>
      <Image source={item.uri} style={styles.image} contentFit="cover" transition={150} />
      <Pressable accessibilityLabel="Remove attachment" accessibilityRole="button" onPress={() => void remove(item)} style={styles.remove}><AppIcon name="close" size={14} color={colors.white} /></Pressable>
      {item.uploading && <View style={styles.uploading}><Text style={styles.uploadingText}>Uploading…</Text></View>}
    </View>)}</View>
    <Pressable accessibilityRole="button" accessibilityLabel="Add photos" disabled={disabled || value.length >= 6} onPress={() => void choose()} style={styles.add}>
      <AppIcon name="add_photo_alternate" color={colors.ocean600} /><Text style={styles.addText}>{value.length ? `${value.length}/6 photos` : 'Add photos'}</Text>
    </Pressable>
  </View>;
}

const styles = StyleSheet.create({
  container: { gap: spacing.sm }, list: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  preview: { width: 92, height: 92, borderRadius: radius.md, overflow: 'hidden', backgroundColor: colors.ocean50 }, image: { width: '100%', height: '100%' },
  remove: { position: 'absolute', top: 5, right: 5, width: 25, height: 25, borderRadius: 13, backgroundColor: 'rgba(0,0,0,0.65)', alignItems: 'center', justifyContent: 'center' },
  uploading: { ...StyleSheet.absoluteFillObject, alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(11,45,39,0.62)' }, uploadingText: { color: colors.white, fontSize: 10, fontWeight: '800' },
  add: { minHeight: 46, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: spacing.sm, borderRadius: radius.md, borderWidth: 1, borderStyle: 'dashed', borderColor: colors.ocean300, backgroundColor: colors.ocean50 }, addText: { color: colors.ocean700, fontWeight: '800', fontSize: 12 },
});
