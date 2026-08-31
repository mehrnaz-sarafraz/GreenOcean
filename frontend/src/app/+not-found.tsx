import { router } from 'expo-router';
import { AppButton } from '@/components/app-button';
import { EmptyState, ProductHeader, ProductPage } from '@/components/product-ui';

export default function NotFound() {
  return <ProductPage><ProductHeader eyebrow="404" title="This page drifted out to sea" subtitle="The link may be old, incomplete, or no longer available." /><EmptyState icon="explore_off" title="Nothing here" body="Return to your feed and keep exploring GreenOcean." /><AppButton label="Go to feed" onPress={() => router.replace('/')} /></ProductPage>;
}
