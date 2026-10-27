/** Serialize local credential operations; a failed operation never poisons the queue. */
export function createSerialQueue(){
 let tail:Promise<void>=Promise.resolve();
 return <T>(operation:()=>Promise<T>|T):Promise<T>=>{
  const result=tail.then(operation);
  tail=result.then(()=>undefined,()=>undefined);
  return result;
 };
}
