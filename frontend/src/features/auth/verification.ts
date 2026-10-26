export function normalizeVerificationCode(value: string): string {
  return value.replace(/[۰-۹]/g, digit => String(digit.charCodeAt(0) - 0x06f0))
    .replace(/[٠-٩]/g, digit => String(digit.charCodeAt(0) - 0x0660)).replace(/[\s-]/g, '');
}
export function validVerificationCode(value: string): boolean { return /^[0-9]{6}$/.test(value); }
export function maskVerificationEmail(email: string): string {
  const parts=email.trim().split('@');
  if(parts.length!==2||!parts[0]||!parts[1].includes('.'))return '';
  const domain=parts[1].split('.');
  return `${[...parts[0]][0]}•••@${domain[0][0]}•••.${domain.at(-1)}`;
}
export function cooldownSeconds(deadline: number, now: number): number {return Math.max(0,Math.ceil((deadline-now)/1000));}
