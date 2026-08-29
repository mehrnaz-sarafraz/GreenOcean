export type Conversation = { id:string; name:string; subtitle:string; lastMessage:string; time:string; unread:number; verified?:boolean; kind:'DIRECT'|'PROFESSIONAL'|'GROUP'; online?:boolean };
export type ChatMessage = { id:string; senderId:string; senderName:string; body:string; time:string; mine:boolean; system?:boolean };
export type SupportChannel = { id:string; name:string; slug:string; description:string; category:string; icon:string; memberCount:number; onlineCount:number; joined:boolean; type:'GROUP'|'ANNOUNCEMENT'; moderated:boolean; nextEvent?:string };
export const conversations:Conversation[]=[
  {id:'maya',name:'Dr. Maya Bennett',subtitle:'Clinical Psychologist',lastMessage:'That sounds like an important thing to notice.',time:'2m',unread:2,verified:true,kind:'PROFESSIONAL',online:true},
  {id:'sleep-circle',name:'Better Sleep Circle',subtitle:'Moderated group chat',lastMessage:'Riley: I am trying the wind-down routine tonight.',time:'18m',unread:5,kind:'GROUP',online:true},
  {id:'jamie',name:'Jamie',subtitle:'Peer connection',lastMessage:'Thank you for checking in. It means a lot.',time:'1h',unread:0,kind:'DIRECT',online:false},
  {id:'relationships',name:'Healthy Relationships',subtitle:'Community channel',lastMessage:'New prompt: What does a healthy boundary feel like?',time:'3h',unread:1,kind:'GROUP',online:true},
];
export const chatMessages:Record<string,ChatMessage[]>={
  maya:[{id:'m1',senderId:'maya',senderName:'Dr. Maya Bennett',body:'Hi Taylor. I read the reflection you shared about boundaries. How has that been sitting with you today?',time:'10:24 AM',mine:false},{id:'m2',senderId:'me',senderName:'Taylor',body:'I feel relieved, but also guilty. I am not used to saying no without explaining everything.',time:'10:27 AM',mine:true},{id:'m3',senderId:'maya',senderName:'Dr. Maya Bennett',body:'That makes sense. Guilt can show up when we begin changing an old pattern, even when the new boundary is healthy.',time:'10:30 AM',mine:false},{id:'m4',senderId:'system',senderName:'GreenOcean',body:'Professional conversations are private, but GreenOcean is not an emergency or crisis service.',time:'',mine:false,system:true}],
  'sleep-circle':[{id:'s1',senderId:'system',senderName:'GreenOcean',body:'Welcome to Better Sleep Circle. Be kind, protect privacy, and avoid giving medical advice.',time:'',mine:false,system:true},{id:'s2',senderId:'riley',senderName:'Riley',body:'Has anyone tried putting their phone outside the bedroom?',time:'8:42 PM',mine:false},{id:'s3',senderId:'jamie',senderName:'Jamie',body:'It helped me, but I had to start with just the last 20 minutes before bed.',time:'8:44 PM',mine:false},{id:'s4',senderId:'me',senderName:'Taylor',body:'Starting with 20 minutes feels much more realistic. I might try that tonight.',time:'8:47 PM',mine:true}],
};
export const channels:SupportChannel[]=[
  {id:'ch1',name:'Anxiety Support Lounge',slug:'anxiety-lounge',description:'A moderated live space to talk through worry and overwhelm.',category:'Anxiety',icon:'air',memberCount:18400,onlineCount:286,joined:true,type:'GROUP',moderated:true,nextEvent:'Guided check-in · Today, 7:00 PM'},
  {id:'ch2',name:'Daily Grounding',slug:'daily-grounding',description:'One small grounding prompt, every morning.',category:'Self-esteem',icon:'self_improvement',memberCount:9200,onlineCount:0,joined:true,type:'ANNOUNCEMENT',moderated:true},
  {id:'ch3',name:'Grief & Remembrance',slug:'grief-remembrance',description:'A gentle group for living with loss and remembering together.',category:'Grief & loss',icon:'spa',memberCount:6400,onlineCount:74,joined:false,type:'GROUP',moderated:true,nextEvent:'Sharing circle · Friday, 6:30 PM'},
  {id:'ch4',name:'Parenting Without Perfection',slug:'parenting-without-perfection',description:'Honest conversations about the emotional side of parenting.',category:'Parenting',icon:'family_restroom',memberCount:5100,onlineCount:91,joined:false,type:'GROUP',moderated:true},
  {id:'ch5',name:'Community Updates',slug:'community-updates',description:'Product news, policy updates, and safety announcements.',category:'GreenOcean',icon:'campaign',memberCount:28700,onlineCount:0,joined:true,type:'ANNOUNCEMENT',moderated:true},
];

