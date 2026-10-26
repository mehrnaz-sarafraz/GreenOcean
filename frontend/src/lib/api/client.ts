import { Platform } from 'react-native';

import { browserSessionLock } from './browser-session';
import { fetchWithTimeout, singleFlight } from './request';
import { tokenStorage } from '@/lib/storage/token-storage';

const fallbackUrl = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
export const API_URL = Platform.OS==='web'&&process.env.EXPO_PUBLIC_WEB_API_ORIGIN==='same-origin'?(typeof globalThis.location==='undefined'?'':globalThis.location.origin):process.env.EXPO_PUBLIC_API_URL??fallbackUrl;

export function assertApiTransport(){if(!__DEV__&&!API_URL.startsWith('https://'))throw new Error('A secure HTTPS API connection is required.');}

export class ApiError extends Error {
  constructor(public status: number, message: string, public fieldErrors: { field: string; message: string }[] = [], public retryAfter: number | null = null) {
    super(message);
  }
}

type RequestOptions = RequestInit & { authenticated?: boolean; retryAfterRefresh?: boolean };

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  assertApiTransport();
  const { authenticated = true, retryAfterRefresh = true, ...requestInit } = options;
  const requestEpoch = tokenStorage.generation();
  const tokens = await tokenStorage.get();
  const headers = new Headers(requestInit.headers);
  if (Platform.OS === 'web') headers.set('X-GreenOcean-CSRF', '1');
  headers.set('Accept', 'application/json');
  if (typeof requestInit.body === 'string' && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (authenticated && tokens?.accessToken) headers.set('Authorization', `Bearer ${tokens.accessToken}`);

  const send=()=>fetchWithTimeout(`${API_URL}${path}`,{...requestInit,headers,credentials:Platform.OS==='web'?'include':'omit'});
  let response=await(Platform.OS==='web'&&(path==='/api/v1/auth/web/login'||path==='/api/v1/auth/web/mfa/authentication/verify')?browserSessionLock(async()=>{if(requestEpoch!==tokenStorage.generation())throw new ApiError(409,'Sign-in cancelled.');return send();}):send());
  if (response.status === 401 && authenticated && retryAfterRefresh && tokens?.refreshToken) {
    const refreshed = await refreshTokens(tokens.refreshToken);
    if (refreshed && requestEpoch===tokenStorage.generation()) {
      headers.set('Authorization', `Bearer ${refreshed.accessToken}`);
      response = await fetchWithTimeout(`${API_URL}${path}`, { ...requestInit, headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
    }
  }

  if(authenticated&&requestEpoch!==tokenStorage.generation())throw new ApiError(409,'This request belongs to a previous sign-in. Please retry.');
  if (!response.ok) {
    if (response.status === 401 && authenticated && requestEpoch === tokenStorage.generation()) await tokenStorage.clear(tokens?.accessToken?'sessionExpired':undefined);
    const body = await response.json().catch(() => null) as { message?: string; fieldErrors?: { field: string; message: string }[] } | null;
    const retry = Number(response.headers.get('Retry-After'));
    throw new ApiError(response.status, body?.message ?? `Request failed with status ${response.status}`, Array.isArray(body?.fieldErrors) ? body.fieldErrors : [], retry > 0 ? retry : null);
  }
  if (response.status === 204) return undefined as T;
  const value=await response.json() as T;
  if(authenticated&&requestEpoch!==tokenStorage.generation())throw new ApiError(409,'This request belongs to a previous sign-in. Please retry.');
  return value;
}

export async function apiDownloadText(path: string): Promise<{ filename: string; text: string }> {
  assertApiTransport();
  const requestEpoch = tokenStorage.generation();
  const tokens = await tokenStorage.get();
  const headers = new Headers({ Accept: 'text/csv' });
  if (tokens?.accessToken) headers.set('Authorization', `Bearer ${tokens.accessToken}`);
  let response = await fetchWithTimeout(`${API_URL}${path}`, { headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
  if (response.status === 401 && tokens?.refreshToken) {
    const refreshed = await refreshTokens(tokens.refreshToken);
    if (refreshed && requestEpoch===tokenStorage.generation()) {
      headers.set('Authorization', `Bearer ${refreshed.accessToken}`);
      response = await fetchWithTimeout(`${API_URL}${path}`, { headers, credentials: Platform.OS === 'web' ? 'include' : 'omit' });
    }
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;
    if (response.status === 401 && requestEpoch === tokenStorage.generation()) await tokenStorage.clear(tokens?.accessToken?'sessionExpired':undefined);
    throw new ApiError(response.status, body?.message ?? `Download failed with status ${response.status}`);
  }
  if(requestEpoch!==tokenStorage.generation())throw new ApiError(409,'Download cancelled after sign-out.');
  const disposition = response.headers.get('Content-Disposition') || '';
  const filename = disposition.match(/filename="?([^";]+)"?/i)?.[1] || 'greenocean-export.csv';
  const text=await response.text();if(requestEpoch!==tokenStorage.generation())throw new ApiError(409,'Download cancelled after sign-out.');return {filename,text};
}

const refreshTokens=singleFlight(async(refreshToken:string)=>{
  if(Platform.OS==='web')return browserSessionLock(()=>performRefresh(refreshToken));
  return performRefresh(refreshToken);
});
async function performRefresh(refreshToken: string) {
  assertApiTransport();
  const epoch = tokenStorage.generation();
  const response = await fetchWithTimeout(`${API_URL}/api/v1/auth/${Platform.OS === 'web' ? 'web/refresh' : 'refresh'}`, {
    method: 'POST',
    credentials: Platform.OS === 'web' ? 'include' : 'omit',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', 'X-GreenOcean-CSRF': '1' },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    if(response.status!==401&&response.status!==403)throw new ApiError(response.status,'Could not confirm your session. Please retry.');
    if (epoch === tokenStorage.generation()){const active=await tokenStorage.get();await tokenStorage.clear(active?.accessToken?'sessionExpired':undefined);}
    return null;
  }
  const tokens = await response.json() as { accessToken: string; refreshToken: string };
  if (epoch !== tokenStorage.generation()) return null;
  await tokenStorage.set(tokens);
  return tokens;
}
