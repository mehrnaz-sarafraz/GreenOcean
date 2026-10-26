export const policyKeys = { terms:'terms', privacy:'privacy', guidelines:'community-guidelines', cookies:'cookies', 'age-eligibility':'age-eligibility', 'service-boundaries':'service-boundaries', 'data-retention':'data-retention' } as const;
export const informationIds = ['how-it-works','faq','about','help','accessibility','community','listener','professional'] as const;
export const assistanceIds = ['contact','forgot-password','reset-password','verify-email'] as const;
export function isInformationId(value: unknown): value is typeof informationIds[number] { return typeof value==='string' && (informationIds as readonly string[]).includes(value); }
export function policyKey(value: unknown): string | null { return typeof value==='string'&&Object.hasOwn(policyKeys,value)?policyKeys[value as keyof typeof policyKeys]:null; }
export function isAssistanceId(value: unknown): value is typeof assistanceIds[number] {return typeof value==='string'&&(assistanceIds as readonly string[]).includes(value);}
export function safeResourceUrl(value: unknown, allowTelephone=false): string | null {
  if(typeof value!=='string'||value.length>2048||/[\u0000-\u0020\u007f]/.test(value))return null;
  if(allowTelephone&&/^tel:\+?[0-9][0-9-]{1,24}$/.test(value))return value;
  try {const url=new URL(value);return url.protocol==='https:'&&!url.username&&!url.password?url.href:null;}catch{return null;}
}
export function accountEntry(status: string): 'loading'|'guest'|'account' {return status==='loading'?'loading':status==='authenticated'?'account':'guest';}
