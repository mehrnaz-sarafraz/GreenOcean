import {Redirect,useLocalSearchParams} from 'expo-router';
export default function ReviewedArticle(){const {id}=useLocalSearchParams<{id:string}>();return <Redirect href={{pathname:'/tools/[tool]',params:{tool:'growth-library',resource:id}}}/>;}
