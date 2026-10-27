export async function installApi(page, options={}) {
  const state={signedIn:false,emailVerified:false,onboardingComplete:false,termsVersion:1,email:'synthetic-adult@example.test',corrections:[],verificationRequests:0,verificationFailures:options.verificationFailures??0,registrations:[],registrationFailures:options.registrationFailures??0,countryFailures:options.countryFailures??0};
  const registrationGate=options.holdRegistration?new Promise(resolve=>{state.releaseRegistration=resolve;}):null;
  await page.route('**/api/v1/**',async route=>{
    const req=route.request(),path=new URL(req.url()).pathname;
    const headers={'Access-Control-Allow-Origin':'http://127.0.0.1:8081','Access-Control-Allow-Credentials':'true','Access-Control-Allow-Headers':'Authorization,Content-Type,Accept,X-GreenOcean-CSRF','Access-Control-Allow-Methods':'GET,POST,PATCH,DELETE,OPTIONS'};
    const reply=(body,status=200)=>route.fulfill({status,contentType:'application/json',headers,body:JSON.stringify(body)});
    if(req.method()==='OPTIONS')return route.fulfill({status:204,headers});
    if(path==='/api/v1/geography/countries'){
      if(state.countryFailures-->0)return route.abort('internetdisconnected');
      return reply([{code:'IR',name:'Iran',local_names:{fa:'ایران',ar:'إيران'}},{code:'US',name:'United States',local_names:{fa:'ایالات متحده',ar:'الولايات المتحدة'}}]);
    }
    if(path==='/api/v1/policies/terms')return reply({key:'terms',version:state.termsVersion,title:'Test Terms',content:'Synthetic QA Terms only',summary:'Test terms',publishedAt:'2026-01-01T00:00:00Z'});
    if(path==='/api/v1/public/resources')return reply({items:[{id:'qa-grounding',title:'Public grounding practice',body:'A synthetic all-age resource.'}],boundaries:['Listeners offer non-clinical support.','GreenOcean is not an emergency service.']});
    if(path==='/api/v1/auth/register'){
      state.registrations.push(req.postDataJSON());
      if(registrationGate)await registrationGate;
      if(options.registrationDelay)await new Promise(resolve=>setTimeout(resolve,options.registrationDelay));
      if(state.registrationFailures-->0)return route.abort('internetdisconnected');
      if(options.staleTerms&&state.registrations.length===1){state.termsVersion=2;return reply({message:'The Terms have changed.',fieldErrors:[{field:'termsVersion',message:'Review and accept the current Terms version.'}]},400);}
      if(options.registrationStatus)return reply({message:'Synthetic server detail',fieldErrors:options.fieldErrors??[]},options.registrationStatus);
      return reply({message:'If registration can proceed, check your email for verification. You can also sign in or use account recovery.'},201);
    }
    if(path==='/api/v1/account-assistance/email-verification/confirm'){
      if(state.verificationFailures-->0)return route.abort('internetdisconnected');
      if(req.postDataJSON().code!==(options.verificationCode??'123456'))return reply({message:'Verification code is invalid or expired'},400);
      state.emailVerified=true;return reply({message:'Email address verified.'});
    }
    if(path==='/api/v1/account-assistance/email-verification'){state.verificationRequests++;return reply({message:'If the account exists, instructions have been requested.',retryAfterSeconds:options.resendSeconds??0},202);}
    if(path==='/api/v1/account-assistance/pending-email'){
      state.corrections.push(req.postDataJSON());state.email=req.postDataJSON().newEmail;
      return reply({message:'If the correction could be completed, verification was requested for the new address.'},202);
    }
    if(path==='/api/v1/auth/web/login'){state.signedIn=true;return reply({accessToken:'synthetic-access',refreshToken:'cookie',tokenType:'Bearer',expiresIn:900});}
    if(path==='/api/v1/auth/web/refresh')return state.signedIn?reply({accessToken:'synthetic-access',refreshToken:'cookie',tokenType:'Bearer',expiresIn:900}):reply({message:'No session'},401);
    if(path==='/api/v1/auth/web/logout'){state.signedIn=false;return route.fulfill({status:204,headers});}
    if(path==='/api/v1/auth/me')return state.signedIn&&req.headers().authorization==='Bearer synthetic-access'?reply({userId:'00000000-0000-0000-0000-000000000001',email:state.email,roles:['USER']}):reply({message:'No session'},401);
    if(path==='/api/v1/identity')return reply({status:state.emailVerified?'ACTIVE':'PENDING',email_verified:state.emailVerified,eligibility:{},services:{COMMUNITY:!!options.completeActivation&&state.signedIn&&state.emailVerified,LISTENER:false,PROFESSIONAL_CARE:false}});
    if(path==='/api/v1/posts/feed'||path==='/api/v1/communities'||path==='/api/v1/notifications')return reply({items:[],page:0,size:20,hasNext:false});
    if(path==='/api/v1/profiles/me')return reply({userId:'00000000-0000-0000-0000-000000000001',username:'member_synthetic',displayName:'New member',profilePrivate:true});
    if(path==='/api/v1/preferences/me'){
      if(req.method()==='PATCH'){const patch=req.postDataJSON();state.onboardingComplete=patch.onboardingComplete??(['SKIPPED','COMPLETED'].includes(patch.onboardingStatus));}
      return reply({onboardingComplete:state.onboardingComplete,supportTopics:[],strongerContentControls:true,blurSensitiveContent:true,allowMessageRequests:false});
    }
    return reply([]);
  });
  return state;
}
