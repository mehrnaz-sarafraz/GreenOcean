import { PublicPage,PublicHeading,PublicText } from '@/features/public/public-page';
import { UrgentHelpContent } from '@/features/safety/urgent-help';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
export default function PublicUrgentHelp(){const {language}=useLanguage();const c=publicCopy[language];return <PublicPage><PublicHeading level={1}>{c.urgentTitle}</PublicHeading><PublicText>{c.urgentIntro}</PublicText><UrgentHelpContent/></PublicPage>;}
