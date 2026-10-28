import AsyncStorage from '@react-native-async-storage/async-storage';
import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { Platform } from 'react-native';
import { Language, TranslationKey, translations } from './translations';
type LanguageContextValue = { language: Language; ready: boolean; isRtl: boolean; setLanguage: (language: Language) => Promise<void>; t: (key: TranslationKey) => string };
const LanguageContext = createContext<LanguageContextValue | null>(null);
const KEY = 'greenocean.language';
export function LanguageProvider({ children }: PropsWithChildren) {
  const [language, updateLanguage] = useState<Language>('en');
  const [ready,setReady]=useState(false);
  const chosen = useRef(false);
  useEffect(() => { let active=true; AsyncStorage.getItem(KEY).then(value=>{if(active&&!chosen.current&&(value==='en'||value==='fa'))updateLanguage(value);}).catch(()=>{}).finally(()=>{if(active)setReady(true);}); return()=>{active=false;}; }, []);
  useEffect(() => { if(Platform.OS==='web'&&typeof document!=='undefined'){document.documentElement.lang=language;document.documentElement.dir=language==='fa'?'rtl':'ltr';} },[language]);
  const value = useMemo<LanguageContextValue>(() => ({ language, ready, isRtl:language==='fa', setLanguage:async next=>{chosen.current=true;updateLanguage(next);try{await AsyncStorage.setItem(KEY,next);}catch{/* Preference persistence is optional. */}}, t:key=>translations[language][key] }), [language,ready]);
  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
}
export function useLanguage() { const context=useContext(LanguageContext);if(!context)throw new Error('useLanguage must be used inside LanguageProvider');return context; }
