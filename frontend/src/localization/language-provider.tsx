import { createContext, PropsWithChildren, useContext, useMemo } from 'react';
import { Language, TranslationKey, translations } from './translations';
type LanguageContextValue = { language: Language; isRtl: false; setLanguage: (language: Language) => Promise<void>; t: (key: TranslationKey) => string };
const LanguageContext = createContext<LanguageContextValue | null>(null);
export function LanguageProvider({ children }: PropsWithChildren) {
  const value = useMemo<LanguageContextValue>(() => ({ language: 'en', isRtl: false, setLanguage: async () => {}, t: key => translations.en[key] }), []);
  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}
export function useLanguage() { const context = useContext(LanguageContext); if (!context) throw new Error('useLanguage must be used inside LanguageProvider'); return context; }
