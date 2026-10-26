import { Platform } from 'react-native';
import { createContext, PropsWithChildren, useContext, useEffect, useState } from 'react';

import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';

import { CurrentUser, LoginInput, RegisterInput, TokenResponse } from './types';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  status: AuthStatus;
  user: CurrentUser | null;
  pendingRegistrationEmail: string | null;
  signIn: (input: LoginInput) => Promise<void>;
  register: (input: RegisterInput) => Promise<void>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [status, setStatus] = useState<AuthStatus>('loading');
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [pendingRegistrationEmail, setPendingRegistrationEmail] = useState<string | null>(null);

  useEffect(() => tokenStorage.subscribeToClear(() => {
    setUser(null);
    setStatus('unauthenticated');
  }), []);

  useEffect(() => {
    async function restoreSession() {
      const epoch = tokenStorage.generation();
      try {
        const tokens = await tokenStorage.get();
        if (!tokens) throw new Error('No session');
        const currentUser = await apiRequest<CurrentUser>('/api/v1/auth/me');
        if (epoch !== tokenStorage.generation()) return;
        setUser(currentUser);
        setStatus('authenticated');
      } catch {
        if (epoch !== tokenStorage.generation()) return;
        await tokenStorage.clear();
        setUser(null);
        setStatus('unauthenticated');
      }
    }
    void restoreSession();
  }, []);

  async function signIn(input: LoginInput) {
    await tokenStorage.clear();
    const epoch = tokenStorage.generation();
    setUser(null); setStatus('loading');
    let tokens: TokenResponse;
    try { tokens = await apiRequest<TokenResponse>(Platform.OS === 'web' ? '/api/v1/auth/web/login' : '/api/v1/auth/login', {
      method: 'POST',
      authenticated: false,
      body: JSON.stringify(input),
    });
    } catch (error) { if (epoch === tokenStorage.generation()) setStatus('unauthenticated'); throw error; }
    if (epoch !== tokenStorage.generation()) throw new Error('Sign-in was cancelled. Please try again.');
    try {
      await tokenStorage.set(tokens);
      const currentUser = await apiRequest<CurrentUser>('/api/v1/auth/me');
      if (epoch !== tokenStorage.generation()) return;
      setUser(currentUser);
      setStatus('authenticated');
      setPendingRegistrationEmail(null);
    } catch (error) {
      if (epoch === tokenStorage.generation()) await tokenStorage.clear();
      throw error;
    }
  }

  async function register(input: RegisterInput) {
    await apiRequest('/api/v1/auth/register', {
      method: 'POST',
      authenticated: false,
      body: JSON.stringify(input),
    });
    setPendingRegistrationEmail(input.email.trim());
  }

  async function signOut() {
    setPendingRegistrationEmail(null);
    const tokens = await tokenStorage.get();
    await tokenStorage.clear();
    setUser(null);
    setStatus('unauthenticated');
    if (tokens?.refreshToken) {
        await apiRequest(Platform.OS === 'web' ? '/api/v1/auth/web/logout' : '/api/v1/auth/logout', {
          method: 'POST',
          authenticated: false,
          body: JSON.stringify({ refreshToken: tokens.refreshToken }),
        });
    }
  }

  const value = { status, user, pendingRegistrationEmail, signIn, register, signOut };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
