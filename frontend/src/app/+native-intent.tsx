export function redirectSystemPath({path}:{path:string;initial:boolean}){
 try{const url=new URL(path,'greenocean://app');if(url.protocol!=='greenocean:')return '/welcome';const route=(url.hostname==='info'?'/info':'')+url.pathname;if(route!=='/info/reset-password')return '/welcome';const token=new URLSearchParams(url.hash.slice(1)).get('token');if(!token||!/^[A-Za-z0-9_-]{64}$/.test(token))return '/info/reset-password';return `/info/reset-password?token=${encodeURIComponent(token)}`;}catch{return '/welcome';}
}
