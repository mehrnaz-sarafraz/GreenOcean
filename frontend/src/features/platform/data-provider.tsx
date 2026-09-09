import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';

import { useAuth } from '@/features/auth/auth-provider';
import { Community } from '@/features/community/types';
import {
  PageResponse,
  PostItem,
  SupportCategory,
} from '@/features/content/types';
import { NotificationItem } from '@/features/notification/types';
import { OwnProfile } from '@/features/profile/types';
import { apiRequest } from '@/lib/api/client';
import { queryClient } from '@/lib/query/query-client';

import {
  Conversation,
  MediaPick,
  PlatformData,
  Professional,
  ProfessionalArticle,
  ProfileStats,
  SupportAvailability,
  SupportChannel,
  UserPreferences,
} from './types';

const emptyData: PlatformData = {
  categories: [],
  posts: [],
  communities: [],
  notifications: [],
  professionals: [],
  articles: [],
  media: [],
  conversations: [],
  channels: [],
  profile: null,
  preferences: null,
  supportAvailability: null,
  profileStats: null,
};

type DataContextValue = PlatformData & {
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  loadMorePosts: () => Promise<void>;
  loadMoreCommunities: () => Promise<void>;
  loadMoreNotifications: () => Promise<void>;
  hasMore: { posts: boolean; communities: boolean; notifications: boolean };

  setPosts: React.Dispatch<React.SetStateAction<PostItem[]>>;
  setCommunities: React.Dispatch<React.SetStateAction<Community[]>>;
  setNotifications: React.Dispatch<
    React.SetStateAction<NotificationItem[]>
  >;
  setChannels: React.Dispatch<
    React.SetStateAction<SupportChannel[]>
  >;
  setConversations: React.Dispatch<
    React.SetStateAction<Conversation[]>
  >;
  setMedia: React.Dispatch<
    React.SetStateAction<MediaPick[]>
  >;
  setProfessionals: React.Dispatch<
    React.SetStateAction<Professional[]>
  >;
  setArticles: React.Dispatch<
    React.SetStateAction<ProfessionalArticle[]>
  >;
  setProfile: React.Dispatch<
    React.SetStateAction<OwnProfile | null>
  >;
  setPreferences: React.Dispatch<
    React.SetStateAction<UserPreferences | null>
  >;
};

const DataContext = createContext<DataContextValue | null>(null);

