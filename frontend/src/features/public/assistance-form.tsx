import { useEffect,useRef,useState } from 'react';
import { Platform,Text } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { PublicPage, PublicHeading, publicStyles } from './public-page';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { apiRequest } from '@/lib/api/client';
import { router } from 'expo-router';
import { useAuth } from '@/features/auth/auth-provider';
import { passwordBounds,safeRecoveryToken } from '@/features/auth/security-contracts';
export function AssistanceForm({page,token}:{page:string;token?:string}) {
 const {signOut}=useAuth();const running=useRef(false);const [confirmation,setConfirmation]=useState('');const [complete,setComplete]=useState(false);
 const {language}=useLanguage();const c=publicCopy[language];const [email,setEmail]=useState(''),[value,setValue]=useState(()=>{if(page==='reset-password'&&Platform.OS==='web'&&typeof globalThis.location!=='undefined'){const fragment=new URLSearchParams(globalThis.location.hash.slice(1)).get('token');if(fragment&&safeRecoveryToken(fragment))return fragment;}return token??'';}),[password,setPassword]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);
 const title=page==='contact'?c.contact:page==='verify-email'?c.verify:page==='reset-password'?c.reset:c.forgot;
 useEffect(()=>{if(page!=='reset-password')return;if(Platform.OS!=='web'){if(token)router.setParams({token:undefined});return;}const fragment=new URLSearchParams(globalThis.location.hash.slice(1)).get('token');if(fragment||token){const scrub=()=>globalThis.history.replaceState(globalThis.history.state,'',globalThis.location.pathname);scrub();const timer=setTimeout(scrub,50);return()=>clearTimeout(timer);}},[page,token]);
 async function submit(confirm=false){
  if(running.current||complete)return;
  if((page!=='reset-password'&&!/^\S+@\S+\.\S+$/.test(email.trim()))||(page==='contact'&&value.trim().length<10)||(page==='verify-email'&&confirm&&!/^\d{6}$/.test(value))||(page==='reset-password'&&(!safeRecoveryToken(value)||!passwordBounds(password)||password!==confirmation))){setMessage(c.formError);return;}
  running.current=true;setBusy(true);setMessage('');
  const path=page==='contact'?'support':page==='forgot-password'?'password-reset':page==='reset-password'?'password-reset/confirm':confirm?'email-verification/confirm':'email-verification';
  const body=page==='contact'?{email:email.trim(),message:value}:page==='reset-password'?{token:value,newPassword:password}:confirm?{email:email.trim(),code:value}:{email:email.trim()};
  try{await apiRequest(`/api/v1/account-assistance/${path}`,{method:'POST',authenticated:false,body:JSON.stringify(body)});if(page==='reset-password'){setValue('');setPassword('');setConfirmation('');setComplete(true);await signOut({notice:'passwordReset'});setMessage(language==='fa'?'رمز تغییر کرد و همه نشست‌ها لغو شدند. دوباره وارد شوید.':'Password updated. All sessions were revoked. Sign in again.');}else setMessage(c.requestDone);if(page==='contact')setValue('');}catch{setMessage(c.error);}finally{running.current=false;setBusy(false);}
 }
 return <PublicPage><PublicHeading level={1}>{title}</PublicHeading>
  {page!=='reset-password'&&<AppField label={c.email} value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" style={{writingDirection:'ltr',textAlign:'left'}}/>}
  {page!=='forgot-password'&&page!=='reset-password'&&<AppField label={page==='contact'?c.message:page==='verify-email'?c.code:c.token} value={value} onChangeText={setValue} multiline={page==='contact'} autoCapitalize="none"/>}
  {page==='reset-password'&&!complete&&<><Text>{language==='fa'?'رمز تازه باید ۱۵ تا ۱۲۸ نویسه داشته باشد.':'Choose a new password with 15–128 characters.'}</Text><AppField label={c.password} value={password} onChangeText={setPassword} secureTextEntry autoComplete="new-password" autoCapitalize="none" autoCorrect={false} ltr/><AppField label={language==='fa'?'تکرار رمز تازه':'Confirm new password'} value={confirmation} onChangeText={setConfirmation} secureTextEntry autoComplete="new-password" autoCapitalize="none" autoCorrect={false} ltr/></>}
  {page==='verify-email'&&<AppButton label={c.resend} variant="secondary" loading={busy} onPress={()=>void submit()}/>}
  {!complete&&<AppButton label={page==='verify-email'?c.verify:page==='reset-password'?c.reset:c.send} loading={busy} onPress={()=>void submit(page==='verify-email')}/>}
  {complete&&<AppButton label={language==='fa'?'ورود به حساب':'Sign in'} onPress={()=>router.replace('/(auth)/login')}/>}
  {page==='reset-password'&&!value&&!complete&&<Text accessibilityRole="alert">{language==='fa'?'لینک بازیابی معتبر را از ایمیل باز کنید یا لینک تازه بخواهید.':'Open a valid recovery link from your email, or request a new one.'}</Text>}
  {!!message&&<Text accessibilityRole="alert" style={publicStyles.body}>{message}</Text>}
 </PublicPage>;
}
