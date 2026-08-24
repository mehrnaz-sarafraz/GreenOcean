export type UserSearchItem = {
  userId: string; username: string; displayName: string; avatarUrl: string | null; bio: string | null;
  profilePrivate: boolean; following: boolean; followerCount: number;
};
