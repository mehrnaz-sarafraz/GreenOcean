# GreenOcean Frontend

Production-oriented Expo SDK 57 / React Native / TypeScript client for GreenOcean.
The current product language is English and the interface is LTR. Persian/RTL is intentionally deferred.

## Run locally

Node.js and npm are required. Start the backend first, then run:

```powershell
cd D:\p\GreenOcean\frontend
npm install
npm start
```

Press `w` for Web or `a` for an Android emulator. Direct commands:

```powershell
npm run web
npm run android
npm run typecheck
npm run lint
```

The app does not use Vite, so `npm run dev` is not a project command.

Default API URLs:

- Android emulator: `http://10.0.2.2:8080`
- iOS simulator and Web: `http://localhost:8080`
- Physical phone: copy `.env.example` to `.env` and set `EXPO_PUBLIC_API_URL` to the computer's LAN address.

Tokens use Expo SecureStore on native platforms. Web uses local storage.

## Implemented product areas

- Welcome, public urgent help, product information, registration intent, durable onboarding, login, session restore, refresh, logout, password change, session management, account archive, and account deletion
- Home feed, post categories, post types, anonymous sharing, visibility controls, comments, replies, likes, bookmarks, professional answers, and content warnings
- Search across people and posts, professional discovery, knowledge articles, psychology-related media, saved library, and search history
- Public/private profiles, follow requests, followers/following, blocks, privacy controls, notification preferences, and data export
- Public/private communities with join approval, member roles, rules, events, attendance, and community feeds
- Private conversations, support channels, listener matching, listener application/training/profile, and polling-based updates
- Verified professional profiles, reviews, credential application/document upload, availability publishing, appointment requests, and consultation management
- Wellbeing check-ins, gentle goals, private journal, non-diagnostic reflection, guided growth paths, and reflection companion
- Reporting, appeals, urgent-help routing, support requests, safety controls, and public policies
- Role-gated admin dashboard for reports, verification, reviews, users, communities, listener applications, campaigns, broadcasts, exports, policies, documents, audit activity, and support queues

All runtime product records come from the API. Search history and onboarding are account-synchronized; configurable option lists and listener training come from PostgreSQL. The retired mock-data and preview-mode files are no longer part of the runtime. Authentication tokens remain in secure device storage by design and are not product content.

## Verification

```powershell
npm run typecheck
npm run lint
npx expo install --check
npx expo export --platform web
```

See [../IMPLEMENTATION_STATUS.md](../IMPLEMENTATION_STATUS.md) for the complete route map, coverage report, and production integration checklist.
