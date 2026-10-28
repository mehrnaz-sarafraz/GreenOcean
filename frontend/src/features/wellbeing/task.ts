import {useEffect,useRef,useState} from 'react';
import {tokenStorage} from '@/lib/storage/token-storage';
/** Keep asynchronous status scoped to the mounted account, never a later sign-in. */
export function useTask(){
 const [busy,setBusy]=useState(false),[error,setError]=useState(''),[notice,setNotice]=useState('');
 const alive=useRef(true);
 useEffect(()=>{alive.current=true;return()=>{alive.current=false;};},[]);
 const run=async(action:()=>Promise<unknown>)=>{
  if(busy)return;setBusy(true);setError('');setNotice('');const epoch=tokenStorage.generation();
  try{await action();}catch(e){if(alive.current&&epoch===tokenStorage.generation())setError(e instanceof Error?e.message:'Please retry.');}
  finally{if(alive.current&&epoch===tokenStorage.generation())setBusy(false);}
 };
 return {busy,error,notice,setNotice,setError,run};
}