export function DataProvider({
  children,
}: PropsWithChildren) {
  const { status, user } = useAuth();

  const userId = user?.userId ?? null;

  const [data, setData] = useState<PlatformData>(emptyData);
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pages, setPages] = useState({ posts: 0, communities: 0, notifications: 0 });
  const [hasMore, setHasMore] = useState({ posts: false, communities: false, notifications: false });

  const fetchData = useCallback(async (): Promise<PlatformData> => {
    const query = <T,>(key: string, path: string) =>
      queryClient.fetchQuery(`${userId}:${key}`, () => apiRequest<T>(path));
    const [
      categories,
      postPage,
      communityPage,
      notificationPage,
      professionals,
      articles,
      media,
      conversations,
      channels,
      profile,
      preferences,
      supportAvailability,
      profileStats,
    ] = await Promise.all([
      query<SupportCategory[]>('categories',
        '/api/v1/catalog/categories',
      ),

      query<PageResponse<PostItem>>('posts:0', '/api/v1/posts/feed?page=0&size=20'),

      query<PageResponse<Community>>('communities:0', '/api/v1/communities?page=0&size=20'),

      query<PageResponse<NotificationItem>>('notifications:0', '/api/v1/notifications?page=0&size=20'),

      apiRequest<Professional[]>(
        '/api/v1/professionals',
      ),

      apiRequest<ProfessionalArticle[]>(
        '/api/v1/articles',
      ),

      apiRequest<MediaPick[]>(
        '/api/v1/media-recommendations',
      ),

      apiRequest<PageResponse<Conversation>>('/api/v1/conversations?page=0&size=20').then(result => result.items),

      apiRequest<SupportChannel[]>(
        '/api/v1/support-channels',
      ),

      apiRequest<OwnProfile>(
        '/api/v1/profiles/me',
      ),

      apiRequest<UserPreferences>(
        '/api/v1/preferences/me',
      ),

      apiRequest<SupportAvailability>(
        '/api/v1/support/availability',
      ),

      apiRequest<ProfileStats>(
        '/api/v1/profiles/me/stats',
      ),
    ]);

    setPages({ posts: 0, communities: 0, notifications: 0 });
    setHasMore({ posts: postPage.hasNext, communities: communityPage.hasNext, notifications: notificationPage.hasNext });
    return {
      categories,
      posts: postPage.items,
      communities: communityPage.items,
      notifications: notificationPage.items,
      professionals,
      articles,
      media,
      conversations,
      channels,
      profile,
      preferences,
      supportAvailability,
      profileStats,
    };
  }, [userId]);

  const refresh = useCallback(async () => {
    if (
      status !== 'authenticated' ||
      !userId
    ) {
      return;
    }

    setRefreshing(true);
    setError(null);

    try {
      queryClient.invalidate(`${userId}:`);
      const result = await fetchData();

      setData(result);
      setLoadedUserId(userId);
    } catch (caught) {
      setError(
        caught instanceof Error
          ? caught.message
          : 'Could not load GreenOcean data',
      );
    } finally {
      setRefreshing(false);
    }
  }, [fetchData, status, userId]);

  const loadMore = useCallback(async <T,>(
    resource: 'posts' | 'communities' | 'notifications',
    path: string,
    update: (items: T[]) => void,
  ) => {
    if (!userId || refreshing || !hasMore[resource]) return;
    const nextPage = pages[resource] + 1;
    try {
      const result = await queryClient.fetchQuery<PageResponse<T>>(
        `${userId}:${resource}:${nextPage}`,
        () => apiRequest<PageResponse<T>>(`${path}?page=${nextPage}&size=20`),
      );
      update(result.items);
      setPages(current => ({ ...current, [resource]: nextPage }));
      setHasMore(current => ({ ...current, [resource]: result.hasNext }));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not load more items');
    }
  }, [hasMore, pages, refreshing, userId]);

  const loadMorePosts = useCallback(() => loadMore<PostItem>('posts', '/api/v1/posts/feed', items =>
    setData(current => ({ ...current, posts: [...current.posts, ...items] }))), [loadMore]);
  const loadMoreCommunities = useCallback(() => loadMore<Community>('communities', '/api/v1/communities', items =>
    setData(current => ({ ...current, communities: [...current.communities, ...items] }))), [loadMore]);
  const loadMoreNotifications = useCallback(() => loadMore<NotificationItem>('notifications', '/api/v1/notifications', items =>
    setData(current => ({ ...current, notifications: [...current.notifications, ...items] }))), [loadMore]);

  useEffect(() => {
    if (
      status !== 'authenticated' ||
      !userId
    ) {
      return;
    }

    let cancelled = false;

    fetchData()
      .then(result => {
        if (cancelled) {
          return;
        }

        setData(result);
        setLoadedUserId(userId);
        setError(null);
      })
      .catch(caught => {
        if (cancelled) {
          return;
        }

        setLoadedUserId(userId);

        setError(
          caught instanceof Error
            ? caught.message
            : 'Could not load GreenOcean data',
        );
      });

    return () => {
      cancelled = true;
    };
  }, [fetchData, status, userId]);

  const sessionData =
    status === 'authenticated' &&
    userId &&
    loadedUserId === userId
      ? data
      : emptyData;

  const initialLoading =
    status === 'authenticated' &&
    !!userId &&
    loadedUserId !== userId &&
    error === null;

  const loading = refreshing || initialLoading;

  const value = useMemo<DataContextValue>(
    () => ({
      ...sessionData,

      loading,
      error,
      refresh,
      loadMorePosts,
      loadMoreCommunities,
      loadMoreNotifications,
      hasMore,

      setPosts: update =>
        setData(current => ({
          ...current,
          posts:
            typeof update === 'function'
              ? update(current.posts)
              : update,
        })),

      setCommunities: update =>
        setData(current => ({
          ...current,
          communities:
            typeof update === 'function'
              ? update(current.communities)
              : update,
        })),

      setNotifications: update =>
        setData(current => ({
          ...current,
          notifications:
            typeof update === 'function'
              ? update(current.notifications)
              : update,
        })),

      setChannels: update =>
        setData(current => ({
          ...current,
          channels:
            typeof update === 'function'
              ? update(current.channels)
              : update,
        })),

      setConversations: update =>
        setData(current => ({
          ...current,
          conversations:
            typeof update === 'function'
              ? update(current.conversations)
              : update,
        })),

      setMedia: update =>
        setData(current => ({
          ...current,
          media:
            typeof update === 'function'
              ? update(current.media)
              : update,
        })),

      setProfessionals: update =>
        setData(current => ({
          ...current,
          professionals:
            typeof update === 'function'
              ? update(current.professionals)
              : update,
        })),

      setArticles: update =>
        setData(current => ({
          ...current,
          articles:
            typeof update === 'function'
              ? update(current.articles)
              : update,
        })),

      setProfile: update =>
        setData(current => ({
          ...current,
          profile:
            typeof update === 'function'
              ? update(current.profile)
              : update,
        })),

      setPreferences: update =>
        setData(current => ({
          ...current,
          preferences:
            typeof update === 'function'
              ? update(current.preferences)
              : update,
        })),
    }),
    [
      error,
      loading,
      refresh,
      sessionData,
      loadMorePosts,
      loadMoreCommunities,
      loadMoreNotifications,
      hasMore,
    ],
  );

  return (
    <DataContext.Provider value={value}>
      {children}
    </DataContext.Provider>
  );
}

export function usePlatformData() {
  const context = useContext(DataContext);

  if (!context) {
    throw new Error(
      'usePlatformData must be used inside DataProvider',
    );
  }

  return context;
}
