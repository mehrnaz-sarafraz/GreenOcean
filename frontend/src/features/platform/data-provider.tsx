import { createContext, PropsWithChildren, useCallback, useContext, useEffect, useMemo, useState } from 'react';

import { useAuth } from '@/features/auth/auth-provider';
import { Community } from '@/features/community/types';
import { NotificationItem } from '@/features/notification/types';
import { PageResponse, PostItem, SupportCategory } from '@/features/content/types';
import { OwnProfile } from '@/features/profile/types';
import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';

import { CatalogOption, Conversation, MediaPick, PlatformData, Professional, ProfessionalArticle, SupportChannel, UserPreferences } from './types';

const emptyData: PlatformData = {
  categories: [], posts: [], communities: [], notifications: [], professionals: [], articles: [], media: [],
  conversations: [], channels: [], profile: null, preferences: null, options: [],
};

type DataContextValue = PlatformData & {
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  setPosts: React.Dispatch<React.SetStateAction<PostItem[]>>;
  setCommunities: React.Dispatch<React.SetStateAction<Community[]>>;
  setNotifications: React.Dispatch<React.SetStateAction<NotificationItem[]>>;
  setChannels: React.Dispatch<React.SetStateAction<SupportChannel[]>>;
  setConversations: React.Dispatch<React.SetStateAction<Conversation[]>>;
  setMedia: React.Dispatch<React.SetStateAction<MediaPick[]>>;
  setPreferences: React.Dispatch<React.SetStateAction<UserPreferences | null>>;
};

const DataContext = createContext<DataContextValue | null>(null);

export function DataProvider({ children }: PropsWithChildren) {
  const { status,privacyHidden } = useAuth();
  const [data, setData] = useState<PlatformData>(emptyData);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (status !== 'authenticated'||privacyHidden) return;
    const epoch=tokenStorage.generation();
    setLoading(true); setError(null);
    try {
      const results = await Promise.allSettled([
        apiRequest<SupportCategory[]>('/api/v1/catalog/categories'),
        apiRequest<PageResponse<PostItem>>('/api/v1/posts/feed?size=50').then(result => result.items),
        apiRequest<PageResponse<Community>>('/api/v1/communities?size=50').then(result => result.items),
        apiRequest<PageResponse<NotificationItem>>('/api/v1/notifications?size=50').then(result => result.items),
        apiRequest<Professional[]>('/api/v1/professionals'),
        apiRequest<ProfessionalArticle[]>('/api/v1/articles'),
        apiRequest<MediaPick[]>('/api/v1/media-recommendations'),
        apiRequest<Conversation[]>('/api/v1/conversations'),
        apiRequest<SupportChannel[]>('/api/v1/support-channels'),
        apiRequest<OwnProfile>('/api/v1/profiles/me'),
        apiRequest<UserPreferences>('/api/v1/preferences/me'),
        apiRequest<CatalogOption[]>('/api/v1/catalog/options'),
      ] as const);
      if(epoch!==tokenStorage.generation())return;
      setData(current => ({
        categories: results[0].status === 'fulfilled' ? results[0].value : current.categories,
        posts: results[1].status === 'fulfilled' ? results[1].value : current.posts,
        communities: results[2].status === 'fulfilled' ? results[2].value : current.communities,
        notifications: results[3].status === 'fulfilled' ? results[3].value : current.notifications,
        professionals: results[4].status === 'fulfilled' ? results[4].value : current.professionals,
        articles: results[5].status === 'fulfilled' ? results[5].value : current.articles,
        media: results[6].status === 'fulfilled' ? results[6].value : current.media,
        conversations: results[7].status === 'fulfilled' ? results[7].value : current.conversations,
        channels: results[8].status === 'fulfilled' ? results[8].value : current.channels,
        profile: results[9].status === 'fulfilled' ? results[9].value : current.profile,
        preferences: results[10].status === 'fulfilled' ? results[10].value : current.preferences,
        options: results[11].status === 'fulfilled' ? results[11].value : current.options,
      }));
      const failed = results.find(result => result.status === 'rejected');
      if (failed?.status === 'rejected') setError(failed.reason instanceof Error ? failed.reason.message : 'Some GreenOcean data could not be refreshed');
    } catch (caught) {
      if(epoch!==tokenStorage.generation())return;
      setError(caught instanceof Error ? caught.message : 'Could not load GreenOcean data');
    } finally {
      if(epoch===tokenStorage.generation())setLoading(false);
    }
  }, [status,privacyHidden]);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (status === 'authenticated') void refresh();
      if (status === 'unauthenticated') setData(emptyData);
    }, 0);
    return () => clearTimeout(timer);
  }, [refresh, status]);

  useEffect(() => {
    if (status !== 'authenticated'||privacyHidden) return;
    const epoch=tokenStorage.generation();
    const interval = setInterval(() => {
      void Promise.allSettled([
        apiRequest<PageResponse<NotificationItem>>('/api/v1/notifications?size=50').then(result => result.items),
        apiRequest<Conversation[]>('/api/v1/conversations'),
      ]).then(([notificationResult, conversationResult]) => {if(epoch!==tokenStorage.generation())return;setData(current => ({
        ...current,
        notifications: notificationResult.status === 'fulfilled' ? notificationResult.value : current.notifications,
        conversations: conversationResult.status === 'fulfilled' ? conversationResult.value : current.conversations,
      }));});
    }, 15000);
    return () => clearInterval(interval);
  }, [status,privacyHidden]);

  const value = useMemo<DataContextValue>(() => ({
    ...data, loading, error, refresh,
    setPosts: update => setData(current => ({ ...current, posts: typeof update === 'function' ? update(current.posts) : update })),
    setCommunities: update => setData(current => ({ ...current, communities: typeof update === 'function' ? update(current.communities) : update })),
    setNotifications: update => setData(current => ({ ...current, notifications: typeof update === 'function' ? update(current.notifications) : update })),
    setChannels: update => setData(current => ({ ...current, channels: typeof update === 'function' ? update(current.channels) : update })),
    setConversations: update => setData(current => ({ ...current, conversations: typeof update === 'function' ? update(current.conversations) : update })),
    setMedia: update => setData(current => ({ ...current, media: typeof update === 'function' ? update(current.media) : update })),
    setPreferences: update => setData(current => ({ ...current, preferences: typeof update === 'function' ? update(current.preferences) : update })),
  }), [data, error, loading, refresh]);

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
}

export function usePlatformData() {
  const context = useContext(DataContext);
  if (!context) throw new Error('usePlatformData must be used inside DataProvider');
  return context;
}
