export type OwnProfile = {
  userId: string;
  username: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  countryCode: string | null;
  city: string | null;
  birthYear: number | null;
  profilePrivate: boolean;
  showLocation: boolean;
  showBirthYear: boolean;
  createdAt: string;
  updatedAt: string;
};

export type PublicProfile = {
  userId: string;
  username: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  countryCode: string | null;
  city: string | null;
  birthYear: number | null;
  profilePrivate: boolean;
  following: boolean;
  followRequested: boolean;
  canSeeActivity: boolean;
  followerCount: number;
  followingCount: number;
  postCount: number;
  helpfulReactionCount: number;
  memberSince: string;
};
