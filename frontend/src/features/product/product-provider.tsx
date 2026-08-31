import AsyncStorage from '@react-native-async-storage/async-storage';
import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';

export type LocalPost = { id: string; body: string; categoryId: string; group: 'EMOTION' | 'CONDITION' | 'LIFE_EXPERIENCE'; postType: 'EXPERIENCE' | 'QUESTION' | 'REFLECTION'; mood: string; anonymous: boolean; audience: 'PUBLIC' | 'COMMUNITY'; communitySlug?: string; contentWarning?: string; createdAt: string };
export type LocalComment = { id: string; postId: string; body: string; createdAt: string; parentId?: string };
export type LocalReport = { id: string; targetType: string; targetId: string; reason: string; details: string; status: 'SUBMITTED' | 'REVIEWING' | 'RESOLVED'; createdAt: string; outcome?: string };
export type LocalBooking = { id: string; professionalId: string; date: string; time: string; mode: string; note: string; status: 'REQUESTED' | 'CONFIRMED' | 'CANCELLED' };
export type LocalReview = { id: string; professionalId: string; rating: number; body: string; createdAt: string };
export type Goal = { id: string; title: string; cadence: string; progress: number; completed: boolean };
export type JournalEntry = { id: string; title: string; body: string; mood: string; createdAt: string };
export type CheckIn = { id: string; mood: string; note: string; createdAt: string };
export type LocalCommunity = { id: string; slug: string; name: string; description: string; privateCommunity: boolean; createdAt: string };
export type SubmittedArticle = { id: string; title: string; summary: string; body: string; references: string; topic: string; pinned: boolean; status: 'IN_REVIEW' | 'PUBLISHED'; createdAt: string };

export type ProductState = {
  onboarding: { complete: boolean; topics: string[]; supportStyle: string; strongSafety: boolean; role: 'MEMBER' | 'PROFESSIONAL' };
  profile: { displayName: string; username: string; bio: string; city: string; country: string; languages: string[]; privateProfile: boolean };
  followingUsernames: string[]; followedProfessionals: string[]; joinedCommunities: string[]; joinedChannels: string[]; savedPosts: string[]; savedArticles: string[]; savedMedia: string[]; blockedAccounts: string[]; mutedWords: string[];
  safety: { blurSensitive: boolean; reduceMedication: boolean; messageRequests: boolean; professionalsOnly: boolean };
  notifications: { messages: boolean; replies: boolean; follows: boolean; groups: boolean; articles: boolean; quietHours: boolean };
  posts: LocalPost[]; comments: LocalComment[]; reports: LocalReport[]; bookings: LocalBooking[]; reviews: LocalReview[]; goals: Goal[]; journal: JournalEntry[]; checkIns: CheckIn[]; completedPaths: string[]; localCommunities: LocalCommunity[]; submittedArticles: SubmittedArticle[]; searchHistory: string[];
  postDraft: Partial<LocalPost> | null;
  articleDraft: { title: string; summary: string; body: string; references: string; topic: string; pinned: boolean } | null;
};

const initialState: ProductState = {
  onboarding: { complete: false, topics: [], supportStyle: '', strongSafety: true, role: 'MEMBER' },
  profile: { displayName: 'Taylor Morgan', username: 'taylor.m', bio: 'Learning to meet myself with a little more patience, one day at a time.', city: 'Portland', country: 'United States', languages: ['English', 'Spanish'], privateProfile: false },
  followingUsernames: ['jamie.grows', 'alex.notes'], followedProfessionals: ['pro-1'], joinedCommunities: ['better-sleep', 'healthy-relationships'], joinedChannels: ['ch1', 'ch2', 'ch5'], savedPosts: [], savedArticles: [], savedMedia: [], blockedAccounts: ['harmful.advice', 'repeat.sender', 'unsafe.promoter'], mutedWords: ['self-harm methods', 'graphic violence'],
  safety: { blurSensitive: true, reduceMedication: false, messageRequests: true, professionalsOnly: false },
  notifications: { messages: true, replies: true, follows: true, groups: true, articles: true, quietHours: false },
  posts: [], comments: [], reports: [], bookings: [], reviews: [], localCommunities: [], submittedArticles: [], searchHistory: [],
  goals: [{ id: 'goal-grounding', title: 'Practice one grounding pause', cadence: 'Daily', progress: 4, completed: false }], journal: [], checkIns: [], completedPaths: [], postDraft: null, articleDraft: null,
};

