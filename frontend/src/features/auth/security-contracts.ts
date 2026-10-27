export function passwordBounds(value:string):boolean{return [...value].length>=15&&[...value].length<=128;}
export function safeRecoveryToken(value:string):boolean{return /^[A-Za-z0-9_-]{64}$/.test(value);}
export function needsBackgroundReauthentication(backgroundAt:number,now:number):boolean{return now-backgroundAt>=300000;}
export function securityEventLabel(action:string,fa:boolean):string{
 const labels:Record<string,[string,string]>={LOGIN_SUCCESS:['Signed in','ورود به حساب'],LOGIN_FAILURE:['Sign-in failed','ورود ناموفق'],PASSWORD_CHANGED:['Password changed','تغییر رمز'],RESET_COMPLETED:['Account recovery completed','تکمیل بازیابی حساب'],SESSION_REVOKED:['Device signed out','خروج دستگاه'],LOGOUT_ALL:['All devices signed out','خروج همه دستگاه‌ها'],REFRESH_REPLAY:['Session reuse detected','تشخیص استفاده دوباره از نشست'],MFA_REGISTERED:['Security key registered','ثبت کلید امنیتی'],MFA_REMOVED:['Security key removed','حذف کلید امنیتی'],MFA_SUCCESS:['Security key verified','تأیید کلید امنیتی'],REAUTH_SUCCESS:['Password confirmed','تأیید دوباره رمز']};return labels[action]?.[fa?1:0]??(fa?'رویداد امنیتی':'Security event');
}
