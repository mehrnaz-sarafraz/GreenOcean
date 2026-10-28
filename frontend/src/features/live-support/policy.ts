export type LiveStatus = 'REQUESTED'|'ACCEPTED'|'ACTIVE'|'INTERRUPTED'|'DECLINED'|'CANCELLED'|'EXPIRED'|'COMPLETED'|'ENDED_SAFETY';
export type LiveRequest = {id:string;status:LiveStatus;reasonCode:string|null;serverTime:string;expiresAt:string;activationExpiresAt:string|null;interruptedAt:string|null;endedAt:string|null;conversationId:string|null;peerId:string;peerName:string;listenerSide:boolean;presenceSeconds:number;reconnectSeconds:number;activatedAt?:string|null;plannedEndsAt?:string|null};
export type Availability={enabled:boolean;state:'OFFLINE'|'ONLINE_NOT_AVAILABLE'|'AVAILABLE_FOR_REQUESTS'|'BUSY'|'UNAVAILABLE';leaseId:string|null;expiresAt:string|null;serverTime:string;heartbeatSeconds:number};
export function isOpen(status:LiveStatus){return ['REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED'].includes(status);}
export function canSend(status:LiveStatus|undefined){return status==='ACTIVE';}
export function remainingSeconds(deadline:string|null,serverTime:string,observedAt:number,now:number){
 if(!deadline)return 0;
 const remaining=Date.parse(deadline)-Date.parse(serverTime)-Math.max(0,now-observedAt);
 return Number.isFinite(remaining)?Math.max(0,Math.ceil(remaining/1000)):0;
}
// Retry identifiers are not authentication credentials. The server never treats them as authority.
export function newClientId(){return globalThis.crypto?.randomUUID?.()??'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const n=Math.floor(Math.random()*16);return(c==='x'?n:(n&3)|8).toString(16);});}
export const statusCopy:Record<LiveStatus,[string,string]>={
 REQUESTED:['Waiting for listener acceptance','در انتظار پذیرش شنونده'],ACCEPTED:['Listener accepted — ready to enter','شنونده پذیرفت؛ آمادهٔ ورود'],ACTIVE:['Live conversation active','گفت‌وگوی زنده فعال است'],INTERRUPTED:['Connection interrupted — waiting for both participants','ارتباط قطع شده؛ در انتظار بازگشت هر دو نفر'],DECLINED:['The listener could not accept','شنونده امکان پذیرش نداشت'],CANCELLED:['Request cancelled','درخواست لغو شد'],EXPIRED:['Request expired — no waiting list was created','مهلت درخواست تمام شد؛ فهرست انتظاری ایجاد نشد'],COMPLETED:['Conversation ended','گفت‌وگو پایان یافت'],ENDED_SAFETY:['Session closed for safety or authorization','جلسه به دلیل ایمنی یا مجوز دسترسی بسته شد']};
export const availabilityCopy:Record<Availability['state'],[string,string]>={OFFLINE:['Offline','آفلاین'],ONLINE_NOT_AVAILABLE:['Not accepting requests','پذیرای درخواست نیست'],AVAILABLE_FOR_REQUESTS:['Available for a live request','آمادهٔ دریافت درخواست زنده'],BUSY:['Busy with a support request or session','مشغول درخواست یا جلسهٔ حمایت'],UNAVAILABLE:['Live listener access is unavailable','دسترسی زندهٔ شنونده فراهم نیست']};
