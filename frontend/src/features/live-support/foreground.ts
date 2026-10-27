import {useCallback,useRef,useEffect} from 'react';
import {AppState,Platform} from 'react-native';
import {useFocusEffect} from 'expo-router';
import {useAuth} from '@/features/auth/auth-provider';
export function useForegroundPoll(callback:()=>Promise<unknown>,interval:number,onLeave?:()=>void){
 const {status,privacyHidden}=useAuth();const tick=useRef(callback);const leave=useRef(onLeave);
 useEffect(()=>{tick.current=callback;leave.current=onLeave;},[callback,onLeave]);
 const enabled=status==='authenticated'&&!privacyHidden;
 useFocusEffect(useCallback(()=>{
  if(!enabled)return;
  let live=true,running=false;
  const visible=()=>AppState.currentState==='active'&&(Platform.OS!=='web'||typeof document==='undefined'||!document.hidden);
  const run=async()=>{if(!live||running||!visible())return;running=true;try{await tick.current();}catch{/* Each screen renders its own recoverable error. */}finally{running=false;}};
  const changed=()=>{if(visible())void run();else leave.current?.();};
  void run();const timer=setInterval(()=>void run(),interval);const subscription=AppState.addEventListener('change',changed);
  if(Platform.OS==='web'&&typeof document!=='undefined')document.addEventListener('visibilitychange',changed);
  return()=>{live=false;clearInterval(timer);subscription.remove();if(Platform.OS==='web'&&typeof document!=='undefined')document.removeEventListener('visibilitychange',changed);leave.current?.();};
 },[enabled,interval]));
}
