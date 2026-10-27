import {useCallback,useEffect,useRef,useState} from 'react';
import {apiRequest} from '@/lib/api/client';
import {useForegroundPoll} from '@/features/live-support/foreground';
import {tokenStorage} from '@/lib/storage/token-storage';
import {PostItem} from './types';
type Feed={items:PostItem[];hasNext:boolean;nextCursor:string|null};
export function useSocialFeed(mode:'DISCOVER'|'FOLLOWING'|'SAVED',categoryId:string){
 const [loadedQuery,setLoadedQuery]=useState('');
 const [items,setItems]=useState<PostItem[]>([]);const [cursor,setCursor]=useState<string|null>(null);
 const [loading,setLoading]=useState(true);const [error,setError]=useState('');const version=useRef(0);const pending=useRef<number|null>(null);
 const query=`/api/v1/posts/discovery?mode=${mode}&size=20${categoryId==='all'?'':`&categoryId=${encodeURIComponent(categoryId)}`}`;
 useEffect(()=>{version.current++;},[query]);
 async function load(isCurrent:()=>boolean,append=false){
  if(pending.current===version.current)return;pending.current=version.current;const generation=version.current,epoch=tokenStorage.generation();setLoading(true);
  try{const r=await apiRequest<Feed>(query+(append&&cursor?'&cursor='+encodeURIComponent(cursor):''));
   if(!isCurrent()||generation!==version.current||epoch!==tokenStorage.generation())return;
   setLoadedQuery(query);setItems(current=>append?[...current,...r.items.filter(p=>!current.some(old=>old.id===p.id))]:r.items);setCursor(r.nextCursor);setError('');
  }catch(e){if(isCurrent()&&generation===version.current){setLoadedQuery(query);setItems([]);setCursor(null);setError(e instanceof Error?e.message:'Could not load stories');}}
  finally{if(pending.current===generation)pending.current=null;if(isCurrent()&&generation===version.current)setLoading(false);}
 }
 const clear=useCallback(()=>{version.current++;setItems([]);setCursor(null);},[]);
 const refresh=useForegroundPoll(isCurrent=>load(isCurrent),15000,clear);
 useEffect(()=>{void refresh();},[query,refresh]);
 return {items:loadedQuery===query?items:[],loading:loadedQuery!==query||loading,error:loadedQuery===query?error:'',hasNext:loadedQuery===query&&cursor!==null,refresh,loadMore:async()=>{const generation=version.current;await load(()=>generation===version.current,true);}};
}
