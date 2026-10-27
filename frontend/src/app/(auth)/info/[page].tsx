import { useLocalSearchParams } from 'expo-router';
import { AssistanceForm } from '@/features/public/assistance-form';
import { isAssistanceId } from '@/features/public/contracts';
import { PublicInformation } from '@/features/public/public-information';
export default function InfoPage(){const {page,token}=useLocalSearchParams<{page?:string;token?:string}>();return isAssistanceId(page)?<AssistanceForm key={page} page={page} token={typeof token==='string'?token:undefined}/>:<PublicInformation page={page}/>;}
