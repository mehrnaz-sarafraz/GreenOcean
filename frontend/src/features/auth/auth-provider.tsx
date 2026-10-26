import { AppState, Platform } from 'react-native';
import { createContext, PropsWithChildren, useContext, useEffect, useRef, useState } from 'react';

import { apiRequest,API_URL } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';
import { browserSessionLock } from '@/lib/api/browser-session';
import { fetchWithTimeout } from '@/lib/api/request';

import { CurrentUser, LoginInput, RegisterInput, TokenResponse } from './types';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  status: AuthStatus;
  user: CurrentUser | null;
  pendingRegistrationEmail: string | null;
  pendingEmailCorrected: boolean;
  notice:string;
  clearNotice:()=>void;
  retryLogout:()=>Promise<void>;
  retrySession:()=>Promise<void>;
  privacyHidden:boolean;
  signIn: (input: LoginInput) => Promise<void>;
  register: (input: RegisterInput) => Promise<void>;
  signOut: (options?: { correctedEmail?: string; notice?:string }) => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [notice,setNotice]=useState('');const [privacyHidden,setPrivacyHidden]=useState(false);const backgroundAt=useRef<number|null>(null);
  const [status, setStatus] = useState<AuthStatus>('loading');
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [pendingRegistrationEmail, setPendingRegistrationEmail] = useState<string | null>(null);
  const [pendingEmailCorrected, setPendingEmailCorrected] = useState(false);

  useEffect(() => tokenStorage.subscribeToClear((reason) => {
    if(reason)setNotice(reason);
    setUser(null);
    setStatus('unauthenticated');
  }), []);

  async function signIn(input: LoginInput) {
    await reconcileLogout();setPrivacyHidden(false);setNotice('');
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
      await tokenStorage.set(tokens);tokenStorage.announceLogin();
      const currentUser = await apiRequest<CurrentUser>('/api/v1/auth/me');
      if (epoch !== tokenStorage.generation()) return;
      setUser(currentUser);
      setStatus('authenticated');
      setPendingRegistrationEmail(null);
      setPendingEmailCorrected(false);
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
    setPendingEmailCorrected(false);
  }

  async function revoke(refreshToken:string){
    const send=()=>fetchWithTimeout(`${API_URL}/api/v1/auth/${Platform.OS==='web'?'web/logout':'logout'}`,{method:'POST',credentials:Platform.OS==='web'?'include':'omit',headers:{'Content-Type':'application/json','X-GreenOcean-CSRF':'1'},body:JSON.stringify({refreshToken})});
    const response=await(Platform.OS==='web'?browserSessionLock(send):send());
    if(!response.ok)throw new Error('Sign-out could not be confirmed');
  }
  async function reconcileLogout(){const pending=await tokenStorage.pendingLogout();if(pending){await revoke(pending);await tokenStorage.allowRestore();}}
  async function retrySession(){setStatus('loading');try{await reconcileLogout();const current=await apiRequest<CurrentUser>('/api/v1/auth/me');setUser(current);setStatus('authenticated');setNotice('');}catch{setUser(null);setStatus('unauthenticated');setNotice('connectionCheck');}}
  async function retryLogout(){try{await reconcileLogout();setNotice('signedOut');}catch{setNotice('logoutPending');}}
  async function signOut(options?: { correctedEmail?:string;notice?:string }) {
    setPendingRegistrationEmail(options?.correctedEmail??null);setPendingEmailCorrected(Boolean(options?.correctedEmail));
    let tokens:Awaited<ReturnType<typeof tokenStorage.get>>=null;let storageFailed=false;try{tokens=await tokenStorage.get();}catch{storageFailed=true;}
    // Establish the guard before clearing so no tab/reload can silently restore the cookie.
    try{await tokenStorage.blockRestore(tokens?.refreshToken);}catch{storageFailed=true;}finally{try{await tokenStorage.clear();}catch{storageFailed=true;}setUser(null);setStatus('unauthenticated');setPrivacyHidden(false);setNotice(storageFailed?'logoutPending':options?.notice??(options?.correctedEmail?'':'signedOut'));}
    try{if(storageFailed&&!tokens?.refreshToken)throw new Error('Secure storage needs retry');if(tokens?.refreshToken)await revoke(tokens.refreshToken);await tokenStorage.allowRestore();}catch{setNotice('logoutPending');}
  }
  useEffect(() => {
    async function restoreSession() {
      try{await reconcileLogout();}catch{setNotice('logoutPending');setStatus('unauthenticated');return;}
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
        setNotice('connectionCheck');
        setUser(null);
        setStatus('unauthenticated');
      }
    }
    void restoreSession();
  }, []);

  useEffect(()=>{
    if(Platform.OS==='web')return;
    const subscription=AppState.addEventListener('change',next=>{
      if(next!=='active'){if(backgroundAt.current===null)backgroundAt.current=Date.now();if(status==='authenticated')setPrivacyHidden(true);}
      else{const elapsed=backgroundAt.current===null?0:Date.now()-backgroundAt.current;backgroundAt.current=null;
        if(status==='authenticated'&&elapsed>=300000)void signOut({notice:'privacyLocked'});else setPrivacyHidden(false);
      }
    });return()=>subscription.remove();
  },[status]);
  useEffect(()=>{if(Platform.OS!=='web')return;const listener=()=>{void retryLogout();};globalThis.addEventListener('online',listener);return()=>globalThis.removeEventListener('online',listener);},[]);

  const value = { status, user, pendingRegistrationEmail, pendingEmailCorrected, notice, clearNotice:()=>setNotice(''),retryLogout,retrySession,privacyHidden, signIn, register, signOut };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
