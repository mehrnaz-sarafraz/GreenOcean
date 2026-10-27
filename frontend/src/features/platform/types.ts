import { Community } from '@/features/community/types';
import { NotificationItem } from '@/features/notification/types';
import { PostItem, SupportCategory } from '@/features/content/types';
import { OwnProfile } from '@/features/profile/types';

export type Professional = {
  id: string; displayName: string; username: string; title: string; specialties: string[]; avatarUrl: string | null;
  rating: number; reviewCount: number; greenOceanScore: number; experienceYears: number; languages: string[];
  verified: boolean; promoted: boolean; promotedReason: string | null; bio: string | null; gender: string | null;
  country: string | null; city: string | null; workplace: string | null; clinicName: string | null;
  clinicAddress: string | null; education: string[]; licenseNumber: string | null; consultationModes: string[];
  acceptingNewClients: boolean;
};

export type ProfessionalArticle = {
  id: string; authorId: string; title: string; summary: string; topic: string; readTime: string;
  status: 'DRAFT' | 'IN_REVIEW' | 'PUBLISHED' | 'REJECTED'; pinned: boolean; evidenceLevel: string;
  sections: { heading: string; body: string }[]; takeaways: string[]; references: string[];
  helpfulCount: number; helpful: boolean; saved: boolean; publishedAt: string | null;
};

export type MediaPick = {
  id: string; title: string; kind: 'MOVIE' | 'SERIES' | 'DOCUMENTARY'; year: number; duration: string;
  theme: string; description: string; discussionPrompt: string; contentNotes: string[];
  recommendedBy: string; accent: string; softAccent: string; saved: boolean;
};

export type Conversation = {
  id: string; name: string; subtitle: string | null; lastMessage: string | null; lastMessageAt: string | null;
  unread: number; verified: boolean; kind: 'DIRECT' | 'PROFESSIONAL' | 'GROUP'; online: boolean;
};

export type ChatMessage = {
  id: string; senderId: string | null; senderName: string | null; body: string; createdAt: string;
  mine: boolean; system: boolean;
};

export type SupportChannel = {
  id: string; conversationId: string | null; name: string; slug: string; description: string; category: string;
  icon: string; memberCount: number; onlineCount: number; joined: boolean; type: 'GROUP' | 'ANNOUNCEMENT';
  moderated: boolean; nextEvent: string | null;
};

export type UserPreferences = {
  supportTopics: string[]; supportStyle: string | null; strongerContentControls: boolean; privateFeed: boolean;
  blurSensitiveContent: boolean; reduceMedicationContent: boolean; allowMessageRequests: boolean;
  professionalsOnlyMessages: boolean; mutedTerms: string[]; notifyMessages: boolean; notifyReplies: boolean;
  notifyFollows: boolean; notifyGroups: boolean; notifyArticles: boolean; quietHoursEnabled: boolean;
  onboardingComplete: boolean; intendedRole: 'MEMBER' | 'PROFESSIONAL';
  onboardingStatus?: import('./onboarding-policy').OnboardingStatus; onboardingStep?: number;
  legacySupportTopics?: string[]; topicCollectionEnabled?: boolean;
};

export type CatalogOption = {
  id: string; group: string; key: string; label: string; description: string | null; icon: string | null;
  accent: string | null; softAccent: string | null; sortOrder: number;
};

export type SearchHistoryItem = { id: string; term: string; searchedAt: string };
export type ListenerTrainingModule = { key: string; title: string; description: string; sortOrder: number };
export type ListenerTraining = { modules: ListenerTrainingModule[]; completedModules: string[]; totalModules: number;
  courseworkComplete: boolean; applicationStatus: string; listenerActivated: boolean };
export type CrisisResource = { id: string; countryCode: string; regionName: string; title: string; detail: string;
  actionUrl: string; sourceUrl: string; lastVerifiedOn: string; sortOrder: number };

