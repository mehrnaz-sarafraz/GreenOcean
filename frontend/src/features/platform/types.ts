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
  helpfulCount: number; helpful: boolean; publishedAt: string | null;
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
  professionalsOnlyMessages: boolean; mutedTerms: string[];
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
    submittedAt: string; status: string; documents: string[] }[];
  members: { id: string; name: string; username: string; status: string; postCount: number; reportCount: number }[];
  auditLog: { id: string; action: string; actor: string; target: string; createdAt: string }[];
};

export type PlatformData = {
  categories: SupportCategory[]; posts: PostItem[]; communities: Community[]; notifications: NotificationItem[];
  professionals: Professional[]; articles: ProfessionalArticle[]; media: MediaPick[]; conversations: Conversation[];
  channels: SupportChannel[]; profile: OwnProfile | null; preferences: UserPreferences | null;
};
