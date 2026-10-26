import { useEffect,useState } from 'react';
import { Text } from 'react-native';
import { AppButton } from '@/components/app-button';
import { PublicPage,PublicHeading,PublicText,PublicCard,publicStyles } from '@/features/public/public-page';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { apiRequest } from '@/lib/api/client';
type Resources={items:{id:string;title:string;body:string}[];boundaries:string[]};
export default function ResourcesScreen(){
 const {language}=useLanguage();const c=publicCopy[language];const [data,setData]=useState<Resources|null>(null),[error,setError]=useState(false),[attempt,setAttempt]=useState(0);
 useEffect(()=>{let active=true;apiRequest<Resources>('/api/v1/public/resources',{authenticated:false}).then(result=>{if(active){setData(result);setError(false);}}).catch(()=>{if(active)setError(true);});return()=>{active=false;};},[attempt]);
 return <PublicPage><PublicHeading level={1}>{c.resourcesTitle}</PublicHeading><PublicText>{c.resourcesIntro}</PublicText><PublicText>{c.resourceLanguage}</PublicText>
  {!data&&!error&&<Text role="status" style={publicStyles.body}>{c.loading}</Text>}
  {data?.items.map(item=><PublicCard key={item.id}><PublicHeading>{item.title}</PublicHeading><PublicText>{item.body}</PublicText></PublicCard>)}
  {data?.items.length===0&&<PublicText>{c.emptyResources}</PublicText>}
  {error&&<><Text accessibilityRole="alert" style={publicStyles.status}>{c.error}</Text><AppButton label={c.retry} onPress={()=>{setData(null);setError(false);setAttempt(n=>n+1);}}/></>}
  <PublicCard><PublicText>{c.boundary}</PublicText></PublicCard>
 </PublicPage>;
}
