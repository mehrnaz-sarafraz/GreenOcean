import {test} from 'node:test';import assert from 'node:assert/strict';
import {needsOnboarding,resumeStep,supportDestination} from '../src/features/platform/onboarding-policy.ts';
test('Happy GH: all support styles resolve to actual allowlisted destinations',()=>{
 for(const [style,path] of Object.entries({SHARED_EXPERIENCES:'/(app)',SUPPORT_GROUPS:'/(app)/communities',LISTENER:'/(app)/support',PROFESSIONAL_KNOWLEDGE:'/(app)/articles'}))assert.equal(supportDestination(style,{COMMUNITY:true}),path);
});
test('Confused GH: skip and legacy unknown dismiss without claiming completion',()=>{
 for(const status of ['SKIPPED','COMPLETED','LEGACY_UNKNOWN'])assert.equal(needsOnboarding({onboardingStatus:status,onboardingComplete:false}),false);
 for(const status of ['NOT_STARTED','IN_PROGRESS'])assert.equal(needsOnboarding({onboardingStatus:status,onboardingComplete:true}),true);
 assert.equal(needsOnboarding({onboardingComplete:true}),false);
});
test('Creative GH: durable confirmed progress resumes within three stages',()=>{assert.equal(resumeStep(0),1);assert.equal(resumeStep(1),2);assert.equal(resumeStep(2),3);assert.equal(resumeStep(3),3);assert.equal(resumeStep(-50),1);});
test('Malicious GH: unknown styles and URL injection cannot become redirects or elevated workflows',()=>{
 for(const value of [null,'','PROFESSIONAL','ADMIN','https://hostile.example','//hostile.example','/(app)/admin'])assert.equal(supportDestination(value,{COMMUNITY:true}),'/(app)/support');
 assert.equal(supportDestination('LISTENER',{COMMUNITY:false}),'/(auth)/activate');
});
