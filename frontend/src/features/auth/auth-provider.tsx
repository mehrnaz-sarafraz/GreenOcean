import { createContext, PropsWithChildren, useContext, useEffect, useState } from 'react';

import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';

import { CurrentUser, LoginInput, RegisterInput, TokenResponse } from './types';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  status: AuthStatus;
  user: CurrentUser | null;
  signIn: (input: LoginInput) => Promise<void>;
  register: (input: RegisterInput) => Promise<void>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [status, setStatus] = useState<AuthStatus>('loading');
  const [user, setUser] = useState<CurrentUser | null>(null);

  useEffect(() => {
    async function restoreSession() {
      try {
        const tokens = await tokenStorage.get();
        if (!tokens) throw new Error('No session');
        const currentUser = await apiRequest<CurrentUser>('/api/v1/auth/me');
        setUser(currentUser);
        setStatus('authenticated');
      } catch {
        await tokenStorage.clear();
        setUser(null);
        setStatus('unauthenticated');
      }
    }
    void restoreSession();
  }, []);

  async function signIn(input: LoginInput) {
    const tokens = await apiRequest<TokenResponse>('/api/v1/auth/login', {
      method: 'POST',
      authenticated: false,
      body: JSON.stringify(input),
    });
    await tokenStorage.set(tokens);
    const currentUser = await apiRequest<CurrentUser>('/api/v1/auth/me');
    setUser(currentUser);
    setStatus('authenticated');
  }

  async function register(input: RegisterInput) {
    await apiRequest('/api/v1/auth/register', {
      method: 'POST',
      authenticated: false,
      body: JSON.stringify(input),
    });
    await signIn({ email: input.email, password: input.password, deviceName: 'GreenOcean mobile app' });
  }

  async function signOut() {
    const tokens = await tokenStorage.get();
    try {
      if (tokens?.refreshToken) {
        await apiRequest('/api/v1/auth/logout', {
          method: 'POST',
          authenticated: false,
          body: JSON.stringify({ refreshToken: tokens.refreshToken }),
        });
      }
    } finally {
      await tokenStorage.clear();
      setUser(null);
      setStatus('unauthenticated');
    }
  }

  const value = { status, user, signIn, register, signOut };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
