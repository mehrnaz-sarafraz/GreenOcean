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
};
