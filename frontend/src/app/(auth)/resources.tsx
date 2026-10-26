import { router } from 'expo-router';
import { useEffect,useState } from 'react';
import { Text } from 'react-native';
import { Screen } from '@/components/screen';
import { AppButton } from '@/components/app-button';
import { apiRequest } from '@/lib/api/client';
export default function ResourcesScreen() {
  const [data,setData]=useState<{items:{id:string;title:string;body:string}[];boundaries:string[]}|null>(null);
  const [error,setError]=useState('');
  async function load(){try{setData(await apiRequest('/api/v1/public/resources',{authenticated:false}));setError('');}catch{setError('Could not load resources. Please retry when connected.');}}
  useEffect(() => {
    let active = true;
    apiRequest<{items:{id:string;title:string;body:string}[];boundaries:string[]}>('/api/v1/public/resources',{authenticated:false})
      .then(result => { if (active) setData(result); })
      .catch(() => { if (active) setError('Could not load resources. Please retry when connected.'); });
    return () => { active = false; };
  }, []);
  return <Screen scroll><Text style={{fontSize:28,fontWeight:'700'}}>Public resources • منابع عمومی</Text>
    <Text>All ages welcome. No account required. • بدون نیاز به حساب</Text>
    {data?.items.map(item=><Text key={item.id} style={{marginVertical:16}}>{item.title}{'\n'}{item.body}</Text>)}
    {data?.boundaries.map(text=><Text key={text} style={{marginVertical:8}}>{text}</Text>)}
    {!!error && <><Text accessibilityRole="alert">{error}</Text><AppButton label="Retry" onPress={()=>void load()} /></>}
    <AppButton label="Urgent help • کمک فوری" onPress={()=>router.push('/(auth)/urgent-help')} />
    <AppButton label="Back to welcome" variant="secondary" onPress={()=>router.replace('/(auth)/welcome')} />
  </Screen>;
}
