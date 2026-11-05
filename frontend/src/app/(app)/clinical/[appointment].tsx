import {useLocalSearchParams} from 'expo-router';
import {ClinicalConsultation} from '@/features/clinical-consultation';
export default function ClinicalAppointment(){const {appointment}=useLocalSearchParams<{appointment:string}>();return <ClinicalConsultation appointment={String(appointment||'')}/>;}
