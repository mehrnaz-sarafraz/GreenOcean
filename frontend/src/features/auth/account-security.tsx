import { useEffect,useRef,useState } from 'react';
import { Platform,Text,View } from 'react-native';
import { router } from 'expo-router';
import { AppField } from '@/components/app-field';
import { AppButton } from '@/components/app-button';
import { apiRequest } from '@/lib/api/client';
import { useLanguage } from '@/localization/language-provider';
import { useAuth } from './auth-provider';
import { passwordBounds,securityEventLabel } from './security-contracts';
import { securityKey } from './webauthn';
import { colors } from '@/theme/tokens';
const card={gap:12,padding:16,borderWidth:1,borderColor:colors.border,borderRadius:16} as const;
function ErrorMessage({message}:{message:string}){return message?<Text accessibilityRole="alert">{message}</Text>:null;}
export function AccountSecurityPanel(){
 const {language}=useLanguage(),fa=language==='fa';const {signOut,user}=useAuth();const [current,setCurrent]=useState(''),[next,setNext]=useState(''),[confirm,setConfirm]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);const running=useRef(false);
 const [deletion,setDeletion]=useState(''),[phrase,setPhrase]=useState('');const [events,setEvents]=useState<{action:string;created_at:string}[]>([]),[eventError,setEventError]=useState('');
 function activity(){setEventError('');void apiRequest<typeof events>('/api/v1/auth/security-events').then(setEvents).catch(()=>setEventError(fa?'رویدادها بارگذاری نشدند.':'Could not load security activity.'));}
 useEffect(()=>{const timer=setTimeout(activity,0);return()=>clearTimeout(timer);},[]);
 async function run(action:()=>Promise<void>){if(running.current)return;running.current=true;setBusy(true);setMessage('');try{await action();}catch(error){setMessage(fa?'عملیات تأیید نشد. رمز و اتصال را بررسی کنید؛ در صورت نیاز کلید امنیتی را دوباره تأیید کنید.':error instanceof Error?error.message:'Please retry.');}finally{running.current=false;setBusy(false);}}
 return <View style={{gap:20}}>
  <Text>{fa?'رمز یکتا با ۱۵ تا ۱۲۸ نویسه انتخاب کنید. تغییر رمز همه دستگاه‌ها، از جمله این دستگاه را خارج می‌کند.':'Choose a unique password with 15–128 characters. Changing it signs out every device, including this one.'}</Text>
  <View style={card}>
   <AppField label={fa?'رمز فعلی':'Current password'} value={current} onChangeText={setCurrent} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr editable={!busy}/>
   <AppField label={fa?'رمز تازه':'New password'} value={next} onChangeText={setNext} secureTextEntry autoComplete="new-password" autoCapitalize="none" autoCorrect={false} ltr editable={!busy} error={next&&!passwordBounds(next)?(fa?'۱۵ تا ۱۲۸ نویسه وارد کنید.':'Use 15–128 characters.'):undefined}/>
   <AppField label={fa?'تکرار رمز تازه':'Confirm new password'} value={confirm} onChangeText={setConfirm} secureTextEntry autoComplete="new-password" autoCapitalize="none" autoCorrect={false} ltr editable={!busy}/>
   <AppButton label={fa?'تغییر رمز':'Update password'} loading={busy} disabled={!current||!passwordBounds(next)||next!==confirm} onPress={()=>void run(async()=>{await apiRequest('/api/v1/auth/change-password',{method:'POST',retryAfterRefresh:false,body:JSON.stringify({currentPassword:current,newPassword:next})});setCurrent('');setNext('');setConfirm('');await signOut({notice:'passwordChanged'});router.replace('/(auth)/login');})}/>
  </View>
  <ErrorMessage message={message}/>
  <AppButton label={fa?'نشست‌های فعال':'Active sessions'} variant="secondary" onPress={()=>router.push({pathname:'/(app)/tools/[tool]',params:{tool:'session-management'}})}/>
  <AppButton label={fa?'دانلود داده‌های من':'Download my data'} variant="secondary" onPress={()=>router.push({pathname:'/(app)/tools/[tool]',params:{tool:'data-export'}})}/>
  {user?.roles.some(role=>role==='ADMIN'||role==='MODERATOR')&&<StaffKeys/>}
  <Text accessibilityRole="header">{fa?'فعالیت امنیتی اخیر':'Recent security activity'}</Text>
  {events.map((event,n)=><Text key={`${event.created_at}-${n}`}>{securityEventLabel(event.action,fa)} · {new Date(event.created_at).toLocaleString(fa?'fa':'en')}</Text>)}
  <ErrorMessage message={eventError}/><AppButton label={fa?'بارگذاری دوباره رویدادها':'Reload security activity'} variant="secondary" onPress={activity}/>
  <Text accessibilityRole="header">{fa?'حذف دائمی حساب':'Delete account permanently'}</Text>
  <Text>{fa?'این کار نشست‌ها را لغو و داده‌های حساب را طبق سیاست حذف می‌کند. در صورت نیاز ابتدا نسخه داده‌ها را دانلود کنید.':'This revokes sessions and erases account data under the existing deletion policy. Download your data first if needed.'}</Text>
  <AppField label={fa?'رمز برای حذف حساب':'Password to delete your account'} value={deletion} onChangeText={setDeletion} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr/>
  <AppField label={fa?'برای تأیید DELETE بنویسید':'Type DELETE to confirm'} value={phrase} onChangeText={setPhrase} ltr/>
  <AppButton label={fa?'حذف دائمی حساب من':'Permanently delete my account'} variant="secondary" loading={busy} disabled={phrase!=='DELETE'||!deletion} onPress={()=>void run(async()=>{await apiRequest('/api/v1/account',{method:'DELETE',retryAfterRefresh:false,body:JSON.stringify({password:deletion})});setDeletion('');setPhrase('');await signOut();router.replace('/(auth)/welcome');})}/>
 </View>;
}
export function SessionsPanel(){
 const {language}=useLanguage(),fa=language==='fa';const {signOut,user}=useAuth();type Session={id:string;deviceName:string|null;createdAt:string;lastUsedAt:string|null;expiresAt:string;current:boolean};
 const [sessions,setSessions]=useState<Session[]>([]),[loading,setLoading]=useState(true),[error,setError]=useState(''),[proof,setProof]=useState(''),[busy,setBusy]=useState(false),[selected,setSelected]=useState<string|null>(null);const running=useRef(false);
 async function load(){setLoading(true);setError('');try{setSessions(await apiRequest<Session[]>('/api/v1/auth/sessions'));}catch{setError(fa?'نشست‌ها بارگذاری نشدند. دوباره تلاش کنید.':'Could not load sessions. Please retry.');}finally{setLoading(false);}}
 useEffect(()=>{const timer=setTimeout(()=>void load(),0);return()=>clearTimeout(timer);},[]);
 async function revoke(){if(running.current||!selected)return;running.current=true;setBusy(true);setError('');try{
  await apiRequest('/api/v1/auth/reauthenticate',{method:'POST',retryAfterRefresh:false,body:JSON.stringify({password:proof})});setProof('');
  const current=selected==='all'||sessions.some(s=>s.id===selected&&s.current);
  await apiRequest(selected==='all'?'/api/v1/auth/logout-all':`/api/v1/auth/sessions/${selected}`,{method:selected==='all'?'POST':'DELETE',retryAfterRefresh:false});setSelected(null);
  if(current){await signOut({notice:selected==='all'?'allSignedOut':'signedOut'});router.replace('/(auth)/login');}else await load();
 }catch(error){setError(fa?'خروج تأیید نشد. رمز، اتصال و در حساب کارکنان کلید امنیتی را بررسی کنید.':error instanceof Error?error.message:'Please retry.');}finally{running.current=false;setBusy(false);}}
 return <View style={{gap:16}}>
  {loading&&<Text accessibilityLiveRegion="polite">{fa?'در حال بارگذاری نشست‌ها…':'Loading sessions…'}</Text>}
  {sessions.map(session=><View key={session.id} style={card}><Text>{session.deviceName||(fa?'برچسب دستگاه مشخص نیست':'Unlabeled device')} {session.current?(fa?'(این دستگاه)':'(Current device)'):''}</Text><Text>{fa?'ورود: ':'Signed in: '}{new Date(session.createdAt).toLocaleString()}</Text><Text>{fa?'آخرین فعالیت: ':'Last meaningful activity: '}{new Date(session.lastUsedAt??session.createdAt).toLocaleString()}</Text><Text>{fa?'پایان نشست: ':'Overall expiry: '}{new Date(session.expiresAt).toLocaleString()}</Text><AppButton label={session.current?(fa?'خروج این دستگاه':'Sign out this device'):(fa?'خروج دستگاه':'Sign out device')} variant="secondary" disabled={busy} onPress={()=>setSelected(session.id)}/></View>)}
  {!loading&&!sessions.length&&!error&&<Text>{fa?'نشست فعالی وجود ندارد.':'No active sessions.'}</Text>}
  <Text>{fa?'این نام، برچسب واردشده هنگام ورود است و هویت سخت‌افزار یا مکان تأییدشده نیست.':'Device names are labels supplied at sign-in, not verified hardware or location.'}</Text>
  <AppButton label={fa?'بارگذاری دوباره نشست‌ها':'Reload sessions'} variant="secondary" onPress={()=>void load()} disabled={busy}/>
  <AppButton label={fa?'خروج همه دستگاه‌ها':'Sign out all devices'} variant="secondary" onPress={()=>setSelected('all')} disabled={busy||!sessions.length}/>
  {selected&&<View style={card}><Text accessibilityRole="header">{selected==='all'?(fa?'خروج همه دستگاه‌ها، از جمله این دستگاه را تأیید می‌کنید؟':'Confirm signing out every device, including this one?'):(fa?'خروج دستگاه انتخاب‌شده را تأیید می‌کنید؟':'Confirm signing out the selected device?')}</Text><AppField label={fa?'رمز برای تأیید خروج':'Password to confirm sign-out'} value={proof} onChangeText={setProof} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr/><AppButton label={fa?'تأیید خروج':'Confirm sign-out'} loading={busy} disabled={!proof} onPress={()=>void revoke()}/><AppButton label={fa?'انصراف':'Cancel'} variant="secondary" disabled={busy} onPress={()=>{setSelected(null);setProof('');}}/></View>}
  <ErrorMessage message={error}/>
  {user?.roles.some(role=>role==='ADMIN'||role==='MODERATOR')&&<StaffKeys/>}
 </View>;
}
function StaffKeys(){
 const {language}=useLanguage(),fa=language==='fa';const {signOut}=useAuth();const [state,setState]=useState<{available:boolean;credentials:{credential_id:string;label:string}[]}|null>(null),[permit,setPermit]=useState(''),[proof,setProof]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);const running=useRef(false);
 function load(){void apiRequest<NonNullable<typeof state>>('/api/v1/auth/mfa').then(setState).catch(()=>setMessage(fa?'وضعیت کلید امنیتی در دسترس نیست.':'Security key status is unavailable.'));}
 useEffect(()=>{const timer=setTimeout(load,0);return()=>clearTimeout(timer);},[]);
 async function run(action:()=>Promise<void>){if(running.current)return;running.current=true;setBusy(true);setMessage('');try{await apiRequest('/api/v1/auth/reauthenticate',{method:'POST',retryAfterRefresh:false,body:JSON.stringify({password:proof})});setProof('');await action();setPermit('');load();}catch{setMessage(fa?'عملیات کلید امنیتی تأیید نشد. دستگاه، رمز و تنظیمات مجاز را بررسی کنید.':'Security key operation was not confirmed. Check your key, password and approved configuration.');}finally{running.current=false;setBusy(false);}}
 return <View style={card}><Text accessibilityRole="header">{fa?'کلید امنیتی کارکنان':'Staff security keys'}</Text>
  {!state?.available||Platform.OS!=='web'?<Text>{fa?'دسترسی قوی کارکنان در این محیط آماده نیست. دسترسی مدیریتی بسته می‌ماند؛ از مرورگر HTTPS مجاز استفاده کنید.':'Staff MFA is unavailable in this environment. Privileged access remains disabled; use an approved HTTPS browser.'}</Text>:<>
  <Text>{fa?'ایمیل یا بازیابی رمز جایگزین کلید امنیتی نیست. برای نخستین ثبت، مجوز مستقل مسئول سامانه لازم است.':'Email and password recovery do not replace a security key. First enrollment requires a separate operator-issued permit.'}</Text>
  <AppField label={fa?'رمز برای کلید امنیتی':'Password for security key'} value={proof} onChangeText={setProof} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr/>
  {!state.credentials.length&&<AppField label={fa?'مجوز ثبت کلید':'Enrollment permit'} value={permit} onChangeText={setPermit} secureTextEntry autoComplete="off" autoCapitalize="none" autoCorrect={false} ltr/>}
  <AppButton label={fa?'تأیید کلید امنیتی':'Verify security key'} disabled={!state.credentials.length||!proof} loading={busy} onPress={()=>void run(async()=>{await securityKey(false);setMessage(fa?'کلید تأیید شد.':'Security key verified.');})}/>
  <AppButton label={fa?'ثبت کلید امنیتی':'Register security key'} disabled={!proof||(!state.credentials.length&&!permit)} loading={busy} onPress={()=>void run(async()=>{await securityKey(true,permit);setMessage(fa?'کلید ثبت شد. برای دسترسی آن را تأیید کنید.':'Security key registered. Verify it before privileged access.');})}/>
  {state.credentials.map(key=><View key={key.credential_id}><Text>{key.label}</Text><AppButton label={fa?'حذف کلید و خروج همه نشست‌ها':'Remove key and sign out all sessions'} disabled={state.credentials.length<2||!proof} variant="secondary" loading={busy} onPress={()=>void run(async()=>{await apiRequest(`/api/v1/auth/mfa/${encodeURIComponent(key.credential_id)}`,{method:'DELETE',retryAfterRefresh:false});await signOut();router.replace('/(auth)/login');})}/></View>)}
  </>}
  <ErrorMessage message={message}/>
 </View>;
}
