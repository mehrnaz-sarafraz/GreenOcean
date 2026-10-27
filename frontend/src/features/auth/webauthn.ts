import { Platform } from 'react-native';
import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';
function decode(value:string){return Uint8Array.from(atob(value.replace(/-/g,'+').replace(/_/g,'/')),c=>c.charCodeAt(0));}
function encode(value:ArrayBuffer){return btoa(String.fromCharCode(...new Uint8Array(value))).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+$/,'');}
export async function securityKey(register:boolean,permit=''){
 if(Platform.OS!=='web'||!globalThis.isSecureContext||!navigator.credentials)throw new Error('Use an approved HTTPS staff browser to verify your security key.');
 const kind=register?'registration':'authentication';
 const begin=await apiRequest<{ceremonyId:string;optionsJson:string}>(`/api/v1/auth/mfa/${kind}/options`,{method:'POST',body:JSON.stringify({permit})});
 const options=JSON.parse(begin.optionsJson);options.publicKey.challenge=decode(options.publicKey.challenge);
 if(register){options.publicKey.user.id=decode(options.publicKey.user.id);if(options.publicKey.excludeCredentials)options.publicKey.excludeCredentials=options.publicKey.excludeCredentials.map((c:{id:string})=>({...c,id:decode(c.id)}));}
 else if(options.publicKey.allowCredentials)options.publicKey.allowCredentials=options.publicKey.allowCredentials.map((c:{id:string})=>({...c,id:decode(c.id)}));
 const result=await (register?navigator.credentials.create(options):navigator.credentials.get(options)) as PublicKeyCredential|null;
 if(!result)throw new Error('Security key operation was cancelled.');
 const response=result.response as AuthenticatorAttestationResponse&AuthenticatorAssertionResponse;
 const credential={id:result.id,rawId:encode(result.rawId),type:result.type,clientExtensionResults:result.getClientExtensionResults(),response:register?{clientDataJSON:encode(response.clientDataJSON),attestationObject:encode(response.attestationObject),transports:response.getTransports?.()??[]}:{clientDataJSON:encode(response.clientDataJSON),authenticatorData:encode(response.authenticatorData),signature:encode(response.signature),userHandle:response.userHandle?encode(response.userHandle):null}};
 const epoch=tokenStorage.generation();const tokens=await tokenStorage.get();
 const path=!register&&Platform.OS==='web'?'/api/v1/auth/web/mfa/authentication/verify':`/api/v1/auth/mfa/${kind}/verify`;
 const updated=await apiRequest<{accessToken:string;refreshToken:string}|undefined>(path,{method:'POST',retryAfterRefresh:false,body:JSON.stringify({ceremonyId:begin.ceremonyId,credentialJson:JSON.stringify(credential),label:'Security key',refreshToken:Platform.OS==='web'?undefined:tokens?.refreshToken})});
 if(updated&&epoch===tokenStorage.generation())await tokenStorage.set(updated);
}
