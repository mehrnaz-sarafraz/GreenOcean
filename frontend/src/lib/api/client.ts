import { Platform } from 'react-native';

import { tokenStorage } from '@/lib/storage/token-storage';

const fallbackUrl = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
export const API_URL = process.env.EXPO_PUBLIC_API_URL ?? fallbackUrl;

export class ApiError extends Error {
  constructor(public status: number, message: string, public retryAfterSeconds?: number) {
    super(message);
    this.name = 'ApiError';
  }
}

export class NetworkError extends Error {
  constructor(message: string, public kind: 'offline' | 'timeout') { super(message); this.name = 'NetworkError'; }
}

type RequestOptions = RequestInit & { authenticated?: boolean; retryAfterRefresh?: boolean; timeoutMs?: number };
type RefreshedTokens = { accessToken: string; refreshToken: string };

let refreshInFlight: Promise<RefreshedTokens | null> | null = null;

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { authenticated = true, retryAfterRefresh = true, timeoutMs = 15_000, ...requestInit } = options;
  const tokens = await tokenStorage.get();
  const headers = new Headers(requestInit.headers);
  headers.set('Accept', 'application/json');
  if (requestInit.body && !(requestInit.body instanceof FormData)) headers.set('Content-Type', 'application/json');
  if (authenticated && tokens?.accessToken) headers.set('Authorization', `Bearer ${tokens.accessToken}`);

  let response = await fetchWithTimeout(`${API_URL}${path}`, { ...requestInit, headers }, timeoutMs);
  if (response.status === 401 && authenticated && retryAfterRefresh && tokens?.refreshToken) {
    const latestTokens = await tokenStorage.get();
    const usableTokens = latestTokens?.refreshToken !== tokens.refreshToken
      ? latestTokens
      : await refreshTokensOnce(tokens.refreshToken);
    if (usableTokens) {
      headers.set('Authorization', `Bearer ${usableTokens.accessToken}`);
      response = await fetchWithTimeout(`${API_URL}${path}`, { ...requestInit, headers }, timeoutMs);
    }
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;
    const retryAfter = Number(response.headers.get('Retry-After'));
    throw new ApiError(response.status, body?.message ?? `Request failed with status ${response.status}`,
      Number.isFinite(retryAfter) ? retryAfter : undefined);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

async function fetchWithTimeout(url: string, init: RequestInit, timeoutMs: number) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);
  const externalSignal = init.signal;
  const abort = () => controller.abort();
  externalSignal?.addEventListener('abort', abort, { once: true });
  try {
    return await fetch(url, { ...init, signal: controller.signal });
  } catch (error) {
    if (controller.signal.aborted) throw new NetworkError('The request timed out', 'timeout');
    throw new NetworkError('Could not connect to GreenOcean', 'offline');
  } finally {
    clearTimeout(timeout);
    externalSignal?.removeEventListener('abort', abort);
  }
}

function refreshTokensOnce(refreshToken: string) {
  if (!refreshInFlight) {
    refreshInFlight = refreshTokens(refreshToken).finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

async function refreshTokens(refreshToken: string): Promise<RefreshedTokens | null> {
  const response = await fetch(`${API_URL}/api/v1/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    await tokenStorage.clear();
    return null;
  }
  const tokens = await response.json() as RefreshedTokens;
  await tokenStorage.set(tokens);
  return tokens;
}
