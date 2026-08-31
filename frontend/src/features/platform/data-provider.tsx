import { createContext, PropsWithChildren, useCallback, useContext, useEffect, useMemo, useState } from 'react';

import { useAuth } from '@/features/auth/auth-provider';
import { Community } from '@/features/community/types';
import { NotificationItem } from '@/features/notification/types';
import { PageResponse, PostItem, SupportCategory } from '@/features/content/types';
import { OwnProfile } from '@/features/profile/types';
import { apiRequest } from '@/lib/api/client';

import { Conversation, MediaPick, PlatformData, Professional, ProfessionalArticle, SupportChannel, UserPreferences } from './types';

const emptyData: PlatformData = {
  categories: [], posts: [], communities: [], notifications: [], professionals: [], articles: [], media: [],
  conversations: [], channels: [], profile: null, preferences: null,
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
  const { status, user } = useAuth();
  const [data, setData] = useState<PlatformData>(emptyData);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (status !== 'authenticated') return;
    setLoading(true); setError(null);
    try {
      const [categories, posts, communities, notifications, professionals, articles, media, conversations, channels, profile, preferences] = await Promise.all([
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
      ]);
      setData({ categories, posts, communities, notifications, professionals, articles, media, conversations, channels, profile, preferences });
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not load GreenOcean data');
    } finally {
      setLoading(false);
    }
  }, [status, user?.userId]);

  useEffect(() => {
    if (status === 'authenticated') void refresh();
    if (status === 'unauthenticated') setData(emptyData);
  }, [refresh, status]);

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
