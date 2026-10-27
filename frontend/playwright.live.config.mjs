import { defineConfig } from '@playwright/test';
import base from './playwright.config.mjs';

export default defineConfig({
 ...base,
 testDir:'./live-e2e', outputDir:'./test-results/live', timeout:60000,
 use:{...base.use,baseURL:'http://127.0.0.1:8082'},
 reporter:[['list'],['html',{open:'never',outputFolder:'playwright-live-report'}],['junit',{outputFile:'test-results/live-junit.xml'}]],
 webServer:{...base.webServer,command:'node live-e2e/https-proxy.mjs',url:'http://127.0.0.1:8082',env:{...base.webServer.env,EXPO_PUBLIC_WEB_API_ORIGIN:'same-origin'}},
});