type ListKey = 'followingUsernames' | 'followedProfessionals' | 'joinedCommunities' | 'joinedChannels' | 'savedPosts' | 'savedArticles' | 'savedMedia' | 'blockedAccounts' | 'completedPaths';
type ProductContextValue = {
  state: ProductState; hydrated: boolean; update: (recipe: (current: ProductState) => ProductState) => void;
  patch: <K extends keyof ProductState>(key: K, value: ProductState[K]) => void; toggleList: (key: ListKey, value: string) => void;
  addPost: (post: Omit<LocalPost, 'id' | 'createdAt'>) => string; addComment: (postId: string, body: string, parentId?: string) => void; addReport: (targetType: string, targetId: string, reason: string, details: string) => void;
  addBooking: (booking: Omit<LocalBooking, 'id' | 'status'>) => void; addReview: (review: Omit<LocalReview, 'id' | 'createdAt'>) => void; resetDemo: () => void;
};

const ProductContext = createContext<ProductContextValue | null>(null);
const STORAGE_KEY = 'greenocean.product.v2';

export function ProductProvider({ children }: PropsWithChildren) {
  const [state, setState] = useState(initialState); const [hydrated, setHydrated] = useState(false);
  useEffect(() => { AsyncStorage.getItem(STORAGE_KEY).then(value => { if (value) { const stored = JSON.parse(value) as Partial<ProductState>; setState(current => ({ ...current, ...stored, profile: { ...current.profile, ...stored.profile }, onboarding: { ...current.onboarding, ...stored.onboarding }, safety: { ...current.safety, ...stored.safety }, notifications: { ...current.notifications, ...stored.notifications } })); } }).catch(() => undefined).finally(() => setHydrated(true)); }, []);
  useEffect(() => { if (hydrated) void AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }, [hydrated, state]);
  const value = useMemo<ProductContextValue>(() => ({
    state, hydrated, update: setState, patch: (key, nextValue) => setState(current => ({ ...current, [key]: nextValue })),
    toggleList: (key, item) => setState(current => ({ ...current, [key]: current[key].includes(item) ? current[key].filter(value => value !== item) : [...current[key], item] })),
    addPost: post => { const id = `local-${Date.now()}`; setState(current => ({ ...current, posts: [{ ...post, id, createdAt: new Date().toISOString() }, ...current.posts], postDraft: null })); return id; },
    addComment: (postId, body, parentId) => setState(current => ({ ...current, comments: [...current.comments, { id: `comment-${Date.now()}`, postId, body, parentId, createdAt: new Date().toISOString() }] })),
    addReport: (targetType, targetId, reason, details) => setState(current => ({ ...current, reports: [{ id: `GO-${Date.now().toString().slice(-6)}`, targetType, targetId, reason, details, status: 'SUBMITTED', createdAt: new Date().toISOString() }, ...current.reports] })),
    addBooking: booking => setState(current => ({ ...current, bookings: [{ ...booking, id: `booking-${Date.now()}`, status: 'REQUESTED' }, ...current.bookings] })),
    addReview: review => setState(current => ({ ...current, reviews: [{ ...review, id: `review-${Date.now()}`, createdAt: new Date().toISOString() }, ...current.reviews] })),
    resetDemo: () => setState(initialState),
  }), [hydrated, state]);
  return <ProductContext.Provider value={value}>{children}</ProductContext.Provider>;
}

export function useProduct() { const context = useContext(ProductContext); if (!context) throw new Error('useProduct must be used inside ProductProvider'); return context; }
