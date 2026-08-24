export type AuthorSummary = { userId: string; username: string; displayName: string; avatarUrl: string | null };
export type PostVisibility = 'PUBLIC' | 'FOLLOWERS' | 'COMMUNITY';
export type PostItem = {
  id: string; author: AuthorSummary | null; communityId: string | null; body: string; anonymous: boolean;
  visibility: PostVisibility; contentWarning: string | null; likeCount: number; commentCount: number;
  liked: boolean; bookmarked: boolean; createdAt: string;
};
export type CommentItem = {
  id: string; postId: string; parentCommentId: string | null; author: AuthorSummary | null; body: string;
  anonymous: boolean; likeCount: number; liked: boolean; createdAt: string;
};
export type PageResponse<T> = { items: T[]; page: number; size: number; hasNext: boolean };
