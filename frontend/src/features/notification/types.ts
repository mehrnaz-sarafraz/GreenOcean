import { AuthorSummary } from '@/features/content/types';

export type NotificationType = 'LIKE' | 'COMMENT' | 'REPLY' | 'FOLLOW' | 'MENTION' | 'PROFESSIONAL_REPLY';

export type NotificationItem = {
  id: string;
  type: NotificationType;
  actor?: AuthorSummary | null;
  postId?: string | null;
  commentId?: string | null;
  read: boolean;
  createdAt: string;
};
