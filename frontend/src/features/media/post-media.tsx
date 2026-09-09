import { Image } from 'expo-image';
import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { API_URL } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';
import { radius, spacing } from '@/theme/tokens';

export function PostMedia({ media }: { media: { id: string; contentType: string }[] }) {
  const [authorization, setAuthorization] = useState<string>();
  useEffect(() => { tokenStorage.get().then(tokens => setAuthorization(tokens ? `Bearer ${tokens.accessToken}` : undefined)); }, []);
  if (!media.length || !authorization) return null;
  return <View style={styles.grid}>{media.map(item => <Image
    key={item.id}
    accessibilityLabel="Post attachment"
    source={{ uri: `${API_URL}/api/v1/media/${item.id}`, headers: { Authorization: authorization } }}
    style={[styles.image, media.length === 1 && styles.single]}
    contentFit="cover"
    transition={180}
    recyclingKey={item.id}
  />)}</View>;
}

const styles = StyleSheet.create({ grid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs }, image: { flexGrow: 1, flexBasis: '48%', height: 180, borderRadius: radius.md }, single: { flexBasis: '100%', height: 280 } });
