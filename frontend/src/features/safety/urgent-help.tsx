import { useEffect,useState } from 'react';
import { Linking,Text,View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppField } from '@/components/app-field';
import { CrisisResource } from '@/features/platform/types';
import { PublicCard,PublicHeading,PublicText,publicStyles } from '@/features/public/public-page';
import { safeResourceUrl } from '@/features/public/contracts';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { apiRequest } from '@/lib/api/client';
type Country={code:string;name:string;local_names?:Record<string,string>};
export function UrgentHelpContent(){
 const {language}=useLanguage();const c=publicCopy[language];
 const [country,setCountry]=useState(''),[query,setQuery]=useState(''),[countries,setCountries]=useState<Country[]>([]),[all,setAll]=useState<CrisisResource[]>([]);
 const [loading,setLoading]=useState(true),[error,setError]=useState(false),[linkError,setLinkError]=useState(false),[attempt,setAttempt]=useState(0);
 useEffect(()=>{let active=true;Promise.allSettled([apiRequest<CrisisResource[]>('/api/v1/crisis-resources',{authenticated:false}),apiRequest<Country[]>('/api/v1/geography/countries',{authenticated:false})]).then(([directory,locations])=>{
   if(!active)return;
   if(directory.status==='fulfilled'){setAll(directory.value);setError(false);}else setError(true);
   if(locations.status==='fulfilled')setCountries(locations.value);
   else if(directory.status==='fulfilled')setCountries(Array.from(new Map(directory.value.filter(r=>r.countryCode!=='GLOBAL').map(r=>[r.countryCode,{code:r.countryCode,name:r.regionName}])).values()));
   setLoading(false);
 });return()=>{active=false;};},[attempt]);
 async function open(value:string,telephone=false){const url=safeResourceUrl(value,telephone);if(!url){setLinkError(true);return;}try{await Linking.openURL(url);setLinkError(false);}catch{setLinkError(true);}}
 function resource(item:CrisisResource){return <PublicCard key={item.id}><PublicHeading level={3}>{item.title}</PublicHeading><PublicText>{item.detail}</PublicText><PublicText>{c.sourceChecked}: {item.lastVerifiedOn}</PublicText>
  <AppButton label={item.actionUrl.startsWith('tel:')?c.call:c.openResource} disabled={!safeResourceUrl(item.actionUrl,true)} onPress={()=>void open(item.actionUrl,true)}/>
  {item.sourceUrl!==item.actionUrl&&<AppButton label={c.officialSource} variant="secondary" disabled={!safeResourceUrl(item.sourceUrl)} onPress={()=>void open(item.sourceUrl)}/>}
 </PublicCard>;}
 const local=all.filter(r=>r.countryCode===country),global=all.filter(r=>r.countryCode==='GLOBAL');
 return <>
  <PublicCard><PublicHeading>{c.immediateTitle}</PublicHeading><PublicText>{c.immediateBody}</PublicText></PublicCard>
  <PublicHeading>{c.chooseCountry}</PublicHeading><PublicText>{c.noCountry}</PublicText>
  <AppField label={c.searchCountry} value={query} onChangeText={setQuery}/>
  <View style={{gap:8}}>{countries.filter(item=>(item.name+' '+item.code+' '+Object.values(item.local_names??{}).join(' ')).toLowerCase().includes(query.toLowerCase())).slice(0,query?20:8).map(item=>{
   const name=language==='fa'?item.local_names?.fa??item.name:item.name;
   return <AppButton key={item.code} label={(country===item.code?'✓ ':'')+name} variant="secondary" onPress={()=>{setCountry(item.code);setQuery(name);}}/>;
  })}</View>
  {loading&&<Text role="status" style={publicStyles.body}>{c.loading}</Text>}
  {!loading&&!error&&country&&local.length===0&&<Text role="status" style={publicStyles.body}>{c.noLocal}</Text>}
  {error&&<><Text accessibilityRole="alert" style={publicStyles.status}>{c.directoryError}</Text><AppButton label={c.retry} onPress={()=>{setError(false);setLoading(true);setAttempt(n=>n+1);}}/></>}
  {local.map(resource)}
  {global.length>0&&<PublicHeading>{c.globalResources}</PublicHeading>}{global.map(resource)}
  {linkError&&<Text accessibilityRole="alert" style={publicStyles.status}>{c.linkError}</Text>}
 </>;
}
