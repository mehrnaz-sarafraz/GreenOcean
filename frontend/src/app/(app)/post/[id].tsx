import {FurtherReading} from '@/features/wellbeing/growth-library';
import {router,useLocalSearchParams} from 'expo-router';
import {useState} from 'react';
import {ActivityIndicator,Switch,Text,TextInput,View} from 'react-native';
import {AppButton} from '@/components/app-button';
import {Screen} from '@/components/screen';
import {PostCard} from '@/features/content/post-card';
import {CommentItem,PageResponse,PostItem} from '@/features/content/types';
import {apiRequest} from '@/lib/api/client';
import {useLanguage} from '@/localization/language-provider';
import {useForegroundPoll} from '@/features/live-support/foreground';
import {tokenStorage} from '@/lib/storage/token-storage';
export default function PostDetail(){
 const {id}=useLocalSearchParams<{id:string}>();const {language,t}=useLanguage();const tr=(en:string,fa:string)=>language==='fa'?fa:en;
 const [post,setPost]=useState<PostItem|null>(null);const [comments,setComments]=useState<CommentItem[]>([]);
 const [body,setBody]=useState('');const [reply,setReply]=useState<string|null>(null);const [anonymous,setAnonymous]=useState(false);
 const [busy,setBusy]=useState(false);const [loading,setLoading]=useState(true);const [error,setError]=useState('');const [notice,setNotice]=useState('');
 const [page,setPage]=useState(0);const [more,setMore]=useState(false);
 const refresh=useForegroundPoll(async current=>{try{const [p,c]=await Promise.all([apiRequest<PostItem>(`/api/v1/posts/${id}`),apiRequest<PageResponse<CommentItem>>(`/api/v1/posts/${id}/comments?size=50`)]);if(current()){setPost(p);setComments(c.items);setPage(0);setMore(c.hasNext);setError('');}}catch(e){if(current()){setPost(null);setComments([]);setError(e instanceof Error?e.message:'Unavailable');}}finally{if(current())setLoading(false);}},10000,()=>{setPost(null);setComments([]);});
 const forcedAnonymous=Boolean(post?.anonymous&&post?.owned);
 async function send(){if(busy||!post||!body.trim())return;setBusy(true);setError('');const epoch=tokenStorage.generation();
  try{const c=await apiRequest<CommentItem>(`/api/v1/posts/${id}/comments`,{method:'POST',body:JSON.stringify({body,parentCommentId:reply,anonymous:anonymous||forcedAnonymous})});if(epoch!==tokenStorage.generation())return;
   setNotice(c.status==='PUBLISHED'?tr('Reply published.','پاسخ منتشر شد.'):c.safetyOutcome==='REQUEST_REVISION'?tr('Reply needs changes; not published.','پاسخ نیازمند اصلاح است؛ منتشر نشد.'):tr('Reply not published; review may be required. No response time is promised.','پاسخ منتشر نشد؛ ممکن است نیازمند بررسی باشد. زمان پاسخ تضمین نمی‌شود.'));
   if(c.status==='PUBLISHED'){setBody('');setReply(null);await refresh();}
  }catch(e){if(epoch===tokenStorage.generation())setError(e instanceof Error?e.message:'Unable to send');}finally{setBusy(false);}}
 async function remove(commentId:string){if(busy)return;setBusy(true);try{await apiRequest(`/api/v1/comments/${commentId}`,{method:'DELETE'});setComments(current=>current.filter(c=>c.id!==commentId));await refresh();}catch(e){setError(e instanceof Error?e.message:'Unable to delete');}finally{setBusy(false);}}
 async function like(c:CommentItem){if(busy)return;setBusy(true);try{await apiRequest(`/api/v1/comments/${c.id}/like`,{method:c.liked?'DELETE':'PUT'});await refresh();}catch(e){setError(e instanceof Error?e.message:'Unable to save');}finally{setBusy(false);}}
 async function loadMore(){if(busy)return;setBusy(true);try{const r=await apiRequest<PageResponse<CommentItem>>(`/api/v1/posts/${id}/comments?size=50&page=${page+1}`);setComments(current=>[...current,...r.items.filter(c=>!current.some(old=>old.id===c.id))]);setPage(page+1);setMore(r.hasNext);}catch(e){setPost(null);setComments([]);setError(e instanceof Error?e.message:'Unavailable');}finally{setBusy(false);}}
 const direction={textAlign:language==='fa'?'right' as const:'left' as const,writingDirection:language==='fa'?'rtl' as const:'ltr' as const};
 return <Screen scroll><View style={{gap:16,maxWidth:720,width:'100%',alignSelf:'center'}}><AppButton label={tr('Back','بازگشت')} variant="secondary" onPress={()=>router.back()}/>
 {loading&&<ActivityIndicator/>}{!!error&&<Text accessibilityRole="alert">{error}</Text>}{!!notice&&<Text accessibilityRole="alert">{notice}</Text>}
 {post&&<><PostCard post={post}/><FurtherReading key={post.id} postId={post.id} owned={post.owned??false}/><Text accessibilityRole="header">{t('comments')}</Text>
 {comments.map(c=><View key={c.id} style={{gap:8,padding:16,marginStart:c.parentCommentId?18:0,borderWidth:1,borderColor:'#b9d2d0'}}>
 <Text style={direction}>{c.author?.displayName??t('anonymous')}</Text>
 {!!c.parentCommentId&&<Text style={direction}>{tr('In reply to','در پاسخ به')}: {comments.find(parent=>parent.id===c.parentCommentId)?.author?.displayName??tr('Anonymous or unavailable response','پاسخ ناشناس یا خارج از دسترس')}</Text>}
 <Text style={direction}>{c.body}</Text>
 <AppButton label={tr('Reply','پاسخ')} variant="secondary" disabled={busy} onPress={()=>setReply(c.id)}/>
 <AppButton label={(c.liked?tr('Unlike reply','لغو پسندیدن پاسخ'):tr('Like reply','پسندیدن پاسخ'))+' · '+c.likeCount} variant="secondary" disabled={busy} onPress={()=>void like(c)}/>
 {c.owned&&<AppButton label={tr('Delete my reply','حذف پاسخ من')} variant="secondary" disabled={busy} onPress={()=>void remove(c.id)}/>}
 <AppButton label={tr('Report reply','گزارش پاسخ')} variant="secondary" onPress={()=>router.push({pathname:'/tools/[tool]',params:{tool:'report',targetType:'COMMENT',targetId:c.id}} as never)}/>
 </View>)}
 {!comments.length&&<Text>{tr('No permitted replies yet. Responding is voluntary.','هنوز پاسخی برای نمایش نیست. پاسخ‌دادن اختیاری است.')}</Text>}
 {more&&<AppButton label={tr('Load more replies','پاسخ‌های بیشتر')} variant="secondary" disabled={busy} onPress={()=>void loadMore()}/>}
 {reply&&<><Text>{tr('Replying to a response','در حال پاسخ‌دادن به یک پاسخ')}</Text><AppButton label={tr('Cancel reply context','لغو پاسخ به نظر')} variant="secondary" onPress={()=>setReply(null)}/></>}
 <Text>{tr('Reply anonymously','پاسخ ناشناس')}</Text><Switch accessibilityLabel={tr('Reply anonymously','پاسخ ناشناس')} disabled={forcedAnonymous} value={anonymous||forcedAnonymous} onValueChange={setAnonymous}/>
 {forcedAnonymous&&<Text>{tr('Your anonymous post identity is preserved in your reply.','هویت ناشناس پست شما در پاسخ خودتان حفظ می‌شود.')}</Text>}
 <TextInput accessibilityLabel={tr('Reply text','متن پاسخ')} multiline maxLength={5000} value={body} onChangeText={setBody} placeholder={t('writeComment')} style={[{minHeight:110,borderWidth:1,borderColor:'#b9d2d0',padding:12},direction]}/>
 <AppButton label={tr('Send reply','ارسال پاسخ')} disabled={busy||!body.trim()} onPress={()=>void send()}/>
 </>}
 </View></Screen>;
}
