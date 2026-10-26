import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { Screen } from '@/components/screen';
import { useAuth } from '@/features/auth/auth-provider';
import { validateRegistration } from '@/features/auth/registration-validation';
import { useLanguage } from '@/localization/language-provider';
import { apiRequest } from '@/lib/api/client';
import { colors, spacing } from '@/theme/tokens';
type Country = { code: string; name: string; local_names: Record<string,string> };
export default function RegisterScreen() {
  const { register } = useAuth(); const { isRtl } = useLanguage();
  const [email,setEmail]=useState(''); const [password,setPassword]=useState('');
  const [country,setCountry]=useState(''); const [query,setQuery]=useState('');
  const [countries,setCountries]=useState<Country[]>([]); const [terms,setTerms]=useState(0);
  const [adult,setAdult]=useState(false); const [accepted,setAccepted]=useState(false);
  const [visible,setVisible]=useState(false);const [busy,setBusy]=useState(false);
  const [errors,setErrors]=useState<Record<string,string>>({});const [error,setError]=useState('');
  async function load() {
    try {const [list,policy]=await Promise.all([apiRequest<Country[]>('/api/v1/geography/countries',{authenticated:false}),apiRequest<{version:number}>('/api/v1/policies/terms',{authenticated:false})]);setCountries(list);setTerms(policy.version);setError('');}
    catch {setError(isRtl?'بارگذاری اطلاعات ممکن نشد؛ دوباره تلاش کنید.':'Could not load registration details. Retry when connected.');}
  }
  useEffect(()=>{void load();},[]); // eslint-disable-line react-hooks/exhaustive-deps
  async function submit() {
    if(busy) return;
    const violations=validateRegistration({email,password,countryCode:country,adultSelfAttested:adult,termsVersion:terms,accepted});
    setErrors(violations); if(Object.keys(violations).length) return;
    setBusy(true);setError('');
    try {await register({email:email.trim(),password,countryCode:country,adultSelfAttested:adult,termsVersion:terms});setPassword('');router.replace({pathname:'/(auth)/activate',params:{email}});}
    catch(e) {setError(e instanceof Error?e.message:'Could not register. Please retry.');}
    finally {setBusy(false);}
  }
  return <Screen scroll style={styles.content}>
    <Text style={styles.title}>{isRtl?'ساخت حساب پایه':'Create your account'}</Text>
    <Text>{isRtl?'حساب تعاملی برای افراد ۱۸ سال به بالا است. منابع عمومی بدون حساب در دسترس‌اند.':'Social accounts are for adults 18+. Public resources are available at every age without an account.'}</Text>
    <AppField label={isRtl?'ایمیل':'Email'} value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" autoComplete="email" />
    <AppField label={isRtl?'رمز عبور (۱۵ تا ۱۲۸ نویسه)':'Password (15–128 characters)'} value={password} onChangeText={setPassword} secureTextEntry={!visible} autoComplete="new-password" />
    <AppButton label={visible?'Hide password':'Show password'} variant="secondary" onPress={()=>setVisible(!visible)} />
    <Text>{isRtl?'از مدیر رمز عبور و جای‌گذاری می‌توانید استفاده کنید.':'You can paste a password or use a password manager. No required mix of symbols or numbers.'}</Text>
    <AppField label={isRtl?'جست‌وجوی کشور محل اقامت':'Search country of residence'} value={query} onChangeText={setQuery} />
    {!!country && <Text>{isRtl?'کشور انتخاب‌شده: ':'Selected country: '}{countries.find(c=>c.code===country)?.name}</Text>}
    <View style={styles.list}>{countries.filter(c=>(c.name+' '+c.code+' '+Object.values(c.local_names).join(' ')).toLowerCase().includes(query.toLowerCase())).slice(0,query?12:5).map(c=><Pressable key={c.code} accessibilityRole="radio" accessibilityState={{selected:country===c.code}} onPress={()=>{setCountry(c.code);setQuery(c.name);}} style={styles.choice}><Text>{country===c.code?'● ':'○ '}{isRtl?c.local_names.fa??c.name:c.name}</Text></Pressable>)}</View>
    <Pressable accessibilityRole="checkbox" accessibilityState={{checked:adult}} onPress={()=>setAdult(!adult)} style={styles.choice}><Text>{adult?'☑':'☐'} {isRtl?'خوداظهاری می‌کنم که حداقل ۱۸ سال دارم.':'I self-attest that I am at least 18 years old.'}</Text></Pressable>
    <Text>{isRtl?'خوداظهاری، احراز مستقل سن نیست.':'Self-attestation is not independent age verification.'}</Text>
    <AppButton label={isRtl?'مطالعه قوانین':'Read Terms'} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'terms'}})} />
    <Pressable accessibilityRole="checkbox" accessibilityState={{checked:accepted}} onPress={()=>setAccepted(!accepted)} style={styles.choice}><Text>{accepted?'☑':'☐'} {isRtl?'نسخه قوانین را می‌پذیرم: ':'I accept Terms version '}{terms||'…'}</Text></Pressable>
    <AppButton label={isRtl?'اطلاعیه حریم خصوصی':'Privacy notice'} variant="secondary" onPress={()=>router.push({pathname:'/(auth)/info/[page]',params:{page:'privacy'}})} />
    {Object.entries(errors).map(([field,message])=><Text key={field} accessibilityRole="alert" style={styles.error}>{message}</Text>)}
    {!!error && <><Text accessibilityRole="alert" style={styles.error}>{error}</Text><AppButton label="Retry loading" variant="secondary" onPress={()=>void load()} /></>}
    <AppButton label={isRtl?'ثبت‌نام':'Create account'} disabled={!terms} loading={busy} onPress={()=>void submit()} />
    <AppButton label={isRtl?'منابع عمومی':'Browse public resources'} variant="secondary" onPress={()=>router.push('/(auth)/resources')} />
    <AppButton label={isRtl?'ورود':'Sign in'} variant="secondary" onPress={()=>router.push('/(auth)/login')} />
  </Screen>;
}
const styles=StyleSheet.create({content:{gap:spacing.md,maxWidth:560,width:'100%',alignSelf:'center',paddingTop:spacing.xl},title:{fontSize:30,color:colors.ocean900,fontWeight:'800'},error:{color:colors.danger},choice:{padding:spacing.md,borderWidth:1,borderColor:colors.border,borderRadius:12},list:{gap:8}});
