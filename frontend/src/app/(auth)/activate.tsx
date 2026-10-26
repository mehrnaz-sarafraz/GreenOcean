import { router, useFocusEffect } from 'expo-router';
import { useCallback,useEffect,useRef,useState } from 'react';
import { AccessibilityInfo,Pressable,Text,TextInput } from 'react-native';
import { Screen } from '@/components/screen';
import { AppField } from '@/components/app-field';
import { AppButton } from '@/components/app-button';
import { ApiError, apiRequest } from '@/lib/api/client';
import { useAuth } from '@/features/auth/auth-provider';
import { useLanguage } from '@/localization/language-provider';
import { cooldownSeconds,maskVerificationEmail,normalizeVerificationCode,validVerificationCode } from '@/features/auth/verification';
import { validAccountEmail } from '@/features/auth/registration-validation';
export default function ActivateScreen(){
 const {user,signIn,signOut,pendingRegistrationEmail,pendingEmailCorrected}=useAuth();
 const {language}=useLanguage();const fa=language==='fa';const running=useRef(false);
 const [email,setEmail]=useState(user?.email??pendingRegistrationEmail??'');const [password,setPassword]=useState('');const [code,setCode]=useState('');
 const [country,setCountry]=useState('');const [adult,setAdult]=useState(false);const [accepted,setAccepted]=useState(false);
 const [query,setQuery]=useState('');const [countries,setCountries]=useState<{code:string;name:string}[]>([]);
 const [terms,setTerms]=useState(0);const [busy,setBusy]=useState(false);const [message,setMessage]=useState(pendingEmailCorrected?(fa?'اگر اصلاح امکان‌پذیر بوده باشد، درخواست تأیید برای آدرس جدید ثبت شده است. دوباره وارد شوید؛ در صورت نیاز آدرس قبلی یا بازیابی حساب را امتحان کنید.':'If the correction could be completed, verification was requested for the new address. Sign in again; if needed, use the original address or account recovery.'):'');
 const codeRef=useRef<TextInput>(null);const [deadline,setDeadline]=useState(0);const [now,setNow]=useState(0);
 const [editingEmail,setEditingEmail]=useState(false);const [newEmail,setNewEmail]=useState('');const [correctionPassword,setCorrectionPassword]=useState('');
 const remaining=cooldownSeconds(deadline,now);
 useEffect(()=>{if(!deadline)return;const timer=setInterval(()=>{const current=Date.now();setNow(current);if(current>=deadline)clearInterval(timer);},1000);return()=>clearInterval(timer);},[deadline]);
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
 async function run(action:()=>Promise<unknown>){
   if(running.current)return;
   running.current=true;setBusy(true);setMessage('');
   try{await action();}
   catch(e){
     if(e instanceof ApiError && e.fieldErrors.some(field=>field.field==='termsVersion')){
       setAccepted(false);await load();
       setMessage(fa?'نسخه قوانین تغییر کرده است. نسخه جدید را بررسی کنید و بپذیرید.':'The Terms have changed. Review and accept the new version.');
     }else if(e instanceof ApiError && e.status===429){
       const retry=e.retryAfter??60;setDeadline(Date.now()+Math.min(3600,retry)*1000);setNow(Date.now());
       setMessage(fa?`تلاش‌های زیادی انجام شده است. ${retry} ثانیه صبر کنید.`:`Too many attempts. Wait ${retry} seconds before trying again.`);
     }else{
       setMessage(fa?(editingEmail&&user?'اصلاح ایمیل انجام نشد. رمز عبور، آدرس جدید و اتصال را بررسی کنید.':'عملیات تأیید نشد. کد نادرست، منقضی یا جایگزین‌شده قابل استفاده نیست. اتصال را بررسی کنید و در صورت نیاز کد تازه بخواهید.'):e instanceof Error?e.message:'Please try again.');
     }
     codeRef.current?.focus();AccessibilityInfo.announceForAccessibility(fa?'تأیید انجام نشد؛ پیام راهنما را بررسی کنید.':'Verification was not completed. Review the recovery message.');
   }finally{running.current=false;setBusy(false);}
 }
 return <Screen scroll style={{gap:16,maxWidth:560,width:'100%',alignSelf:'center'}}>
 <Text accessibilityRole="header" style={{fontSize:28,fontWeight:'700'}}>{fa?'فعال‌سازی حساب':'Activate your account • فعال‌سازی حساب'}</Text>
 <Text>{fa?'پیش از استفاده از فضای تعاملی، ایمیل خود را تأیید کنید. شرایط سن و منطقه در سرور بررسی می‌شوند.':'Verify your email before joining social spaces. Regional availability and age eligibility are checked by the server.'}</Text>
 <Text>{fa?'اگر ثبت‌نام امکان‌پذیر باشد، کد تأیید به ایمیل شما ارسال می‌شود. اگر برنامه را ببندید، دوباره این صفحه را باز کنید و ایمیل خود را وارد کنید. کد ۱۵ دقیقه اعتبار دارد.':'If registration can proceed, check your email for a verification code. If you close the app, return here and enter your email. Codes expire after 15 minutes. You can also sign in or use account recovery.'}</Text>
 <Text>{fa?'پوشه هرزنامه را هم بررسی کنید. فقط جدیدترین کد معتبر است؛ درخواست ارسال به معنی تحویل در صندوق ورودی نیست.':'Check Spam too. Only the newest code is valid; requesting email does not guarantee inbox delivery.'}</Text>
 {!!maskVerificationEmail(email)&&<Text>{fa?'آدرس موردنظر: ':'Intended address: '}{maskVerificationEmail(email)}</Text>}
 <AppField label={fa?'ایمیل':'Email'} value={email} onChangeText={setEmail} autoCapitalize="none" autoCorrect={false} autoComplete="email" keyboardType="email-address" ltr editable={!busy&&!user} />
 <AppField ref={codeRef} label={fa?'کد تأیید':'Verification code'} value={code} onChangeText={value=>{setCode(normalizeVerificationCode(value).slice(0,6));setMessage('');}} error={code&&!validVerificationCode(code)?(fa?'هر شش رقم کد را وارد کنید.':'Enter all six digits of the code.'):undefined} keyboardType="number-pad" autoComplete="one-time-code" textContentType="oneTimeCode" autoCorrect={false} autoCapitalize="none" maxLength={12} ltr editable={!busy} />
 <AppButton label={fa?'ارسال دوباره کد':'Resend code'} disabled={!email.trim()||remaining>0} variant="secondary" loading={busy} onPress={()=>void run(async()=>{const result=await apiRequest<{retryAfterSeconds?:number}>('/api/v1/account-assistance/email-verification',{method:'POST',authenticated:false,body:JSON.stringify({email:email.trim()})});setDeadline(Date.now()+Math.min(3600,Math.max(0,result.retryAfterSeconds??0))*1000);setNow(Date.now());setCode('');codeRef.current?.focus();setMessage(fa?'اگر حساب وجود داشته و نیاز به تأیید داشته باشد، درخواست کد ثبت شده است. جدیدترین ایمیل و پوشه هرزنامه را بررسی کنید.':'If the account exists and needs verification, a code has been requested. Check the newest email and Spam.');})} />
 {remaining>0&&<Text accessibilityLiveRegion="polite">{fa?`ارسال دوباره پس از ${remaining} ثانیه`:`Resend available in ${remaining} seconds`}</Text>}
 <AppButton label={fa?'تأیید ایمیل':'Verify email'} disabled={!email.trim()||!/^\d{6}$/.test(code)} loading={busy} onPress={()=>void run(async()=>{await apiRequest('/api/v1/account-assistance/email-verification/confirm',{method:'POST',authenticated:false,body:JSON.stringify({email:email.trim(),code})});setCode('');setMessage(fa?'ایمیل تأیید شد. برای ادامه وارد شوید.':'Email verified. Sign in to continue.');if(user){const identity=await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity');if(identity.services.COMMUNITY)router.replace('/(app)');}})} />
 {!user && <><AppField label={fa?'رمز عبور':'Password'} value={password} onChangeText={setPassword} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr /><AppButton label={fa?'ورود':'Sign in'} disabled={!email.trim()||!password} loading={busy} onPress={()=>void run(async()=>{await signIn({email:email.trim(),password});setPassword('');const identity=await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity');if(identity.services.COMMUNITY)router.replace('/(app)');else setMessage(fa?'وارد شدید. تأیید ایمیل و شرایط دسترسی را کامل کنید.':'Signed in. Complete email verification and eligibility if required. Services also depend on regional availability.');})} /><AppButton label={fa?'بازیابی حساب':'Account recovery'} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'forgot-password'}})}/></>}
 <AppButton label={fa?'اصلاح ایمیل اشتباه':'Edit email'} disabled={busy} variant="secondary" onPress={()=>setEditingEmail(!editingEmail)}/>
 {editingEmail&&<>
   <Text>{fa?'برای اصلاح ایمیل حساب در انتظار تأیید، ابتدا با آدرس قبلی وارد شوید؛ سپس رمز را دوباره وارد کنید. این مسیر برای حساب تأییدشده نیست.':'To correct a pending account, first sign in using the address originally entered, then confirm your password. This flow is not for verified accounts.'}</Text>
   {user&&<>
     <AppField label={fa?'ایمیل جدید':'New email'} value={newEmail} onChangeText={setNewEmail} error={newEmail&&!validAccountEmail(newEmail)?(fa?'ایمیل معتبر وارد کنید.':'Enter a valid email address.'):undefined} keyboardType="email-address" autoComplete="email" autoCapitalize="none" autoCorrect={false} ltr editable={!busy}/>
     <AppField label={fa?'رمز برای تأیید اصلاح ایمیل':'Password to confirm email correction'} value={correctionPassword} onChangeText={setCorrectionPassword} secureTextEntry autoComplete="current-password" autoCapitalize="none" autoCorrect={false} ltr editable={!busy}/>
     <AppButton label={fa?'تأیید اصلاح ایمیل':'Confirm email correction'} disabled={!validAccountEmail(newEmail)||!correctionPassword} loading={busy} onPress={()=>void run(async()=>{
       await apiRequest('/api/v1/account-assistance/pending-email',{method:'POST',body:JSON.stringify({newEmail:newEmail.trim(),password:correctionPassword})});
       await signOut({correctedEmail:newEmail.trim()});setEmail(newEmail.trim());setNewEmail('');setCorrectionPassword('');setCode('');setDeadline(0);setEditingEmail(false);
       setMessage(fa?'اگر اصلاح امکان‌پذیر بوده باشد، درخواست تأیید برای آدرس جدید ثبت شده است. دوباره وارد شوید؛ در صورت نیاز آدرس قبلی یا بازیابی حساب را امتحان کنید.':'If the correction could be completed, verification was requested for the new address. Sign in again; if needed, use the original address or account recovery.');
     })}/>
   </>}
 </>}
 {user && <>
 <Text>{fa?'اگر شرایط حساب نیاز به تکمیل دارد، قوانین جاری را بررسی و بزرگسال بودن خود را اعلام کنید. خوداظهاری، احراز مستقل سن نیست.':'If account eligibility needs updating, review the current Terms and confirm adult eligibility. This self-attestation is not independent age verification.'}</Text>
 <AppField label={fa?'جست‌وجوی کشور محل اقامت':'Search country of residence'} value={query} onChangeText={setQuery} editable={!busy} />
 {countries.filter(c=>(c.name+' '+c.code).toLowerCase().includes(query.toLowerCase())).slice(0,query?10:5).map(c=><AppButton key={c.code} label={(country===c.code?'✓ ':'')+c.name} variant="secondary" onPress={()=>{setCountry(c.code);setQuery(c.name);}} />)}
 <Pressable disabled={busy} accessibilityRole="checkbox" aria-checked={adult} accessibilityState={{checked:adult,disabled:busy}} onPress={()=>setAdult(!adult)}><Text>{adult?'☑':'☐'} {fa?'خوداظهاری می‌کنم که حداقل ۱۸ سال دارم.':'I self-attest that I am at least 18.'}</Text></Pressable>
 <AppButton label={fa?'مطالعه قوانین':'Read Terms'} disabled={busy} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'terms'}})} />
 <Pressable disabled={busy||!terms} accessibilityRole="checkbox" aria-checked={accepted} accessibilityState={{checked:accepted,disabled:busy||!terms}} onPress={()=>setAccepted(!accepted)}><Text>{accepted?'☑':'☐'} {fa?'نسخه قوانین را می‌پذیرم: ':'I accept Terms version '}{terms}.</Text></Pressable>
 <AppButton label={fa?'ذخیره شرایط و ادامه':'Save eligibility and continue'} disabled={!adult||!accepted||!country||!terms} loading={busy} onPress={()=>void run(async()=>{const identity=await apiRequest<{services:{COMMUNITY:boolean}}>('/api/v1/identity/activate',{method:'POST',body:JSON.stringify({adultSelfAttested:adult,residenceCountry:country,termsVersion:terms})});if(identity.services.COMMUNITY)router.replace('/(app)');else setMessage(fa?'اطلاعات حساب ذخیره شد. خدمات تعاملی هنوز برای این حساب یا منطقه در دسترس نیست. منابع عمومی باز هستند.':'Your account details are saved. Social services are not yet available for this account or region. Public resources remain available.');})} />
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
