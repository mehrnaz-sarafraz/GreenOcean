# GreenOcean Frontend

Expo SDK 57 / React Native / TypeScript client for GreenOcean.

## Run locally

Start the Spring Boot backend first. Then:

```powershell
cd D:\projects\GreenOcean\frontend
npm start
```

Useful direct commands:

```powershell
npm run web
npm run android
npm run typecheck
npm run lint
```

Default API URLs:

- Android Emulator: `http://10.0.2.2:8080`
- iOS Simulator and Web: `http://localhost:8080`
- Physical phone: copy `.env.example` to `.env` and set `EXPO_PUBLIC_API_URL` to the computer's LAN address.

Tokens use Expo SecureStore on native platforms. The Web development fallback uses browser local storage.

## Implemented application flow

- English and Persian localization with RTL-aware layouts
- Welcome, registration, login, session restore, token refresh, and logout
- Home feed with pagination and pull-to-refresh
- Public, followers-only, and anonymous post creation
- Post details, comments, replies, likes, and bookmarks
- User and post search with follow actions
- Community discovery, creation, membership, member-only feeds, and community posting
- Notification inbox for follows, likes, comments, and replies with read state
- Own profile and privacy-aware public profiles

The API remains the source of truth for visibility, block, privacy, and authorization rules.
