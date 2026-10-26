import { Platform } from 'react-native';

import { fetchWithTimeout, singleFlight } from './request';
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
  const requestEpoch = tokenStorage.generation();
  const tokens = await tokenStorage.get();
  const headers = new Headers(requestInit.headers);
  if (Platform.OS === 'web') headers.set('X-GreenOcean-CSRF', '1');
  headers.set('Accept', 'application/json');
  if (typeof requestInit.body === 'string' && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (authenticated && tokens?.accessToken) headers.set('Authorization', `Bearer ${tokens.accessToken}`);

  let response = await fetchWithTimeout(`${API_URL}${path}`, { ...requestInit, headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
  if (response.status === 401 && authenticated && retryAfterRefresh && tokens?.refreshToken) {
    const refreshed = await refreshTokens(tokens.refreshToken);
    if (refreshed) {
      headers.set('Authorization', `Bearer ${refreshed.accessToken}`);
      response = await fetchWithTimeout(`${API_URL}${path}`, { ...requestInit, headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
    }
  }

  if (!response.ok) {
    if (response.status === 401 && authenticated && requestEpoch === tokenStorage.generation()) await tokenStorage.clear();
    const body = await response.json().catch(() => null) as { message?: string } | null;
    throw new ApiError(response.status, body?.message ?? `Request failed with status ${response.status}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function apiDownloadText(path: string): Promise<{ filename: string; text: string }> {
  const requestEpoch = tokenStorage.generation();
  const tokens = await tokenStorage.get();
  const headers = new Headers({ Accept: 'text/csv' });
  if (tokens?.accessToken) headers.set('Authorization', `Bearer ${tokens.accessToken}`);
  let response = await fetchWithTimeout(`${API_URL}${path}`, { headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
  if (response.status === 401 && tokens?.refreshToken) {
    const refreshed = await refreshTokens(tokens.refreshToken);
    if (refreshed) {
      headers.set('Authorization', `Bearer ${refreshed.accessToken}`);
      response = await fetchWithTimeout(`${API_URL}${path}`, { headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
    }
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;
    if (response.status === 401 && requestEpoch === tokenStorage.generation()) await tokenStorage.clear();
    throw new ApiError(response.status, body?.message ?? `Download failed with status ${response.status}`);
  }
  const disposition = response.headers.get('Content-Disposition') || '';
  const filename = disposition.match(/filename="?([^";]+)"?/i)?.[1] || 'greenocean-export.csv';
  return { filename, text: await response.text() };
}

const refreshTokens = singleFlight(performRefresh);
async function performRefresh(refreshToken: string) {
  const epoch = tokenStorage.generation();
  const response = await fetchWithTimeout(`${API_URL}/api/v1/auth/${Platform.OS === 'web' ? 'web/refresh' : 'refresh'}`, {
    method: 'POST',
    credentials: Platform.OS === 'web' ? 'include' : 'omit',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', 'X-GreenOcean-CSRF': '1' },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    if (epoch === tokenStorage.generation()) await tokenStorage.clear();
    return null;
  }
  const tokens = await response.json() as { accessToken: string; refreshToken: string };
  if (epoch !== tokenStorage.generation()) return null;
  await tokenStorage.set(tokens);
  return tokens;
}
