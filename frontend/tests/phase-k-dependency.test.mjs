import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
const {quote}=createRequire(import.meta.url)('shell-quote');
test('K dependency regression: shell-quote rejects line terminators after comment tokens without executing a shell',()=>{
 for(const separator of ['\n','\r','\u2028','\u2029']){
  assert.throws(()=>quote(['echo','synthetic',{comment:'synthetic'},'first'+separator+'second']),TypeError);
 }
});
