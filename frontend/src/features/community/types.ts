export type Community = {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  iconUrl?: string | null;
  privateCommunity: boolean;
  memberCount: number;
  member: boolean;
  membershipRole?: 'MEMBER' | 'MODERATOR' | 'OWNER' | null;
  createdAt: string;
};
