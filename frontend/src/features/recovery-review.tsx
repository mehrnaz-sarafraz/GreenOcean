import {useRef,useState} from 'react';
import {Text,TextInput,View} from 'react-native';
import {ProductPage,ProductHeader,ProductCard} from '@/components/product-ui';
import {AppButton} from '@/components/app-button';
import {useAuth} from '@/features/auth/auth-provider';
import {useLanguage} from '@/localization/language-provider';
import {apiRequest} from '@/lib/api/client';
import {tokenStorage} from '@/lib/storage/token-storage';
import {useForegroundPoll} from '@/features/live-support/foreground';

type Run={id:string;source_commit:string;manifest_sha256:string;prepared_by:string;reviewer_id:string|null;expires_at:string};
type Status={restoreMode:boolean;replayReady:boolean;runs:Run[]};
type Detail={run:Run;subjects:{subject_id:string;domain:string}[];stoppedServices:{service:string}[]};

export function RecoveryReview(){
  const {user}=useAuth();
  return <RecoveryWorkspace key={String(user?.userId)+':'+String(user?.roles.includes('TRUST_ACCOUNT'))}/>;
}

function RecoveryWorkspace(){
  const {user}=useAuth();const {language,isRtl}=useLanguage();
  const tr=(en:string,fa:string)=>language==='fa'?fa:en;
  const authorized=!!user?.roles.includes('TRUST_ACCOUNT');
  const [status,setStatus]=useState<Status|null>(null),[detail,setDetail]=useState<Detail|null>(null);
  const [manifest,setManifest]=useState(''),[confirmed,setConfirmed]=useState(false);
  const [busy,setBusy]=useState(false),[error,setError]=useState('');
  const generation=useRef(0);
  const perform=async(action:(isCurrent:()=>boolean)=>Promise<void>)=>{
    if(busy||!authorized)return;const current=generation.current,epoch=tokenStorage.generation();
    const valid=()=>current===generation.current&&epoch===tokenStorage.generation();
    setBusy(true);setError('');
    try{await action(valid);}catch(e){if(valid()){setStatus(null);setDetail(null);setConfirmed(false);setError(e instanceof Error?e.message:tr('Please retry.','دوباره تلاش کنید.'));}}
    finally{if(valid())setBusy(false);}
  };
  const load=async()=>{const current=generation.current,epoch=tokenStorage.generation();
    const result=await apiRequest<Status>('/api/v1/recovery/status');
    if(current===generation.current&&epoch===tokenStorage.generation()){setStatus(result);setDetail(null);setConfirmed(false);}};
  const inspect=async(id:string)=>{const current=generation.current,epoch=tokenStorage.generation();
    setDetail(null);setConfirmed(false);const result=await apiRequest<Detail>(`/api/v1/recovery/runs/${id}`);
    if(current===generation.current&&epoch===tokenStorage.generation())setDetail(result);};
  useForegroundPoll(async isCurrent=>{
    if(!authorized)return;const current=generation.current,epoch=tokenStorage.generation();
    const valid=()=>isCurrent()&&current===generation.current&&epoch===tokenStorage.generation();
    try{const result=await apiRequest<Status>('/api/v1/recovery/status');if(valid())setStatus(result);}
    catch(e){if(valid()){setStatus(null);setDetail(null);setConfirmed(false);setError(e instanceof Error?e.message:tr('Please retry.','دوباره تلاش کنید.'));}}
  },15000,()=>{generation.current++;setStatus(null);setDetail(null);setManifest('');setConfirmed(false);setBusy(false);setError('');});
  return <ProductPage><View style={{gap:16,direction:isRtl?'rtl':'ltr'}}>
    <ProductHeader title={tr('Privacy review after restoration','بازبینی حریم خصوصی پس از بازیابی')}
      subtitle={tr('Two independent account-safety operators verify the exact external replay manifest.','دو مسئول مستقل ایمنی حساب، فهرست دقیق محدودیت‌های خارج از نسخهٔ پشتیبان را بررسی می‌کنند.')}/>
    {!authorized?<Text>{tr('Current account-safety authority is required.','مجوز فعلی ایمنی حساب لازم است.')}</Text>:<>
      <Text>{tr('Approval preserves subject restrictions and paused services. Separate service authorization is still required.','تأیید، محدودیت اشخاص و توقف خدمات را حفظ می‌کند. مجوز مستقل هر خدمت همچنان لازم است.')}</Text>
      <AppButton label={tr('Refresh recovery status','بازخوانی وضعیت بازیابی')} disabled={busy} onPress={()=>void perform(load)}/>
      {!!error&&<Text accessibilityRole="alert">{error}</Text>}
      {status&&<Text>{!status.restoreMode?tr('Restore mode is not configured.','حالت بازیابی پیکربندی نشده است.'):status.replayReady?tr('Independent replay review is recorded.','بازبینی مستقل محدودیت‌ها ثبت شده است.'):tr('Data access remains blocked pending independent review.','دسترسی داده تا بازبینی مستقل مسدود است.')}</Text>}
      {status?.restoreMode&&<ProductCard><Text>{tr('Exact external manifest JSON','فهرست دقیق خارجی در قالب JSON')}</Text>
        <TextInput accessibilityLabel={tr('External replay manifest','فهرست خارجی محدودیت‌ها')} value={manifest} onChangeText={setManifest} multiline maxLength={500000} style={{minHeight:140,borderWidth:1,padding:10}}/>
        <AppButton label={tr('Prepare exact replay for independent review','آماده‌سازی محدودیت‌های دقیق برای بازبینی مستقل')} disabled={busy||!manifest.trim()} onPress={()=>void perform(async isCurrent=>{
          const input:unknown=JSON.parse(manifest);await apiRequest('/api/v1/recovery/prepare',{method:'POST',body:JSON.stringify(input)});if(isCurrent()){setManifest('');await load();}
        })}/></ProductCard>}
      {status?.runs.map(run=><ProductCard key={run.id}><Text>{tr('Recovery reference: ','شناسهٔ بازیابی: ')}{run.id}</Text>
        <Text>{tr('Source commit: ','کامیت مبنا: ')}{run.source_commit}</Text>
        <Text>{tr('External manifest SHA-256: ','هش فهرست خارجی: ')}{run.manifest_sha256}</Text>
        <AppButton label={tr('Inspect replay restrictions','بررسی محدودیت‌های بازیابی')} disabled={busy} onPress={()=>void perform(()=>inspect(run.id))}/>
      </ProductCard>)}
      {detail&&<ProductCard><Text>{tr('Compare these entries with the authoritative current rights ledger outside the backup.','این موارد را با سوابق معتبر و فعلی حقوق کاربران، خارج از نسخهٔ پشتیبان مقایسه کنید.')}</Text>
        <Text>{detail.run.manifest_sha256}</Text>
        {detail.subjects.map(s=><Text key={s.subject_id+':'+s.domain}>{s.subject_id} · {s.domain}</Text>)}
        <Text>{tr('Services remaining paused: ','خدماتی که متوقف می‌مانند: ')}{detail.stoppedServices.map(s=>s.service).join(', ')||tr('None listed','موردی ثبت نشده است')}</Text>
        <AppButton variant="secondary" label={(confirmed?'✓ ':'')+tr('I independently compared the exact source and current rights manifest','مبنا و فهرست فعلی حقوق کاربران را مستقل مقایسه کردم')} disabled={busy||detail.run.prepared_by===user?.userId} onPress={()=>setConfirmed(!confirmed)}/>
        <AppButton label={tr('Record independent replay review','ثبت بازبینی مستقل محدودیت‌ها')} disabled={busy||!confirmed||detail.run.prepared_by===user?.userId} onPress={()=>void perform(async isCurrent=>{
          await apiRequest(`/api/v1/recovery/runs/${detail.run.id}/review`,{method:'POST'});if(isCurrent())await load();
        })}/>
        <AppButton variant="secondary" label={tr('Invalidate this recovery approval','لغو اعتبار تأیید این بازیابی')} disabled={busy} onPress={()=>void perform(async isCurrent=>{
          await apiRequest(`/api/v1/recovery/runs/${detail.run.id}/invalidate`,{method:'POST'});if(isCurrent())await load();
        })}/>
      </ProductCard>}
    </>}
  </View></ProductPage>;
}
