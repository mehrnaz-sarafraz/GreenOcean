export type CurrentUser = {
  userId: string;
  email: string;
  roles: string[];
};

export type LoginInput = { email: string; password: string; deviceName?: string };

export type RegisterInput = {
  email: string;
  password: string;
  countryCode: string;
  adultSelfAttested: boolean;
  termsVersion: number;
};

export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
};
