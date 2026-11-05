import {Platform} from 'react-native';
import {apiDownloadBinary} from './client';
import {tokenStorage} from '@/lib/storage/token-storage';
import {newClientId} from '@/features/live-support/policy';
/** Only an explicit user export; never a persisted background private-file cache. */
export async function exportPrivateFile(path:string,reason:string){
 const epoch=tokenStorage.generation();const file=await apiDownloadBinary(path,{reason});if(epoch!==tokenStorage.generation())throw new Error('Private export cancelled after account change.');
 if(Platform.OS==='web'){if(document.hidden)throw new Error('Return to the private workspace before exporting.');const url=URL.createObjectURL(new Blob([file.bytes],{type:file.mime}));try{const a=document.createElement('a');a.href=url;a.download=file.filename;a.click();}finally{setTimeout(()=>URL.revokeObjectURL(url),1000);}return;}
 const [{File,Paths},Sharing]=await Promise.all([import('expo-file-system'),import('expo-sharing')]);if(!await Sharing.isAvailableAsync())throw new Error('Secure export is not available on this device.');if(epoch!==tokenStorage.generation())throw new Error('Private export cancelled after account change.');
 const {Directory}=await import('expo-file-system');const folder=new Directory(Paths.cache,'private-exports');folder.create({idempotent:true});const copy=new File(folder,`private-copy-${newClientId()}-${file.filename}`);try{copy.create();copy.write(new Uint8Array(file.bytes));if(epoch!==tokenStorage.generation())throw new Error('Private export cancelled after account change.');await Sharing.shareAsync(copy.uri,{mimeType:file.mime,dialogTitle:'Private document copy'});}finally{if(copy.exists)copy.delete();}
}

export async function clearPrivateExportCache(){if(Platform.OS==='web')return;try{const {Directory,Paths}=await import('expo-file-system');const folder=new Directory(Paths.cache,'private-exports');if(folder.exists)folder.delete();}catch{/* No document names, contents or keys enter ordinary logs. Native-device verification remains a release gate. */}}
