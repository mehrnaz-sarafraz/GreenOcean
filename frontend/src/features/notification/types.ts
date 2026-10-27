import { AuthorSummary } from '@/features/content/types';

export type NotificationType = 'LIKE' | 'COMMENT' | 'REPLY' | 'FOLLOW' | 'MENTION' | 'PROFESSIONAL_REPLY' | 'SYSTEM_NOTICE' | 'FOLLOW_REQUEST' | 'FOLLOW_ACCEPTED' | 'MESSAGE_REQUEST';

export type NotificationItem = {
  id: string;
  type: NotificationType;
  actor?: AuthorSummary | null;
  postId?: string | null;
  commentId?: string | null;
  title?: string | null;
  body?: string | null;
  read: boolean;
  createdAt: string;
};