export type ProfessionalReview = { id: string; professionalId: string; reviewerName: string; rating: number; body: string; status: 'PENDING' | 'PUBLISHED' | 'REJECTED'; mine: boolean; createdAt: string };
export type ProfessionalDocument = { id: string; originalName: string; contentType: string; byteSize: number; sha256: string; scanStatus: string; uploadedAt: string };
export type ProfessionalApplication = { id: string; profession: string; country: string; documentNames: string[]; documents: ProfessionalDocument[]; status: string; note: string | null; submittedAt: string; reviewedAt: string | null };
export type Consultation = { id: string; professionalId: string; professionalName: string; requesterId: string; requesterName: string; scheduledAt: string; consultationMode: string; note: string | null; status: string; createdAt: string };
export type AvailabilitySlot = { id: string; professionalId: string; startsAt: string; endsAt: string; consultationMode: string; status: 'AVAILABLE' | 'BOOKED' | 'CANCELLED' };
export type Listener = { id: string; displayName: string; username: string; avatarUrl: string | null; displayTitle: string; listeningStyle: string; languages: string[]; topics: string[]; completedSessions: number; safetyScore: number; online: boolean };
export type ListenerApplication = { id: string; motivation: string; experience: string; weeklyAvailability: string; status: string; reviewNote: string | null; submittedAt: string; reviewedAt: string | null };
export type WellbeingSnapshot = {
  checkIns: { id: string; mood: string; note: string | null; createdAt: string }[];
  journalEntries: { id: string; title: string; body: string; mood: string | null; createdAt: string; updatedAt: string }[];
  goals: { id: string; title: string; cadence: string; progress: number; completed: boolean; createdAt: string }[];
  growthPaths: { id: string; slug: string; title: string; summary: string; lessonCount: number; durationMinutes: number;
    authorName: string; evidenceNote: string; references: string[]; reviewedAt: string; progress: number;
    completed: boolean; updatedAt: string | null }[];
  reflections: { id: string; answers: number[]; score: number; summaryBand: 'SUPPORTED' | 'MIXED' | 'STRAINED'; createdAt: string }[];
};
export type SocialConnection = { userId: string; displayName: string; username: string; avatarUrl: string | null; connectedAt: string };
export type FollowRequest = { userId: string; displayName: string; username: string; avatarUrl: string | null; requestedAt: string };
export type FollowAction = { status: 'FOLLOWING' | 'REQUESTED' };
export type SessionItem = { id: string; deviceName: string | null; createdAt: string; lastUsedAt: string | null; expiresAt: string; current: boolean };
export type AccountArchive = {
  generatedAt: string; profile: OwnProfile; preferences: UserPreferences; wellbeing: WellbeingSnapshot;
  reports: { id: string; status: string }[]; following: SocialConnection[]; followers: SocialConnection[];
  blockedAccounts: SocialConnection[]; searchHistory: SearchHistoryItem[]; activity: { posts: { id: string }[]; comments: { id: string }[];
    communities: { id: string }[]; savedItems: { type: string; id: string; title: string }[] };
};

export type AdminDashboard = {
  stats: { members: number; activeToday: number; postsToday: number; openReports: number; criticalReports: number;
    verifiedProfessionals: number; pendingVerifications: number; resolvedWeek: number };
  reportTrend: number[];
  reasonBreakdown: { name: string; value: number; color: string }[];
  reports: { id: string; targetType: string; reason: string; summary: string | null; reportedUser: string;
    reporter: string; severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'; status: 'OPEN' | 'REVIEWING' | 'RESOLVED';
    createdAt: string; category: string; signals: string[] }[];
  verificationQueue: { id: string; professionalId: string; name: string; profession: string | null; country: string | null;
    submittedAt: string; status: string; documents: { id: string; name: string; contentType: string; byteSize: number; sha256: string; scanStatus: string; uploadedAt: string }[] }[];
  members: { id: string; name: string; username: string; status: string; postCount: number; reportCount: number }[];
  auditLog: { id: string; action: string; actor: string; target: string; createdAt: string }[];
};

export type AdminOperations = {
  pendingReviews: { id: string; professionalId: string; professionalName: string; reviewerName: string; rating: number; body: string; status: string; createdAt: string }[];
  listenerApplications: { id: string; userId: string; name: string; motivation: string; experience: string; weeklyAvailability: string; status: string; submittedAt: string }[];
  campaigns: { id: string; professionalId: string; professionalName: string; title: string; startsAt: string; endsAt: string; status: string; impressions: number; clicks: number }[];
  policies: { id: string; key: string; version: number; title: string; summary: string; status: string; publishedAt: string | null }[];
  exports: { id: string; type: string; requester: string; status: string; rowCount: number | null; createdAt: string; completedAt: string | null }[];
  appeals: { id: string; appellantId: string; appellantName: string; reportId: string | null; reason: string; status: string; decisionNote: string | null; createdAt: string; decidedAt: string | null }[];
  communities: { id: string; name: string; slug: string; members: number; moderators: number; posts: number; openReports: number; status: string }[];
  broadcasts: { id: string; title: string; body: string; audience: string; category: string; status: string; recipientCount: number; createdAt: string; sentAt: string | null }[];
  supportRequests: { id: string; userId: string | null; email: string; message: string; status: string; resolutionNote: string | null; createdAt: string; resolvedAt: string | null }[];
  emailDeliveries: { id: string; recipient: string; templateKey: string; status: string; attempts: number; createdAt: string; sentAt: string | null }[];
};

export type PlatformData = {
  categories: SupportCategory[]; posts: PostItem[]; communities: Community[]; notifications: NotificationItem[];
  professionals: Professional[]; articles: ProfessionalArticle[]; media: MediaPick[]; conversations: Conversation[];
  channels: SupportChannel[]; profile: OwnProfile | null; preferences: UserPreferences | null; options: CatalogOption[];
};
