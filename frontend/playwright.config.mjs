import { defineConfig } from '@playwright/test';
export default defineConfig({
  testDir: './e2e', timeout: 45000, fullyParallel: false, workers: 1, retries: 0,
  reporter: [['list'], ['html', { open: 'never' }], ['junit', { outputFile: 'test-results/browser-junit.xml' }]],
  use: { baseURL: 'http://127.0.0.1:8081', browserName: 'chromium', trace: 'retain-on-failure' },
  webServer: { command: 'npx expo start --web --port 8081', url: 'http://127.0.0.1:8081', timeout: 180000, reuseExistingServer: false, env: { CI: '1', BROWSER: 'none' } },
});
