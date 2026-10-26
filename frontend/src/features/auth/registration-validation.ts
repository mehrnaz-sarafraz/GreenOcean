export type RegistrationValues = { email: string; password: string; countryCode: string; adultSelfAttested: boolean; termsVersion: number; accepted: boolean };
export function validateRegistration(input: RegistrationValues): Record<string, string> {
  const errors: Record<string, string> = {};
  if (!/^\S+@\S+\.\S+$/.test(input.email.trim())) errors.email = 'Enter a valid email address.';
  if ([...input.password].length < 15 || [...input.password].length > 128) errors.password = 'Use 15–128 characters. Spaces and Unicode are welcome.';
  if (!/^[A-Z]{2}$/.test(input.countryCode)) errors.countryCode = 'Select a country of residence.';
  if (!input.adultSelfAttested) errors.adultSelfAttested = 'Social accounts are for adults 18+. Public resources are open to everyone.';
  if (!input.accepted || input.termsVersion < 1) errors.termsVersion = 'Review and accept the current Terms.';
  return errors;
}
