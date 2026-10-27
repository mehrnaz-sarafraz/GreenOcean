import { useState } from 'react';
import { Text, View } from 'react-native';
import { AppField } from '@/components/app-field';
import { AppButton } from '@/components/app-button';
import { usePlatformData } from '@/features/platform/data-provider';
import { apiRequest } from '@/lib/api/client';
export function DisplayReview({onReviewed}:{onReviewed:()=>void}) {
  const {profile,refresh}=usePlatformData();
  const [username,setUsername]=useState(profile?.username??'');
  const [displayName,setDisplayName]=useState(profile?.displayName??'');
  const [busy,setBusy]=useState(false);const [error,setError]=useState('');
  async function save() {
    if(busy)return;setBusy(true);setError('');
    try {
      await apiRequest('/api/v1/profiles/me',{method:'PATCH',body:JSON.stringify({username,displayName})});
      await apiRequest('/api/v1/identity/display-reviewed',{method:'POST'});await refresh();onReviewed();
    } catch(e) {setError(e instanceof Error?e.message:'Could not save. Please retry.');} finally {setBusy(false);}
  }
  return <View style={{gap:16,padding:24}}><Text style={{fontSize:24,fontWeight:'700'}}>Review your public identity</Text>
    <Text>Before your first contribution, choose how others will see you. Avoid identifying or medical details. A title does not create professional verification.</Text>
    <AppField label="Username" value={username} onChangeText={setUsername} autoCapitalize="none" />
    <AppField label="Display name" value={displayName} onChangeText={setDisplayName} />
    {!!error&&<Text accessibilityRole="alert">{error}</Text>}
    <AppButton label="Save and continue" loading={busy} onPress={()=>void save()} />
    <Text>Community members share lived experience. Listeners offer non-clinical support. Professional care requires verified scope and regional availability.</Text>
  </View>;
}
