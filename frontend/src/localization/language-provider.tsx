import AsyncStorage from '@react-native-async-storage/async-storage';
import { getLocales } from 'expo-localization';
import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';

import { Language, TranslationKey, translations } from './translations';

const LANGUAGE_KEY = 'greenocean.language';

type LanguageContextValue = {
  language: Language;
  isRtl: boolean;
  setLanguage: (language: Language) => Promise<void>;
  t: (key: TranslationKey) => string;
};

const LanguageContext = createContext<LanguageContextValue | null>(null);

export function LanguageProvider({ children }: PropsWithChildren) {
  const deviceLanguage: Language = getLocales()[0]?.languageCode === 'fa' ? 'fa' : 'en';
  const [language, setLanguageState] = useState<Language>(deviceLanguage);

  useEffect(() => {
    AsyncStorage.getItem(LANGUAGE_KEY).then((savedLanguage) => {
      if (savedLanguage === 'fa' || savedLanguage === 'en') setLanguageState(savedLanguage);
    });
  }, []);

  const value = useMemo<LanguageContextValue>(() => ({
    language,
    isRtl: language === 'fa',
    setLanguage: async (nextLanguage) => {
      setLanguageState(nextLanguage);
      await AsyncStorage.setItem(LANGUAGE_KEY, nextLanguage);
    },
    t: (key) => translations[language][key],
  }), [language]);

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) throw new Error('useLanguage must be used inside LanguageProvider');
  return context;
}
