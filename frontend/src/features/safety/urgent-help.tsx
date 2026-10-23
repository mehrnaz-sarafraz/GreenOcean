import { useEffect, useState } from 'react';
import { Linking, StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { EmptyState, Notice, Pill, ProductCard } from '@/components/product-ui';
import { CrisisResource } from '@/features/platform/types';
import { apiRequest } from '@/lib/api/client';
import { colors, spacing } from '@/theme/tokens';

export function UrgentHelpContent() {
  const [countryCode, setCountryCode] = useState('US');
  const [allResources, setAllResources] = useState<CrisisResource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    apiRequest<CrisisResource[]>('/api/v1/crisis-resources', { authenticated: false })
      .then(setAllResources)
      .catch(caught => setError(caught instanceof Error ? caught.message : 'Could not load urgent resources.'))
      .finally(() => setLoading(false));
  }, []);

  const regions = Array.from(new Map(allResources.filter(item => item.countryCode !== 'GLOBAL').map(item => [item.countryCode, item.regionName])).entries());
  const resources = allResources.filter(item => item.countryCode === countryCode);
  const globalResources = allResources.filter(item => item.countryCode === 'GLOBAL');

  return <>
    <Notice tone="warning" icon="emergency" title="If someone is in immediate danger" body="Call the local emergency number now or go to the nearest emergency department. GreenOcean cannot monitor this page or dispatch help." />
    <Text style={styles.label}>Choose your location</Text>
    <View style={styles.wrap}>{regions.map(([code, name]) => <Pill key={code} label={name} active={countryCode === code} onPress={() => setCountryCode(code)} />)}</View>
    {resources.map(item => <ProductCard key={item.id}><Text style={styles.cardTitle}>{item.title}</Text><Text style={styles.body}>{item.detail}</Text><Text style={styles.verified}>Source checked {new Date(item.lastVerifiedOn).toLocaleDateString()}</Text><View style={styles.actions}><AppButton label={item.actionUrl.startsWith('tel:') ? 'Call now' : 'Open resource'} onPress={() => void Linking.openURL(item.actionUrl)} />{item.sourceUrl !== item.actionUrl && <AppButton label="Official source" variant="secondary" onPress={() => void Linking.openURL(item.sourceUrl)} />}</View></ProductCard>)}
    {globalResources.map(item => <ProductCard key={item.id} tone="soft"><Text style={styles.cardTitle}>{item.regionName}</Text><Text style={styles.body}>{item.detail}</Text><Text style={styles.verified}>Source checked {new Date(item.lastVerifiedOn).toLocaleDateString()}</Text><AppButton label={item.title} variant="secondary" onPress={() => void Linking.openURL(item.actionUrl)} /></ProductCard>)}
    {loading && <EmptyState icon="hourglass_top" title="Loading verified resources" body="Retrieving the current crisis-support directory." />}
    {!!error && <Notice tone="warning" title="Urgent directory unavailable" body={`${error} Call your local emergency number or go to the nearest emergency department now if there is immediate danger.`} />}
  </>;
}

const styles = StyleSheet.create({
  label: { color: colors.ink, fontSize: 11, fontWeight: '900' },
  wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm, alignItems: 'center' },
  actions: { gap: spacing.sm },
  cardTitle: { color: colors.ink, fontSize: 13, fontWeight: '900' },
  body: { color: colors.inkSoft, fontSize: 10, lineHeight: 17 },
  verified: { color: colors.muted, fontSize: 9, fontWeight: '700' },
});
