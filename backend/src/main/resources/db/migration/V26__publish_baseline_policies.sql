UPDATE policy_documents
   SET content = 'Speak from personal experience rather than diagnosing or prescribing for others.\n\nProtect identities and never publish contact details, addresses, workplaces, or identifying medical documents.\n\nUse clear content notes for grief, violence, abuse, self-harm, eating disorders, or frightening detail.\n\nReport unsafe conduct instead of retaliating. Harassment, hate, exploitation, impersonation, doxxing, and repeated unwanted contact are prohibited.'
 WHERE policy_key = 'community-guidelines' AND version = 1 AND content IS NULL;

INSERT INTO policy_documents (policy_key, version, title, summary, content, status, published_at)
VALUES
('privacy', 1, 'Privacy policy', 'How GreenOcean handles account, community, wellbeing, and safety data.',
 'GreenOcean uses account details, preferences, content you choose to share, safety actions, and the product activity needed to provide the service.\n\nMental-health content is treated as sensitive and is not sold for advertising or used to alter a professional trust score.\n\nYou can manage visibility, download your data, remove content, block accounts, and permanently delete your account. Access to reported material is limited to authorized safety operations.',
 'PUBLISHED', CURRENT_TIMESTAMP),
('terms', 1, 'Terms of use', 'The core agreement for using GreenOcean safely and respectfully.',
 'GreenOcean provides peer support and educational information, not medical or emergency care. Members must not diagnose others or direct treatment changes.\n\nHarassment, hate, exploitation, doxxing, impersonation, and repeated unwanted contact are prohibited. Professionals must remain within verified scope and disclose sponsored placement.\n\nContent may be labelled, limited, removed, or escalated under the moderation process. Eligible decisions can be appealed.',
 'PUBLISHED', CURRENT_TIMESTAMP),
('cookies', 1, 'Cookie and local storage choices', 'Essential storage keeps the service secure and available.',
 'GreenOcean uses essential session, security, routing, accessibility, and preference storage for core operation.\n\nOptional analytics must be privacy-minimized, documented, and disabled until consent is recorded where applicable. You can remove optional browser storage through your device or browser controls.',
 'PUBLISHED', CURRENT_TIMESTAMP)
ON CONFLICT (policy_key, version) DO NOTHING;
