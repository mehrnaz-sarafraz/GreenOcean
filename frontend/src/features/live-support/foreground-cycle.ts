// A pending response belongs to the foreground interval in which it started.
// Leaving and returning must not make that earlier response current again.
export function createForegroundCycle(visible:()=>boolean){
 let generation=0,closed=false,running=false;
 return {
  invalidate(){generation++;},
  close(){closed=true;generation++;},
  async run(task:(isCurrent:()=>boolean)=>Promise<unknown>){
   if(closed||running||!visible())return;
   const started=generation;
   const isCurrent=()=>!closed&&started===generation&&visible();
   running=true;
   try{await task(isCurrent);}finally{running=false;}
  },
 };
}
