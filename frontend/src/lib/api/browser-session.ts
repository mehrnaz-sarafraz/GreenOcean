/** All browser cookie mutations share one origin-scoped lock. No credentials are put in storage/messages. */
export async function browserSessionLock<T>(operation:()=>Promise<T>):Promise<T>{
 if(typeof navigator==='undefined'||!navigator.locks)throw new Error('This browser cannot safely coordinate sign-in across tabs. Use a supported secure browser.');
 return navigator.locks.request('greenocean-browser-session',operation);
}
