import {router} from 'expo-router';
import {AppButton} from '@/components/app-button';
import {ProductHeader,ProductPage} from '@/components/product-ui';
import {useLanguage} from '@/localization/language-provider';
import {Alternatives,Boundaries} from '@/features/live-support/ui';
export default function SupportNow(){
 const {language}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 return <ProductPage><ProductHeader backLabel={tr('Go back','بازگشت')} title={tr('What would feel helpful right now?','اکنون چه نوع حمایتی برایتان مناسب است؟')} subtitle={tr('Explore available support without sharing health topics or automatically joining a conversation.','بدون افشای موضوعات سلامت یا پیوستن خودکار به گفت‌وگو، راه‌های حمایت را بررسی کنید.')} /><Boundaries/>
 <AppButton label={tr('Check current listener availability','بررسی دسترسی فعلی به شنونده')} onPress={()=>router.push({pathname:'/listener/[action]',params:{action:'match'}})}/>
 <AppButton variant="secondary" label={tr('Future booking','رزرو برای آینده')} onPress={()=>router.push({pathname:'/listener/[action]',params:{action:'bookings'}})}/>
 <Alternatives/><AppButton variant="secondary" label={tr('Listener workspace','محیط کار شنونده')} onPress={()=>router.push({pathname:'/listener/[action]',params:{action:'workspace'}})}/></ProductPage>;
}
