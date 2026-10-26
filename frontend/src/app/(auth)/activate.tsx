import { router, useFocusEffect } from 'expo-router';
import { useCallback,useRef,useState } from 'react';
import { Pressable,Text } from 'react-native';
import { Screen } from '@/components/screen';
import { AppField } from '@/components/app-field';
import { AppButton } from '@/components/app-button';
import { ApiError, apiRequest } from '@/lib/api/client';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
export default function ActivateScreen(){
 const {user,signIn,signOut,pendingRegistrationEmail}=useAuth();
 const {language}=useLanguage();const fa=language==='fa';const running=useRef(false);
 const [email,setEmail]=useState(user?.email??pendingRegistrationEmail??'');const [password,setPassword]=useState('');const [code,setCode]=useState('');
 const [country,setCountry]=useState('');const [adult,setAdult]=useState(false);const [accepted,setAccepted]=useState(false);
 const [query,setQuery]=useState('');const [countries,setCountries]=useState<{code:string;name:string}[]>([]);
 const [terms,setTerms]=useState(0);const [busy,setBusy]=useState(false);const [message,setMessage]=useState('');
 const [detailsFailed,setDetailsFailed]=useState(false);const loadEpoch=useRef(0);const version=useRef(0);
 const load=useCallback(async()=>{
   const epoch=++loadEpoch.current;
   try {
     const [c,t]=await Promise.all([apiRequest<{code:string;name:string}[]>('/api/v1/geography/countries',{authenticated:false}),apiRequest<{version:number}>('/api/v1/policies/terms',{authenticated:false})]);
     if(epoch!==loadEpoch.current)return;
     if(version.current!==t.version)setAccepted(false);version.current=t.version;
     setCountries(c);setTerms(t.version);setDetailsFailed(false);
   }catch{if(epoch===loadEpoch.current){setDetailsFailed(true);setTerms(0);setAccepted(false);}}
 },[]);
 useFocusEffect(useCallback(()=>{void load();return()=>{++loadEpoch.current;};},[load]));
 async function run(action:()=>Promise<unknown>){if(running.current)return;running.current=true;setBusy(true);setMessage('');try{await action();}catch(e){setMessage(e instanceof ApiError&&e.status===429?(fa?'تلاش‌های زیادی انجام شده است. کمی صبر کنید.':`Too many attempts. Wait ${e.retryAfter??60} seconds before trying again.`):fa?'عملیات تأیید نشد. اتصال و اطلاعات را بررسی کنید و دوباره تلاش کنید.':e instanceof Error?e.message:'Please try again.');}finally{running.current=false;setBusy(false);}}
 return <Screen scroll style={{gap:16,maxWidth:560,width:'100%',alignSelf:'center'}}>
 <Text accessibilityRole="header" style={{fontSize:28,fontWeight:'700'}}>{fa?'فعال‌سازی حساب':'Activate your account • فعال‌سازی حساب'}</Text>
 <Text>{fa?'پیش از استفاده از فضای تعاملی، ایمیل خود را تأیید کنید. شرایط سن و منطقه در سرور بررسی می‌شوند.':'Verify your email before joining social spaces. Regional availability and age eligibility are checked by the server.'}</Text>
 <Text>{fa?'اگر ثبت‌نام امکان‌پذیر باشد، کد تأیید به ایمیل شما ارسال می‌شود. اگر برنامه را ببندید، دوباره این صفحه را باز کنید و ایمیل خود را وارد کنید. کد ۱۵ دقیقه اعتبار دارد.':'If registration can proceed, check your email for a verification code. If you close the app, return here and enter your email. Codes expire after 15 minutes. You can also sign in or use account recovery.'}</Text>
 <AppField label={fa?'ایمیل':'Email'} value={email} onChangeText={setEmail} autoCapitalize="none" autoCorrect={false} autoComplete="email" keyboardType="email-address" ltr editable={!busy&&!user} />
 <AppField label={fa?'کد تأیید':'Verification code'} value={code} onChangeText={setCode} keyboardType="number-pad" autoComplete="one-time-code" textContentType="oneTimeCode" maxLength={6} ltr editable={!busy} />
 <AppButton label={fa?'ارسال دوباره کد':'Resend code'} disabled={!email.trim()} variant="secondary" loading={busy} onPress={()=>void run(async()=>{await apiRequest('/api/v1/account-assistance/email-verification',{method:'POST',authenticated:false,body:JSON.stringify({email:email.trim()})});setMessage(fa?'اگر حساب وجود داشته باشد، کد ارسال می‌شود. ایمیل خود را بررسی کنید.':'If the account exists, a code has been queued. Check your email.');})} />
 <AppButton label={fa?'تأیید ایمیل':'Verify email'} disabled={!email.trim()||!/^\d{6}$/.test(code)} loading={busy} onPress={()=>void run(async()=>{await apiRequest('/api/v1/account-assistance/email-verification/confirm',{method:'POST',authenticated:false,body:JSON.stringify({email:email.trim(),code})});setCode('');setMessage(fa?'ایمیل تأیید شد. برای ادامه وارد شوید.':'Email verified. Sign in to continue.');if(user){const identity=await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity');if(identity.services.COMMUNITY)router.replace('/(app)');}})} />
 {!user && <><AppField label={fa?'رمز عبور':'Password'} value={password} onChangeText={setPassword} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr /><AppButton label={fa?'ورود':'Sign in'} disabled={!email.trim()||!password} loading={busy} onPress={()=>void run(async()=>{await signIn({email:email.trim(),password});setPassword('');const identity=await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity');if(identity.services.COMMUNITY)router.replace('/(app)');else setMessage(fa?'وارد شدید. تأیید ایمیل و شرایط دسترسی را کامل کنید.':'Signed in. Complete email verification and eligibility if required. Services also depend on regional availability.');})} /><AppButton label={fa?'بازیابی حساب':'Account recovery'} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'forgot-password'}})}/></>}
 {user && <>
 <Text>{fa?'اگر شرایط حساب نیاز به تکمیل دارد، قوانین جاری را بررسی و بزرگسال بودن خود را اعلام کنید. خوداظهاری، احراز مستقل سن نیست.':'If account eligibility needs updating, review the current Terms and confirm adult eligibility. This self-attestation is not independent age verification.'}</Text>
 <AppField label={fa?'جست‌وجوی کشور محل اقامت':'Search country of residence'} value={query} onChangeText={setQuery} editable={!busy} />
 {countries.filter(c=>(c.name+' '+c.code).toLowerCase().includes(query.toLowerCase())).slice(0,query?10:5).map(c=><AppButton key={c.code} label={(country===c.code?'✓ ':'')+c.name} variant="secondary" onPress={()=>{setCountry(c.code);setQuery(c.name);}} />)}
 <Pressable disabled={busy} accessibilityRole="checkbox" aria-checked={adult} accessibilityState={{checked:adult,disabled:busy}} onPress={()=>setAdult(!adult)}><Text>{adult?'☑':'☐'} {fa?'خوداظهاری می‌کنم که حداقل ۱۸ سال دارم.':'I self-attest that I am at least 18.'}</Text></Pressable>
 <AppButton label={fa?'مطالعه قوانین':'Read Terms'} disabled={busy} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'terms'}})} />
 <Pressable disabled={busy||!terms} accessibilityRole="checkbox" aria-checked={accepted} accessibilityState={{checked:accepted,disabled:busy||!terms}} onPress={()=>setAccepted(!accepted)}><Text>{accepted?'☑':'☐'} {fa?'نسخه قوانین را می‌پذیرم: ':'I accept Terms version '}{terms}.</Text></Pressable>
 <AppButton label="Save eligibility and continue" disabled={!adult||!accepted||!country||!terms} loading={busy} onPress={()=>void run(async()=>{const identity=await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity/activate',{method:'POST',body:JSON.stringify({adultSelfAttested:adult,residenceCountry:country,termsVersion:terms})});if(identity.services.COMMUNITY)router.replace('/(app)');else setMessage('Your account details are saved. Social services are not yet available for this account or region. Public resources remain available.');})} />
 <AppField label={fa?'رمز عبور برای حذف حساب':'Password to delete your account'} value={password} onChangeText={setPassword} secureTextEntry ltr />
 <AppButton label={fa?'حذف دائمی حساب من':'Delete my account permanently'} variant="secondary" disabled={!password} loading={busy} onPress={()=>void run(async()=>{await apiRequest('/api/v1/account',{method:'DELETE',body:JSON.stringify({password})});await signOut();router.replace('/(auth)/welcome');})} />
 <AppButton label={fa?'خروج':'Sign out'} variant="secondary" onPress={()=>void run(signOut)} />
 </>}
 {!!message&&<Text accessibilityRole="alert">{message}</Text>}
 {detailsFailed&&<><Text accessibilityRole="alert">{fa?'اطلاعات حساب بارگذاری نشد. دوباره تلاش کنید.':'Could not load account details. Please retry.'}</Text><AppButton label={fa?'تلاش دوباره':'Retry account details'} variant="secondary" onPress={()=>void load()}/></>}
 {busy&&<Text accessibilityLiveRegion="polite">{fa?'در حال بررسی…':'Checking…'}</Text>}
 <AppButton label="Public resources • منابع عمومی" variant="secondary" onPress={()=>router.push('/(auth)/resources')} />
 <AppButton label="Urgent help" variant="secondary" onPress={()=>router.push('/(auth)/urgent-help')} />
 </Screen>;
}
