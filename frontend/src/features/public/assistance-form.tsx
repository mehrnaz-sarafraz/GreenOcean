import { useState } from 'react';
import { Text } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { PublicPage, PublicHeading, publicStyles } from './public-page';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { apiRequest } from '@/lib/api/client';
export function AssistanceForm({page,token}:{page:string;token?:string}) {
 const {language}=useLanguage();const c=publicCopy[language];const [email,setEmail]=useState(''),[value,setValue]=useState(token??''),[password,setPassword]=useState(''),[message,setMessage]=useState(''),[busy,setBusy]=useState(false);
 const title=page==='contact'?c.contact:page==='verify-email'?c.verify:page==='reset-password'?c.reset:c.forgot;
 async function submit(confirm=false){
  if(busy)return;
  if((page!=='reset-password'&&!/^\S+@\S+\.\S+$/.test(email.trim()))||(page==='contact'&&value.trim().length<10)||(page==='verify-email'&&confirm&&!/^\d{6}$/.test(value))||(page==='reset-password'&&(!value.trim()||[...password].length<15||[...password].length>128))){setMessage(c.formError);return;}
  setBusy(true);setMessage('');
  const path=page==='contact'?'support':page==='forgot-password'?'password-reset':page==='reset-password'?'password-reset/confirm':confirm?'email-verification/confirm':'email-verification';
  const body=page==='contact'?{email:email.trim(),message:value}:page==='reset-password'?{token:value,newPassword:password}:confirm?{email:email.trim(),code:value}:{email:email.trim()};
  try{await apiRequest(`/api/v1/account-assistance/${path}`,{method:'POST',authenticated:false,body:JSON.stringify(body)});setMessage(c.requestDone);if(page==='contact')setValue('');}catch{setMessage(c.error);}finally{setBusy(false);}
 }
 return <PublicPage><PublicHeading level={1}>{title}</PublicHeading>
  {page!=='reset-password'&&<AppField label={c.email} value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" style={{writingDirection:'ltr',textAlign:'left'}}/>}
  {page!=='forgot-password'&&<AppField label={page==='contact'?c.message:page==='verify-email'?c.code:c.token} value={value} onChangeText={setValue} multiline={page==='contact'} autoCapitalize="none"/>}
  {page==='reset-password'&&<AppField label={c.password} value={password} onChangeText={setPassword} secureTextEntry autoComplete="new-password"/>}
  {page==='verify-email'&&<AppButton label={c.resend} variant="secondary" loading={busy} onPress={()=>void submit()}/>}
  <AppButton label={page==='verify-email'?c.verify:page==='reset-password'?c.reset:c.send} loading={busy} onPress={()=>void submit(page==='verify-email')}/>
  {!!message&&<Text accessibilityRole="alert" style={publicStyles.body}>{message}</Text>}
 </PublicPage>;
}