export type AdminReport={id:string;targetType:'POST'|'COMMENT'|'MESSAGE'|'PROFILE';reason:string;summary:string;reportedUser:string;reporter:string;severity:'CRITICAL'|'HIGH'|'MEDIUM'|'LOW';status:'OPEN'|'REVIEWING'|'RESOLVED';createdAt:string;category:string;signals:string[]};
export const adminReports:AdminReport[]=[
  {id:'R-1048',targetType:'POST',reason:'Self-harm concern',summary:'Post contains language indicating possible immediate risk and needs a safety review.',reportedUser:'quiet.river',reporter:'Automated safety signal',severity:'CRITICAL',status:'OPEN',createdAt:'4 minutes ago',category:'Depression',signals:['High-risk phrase','First-time account']},
  {id:'R-1047',targetType:'MESSAGE',reason:'Harassment',summary:'Repeated unwanted messages after the recipient asked the sender to stop.',reportedUser:'northwind88',reporter:'Jamie R.',severity:'HIGH',status:'REVIEWING',createdAt:'18 minutes ago',category:'Direct message',signals:['3 user reports','Repeat offender']},
  {id:'R-1046',targetType:'COMMENT',reason:'Medical misinformation',summary:'Comment recommends stopping prescribed medication without professional supervision.',reportedUser:'healing.fast',reporter:'Dr. Maya Bennett',severity:'HIGH',status:'OPEN',createdAt:'34 minutes ago',category:'Anxiety',signals:['Professional report']},
  {id:'R-1045',targetType:'PROFILE',reason:'Impersonating a professional',summary:'Account claims clinical credentials but has not submitted verification documents.',reportedUser:'doctor.wellness',reporter:'2 community members',severity:'MEDIUM',status:'OPEN',createdAt:'1 hour ago',category:'Profile',signals:['Credential claim']},
  {id:'R-1044',targetType:'POST',reason:'Spam / promotion',summary:'Repeated promotion of an unrelated paid service across multiple categories.',reportedUser:'growth.offer',reporter:'Automated spam filter',severity:'LOW',status:'RESOLVED',createdAt:'2 hours ago',category:'Work & burnout',signals:['Duplicate content','Outbound links']},
];
export const verificationQueue=[
  {id:'V-291',name:'Dr. Elena Morris',profession:'Licensed Clinical Social Worker',country:'United States',submitted:'12 min ago',documents:4,status:'Ready for review'},
  {id:'V-290',name:'Dr. Noah Williams',profession:'Clinical Psychologist',country:'Canada',submitted:'48 min ago',documents:5,status:'External check pending'},
  {id:'V-289',name:'Aisha Rahman',profession:'Family Therapist',country:'United Kingdom',submitted:'2 hr ago',documents:3,status:'Needs clarification'},
];
export const adminStats={members:28742,activeToday:6841,postsToday:1248,openReports:38,criticalReports:3,verifiedProfessionals:184,pendingVerifications:12,medianResponse:'11m',resolvedWeek:326};
export const reportTrend=[18,24,21,32,27,41,38];
export const reasonBreakdown=[{name:'Safety concern',value:31,color:'#F27D69'},{name:'Harassment',value:24,color:'#8D7CC3'},{name:'Misinformation',value:19,color:'#F4B860'},{name:'Spam',value:16,color:'#5B9BD5'},{name:'Other',value:10,color:'#86D9C5'}];
export const auditLog=[
  {id:'A1',actor:'Morgan Lee',action:'Removed a reported comment',target:'Comment C-8821',time:'6 min ago'},
  {id:'A2',actor:'Safety automation',action:'Escalated report to critical',target:'Report R-1048',time:'9 min ago'},
  {id:'A3',actor:'Taylor Admin',action:'Approved professional verification',target:'Dr. Amara Cole',time:'25 min ago'},
  {id:'A4',actor:'Jordan Kim',action:'Suspended account for 7 days',target:'northwind88',time:'41 min ago'},
];
