import { ProductHeader, ProductPage } from '@/components/product-ui';
import { UrgentHelpContent } from '@/features/safety/urgent-help';

export default function PublicUrgentHelp() {
  return <ProductPage><ProductHeader eyebrow="NOT A CRISIS SERVICE" title="Urgent help" subtitle="Immediate, country-aware routes to crisis and emergency support." /><UrgentHelpContent /></ProductPage>;
}
