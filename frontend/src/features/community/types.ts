export type Community = {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  iconUrl?: string | null;
  privateCommunity: boolean;
  memberCount: number | null;
  privacyType?: 'PUBLIC'|'PRIVATE'|'HIDDEN';
  status?: string;
  rulesVersion?: number;
  rulesJson?: string | null;
  member: boolean;
  membershipRole?: 'MEMBER' | 'MODERATOR' | 'OWNER' | null;
  joinRequested: boolean;
  joinRequestRevision?: number | null;
  createdAt: string;
};

export type CommunityDetails = {
  rules: { id: string; position: number; text: string }[];
  members: {
    userId: string;
    displayName: string;
    username: string;
    avatarUrl?: string | null;
    role: 'MEMBER' | 'MODERATOR' | 'OWNER';
    joinedAt: string;
  }[];
  events: {
    id: string;
    title: string;
    description?: string | null;
    format: 'TEXT_CIRCLE' | 'VIDEO' | 'AUDIO' | 'WORKSHOP' | 'Q_AND_A';
    hostName?: string | null;
    startsAt: string;
    endsAt: string;
    capacity?: number | null;
    revision: number;
    discussionPostId?: string|null;
    attendeeCount: number;
    joined: boolean;
    status: 'SCHEDULED' | 'LIVE' | 'COMPLETED' | 'CANCELLED';
  }[];
};
