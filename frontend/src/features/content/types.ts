export type AuthorSummary = { userId: string; username: string; displayName: string; avatarUrl: string | null };
export type PostVisibility = 'PUBLIC' | 'FOLLOWERS' | 'COMMUNITY';
export type SupportCategoryGroup = 'EMOTION' | 'CONDITION' | 'LIFE_EXPERIENCE';
export type SupportCategory = {
  id: string; slug: string; group: SupportCategoryGroup; name: string; description: string;
  icon: string; color: string; softColor: string; postCount: number;
};
export type ProfessionalReply = {
  id: string; professional: { userId: string; username: string; displayName: string; title: string; avatarUrl: string | null };
  body: string; helpfulCount: number; createdAt: string;
};
export type PostItem = {
  id: string; author: AuthorSummary | null; communityId: string | null; body: string; anonymous: boolean;
  visibility: PostVisibility; contentWarning: string | null; likeCount: number; commentCount: number;
  liked: boolean; bookmarked: boolean; category: SupportCategory | null; postType: 'EXPERIENCE' | 'QUESTION' | 'REFLECTION';
  mood: string | null; professionalReply: ProfessionalReply | null; createdAt: string;
};
export type CommentItem = {
  id: string; postId: string; parentCommentId: string | null; author: AuthorSummary | null; body: string;
  anonymous: boolean; likeCount: number; liked: boolean; createdAt: string;
};
export type PageResponse<T> = { items: T[]; page: number; size: number; hasNext: boolean };
