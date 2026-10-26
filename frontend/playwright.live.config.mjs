import { defineConfig } from '@playwright/test';
import base from './playwright.config.mjs';

export default defineConfig({
 ...base,
 testDir:'./live-e2e', timeout:60000,
 reporter:[['list'],['html',{open:'never',outputFolder:'playwright-live-report'}],['junit',{outputFile:'test-results/live-junit.xml'}]],
 webServer:{...base.webServer,env:{...base.webServer.env,EXPO_PUBLIC_API_URL:'http://127.0.0.1:8080'}},
});
