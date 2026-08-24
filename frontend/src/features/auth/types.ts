export type CurrentUser = {
  userId: string;
  email: string;
  roles: string[];
};

export type LoginInput = { email: string; password: string; deviceName?: string };

export type RegisterInput = {
  email: string;
  password: string;
  username: string;
  displayName: string;
  birthYear: number;
  countryCode: string;
  city: string;
};

export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
};
