import { Platform } from 'react-native';

import { tokenStorage } from '@/lib/storage/token-storage';

const fallbackUrl = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
export const API_URL = process.env.EXPO_PUBLIC_API_URL ?? fallbackUrl;

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

type RequestOptions = RequestInit & { authenticated?: boolean; retryAfterRefresh?: boolean };

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { authenticated = true, retryAfterRefresh = true, ...requestInit } = options;
  const tokens = await tokenStorage.get();
  const headers = new Headers(requestInit.headers);
  headers.set('Accept', 'application/json');
  if (requestInit.body) headers.set('Content-Type', 'application/json');
  if (authenticated && tokens?.accessToken) headers.set('Authorization', `Bearer ${tokens.accessToken}`);

  let response = await fetch(`${API_URL}${path}`, { ...requestInit, headers });
  if (response.status === 401 && authenticated && retryAfterRefresh && tokens?.refreshToken) {
    const refreshed = await refreshTokens(tokens.refreshToken);
    if (refreshed) {
      headers.set('Authorization', `Bearer ${refreshed.accessToken}`);
      response = await fetch(`${API_URL}${path}`, { ...requestInit, headers });
    }
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;
    throw new ApiError(response.status, body?.message ?? `Request failed with status ${response.status}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

async function refreshTokens(refreshToken: string) {
  const response = await fetch(`${API_URL}/api/v1/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    await tokenStorage.clear();
    return null;
  }
  const tokens = await response.json() as { accessToken: string; refreshToken: string };
  await tokenStorage.set(tokens);
  return tokens;
}
