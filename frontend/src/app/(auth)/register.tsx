import { router, Redirect, useFocusEffect } from 'expo-router';
import { useCallback, useRef, useState } from 'react';
import { AccessibilityInfo, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { PublicPage, PublicText } from '@/features/public/public-page';
import { publicCopy } from '@/localization/public-copy';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { registrationFailure, validateRegistration } from '@/features/auth/registration-validation';
import { useLanguage } from '@/localization/language-provider';
import { ApiError, apiRequest } from '@/lib/api/client';
import { colors, spacing } from '@/theme/tokens';
type Country = { code: string; name: string; local_names: Record<string,string> };
export default function RegisterScreen() {
  const { register, status } = useAuth(); const { isRtl, language } = useLanguage();
  const [email,setEmail]=useState(''); const [password,setPassword]=useState('');
  const [country,setCountry]=useState(''); const [query,setQuery]=useState('');
  const [countries,setCountries]=useState<Country[]>([]); const [terms,setTerms]=useState(0);
  const [adult,setAdult]=useState(false); const [accepted,setAccepted]=useState(false);
  const [visible,setVisible]=useState(false);const [busy,setBusy]=useState(false);
  const [errors,setErrors]=useState<Record<string,string>>({});const [error,setError]=useState('');
  const [loading,setLoading]=useState(true); const [detailsFailed,setDetailsFailed]=useState(false);
  const submitting=useRef(false);const epoch=useRef(0);const version=useRef(0);
  const emailRef=useRef<TextInput>(null);const passwordRef=useRef<TextInput>(null);const countryRef=useRef<TextInput>(null);
  const adultRef=useRef<View>(null);const termsRef=useRef<View>(null);
  function clear(field:string) {setErrors(old=>{const next={...old};delete next[field];return next;});setError('');}
  function fieldError(field:string) {return !errors[field]?undefined:language==='fa'?publicCopy.fa.registrationErrors[field as keyof typeof publicCopy.fa.registrationErrors]??errors[field]:errors[field];}
  function focusErrors(fields:Record<string,string>) {
    const refs={email:emailRef,password:passwordRef,countryCode:countryRef,adultSelfAttested:adultRef,termsVersion:termsRef};
    const first=Object.keys(refs).find(key=>fields[key]);
    const target=first?refs[first as keyof typeof refs].current:null;
    (target as unknown as {focus?:()=>void}|null)?.focus?.();
    AccessibilityInfo.announceForAccessibility(language==='fa'?'موارد مشخص‌شده را اصلاح کنید.':Object.values(fields).join(' '));
  }
  const load=useCallback(async () => {
    const current=++epoch.current;setLoading(true);
    try {
      const [list,policy]=await Promise.all([apiRequest<Country[]>('/api/v1/geography/countries',{authenticated:false}),apiRequest<{version:number}>('/api/v1/policies/terms',{authenticated:false})]);
      if(current!==epoch.current)return;
      if(version.current!==policy.version)setAccepted(false);
      version.current=policy.version;setCountries(list);setTerms(policy.version);setDetailsFailed(false);
    } catch {if(current===epoch.current){setDetailsFailed(true);setTerms(0);setAccepted(false);}}
    finally {if(current===epoch.current)setLoading(false);}
  },[]);
  useFocusEffect(useCallback(()=>{void load();return()=>{++epoch.current;};},[load]));
  async function submit() {
    if(submitting.current || loading || detailsFailed) return;
    const violations=validateRegistration({email,password,countryCode:country,adultSelfAttested:adult,termsVersion:terms,accepted});
    setErrors(violations); if(Object.keys(violations).length){focusErrors(violations);return;}
    submitting.current=true;setBusy(true);setError('');
    try {await register({email:email.trim(),password,countryCode:country,adultSelfAttested:adult,termsVersion:terms});setPassword('');router.replace('/(auth)/activate');}
    catch(e) {
      const failure=registrationFailure(e instanceof ApiError?e:{},language==='fa');
      setErrors(failure.fields);setError(failure.message);
      if(failure.refreshTerms){setAccepted(false);await load();}
      focusErrors(failure.fields);
    }
    finally {submitting.current=false;setBusy(false);}
  }
  if(status==='authenticated')return <Redirect href="/(app)"/>;
  if(status==='loading')return <PublicPage><PublicText>{publicCopy[language].checking}</PublicText></PublicPage>;
  return <Screen scroll style={styles.content}>
    <Text style={styles.title}>{isRtl?'ساخت حساب پایه':'Create your account'}</Text>
    <Text>{isRtl?'حساب تعاملی برای افراد ۱۸ سال به بالا است. منابع عمومی بدون حساب در دسترس‌اند.':'Social accounts are for adults 18+. Public resources are available at every age without an account.'}</Text>
    <AppField ref={emailRef} label={isRtl?'ایمیل':'Email'} value={email} onChangeText={value=>{setEmail(value);clear('email');}} error={fieldError('email')} keyboardType="email-address" autoCapitalize="none" autoCorrect={false} autoComplete="email" textContentType="emailAddress" ltr editable={!busy} />
    <AppField ref={passwordRef} label={isRtl?'رمز عبور (۱۵ تا ۱۲۸ نویسه)':'Password (15–128 characters)'} value={password} onChangeText={value=>{setPassword(value);clear('password');}} error={fieldError('password')} secureTextEntry={!visible} autoComplete="new-password" textContentType="newPassword" autoCapitalize="none" autoCorrect={false} ltr editable={!busy} />
    <AppButton label={visible?publicCopy[language].hidePassword:publicCopy[language].showPassword} variant="secondary" onPress={()=>setVisible(!visible)} />
    <Text>{isRtl?'از مدیر رمز عبور و جای‌گذاری می‌توانید استفاده کنید.':'You can paste a password or use a password manager. No required mix of symbols or numbers.'}</Text>
    <Text>{isRtl?'ایمیل باید با حروف لاتین باشد. ساخت حساب به معنی دسترسی به خدمات در کشور شما نیست.':'Use an ASCII email mailbox and domain. Creating an account does not guarantee services in your country.'}</Text>
    <AppField ref={countryRef} label={isRtl?'جست‌وجوی کشور محل اقامت':'Search country of residence'} value={query} onChangeText={setQuery} error={fieldError('countryCode')} editable={!busy} />
    {!!country && <Text>{isRtl?'کشور انتخاب‌شده: ':'Selected country: '}{countries.find(c=>c.code===country)?.name}</Text>}
    <View style={styles.list}>{countries.filter(c=>(c.name+' '+c.code+' '+Object.values(c.local_names).join(' ')).toLowerCase().includes(query.toLowerCase())).slice(0,query?12:5).map(c=><Pressable key={c.code} accessibilityRole="radio" aria-checked={country===c.code} accessibilityState={{checked:country===c.code}} disabled={busy} onPress={()=>{setCountry(c.code);setQuery(c.name);clear('countryCode');}} style={styles.choice}><Text>{country===c.code?'● ':'○ '}{isRtl?c.local_names.fa??c.name:c.name}</Text></Pressable>)}</View>
    <Pressable ref={adultRef} disabled={busy} accessibilityRole="checkbox" aria-checked={adult} accessibilityState={{checked:adult,disabled:busy}} onPress={()=>{setAdult(!adult);clear('adultSelfAttested');}} style={styles.choice}><Text>{adult?'☑':'☐'} {isRtl?'خوداظهاری می‌کنم که حداقل ۱۸ سال دارم.':'I self-attest that I am at least 18 years old.'}</Text></Pressable>
    {!!errors.adultSelfAttested&&<Text accessibilityRole="alert" style={styles.error}>{fieldError('adultSelfAttested')}</Text>}
    <Text>{isRtl?'خوداظهاری، احراز مستقل سن نیست.':'Self-attestation is not independent age verification.'}</Text>
    <AppButton label={isRtl?'مطالعه قوانین':'Read Terms'} disabled={busy} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'terms'}})} />
    <Pressable ref={termsRef} disabled={busy||!terms} accessibilityRole="checkbox" aria-checked={accepted} accessibilityState={{checked:accepted,disabled:busy||!terms}} onPress={()=>{setAccepted(!accepted);clear('termsVersion');}} style={styles.choice}><Text>{accepted?'☑':'☐'} {isRtl?'نسخه قوانین را می‌پذیرم: ':'I accept Terms version '}{terms||'…'}</Text></Pressable>
    {!!errors.termsVersion&&<Text accessibilityRole="alert" style={styles.error}>{fieldError('termsVersion')}</Text>}
    <AppButton label={isRtl?'اطلاعیه حریم خصوصی':'Privacy notice'} disabled={busy} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'privacy'}})} />
    {loading&&<Text accessibilityLiveRegion="polite">{publicCopy[language].loading}</Text>}
    {detailsFailed&&<><Text accessibilityRole="alert" style={styles.error}>{isRtl?'بارگذاری اطلاعات ممکن نشد؛ دوباره تلاش کنید.':'Could not load registration details. Retry when connected.'}</Text><AppButton label={publicCopy[language].retry} variant="secondary" onPress={()=>void load()}/></>}
    {!!error && <Text accessibilityRole="alert" style={styles.error}>{error}</Text>}
    {busy&&<Text accessibilityLiveRegion="polite">{isRtl?'در حال ارسال ثبت‌نام…':'Submitting registration…'}</Text>}
    <AppButton label={isRtl?'ثبت‌نام':'Create account'} disabled={!terms||loading||detailsFailed} loading={busy} onPress={()=>void submit()} />
    <AppButton label={isRtl?'قبلاً ارسال کرده‌اید؟ تأیید ایمیل':'Already submitted? Verify email'} disabled={busy} variant="secondary" onPress={()=>{setPassword('');router.replace('/(auth)/activate');}} />
    <AppButton label={isRtl?'منابع عمومی':'Browse public resources'} disabled={busy} variant="secondary" onPress={()=>router.push('/(auth)/resources')} />
    <AppButton label={isRtl?'ورود':'Sign in'} disabled={busy} variant="secondary" onPress={()=>router.push('/(auth)/login')} />
  </Screen>;
}
const styles=StyleSheet.create({content:{gap:spacing.md,maxWidth:560,width:'100%',alignSelf:'center',paddingTop:spacing.xl},title:{fontSize:30,color:colors.ocean900,fontWeight:'800'},error:{color:colors.danger},choice:{padding:spacing.md,borderWidth:1,borderColor:colors.border,borderRadius:12},list:{gap:8}});
