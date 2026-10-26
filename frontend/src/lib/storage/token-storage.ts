import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';
const ACCESS_TOKEN_KEY = 'greenocean.accessToken';
const REFRESH_TOKEN_KEY = 'greenocean.refreshToken';
export type StoredTokens = { accessToken: string; refreshToken: string };
let browserTokens: StoredTokens | null = null;
let epoch = 0;
const clearListeners = new Set<() => void>();
function purgeLegacyWebStorage() {
  if (Platform.OS !== 'web') return;
  try {
    for (const store of [globalThis.localStorage, globalThis.sessionStorage]) {
      store?.removeItem(ACCESS_TOKEN_KEY); store?.removeItem(REFRESH_TOKEN_KEY);
    }
  } catch { /* Storage can be disabled; web sessions do not need it. */ }
}
export const tokenStorage = {
  generation: () => epoch,
  subscribeToClear(listener: () => void) {
    clearListeners.add(listener);
    return () => { clearListeners.delete(listener); };
  },
  async get(): Promise<StoredTokens | null> {
    if (Platform.OS === 'web') { purgeLegacyWebStorage(); return browserTokens ?? { accessToken: '', refreshToken: 'cookie' }; }
    const [accessToken, refreshToken] = await Promise.all([SecureStore.getItemAsync(ACCESS_TOKEN_KEY), SecureStore.getItemAsync(REFRESH_TOKEN_KEY)]);
    return accessToken && refreshToken ? { accessToken, refreshToken } : null;
  },
  async set(tokens: StoredTokens) {
    if (Platform.OS === 'web') { purgeLegacyWebStorage(); browserTokens = { accessToken: tokens.accessToken, refreshToken: 'cookie' }; return; }
    await Promise.all([SecureStore.setItemAsync(ACCESS_TOKEN_KEY, tokens.accessToken), SecureStore.setItemAsync(REFRESH_TOKEN_KEY, tokens.refreshToken)]);
  },
  async clear() {
    epoch += 1; browserTokens = null;
    clearListeners.forEach(listener => listener());
    if (Platform.OS === 'web') { purgeLegacyWebStorage(); return; }
    await Promise.all([SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY), SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY)]);
  },
};
