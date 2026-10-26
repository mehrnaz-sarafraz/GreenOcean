export async function installApi(page, options={}) {
  const state={signedIn:false,registrations:[],registrationFailures:options.registrationFailures??0,countryFailures:options.countryFailures??0};
  await page.route('**/api/v1/**',async route=>{
    const req=route.request(),path=new URL(req.url()).pathname;
    const headers={'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true','Access-Control-Allow-Headers':'Authorization,Content-Type,Accept,X-GreenOcean-CSRF','Access-Control-Allow-Methods':'GET,POST,PATCH,DELETE,OPTIONS'};
    const reply=(body,status=200)=>route.fulfill({status,contentType:'application/json',headers,body:JSON.stringify(body)});
    if(req.method()==='OPTIONS')return route.fulfill({status:204,headers});
    if(path==='/api/v1/geography/countries'){
      if(state.countryFailures-->0)return route.abort('internetdisconnected');
      return reply([{code:'IR',name:'Iran',local_names:{fa:'ایران',ar:'إيران'}},{code:'US',name:'United States',local_names:{fa:'ایالات متحده',ar:'الولايات المتحدة'}}]);
    }
    if(path==='/api/v1/policies/terms')return reply({key:'terms',version:1,title:'Test Terms',content:'Synthetic QA Terms only',summary:'Test terms',publishedAt:'2026-01-01T00:00:00Z'});
    if(path==='/api/v1/public/resources')return reply({items:[{id:'qa-grounding',title:'Public grounding practice',body:'A synthetic all-age resource.'}],boundaries:['Listeners offer non-clinical support.','GreenOcean is not an emergency service.']});
    if(path==='/api/v1/auth/register'){
      state.registrations.push(req.postDataJSON());
      if(state.registrationFailures-->0)return route.abort('internetdisconnected');
      return reply({userId:'00000000-0000-0000-0000-000000000001',email:'synthetic-adult@example.test',username:'member_synthetic'},201);
    }
    if(path==='/api/v1/account-assistance/email-verification/confirm')return req.postDataJSON().code==='123456'?reply({message:'Email address verified.'}):reply({message:'Verification code is invalid or expired'},400);
    if(path==='/api/v1/account-assistance/email-verification')return reply({message:'If the account exists, instructions have been queued.'},202);
    if(path==='/api/v1/auth/web/login'){state.signedIn=true;return reply({accessToken:'synthetic-access',refreshToken:'cookie',tokenType:'Bearer',expiresIn:900});}
    if(path==='/api/v1/auth/web/refresh')return state.signedIn?reply({accessToken:'synthetic-access',refreshToken:'cookie',tokenType:'Bearer',expiresIn:900}):reply({message:'No session'},401);
    if(path==='/api/v1/auth/web/logout'){state.signedIn=false;return route.fulfill({status:204,headers});}
    if(path==='/api/v1/auth/me')return state.signedIn&&req.headers().authorization==='Bearer synthetic-access'?reply({userId:'00000000-0000-0000-0000-000000000001',email:'synthetic-adult@example.test',roles:['USER']}):reply({message:'No session'},401);
    if(path==='/api/v1/identity')return reply({status:'PENDING',email_verified:false,eligibility:{},services:{COMMUNITY:false,LISTENER:false,PROFESSIONAL_CARE:false}});
    if(path==='/api/v1/posts/feed'||path==='/api/v1/communities'||path==='/api/v1/notifications')return reply({items:[],page:0,size:20,hasNext:false});
    if(path==='/api/v1/profiles/me')return reply({userId:'00000000-0000-0000-0000-000000000001',username:'member_synthetic',displayName:'New member',profilePrivate:true});
    if(path==='/api/v1/preferences/me')return reply({onboardingComplete:false,supportTopics:[],strongerContentControls:true,blurSensitiveContent:true,allowMessageRequests:false});
    return reply([]);
  });
  return state;
}
