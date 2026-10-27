export type OnboardingStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'SKIPPED' | 'COMPLETED' | 'LEGACY_UNKNOWN';
export function needsOnboarding(value: { onboardingStatus?: string; onboardingComplete: boolean }) {
  return value.onboardingStatus ? ['NOT_STARTED', 'IN_PROGRESS'].includes(value.onboardingStatus) : !value.onboardingComplete;
}
export function resumeStep(step: number = 0) { return Math.min(3, Math.max(1, step + 1)); }
export function supportDestination(style: string | null, services?: Record<string, boolean>) {
  if (services && !services.COMMUNITY) return '/(auth)/activate' as const;
  switch (style) {
    case 'SHARED_EXPERIENCES': return '/(app)' as const;
    case 'SUPPORT_GROUPS': return '/(app)/communities' as const;
    case 'PROFESSIONAL_KNOWLEDGE': return '/(app)/articles' as const;
    default: return '/(app)/support' as const;
  }
}
