export type RegistrationValues = { email: string; password: string; countryCode: string; adultSelfAttested: boolean; termsVersion: number; accepted: boolean };
// Mirrors EmailIdentity.PATTERN. ASCII mailboxes and DNS/punycode domains; no dot/plus rewriting.
const emailPattern = /^(?=.{1,320}$)(?=.{1,64}@)[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$/;
const commonPasswords = new Set(['passwordpassword', '123456789012345', '1234567890123456', 'qwertyuiopasdfgh', 'letmeinletmeinletmein']);
export function validAccountEmail(email: string): boolean { return emailPattern.test(email.trim()); }
export function validateRegistration(input: RegistrationValues): Record<string, string> {
  const errors: Record<string, string> = {};
  if (!validAccountEmail(input.email)) errors.email = 'Enter a valid email address.';
  if ([...input.password].length < 15 || [...input.password].length > 128) errors.password = 'Use 15–128 characters. Spaces and Unicode are welcome.';
  else if (commonPasswords.has(input.password.toLowerCase())) errors.password = 'Choose a less common password.';
  if (!/^[A-Z]{2}$/.test(input.countryCode)) errors.countryCode = 'Select a country of residence.';
  if (!input.adultSelfAttested) errors.adultSelfAttested = 'Social accounts are for adults 18+. Public resources are open to everyone.';
  if (!input.accepted || input.termsVersion < 1) errors.termsVersion = 'Review and accept the current Terms.';
  return errors;
}

export function registrationFailure(error: { status?: number; fieldErrors?: { field: string; message: string }[]; message?: string; retryAfter?: number | null }, fa = false) {
  const fields: Record<string, string> = {};
  const allowed = new Set(['email', 'password', 'countryCode', 'adultSelfAttested', 'termsVersion']);
  for (const violation of (error.status === 400 || error.status === 422) ? error.fieldErrors ?? [] : []) {
    if (allowed.has(violation.field)) fields[violation.field] = violation.message;
  }
  let message = fa ? 'ثبت‌نام تأیید نشد. دوباره تلاش کنید یا تأیید ایمیل را ادامه دهید.' : 'Registration was not confirmed. Try again or continue email verification if you already submitted.';
  if (error.status === 429) message = fa ? `تلاش‌های زیادی انجام شده است. ${error.retryAfter ?? 60} ثانیه صبر کنید.` : `Too many attempts. Wait ${error.retryAfter ?? 60} seconds before trying again.`;
  else if (error.status && error.status >= 500) message = fa ? 'سرویس موقتاً در دسترس نیست. کمی بعد دوباره تلاش کنید.' : 'Registration is temporarily unavailable. Please try again later.';
  else if (!error.status) message = fa ? 'اتصال برقرار نشد یا زمان درخواست تمام شد. اطلاعات شما حفظ شده؛ در صورت ارسال قبلی، تأیید ایمیل را ادامه دهید.' : 'Could not connect or the request timed out. Your changes were not confirmed. Retry or continue email verification if you already submitted.';
  else if (Object.keys(fields).length) message = fa ? 'موارد مشخص‌شده را اصلاح کنید.' : 'Please correct the highlighted fields.';
  return { fields, message, refreshTerms: !!fields.termsVersion };
}
