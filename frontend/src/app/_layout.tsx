import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useFonts } from '@expo-google-fonts/material-symbols/useFonts';
import { MaterialSymbols_400Regular } from '@expo-google-fonts/material-symbols/400Regular';
import { MaterialSymbols_600SemiBold } from '@expo-google-fonts/material-symbols/600SemiBold';

import { AuthProvider, useAuth } from '@/features/auth/auth-provider';
import { DataProvider } from '@/features/platform/data-provider';
import { LanguageProvider } from '@/localization/language-provider';
import { Platform,Text,View } from 'react-native';
import { useEffect,useRef,useState } from 'react';
import * as ScreenCapture from 'expo-screen-capture';
import { AppButton } from '@/components/app-button';
import { useLanguage } from '@/localization/language-provider';
import { tokenStorage } from '@/lib/storage/token-storage';
import { apiRequest } from '@/lib/api/client';
import { colors } from '@/theme/tokens';

function AccountData({ children }: { children: React.ReactNode }) {
  const {user,status,privacyHidden,notice,retryLogout,retrySession,clearNotice,signOut}=useAuth();const {language}=useLanguage();const lastSignal=useRef(0);const fa=language==='fa';const [protectedReady,setProtectedReady]=useState(-1);const [protectionFailed,setProtectionFailed]=useState(false);
  useEffect(()=>{if(Platform.OS==='web'||status!=='authenticated')return;let active=true;const epoch=tokenStorage.generation();Promise.all([ScreenCapture.preventScreenCaptureAsync('private-account'),Platform.OS==='ios'?ScreenCapture.enableAppSwitcherProtectionAsync(1):Promise.resolve()]).then(()=>{if(active)setProtectedReady(epoch);}).catch(()=>{if(active)setProtectionFailed(true);});return()=>{active=false;void ScreenCapture.allowScreenCaptureAsync('private-account').catch(()=>undefined);if(Platform.OS==='ios')void ScreenCapture.disableAppSwitcherProtectionAsync().catch(()=>undefined);};},[status]);
  const messages:Record<string,string>={sessionExpired:fa?'نشست پایان یافته است. برای ادامه دوباره وارد شوید.':'Your session has expired. Sign in again to continue.',connectionCheck:fa?'اتصال برای بررسی نشست برقرار نشد. دوباره تلاش کنید یا وارد شوید.':'Could not confirm your session because of the connection. Retry or sign in.',signedOut:fa?'از حساب خارج شدید.':'You are signed out.',logoutPending:fa?'داده‌های خصوصی در این دستگاه بسته شدند؛ خروج از سرور هنوز تأیید نشده است. اتصال را برقرار کنید و دوباره تلاش کنید.':'Private data is hidden on this device. Server sign-out is not confirmed. Reconnect and retry before signing in.',passwordChanged:fa?'رمز تغییر کرد و همه دستگاه‌ها خارج شدند. دوباره وارد شوید.':'Password updated. All devices were signed out. Sign in again.',passwordReset:fa?'بازیابی تکمیل شد و همه نشست‌ها لغو شدند. دوباره وارد شوید.':'Account recovery completed. All sessions were revoked. Sign in again.',privacyLocked:fa?'برای حفظ حریم خصوصی پس از بازگشت، دوباره وارد شوید.':'Sign in again after returning to protect your private content.',allSignedOut:fa?'همه دستگاه‌ها خارج شدند.':'All devices were signed out.'};
  function activity(){if(status!=='authenticated'||privacyHidden||Date.now()-lastSignal.current<60000)return;lastSignal.current=Date.now();void apiRequest('/api/v1/auth/activity',{method:'POST'}).catch(()=>undefined);}
  useEffect(()=>{if(Platform.OS!=='web'||status!=='authenticated')return;const signal=()=>activity();globalThis.addEventListener('pointerdown',signal);globalThis.addEventListener('keydown',signal);return()=>{globalThis.removeEventListener('pointerdown',signal);globalThis.removeEventListener('keydown',signal);};},[status,privacyHidden]);
  if(status==='authenticated'&&Platform.OS!=='web'&&protectedReady!==tokenStorage.generation())return <View style={{padding:24}}><Text>{protectionFailed?(fa?'محافظت از تصویر صفحه در دسترس نیست. برای ادامه امن از حساب خارج شوید.':'Screen privacy protection is unavailable. Sign out to continue safely.'):(fa?'در حال فعال‌سازی محافظت صفحه…':'Preparing screen privacy protection…')}</Text>{protectionFailed&&<AppButton label={fa?'خروج':'Sign out'} onPress={()=>void signOut()}/>}</View>;
  return <View style={{flex:1}} onTouchStart={activity}>
    {!!notice&&<View style={{padding:12,gap:8}}><Text accessibilityRole="alert">{messages[notice]??notice}</Text>{notice==='connectionCheck'?<AppButton label={fa?'بررسی دوباره نشست':'Retry session check'} onPress={()=>void retrySession()}/>:notice==='logoutPending'?<AppButton label={fa?'تلاش دوباره برای خروج':'Retry server sign-out'} onPress={()=>void retryLogout()}/>:<AppButton label={fa?'بستن پیام':'Dismiss message'} variant="secondary" onPress={clearNotice}/>}</View>}
    {privacyHidden&&<Text>{fa?'محتوای خصوصی پنهان است.':'Private content is hidden.'}</Text>}
    <View style={{flex:1,display:privacyHidden?'none':'flex'}} accessibilityElementsHidden={privacyHidden} importantForAccessibility={privacyHidden?'no-hide-descendants':'auto'}><DataProvider key={user?.userId??'visitor'}>{children}</DataProvider></View>
  </View>;
}

export default function RootLayout() {
  useFonts({ MaterialSymbols_400Regular, MaterialSymbols_600SemiBold });
  // Public urgent information remains usable if decorative icon fonts fail to load.
  return (
    <LanguageProvider>
      <AuthProvider>
        <AccountData>
          <StatusBar style="dark" />
          <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.foam } }} />
        </AccountData>
      </AuthProvider>
    </LanguageProvider>
  );
}
