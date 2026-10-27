import {test} from 'node:test';
import assert from 'node:assert/strict';
import {createForegroundCycle} from '../src/features/live-support/foreground-cycle.ts';

function deferred(){let resolve;const promise=new Promise(done=>{resolve=done;});return {promise,resolve};}

test('I-04: delayed availability cannot renew readiness after backgrounding',async()=>{
 let visible=true,heartbeats=0;
 const cycle=createForegroundCycle(()=>visible),response=deferred();
 const pending=cycle.run(async current=>{await response.promise;if(current())heartbeats++;});
 visible=false;cycle.invalidate();response.resolve();await pending;
 assert.equal(heartbeats,0);
 await cycle.run(async()=>{heartbeats++;});assert.equal(heartbeats,0);
 visible=true;await cycle.run(async current=>{if(current())heartbeats++;});assert.equal(heartbeats,1);
});

test('I-10: returning to foreground cannot revive a response from an earlier visit',async()=>{
 let visible=true,updates=0;
 const cycle=createForegroundCycle(()=>visible),response=deferred();
 const pending=cycle.run(async current=>{await response.promise;if(current())updates++;});
 visible=false;cycle.invalidate();visible=true;response.resolve();await pending;
 assert.equal(updates,0);
 await cycle.run(async current=>{if(current())updates++;});assert.equal(updates,1);
});

test('I-04: leaving a focused screen permanently invalidates its pending poll',async()=>{
 const cycle=createForegroundCycle(()=>true),response=deferred();let writes=0;
 const pending=cycle.run(async current=>{await response.promise;if(current())writes++;});
 cycle.close();response.resolve();await pending;
 await cycle.run(async()=>{writes++;});assert.equal(writes,0);
});

test('I-10: overlapping polls are suppressed and failed polls remain retryable',async()=>{
 const cycle=createForegroundCycle(()=>true),response=deferred();let runs=0;
 const pending=cycle.run(async()=>{runs++;await response.promise;});
 await cycle.run(async()=>{runs++;});assert.equal(runs,1);response.resolve();await pending;
 await assert.rejects(cycle.run(async()=>{throw new Error('offline');}),/offline/);
 await cycle.run(async current=>{assert.equal(current(),true);runs++;});assert.equal(runs,2);
});
