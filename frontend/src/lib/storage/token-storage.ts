import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';
const ACCESS_TOKEN_KEY='greenocean.accessToken',REFRESH_TOKEN_KEY='greenocean.refreshToken';
const LOGOUT_PENDING='greenocean.logoutPending',NATIVE_PENDING='greenocean.pendingLogout';
const secureOptions={keychainAccessible:SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY};
export type StoredTokens={accessToken:string;refreshToken:string};
let browserTokens:StoredTokens|null=null,epoch=0,blocked=false;
const clearListeners=new Set<(reason?:string)=>void>();
const channel=Platform.OS==='web'&&typeof BroadcastChannel!=='undefined'?new BroadcastChannel('greenocean-auth-state'):null;
channel?.addEventListener('message',event=>{if(event.data==='logout'){blocked=true;clearMemory();}if(event.data==='login'){blocked=false;clearMemory();}});
function persistentGuardAvailable(){try{const storage=globalThis.localStorage;if(!storage)return false;const probe='greenocean.storageProbe';storage.setItem(probe,'1');storage.removeItem(probe);return true;}catch{return false;}}
function purgeLegacyWebStorage(){if(Platform.OS!=='web')return;try{for(const storage of [globalThis.localStorage,globalThis.sessionStorage]){storage?.removeItem(ACCESS_TOKEN_KEY);storage?.removeItem(REFRESH_TOKEN_KEY);}}catch{/* Storage disabled: memory still clears. */}}
function webBlocked(){try{return blocked||globalThis.localStorage?.getItem(LOGOUT_PENDING)==='1'||globalThis.document?.cookie.split('; ').includes('greenocean_logout_pending=1');}catch{return blocked;}}
function clearMemory(reason?:string){epoch+=1;browserTokens=null;clearListeners.forEach(listener=>listener(reason));}
if(Platform.OS==='web'&&typeof globalThis.addEventListener==='function')globalThis.addEventListener('storage',event=>{if(event.key===LOGOUT_PENDING&&event.newValue==='1'){blocked=true;clearMemory();}if(event.key==='greenocean.authChanged'){blocked=false;clearMemory();}});
export const tokenStorage={
 generation:()=>epoch,
 announceLogin(){if(Platform.OS==='web')try{channel?.postMessage('login');globalThis.localStorage?.setItem('greenocean.authChanged',String(Date.now())+Math.random());}catch{/* no bearer data; storage unavailable limits cross-tab signals */}},
 subscribeToClear(listener:(reason?:string)=>void){clearListeners.add(listener);return()=>{clearListeners.delete(listener);};},
 async get():Promise<StoredTokens|null>{
  if(Platform.OS==='web'){purgeLegacyWebStorage();return webBlocked()?null:browserTokens??(persistentGuardAvailable()?{accessToken:'',refreshToken:'cookie'}:null);}
  const [accessToken,refreshToken]=await Promise.all([SecureStore.getItemAsync(ACCESS_TOKEN_KEY,secureOptions),SecureStore.getItemAsync(REFRESH_TOKEN_KEY,secureOptions)]);return accessToken&&refreshToken?{accessToken,refreshToken}:null;
 },
 async set(tokens:StoredTokens){
  if(Platform.OS==='web'){purgeLegacyWebStorage();if(webBlocked())throw new Error('Complete pending sign-out before signing in');browserTokens={accessToken:tokens.accessToken,refreshToken:'cookie'};return;}
  try{await SecureStore.setItemAsync(REFRESH_TOKEN_KEY,tokens.refreshToken,secureOptions);await SecureStore.setItemAsync(ACCESS_TOKEN_KEY,tokens.accessToken,secureOptions);}catch{await this.clear();throw new Error('Secure credential storage is unavailable. Sign in again.');}
 },
 async blockRestore(refreshToken?:string){
  if(Platform.OS==='web'){blocked=true;channel?.postMessage('logout');try{globalThis.document.cookie='greenocean_logout_pending=1; Path=/; SameSite=Strict'+(globalThis.location.protocol==='https:'?'; Secure':'');globalThis.localStorage?.setItem(LOGOUT_PENDING,'1');}catch{/* This browser cannot persist a cross-reload logout guard; UI explains reconciliation. */}}
  else if(refreshToken)await SecureStore.setItemAsync(NATIVE_PENDING,refreshToken,secureOptions);
 },
 async pendingLogout():Promise<string|null>{return Platform.OS==='web'?(webBlocked()||(!browserTokens&&!persistentGuardAvailable())?'cookie':null):SecureStore.getItemAsync(NATIVE_PENDING,secureOptions);},
 async allowRestore(){if(Platform.OS==='web'){blocked=false;try{globalThis.document.cookie='greenocean_logout_pending=; Path=/; Max-Age=0; SameSite=Strict'+(globalThis.location.protocol==='https:'?'; Secure':'');globalThis.localStorage?.removeItem(LOGOUT_PENDING);}catch{/* local process flag remains clear */}}else await SecureStore.deleteItemAsync(NATIVE_PENDING,secureOptions);},
 async clear(reason?:string){clearMemory(reason);if(Platform.OS==='web'){purgeLegacyWebStorage();return;}await Promise.all([SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY,secureOptions),SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY,secureOptions)]);},
};
