import {router,useFocusEffect} from 'expo-router';
import {useCallback,useState} from 'react';
import {Text,TextInput,View} from 'react-native';
import {Screen} from '@/components/screen';
import {AppButton} from '@/components/app-button';
import {PageResponse,PostItem} from '@/features/content/types';
import {apiRequest} from '@/lib/api/client';
import {useLanguage} from '@/localization/language-provider';
import {tokenStorage} from '@/lib/storage/token-storage';

export default function MyPosts(){
 const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [items,setItems]=useState<PostItem[]>([]);const [page,setPage]=useState(0);const [more,setMore]=useState(false);
 const [edit,setEdit]=useState<PostItem|null>(null);const [body,setBody]=useState('');const [error,setError]=useState('');const [busy,setBusy]=useState(false);
 useFocusEffect(useCallback(()=>{let active=true;const epoch=tokenStorage.generation();setItems([]);setEdit(null);
  void apiRequest<PageResponse<PostItem>>('/api/v1/posts/mine').then(r=>{if(active&&epoch===tokenStorage.generation()){setItems(r.items);setPage(0);setMore(r.hasNext);}}).catch(e=>{if(active)setError(e.message);});return()=>{active=false;setItems([]);setEdit(null);};
 },[]));
 async function next(){setBusy(true);try{const r=await apiRequest<PageResponse<PostItem>>(`/api/v1/posts/mine?page=${page+1}`);setItems(current=>[...current,...r.items]);setPage(page+1);setMore(r.hasNext);}catch(e){setError(e instanceof Error?e.message:'Unable to load');}finally{setBusy(false);}}
 async function save(){if(!edit||busy)return;setBusy(true);setError('');try{const r=await apiRequest<PostItem>(`/api/v1/posts/${edit.id}`,{method:'PUT',body:JSON.stringify({body,revision:edit.revision})});setItems(current=>current.map(p=>p.id===r.id?r:p));setEdit(null);}catch(e){setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 async function remove(id:string){setBusy(true);try{await apiRequest(`/api/v1/posts/${id}`,{method:'DELETE'});setItems(current=>current.filter(p=>p.id!==id));setEdit(null);}catch(e){setError(e instanceof Error?e.message:'Unable to delete');}finally{setBusy(false);}}
 const status=(p:PostItem)=>p.status==='PUBLISHED'?tr('Published','منتشرشده'):p.safetyOutcome==='REQUEST_REVISION'?tr('Changes requested','نیازمند اصلاح'):p.safetyOutcome==='BLOCK_PROHIBITED'?tr('Not published','منتشرنشده'):p.status==='HIDDEN'?tr('Hidden','پنهان‌شده'):tr('Awaiting review; no response time promised','در انتظار بررسی؛ زمان پاسخ تضمین نمی‌شود');
 return <Screen scroll><View style={{gap:16,maxWidth:720,width:'100%',alignSelf:'center'}}><Text accessibilityRole="header">{tr('My posts and decisions','پست‌های من و تصمیم‌ها')}</Text>
 <AppButton label={tr('My content decisions and appeals','تصمیم‌ها و اعتراض‌های من')} variant="secondary" onPress={()=>router.push('/content-decisions')}/>
 {!!error&&<Text accessibilityRole="alert">{error}</Text>}
 {items.length===0&&<Text>{tr('No posts to manage.','پستی برای مدیریت وجود ندارد.')}</Text>}
 {items.map(p=><View key={p.id} style={{gap:10,padding:16,borderWidth:1,borderColor:'#b9d2d0'}}><Text accessibilityRole="alert">{status(p)}</Text><Text>{p.anonymous?tr('Anonymous','ناشناس'):tr('Social identity','هویت اجتماعی')} · {p.visibility}</Text><Text>{p.body}</Text>
 <AppButton label={tr('Edit text','ویرایش متن')} disabled={busy} variant="secondary" onPress={()=>{setEdit(p);setBody(p.body);}}/>
 <AppButton label={tr('Delete post','حذف پست')} disabled={busy} variant="secondary" onPress={()=>void remove(p.id)}/></View>)}
 {edit&&<View style={{gap:12}}><Text>{tr('Identity and audience stay unchanged. Changes are screened again.','هویت و مخاطبان ثابت می‌مانند. تغییرات دوباره بررسی می‌شوند.')}</Text><TextInput accessibilityLabel={tr('Revised post text','متن ویرایش‌شده')} multiline maxLength={10000} value={body} onChangeText={setBody} style={{minHeight:140,borderWidth:1,padding:12}}/><AppButton label={tr('Save reviewed changes','ذخیره و بررسی تغییرات')} disabled={busy||!body.trim()} onPress={()=>void save()}/><AppButton label={tr('Cancel edit','لغو ویرایش')} variant="secondary" onPress={()=>setEdit(null)}/></View>}
 {more&&<AppButton label={tr('Load more','نمایش بیشتر')} disabled={busy} onPress={()=>void next()}/>}
 </View></Screen>;
}
