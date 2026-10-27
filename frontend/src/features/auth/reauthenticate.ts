import { Platform } from 'react-native';
import { apiRequest,ApiError } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';
import type { TokenResponse } from './types';
import { nativeSessionLock } from '@/lib/api/browser-session';

export async function confirmPassword(password:string){
 const epoch=tokenStorage.generation();
 const operation=()=>{if(epoch!==tokenStorage.generation())throw new ApiError(409,'Password confirmation cancelled after sign-out.');return confirmCurrentPassword(password,epoch);};
 return Platform.OS!=='web'?nativeSessionLock(operation):operation();
}
async function confirmCurrentPassword(password:string,epoch:number){
 const current=await tokenStorage.get();
 if(epoch!==tokenStorage.generation())throw new ApiError(409,'Password confirmation cancelled after sign-out.');
 const updated=await apiRequest<TokenResponse>(Platform.OS==='web'?'/api/v1/auth/web/reauthenticate':'/api/v1/auth/reauthenticate',{
  method:'POST',retryAfterRefresh:false,body:JSON.stringify({password,refreshToken:Platform.OS==='web'?undefined:current?.refreshToken}),
 });
 if(epoch!==tokenStorage.generation())throw new ApiError(409,'Password confirmation cancelled after sign-out.');
 await tokenStorage.set(updated);
 if(epoch!==tokenStorage.generation())throw new ApiError(409,'Password confirmation cancelled after sign-out.');
}
