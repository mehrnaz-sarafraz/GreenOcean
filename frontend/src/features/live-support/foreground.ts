import {useCallback,useRef,useEffect} from 'react';
import {AppState,Platform} from 'react-native';
import {useFocusEffect} from 'expo-router';
import {useAuth} from '@/features/auth/auth-provider';
import {createForegroundCycle} from './foreground-cycle';
export function useForegroundPoll(callback:(isCurrent:()=>boolean)=>Promise<unknown>,interval:number,onLeave?:()=>void){
 const {status,privacyHidden}=useAuth();const tick=useRef(callback);const leave=useRef(onLeave);
 const cycle=useRef<ReturnType<typeof createForegroundCycle>|null>(null);
 useEffect(()=>{tick.current=callback;leave.current=onLeave;},[callback,onLeave]);
 const enabled=status==='authenticated'&&!privacyHidden;
 useFocusEffect(useCallback(()=>{
  if(!enabled)return;
  const visible=()=>AppState.currentState==='active'&&(Platform.OS!=='web'||typeof document==='undefined'||!document.hidden);
  const current=createForegroundCycle(visible);cycle.current=current;
  const run=async()=>{try{await current.run(isCurrent=>tick.current(isCurrent));}catch{/* Each screen renders its own recoverable error. */}};
  const changed=()=>{if(visible())void run();else{current.invalidate();leave.current?.();}};
  void run();const timer=setInterval(()=>void run(),interval);const subscription=AppState.addEventListener('change',changed);
  if(Platform.OS==='web'&&typeof document!=='undefined')document.addEventListener('visibilitychange',changed);
  return()=>{current.close();if(cycle.current===current)cycle.current=null;clearInterval(timer);subscription.remove();if(Platform.OS==='web'&&typeof document!=='undefined')document.removeEventListener('visibilitychange',changed);leave.current?.();};
 },[enabled,interval]));
 return useCallback(async()=>{await cycle.current?.run(isCurrent=>tick.current(isCurrent));},[]);
}
