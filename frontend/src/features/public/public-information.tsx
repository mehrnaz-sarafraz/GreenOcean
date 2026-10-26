import { useEffect, useState } from 'react';
import { View, Text } from 'react-native';
import { AppButton } from '@/components/app-button';
import { PublicPage, PublicHeading, PublicText, PublicCard, PublicLinks, publicStyles, usePublicNavigation } from './public-page';
import { policyKey, isInformationId } from './contracts';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { apiRequest } from '@/lib/api/client';
type Policy={key:string;version:number;title:string;summary:string;content:string;publishedAt:string};
export function PublicInformation({page}:{page:unknown}) {
  const {language}=useLanguage();const copy=publicCopy[language];const navigate=usePublicNavigation();const key=policyKey(page);
  const [request,setRequest]=useState(0);const [result,setResult]=useState<{key:string;document:Policy|null;failed:boolean}|null>(null);
  useEffect(()=>{
    if(!key)return;let active=true;
    apiRequest<Policy>(`/api/v1/policies/${key}`,{authenticated:false}).then(document=>{
      if(!document.content?.trim()||document.key!==key||!Number.isInteger(document.version)||document.version<1||!Number.isFinite(Date.parse(document.publishedAt)))throw new Error('Invalid published policy');
      if(active)setResult({key,document,failed:false});
    }).catch(()=>{if(active)setResult({key,document:null,failed:true});});return()=>{active=false;};
  },[key,request]);
  if(key){
    const settled=result?.key===key?result:null;
    const title=[...copy.policyLinks,...copy.infoLinks].find(link=>link.id===page)?.label??key;
    return <PublicPage><PublicHeading level={1}>{title}</PublicHeading>
      {!settled&&<Text role="status" style={publicStyles.body}>{copy.policyLoading}</Text>}
      {settled?.document&&<PublicCard><PublicHeading>{settled.document.title}</PublicHeading><PublicText>{copy.policyLanguage}</PublicText>
        <Text selectable style={[publicStyles.body,{writingDirection:'auto'}]}>{settled.document.content.replaceAll('\\n','\n')}</Text>
        <PublicText>{copy.policyVersion} {settled.document.version} · {copy.published} {new Date(settled.document.publishedAt).toLocaleDateString(language==='fa'?'fa-IR':'en-GB')}</PublicText>
      </PublicCard>}
      {settled?.failed&&<PublicCard><Text accessibilityRole="alert" style={publicStyles.status}>{copy.policyUnavailable}</Text><AppButton label={copy.retry} onPress={()=>{setResult(null);setRequest(n=>n+1);}}/></PublicCard>}
      <AppButton label={copy.publicResources} variant="secondary" onPress={()=>navigate('/(auth)/resources')}/><PublicLinks/>
    </PublicPage>;
  }
  if(!isInformationId(page))return <PublicPage><PublicHeading level={1}>{copy.notFoundTitle}</PublicHeading><PublicText>{copy.notFoundBody}</PublicText><PublicLinks/></PublicPage>;
  const info=copy.pages[page];
  return <PublicPage><PublicHeading level={1}>{info.title}</PublicHeading><PublicText>{info.intro}</PublicText>
    {(page==='how-it-works'||page==='about')&&copy.steps.map(step=><PublicCard key={step.title}><PublicHeading>{step.title}</PublicHeading><PublicText>{step.body}</PublicText></PublicCard>)}
    {page==='faq'&&copy.faqs.map(item=><PublicCard key={item.title}><PublicHeading>{item.title}</PublicHeading><PublicText>{item.body}</PublicText></PublicCard>)}
    {['community','listener','professional'].includes(page)&&<><PublicCard><PublicText>{copy.accountRequired}</PublicText><PublicText>{copy.availability}</PublicText><PublicText>{copy.boundary}</PublicText></PublicCard><AppButton label={copy.create} onPress={()=>navigate('/(auth)/register')}/></>}
    {page==='help'&&<View style={{gap:16}}><AppButton label={copy.contact} variant="secondary" onPress={()=>navigate({pathname:'/(auth)/info/[page]',params:{page:'contact'}})}/><AppButton label={copy.forgot} variant="secondary" onPress={()=>navigate({pathname:'/(auth)/info/[page]',params:{page:'forgot-password'}})}/></View>}
    <AppButton label={copy.publicResources} variant="secondary" onPress={()=>navigate('/(auth)/resources')}/><PublicLinks/>
  </PublicPage>;
}
