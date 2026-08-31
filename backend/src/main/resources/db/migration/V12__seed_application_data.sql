-- Curated development content stored as normal relational data.
-- Seed author accounts are suspended and cannot be used to sign in.

INSERT INTO support_categories (id, slug, category_group, name, description, icon, color, soft_color, sort_order) VALUES
('01911000-0000-7000-8000-000000000001', 'feeling-anxious', 'EMOTION', 'Feeling anxious', 'Worry, tension, panic, and uncertainty.', 'air', '#4E8CB8', '#EAF4FA', 10),
('01911000-0000-7000-8000-000000000002', 'feeling-low', 'EMOTION', 'Feeling low', 'Sadness, low energy, and difficult days.', 'rainy', '#6B6FAE', '#EEEDFA', 20),
('01911000-0000-7000-8000-000000000003', 'feeling-lonely', 'EMOTION', 'Feeling lonely', 'Isolation, disconnection, and wanting company.', 'person_off', '#8870A8', '#F3EEFA', 30),
('01911000-0000-7000-8000-000000000004', 'feeling-overwhelmed', 'EMOTION', 'Feeling overwhelmed', 'When everything feels like too much at once.', 'waves', '#D56C5C', '#FDEDEA', 40),
('01911000-0000-7000-8000-000000000005', 'anxiety', 'CONDITION', 'Anxiety', 'Experiences with anxiety symptoms and recovery.', 'psychology', '#4E8CB8', '#EAF4FA', 50),
('01911000-0000-7000-8000-000000000006', 'depression', 'CONDITION', 'Depression', 'Living with depression and finding support.', 'cloud', '#6B6FAE', '#EEEDFA', 60),
('01911000-0000-7000-8000-000000000007', 'trauma', 'CONDITION', 'Trauma', 'Trauma-aware stories, safety, and healing.', 'shield', '#D56C5C', '#FDEDEA', 70),
('01911000-0000-7000-8000-000000000008', 'adhd', 'CONDITION', 'ADHD', 'Attention, energy, routines, and self-understanding.', 'neurology', '#2F927D', '#E7F6F1', 80),
('01911000-0000-7000-8000-000000000009', 'ocd', 'CONDITION', 'OCD', 'Obsessions, compulsions, and evidence-based support.', 'repeat', '#8870A8', '#F3EEFA', 90),
('01911000-0000-7000-8000-000000000010', 'bipolar', 'CONDITION', 'Bipolar', 'Mood changes, stability, and shared experience.', 'contrast', '#B77A28', '#FFF4DE', 100),
('01911000-0000-7000-8000-000000000011', 'sleep', 'CONDITION', 'Sleep', 'Insomnia, rest, routines, and recovery.', 'bedtime', '#536AAE', '#EDF0FB', 110),
('01911000-0000-7000-8000-000000000012', 'frightening-experiences', 'LIFE_EXPERIENCE', 'Frightening experiences', 'A safer place to process frightening moments.', 'warning', '#D56C5C', '#FDEDEA', 120),
('01911000-0000-7000-8000-000000000013', 'grief', 'LIFE_EXPERIENCE', 'Grief', 'Loss, remembrance, and learning to carry grief.', 'spa', '#8870A8', '#F3EEFA', 130),
('01911000-0000-7000-8000-000000000014', 'relationships', 'LIFE_EXPERIENCE', 'Relationships', 'Connection, conflict, boundaries, and repair.', 'favorite', '#D56C5C', '#FDEDEA', 140),
('01911000-0000-7000-8000-000000000015', 'work-burnout', 'LIFE_EXPERIENCE', 'Work & burnout', 'Work pressure, exhaustion, and sustainable change.', 'work', '#B77A28', '#FFF4DE', 150),
('01911000-0000-7000-8000-000000000016', 'parenting-family', 'LIFE_EXPERIENCE', 'Parenting & family', 'Family dynamics, caregiving, and communication.', 'family_restroom', '#2F927D', '#E7F6F1', 160),
('01911000-0000-7000-8000-000000000017', 'recovery', 'LIFE_EXPERIENCE', 'Recovery', 'Small steps, setbacks, and reasons for hope.', 'trending_up', '#2F927D', '#E7F6F1', 170);

INSERT INTO users (id, email, password_hash, status, email_verified, created_at, updated_at) VALUES
('01910000-0000-7000-8000-000000000001', 'jamie.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '18 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000002', 'alex.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '14 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000003', 'riley.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '10 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000004', 'maya.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '24 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000005', 'daniel.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '20 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000006', 'sarah.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '16 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000007', 'admin.seed@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '30 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000008', 'amina.verify@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '2 months', CURRENT_TIMESTAMP),
('01910000-0000-7000-8000-000000000009', 'noah.verify@greenocean.invalid', 'SEEDED_ACCOUNT_DISABLED', 'SUSPENDED', TRUE, CURRENT_TIMESTAMP - INTERVAL '1 month', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO profiles (user_id, username, display_name, bio, country_code, city, birth_year, show_location) VALUES
('01910000-0000-7000-8000-000000000001', 'jamie.grows', 'Jamie Rivera', 'Taking recovery one gentle step at a time.', 'US', 'Portland', 1994, TRUE),
('01910000-0000-7000-8000-000000000002', 'alex.notes', 'Alex Morgan', 'Sharing practical reflections about anxiety and work.', 'CA', 'Toronto', 1991, TRUE),
('01910000-0000-7000-8000-000000000003', 'riley.rests', 'Riley Chen', 'Learning that rest can be part of progress.', 'GB', 'London', 1997, TRUE),
('01910000-0000-7000-8000-000000000004', 'dr.maya.bennett', 'Dr. Maya Bennett', 'Clinical psychologist focused on anxiety, trauma, and compassionate behaviour change.', 'US', 'Portland', 1982, TRUE),
('01910000-0000-7000-8000-000000000005', 'dr.daniel.green', 'Dr. Daniel Green', 'Psychiatrist and sleep-medicine educator.', 'CA', 'Vancouver', 1978, TRUE),
('01910000-0000-7000-8000-000000000006', 'dr.sarah.kim', 'Dr. Sarah Kim', 'Family therapist helping people build safer conversations.', 'GB', 'London', 1985, TRUE),
('01910000-0000-7000-8000-000000000007', 'greenocean.admin', 'GreenOcean Safety', 'Trust and safety operations account.', 'US', 'Remote', 1990, FALSE),
('01910000-0000-7000-8000-000000000008', 'amina.rahman', 'Amina Rahman', 'Professional verification applicant.', 'AE', 'Dubai', 1988, TRUE),
('01910000-0000-7000-8000-000000000009', 'noah.williams', 'Noah Williams', 'Professional verification applicant.', 'AU', 'Sydney', 1986, TRUE)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT seeded.user_id, roles.id
FROM (VALUES
    ('01910000-0000-7000-8000-000000000001'::uuid, 'USER'),
    ('01910000-0000-7000-8000-000000000002'::uuid, 'USER'),
    ('01910000-0000-7000-8000-000000000003'::uuid, 'USER'),
    ('01910000-0000-7000-8000-000000000004'::uuid, 'PROFESSIONAL'),
    ('01910000-0000-7000-8000-000000000005'::uuid, 'PROFESSIONAL'),
    ('01910000-0000-7000-8000-000000000006'::uuid, 'PROFESSIONAL'),
    ('01910000-0000-7000-8000-000000000007'::uuid, 'ADMIN'),
    ('01910000-0000-7000-8000-000000000008'::uuid, 'PROFESSIONAL'),
    ('01910000-0000-7000-8000-000000000009'::uuid, 'PROFESSIONAL')
) AS seeded(user_id, role_name)
JOIN roles ON roles.name = seeded.role_name
ON CONFLICT DO NOTHING;

INSERT INTO professional_profiles (
    user_id, professional_type, title, specialization, bio, years_of_experience,
    verification_status, verified_at, specialties, rating, review_count, green_ocean_score,
    languages, gender, country, city, workplace, clinic_name, clinic_address, education,
    license_number, consultation_modes, accepting_new_clients, promoted, promoted_reason
) VALUES
('01910000-0000-7000-8000-000000000004', 'PSYCHOLOGIST', 'Clinical Psychologist', 'Anxiety, Trauma, Self-esteem',
 'Maya works with adults navigating anxiety, trauma, and major life transitions. Her public work focuses on practical, non-judgmental education.', 14,
 'VERIFIED', CURRENT_TIMESTAMP - INTERVAL '12 months', ARRAY['Anxiety','Trauma','Self-esteem'], 4.90, 184, 96,
 ARRAY['English','Spanish'], 'Woman', 'United States', 'Portland', 'Greenway Behavioral Health', 'Bennett Psychology', '1120 River Street, Portland',
 ARRAY['PsyD, Clinical Psychology — Pacific University','BA, Psychology — University of Oregon'], 'OR-PSY-20418', ARRAY['Video','In-person'], TRUE, TRUE, 'Strong match for anxiety and trauma support'),
('01910000-0000-7000-8000-000000000005', 'PSYCHIATRIST', 'Consultant Psychiatrist', 'Sleep, Anxiety, Medication education',
 'Daniel provides careful educational guidance about sleep, anxiety, and medication conversations while keeping clear clinical boundaries.', 18,
 'VERIFIED', CURRENT_TIMESTAMP - INTERVAL '10 months', ARRAY['Sleep','Anxiety','Medication education'], 4.80, 129, 93,
 ARRAY['English','French'], 'Man', 'Canada', 'Vancouver', 'North Shore Wellness Centre', 'Green Sleep Clinic', '455 Harbour Avenue, Vancouver',
 ARRAY['MD — University of British Columbia','Psychiatry Residency — UBC'], 'CPSBC-77821', ARRAY['Video'], FALSE, TRUE, 'Evidence-based sleep education'),
('01910000-0000-7000-8000-000000000006', 'THERAPIST', 'Family & Relationship Therapist', 'Relationships, Family, Grief',
 'Sarah helps individuals and families replace defensive cycles with clearer, safer communication.', 11,
 'VERIFIED', CURRENT_TIMESTAMP - INTERVAL '8 months', ARRAY['Relationships','Family','Grief'], 4.90, 211, 95,
 ARRAY['English','Korean'], 'Woman', 'United Kingdom', 'London', 'Harbour Family Practice', 'Kim Relationship Therapy', '24 Willow Lane, London',
 ARRAY['MSc, Family Therapy — King''s College London','BA, Social Sciences — University of Leeds'], 'UKCP-119804', ARRAY['Video','In-person'], TRUE, FALSE, NULL),
('01910000-0000-7000-8000-000000000008', 'COUNSELOR', 'Licensed Counselor', 'Anxiety, Grief', 'Verification pending.', 8,
 'PENDING', NULL, ARRAY['Anxiety','Grief'], 0, 0, 0, ARRAY['English','Arabic'], 'Woman', 'United Arab Emirates', 'Dubai', NULL, NULL, NULL, '{}', 'PENDING-1001', ARRAY['Video'], FALSE, FALSE, NULL),
('01910000-0000-7000-8000-000000000009', 'PSYCHOLOGIST', 'Clinical Psychologist', 'ADHD, Burnout', 'Verification pending.', 9,
 'PENDING', NULL, ARRAY['ADHD','Burnout'], 0, 0, 0, ARRAY['English'], 'Man', 'Australia', 'Sydney', NULL, NULL, NULL, '{}', 'PENDING-1002', ARRAY['Video'], FALSE, FALSE, NULL)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO professional_verifications (id, professional_user_id, status, submitted_at, profession, country, document_names) VALUES
('0191b000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000008', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Licensed Counselor', 'United Arab Emirates', ARRAY['Government ID','Degree certificate','Professional license']),
('0191b000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000009', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '1 day', 'Clinical Psychologist', 'Australia', ARRAY['Government ID','Degree certificate','Professional license','Certificate of standing'])
ON CONFLICT (id) DO NOTHING;

INSERT INTO communities (id, name, slug, description, is_private, status, created_by, created_at) VALUES
('01912000-0000-7000-8000-000000000001', 'Better Sleep Circle', 'better-sleep', 'A moderated circle for sleep routines, insomnia, and gentle accountability.', FALSE, 'ACTIVE', '01910000-0000-7000-8000-000000000005', CURRENT_TIMESTAMP - INTERVAL '10 months'),
('01912000-0000-7000-8000-000000000002', 'Anxiety Support', 'anxiety-support', 'Shared experiences and practical support for anxious days.', FALSE, 'ACTIVE', '01910000-0000-7000-8000-000000000004', CURRENT_TIMESTAMP - INTERVAL '14 months'),
('01912000-0000-7000-8000-000000000003', 'Relationships & Boundaries', 'relationships-boundaries', 'A respectful place to talk about communication and boundaries.', FALSE, 'ACTIVE', '01910000-0000-7000-8000-000000000006', CURRENT_TIMESTAMP - INTERVAL '8 months'),
('01912000-0000-7000-8000-000000000004', 'Quiet Recovery', 'quiet-recovery', 'A private-by-invitation recovery circle.', TRUE, 'ACTIVE', '01910000-0000-7000-8000-000000000001', CURRENT_TIMESTAMP - INTERVAL '6 months')
ON CONFLICT (id) DO NOTHING;

INSERT INTO community_members (community_id, user_id, role, joined_at) VALUES
('01912000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000005', 'OWNER', CURRENT_TIMESTAMP - INTERVAL '10 months'),
('01912000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000003', 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '3 months'),
('01912000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000004', 'OWNER', CURRENT_TIMESTAMP - INTERVAL '14 months'),
('01912000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000001', 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '9 months'),
('01912000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000006', 'OWNER', CURRENT_TIMESTAMP - INTERVAL '8 months'),
('01912000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000002', 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '5 months'),
('01912000-0000-7000-8000-000000000004', '01910000-0000-7000-8000-000000000001', 'OWNER', CURRENT_TIMESTAMP - INTERVAL '6 months')
ON CONFLICT DO NOTHING;

INSERT INTO posts (id, author_id, body, is_anonymous, visibility, status, content_warning, category_id, post_type, mood, created_at, updated_at) VALUES
('01913000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000001', 'Today I noticed that anxiety gets louder when I try to solve the whole week at once. Writing down the next ten-minute step made the day feel possible again.', FALSE, 'PUBLIC', 'PUBLISHED', NULL, '01911000-0000-7000-8000-000000000001', 'EXPERIENCE', 'Hopeful', CURRENT_TIMESTAMP - INTERVAL '45 minutes', CURRENT_TIMESTAMP - INTERVAL '45 minutes'),
('01913000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000002', 'How do you explain burnout to people who only see that you are still getting the work done? I look functional, but every small decision feels expensive.', FALSE, 'PUBLIC', 'PUBLISHED', NULL, '01911000-0000-7000-8000-000000000015', 'QUESTION', 'Overwhelmed', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('01913000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000003', 'My sleep did not change in one perfect night. It changed after a week of keeping one small promise: getting up at the same time without judging the night before.', FALSE, 'PUBLIC', 'PUBLISHED', NULL, '01911000-0000-7000-8000-000000000011', 'REFLECTION', 'Calm', CURRENT_TIMESTAMP - INTERVAL '7 hours', CURRENT_TIMESTAMP - INTERVAL '7 hours'),
('01913000-0000-7000-8000-000000000004', '01910000-0000-7000-8000-000000000001', 'Grief surprised me today in an ordinary grocery aisle. I left, took a breath outside, and called someone who knew the person I miss. I am learning that this is not going backwards.', TRUE, 'PUBLIC', 'PUBLISHED', 'Grief and loss', '01911000-0000-7000-8000-000000000013', 'EXPERIENCE', 'Low', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('01913000-0000-7000-8000-000000000005', '01910000-0000-7000-8000-000000000002', 'A boundary sounded harsh in my head, so I tried a shorter version: I care about this conversation and I need to continue it tomorrow. It felt honest rather than rejecting.', FALSE, 'PUBLIC', 'PUBLISHED', NULL, '01911000-0000-7000-8000-000000000014', 'REFLECTION', 'Calm', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO comments (id, post_id, author_id, body, is_anonymous, status, created_at, updated_at) VALUES
('01913100-0000-7000-8000-000000000001', '01913000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000003', 'The ten-minute idea feels kind and realistic. Thank you for sharing it.', FALSE, 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
('01913100-0000-7000-8000-000000000002', '01913000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000001', 'I relate to looking functional while feeling empty. You are not alone in that gap.', FALSE, 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO post_likes (post_id, user_id) VALUES
('01913000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000002'),
('01913000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000003'),
('01913000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000001'),
('01913000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000001')
ON CONFLICT DO NOTHING;

INSERT INTO professional_answers (id, post_id, professional_user_id, body, helpful_count, created_at) VALUES
('01914000-0000-7000-8000-000000000001', '01913000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000004', 'Narrowing attention to the next manageable action can reduce the demand placed on working memory. It is a useful grounding skill, especially when paired with a slower exhale.', 128, CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
('01914000-0000-7000-8000-000000000002', '01913000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000005', 'A consistent wake time is one of the strongest cues for the body clock. The gentle, non-judgmental approach described here is just as important as the routine itself.', 94, CURRENT_TIMESTAMP - INTERVAL '6 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO professional_articles (id, author_id, title, summary, topic, read_time_minutes, status, pinned, evidence_level, sections, takeaways, reference_list, helpful_count, published_at) VALUES
('01915000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000004', 'The anxiety cycle: why avoidance feels helpful—and keeps fear alive', 'A practical guide to the short-term relief and long-term cost of avoidance, with gentle ways to approach what matters.', 'Anxiety', 7, 'PUBLISHED', TRUE, 'Clinician reviewed',
 '[{"heading":"The short-term relief loop","body":"Avoidance can lower distress immediately. The brain notices that relief and learns to repeat the escape, even when the situation is not dangerous."},{"heading":"Build an approach ladder","body":"Choose a step small enough to repeat. Stay long enough to notice that discomfort can rise and fall without requiring an escape."},{"heading":"Measure willingness, not calm","body":"Success is showing up for the chosen step. Feeling anxious does not mean the practice failed."}]'::jsonb,
 '["Make the first step smaller than your ambition.","Repeat before increasing difficulty.","Seek professional support when anxiety seriously limits daily life."]'::jsonb,
 '["NICE guideline: Generalised anxiety disorder and panic disorder in adults","APA Clinical Practice Guideline for the Treatment of Depression"]'::jsonb,
 824, CURRENT_TIMESTAMP - INTERVAL '5 days'),
('01915000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000005', 'A seven-day sleep reset built around consistency, not perfection', 'Evidence-informed steps for strengthening sleep rhythm without turning bedtime into another performance test.', 'Sleep', 6, 'PUBLISHED', FALSE, 'Evidence informed',
 '[{"heading":"Anchor the morning","body":"Choose a wake time you can keep most days. Morning light and movement help strengthen the body clock."},{"heading":"Lower the struggle at night","body":"If you are awake and frustrated, move to a quiet activity until sleepiness returns instead of forcing sleep."}]'::jsonb,
 '["Keep wake time more stable than bedtime.","Protect the bed as a cue for rest.","Persistent sleep problems deserve qualified assessment."]'::jsonb,
 '["American Academy of Sleep Medicine clinical guidance","NHS insomnia self-help guidance"]'::jsonb,
 611, CURRENT_TIMESTAMP - INTERVAL '9 days'),
('01915000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000006', 'Repair after conflict: three sentences that make room for safety', 'A relationship therapist explains how to slow defensiveness and begin a more honest repair conversation.', 'Relationships', 5, 'PUBLISHED', FALSE, 'Clinician reviewed',
 '[{"heading":"Name your part","body":"Start with the behaviour you can own without adding a defence or a counter-accusation."},{"heading":"Name the impact","body":"Show that you understand how the moment may have felt to the other person."},{"heading":"Offer a specific next step","body":"A repair becomes trustworthy when it includes a small action that can be observed."}]'::jsonb,
 '["Repair is a process, not a perfect sentence.","Safety comes before resolution.","Accountability and boundaries can exist together."]'::jsonb,
 '["Gottman Institute resources on repair attempts","AAMFT consumer guidance"]'::jsonb,
 493, CURRENT_TIMESTAMP - INTERVAL '12 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO media_recommendations (id, title, media_kind, release_year, duration_label, theme, description, discussion_prompt, content_notes, recommended_by, accent, soft_accent) VALUES
('01916000-0000-7000-8000-000000000001', 'Inside Out 2', 'MOVIE', 2024, '1h 36m', 'Anxiety, identity, and growing up', 'A warm conversation starter about how new emotions can protect us and overwhelm us at the same time.', 'Which emotion tries hardest to protect you, and what might it need to hear?', ARRAY['Anxiety','Embarrassment'], 'GreenOcean editorial team', '#6B4F8D', '#F3EEFA'),
('01916000-0000-7000-8000-000000000002', 'Stutz', 'DOCUMENTARY', 2022, '1h 36m', 'Tools, vulnerability, and the therapeutic relationship', 'A personal documentary about practical mental-health tools and the human relationship behind them.', 'Which tool felt useful, and which claim would you want to examine more carefully?', ARRAY['Illness','Family loss'], 'Dr. Maya Bennett', '#2F7668', '#E7F6F1'),
('01916000-0000-7000-8000-000000000003', 'Ted Lasso', 'SERIES', 2020, '3 seasons', 'Belonging, leadership, and asking for help', 'A comedy-drama that can open conversations about panic, friendship, masculinity, and receiving support.', 'What changes when a leader lets other people see that they need help?', ARRAY['Panic attacks','Grief'], 'Dr. Daniel Green', '#4E8CB8', '#EAF4FA'),
('01916000-0000-7000-8000-000000000004', 'The Mind, Explained', 'SERIES', 2019, '2 seasons', 'Accessible psychology and neuroscience', 'Short introductions to memory, anxiety, mindfulness, and other topics that invite further learning.', 'Which explanation helped you understand an experience differently?', ARRAY['Anxiety','Trauma references'], 'GreenOcean knowledge team', '#D56C5C', '#FDEDEA')
ON CONFLICT (id) DO NOTHING;

INSERT INTO support_channels (id, name, slug, description, category, icon, channel_type, moderated, next_event) VALUES
('01917000-0000-7000-8000-000000000001', 'Better Sleep Circle', 'better-sleep-circle', 'A moderated group for realistic sleep routines and compassionate accountability.', 'Sleep', 'bedtime', 'GROUP', TRUE, 'Guided wind-down · Thursday 20:00 UTC'),
('01917000-0000-7000-8000-000000000002', 'Anxious Moments', 'anxious-moments', 'Peer support for getting through intense but non-emergency anxious moments.', 'Anxiety', 'air', 'GROUP', TRUE, 'Grounding practice · Daily 18:00 UTC'),
('01917000-0000-7000-8000-000000000003', 'Boundaries Practice', 'boundaries-practice', 'Practice clear, respectful boundary language with a moderated group.', 'Relationships', 'diversity_1', 'GROUP', TRUE, 'Role-play circle · Saturday'),
('01917000-0000-7000-8000-000000000004', 'GreenOcean Safety Updates', 'safety-updates', 'Product safety notes, policy updates, and community guidance.', 'Announcements', 'verified_user', 'ANNOUNCEMENT', TRUE, NULL),
('01917000-0000-7000-8000-000000000005', 'Knowledge Hub Updates', 'knowledge-updates', 'New clinician-reviewed articles and editorial notes.', 'Announcements', 'science', 'ANNOUNCEMENT', TRUE, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO conversations (id, conversation_kind, title, subtitle, channel_id, created_at, updated_at) VALUES
('01918000-0000-7000-8000-000000000001', 'GROUP', 'Better Sleep Circle', 'Moderated support group', '01917000-0000-7000-8000-000000000001', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
('01918000-0000-7000-8000-000000000002', 'GROUP', 'Anxious Moments', 'Moderated support group', '01917000-0000-7000-8000-000000000002', CURRENT_TIMESTAMP - INTERVAL '8 months', CURRENT_TIMESTAMP - INTERVAL '35 minutes'),
('01918000-0000-7000-8000-000000000003', 'GROUP', 'Boundaries Practice', 'Moderated support group', '01917000-0000-7000-8000-000000000003', CURRENT_TIMESTAMP - INTERVAL '4 months', CURRENT_TIMESTAMP - INTERVAL '1 hour')
ON CONFLICT (id) DO NOTHING;

INSERT INTO conversation_members (conversation_id, user_id, member_role, joined_at) VALUES
('01918000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000005', 'MODERATOR', CURRENT_TIMESTAMP - INTERVAL '6 months'),
('01918000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000003', 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '3 months'),
('01918000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000004', 'MODERATOR', CURRENT_TIMESTAMP - INTERVAL '8 months'),
('01918000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000001', 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '5 months'),
('01918000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000006', 'MODERATOR', CURRENT_TIMESTAMP - INTERVAL '4 months'),
('01918000-0000-7000-8000-000000000003', '01910000-0000-7000-8000-000000000002', 'MEMBER', CURRENT_TIMESTAMP - INTERVAL '2 months')
ON CONFLICT DO NOTHING;

INSERT INTO messages (id, conversation_id, sender_id, message_kind, body, created_at) VALUES
('01919000-0000-7000-8000-000000000001', '01918000-0000-7000-8000-000000000001', NULL, 'SYSTEM', 'This is a moderated peer-support space. It is not a crisis or medical service.', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('01919000-0000-7000-8000-000000000002', '01918000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000003', 'USER', 'I am trying a consistent wake time this week. Last night was rough, but I still kept the morning anchor.', CURRENT_TIMESTAMP - INTERVAL '50 minutes'),
('01919000-0000-7000-8000-000000000003', '01918000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000005', 'USER', 'That consistency matters more than one difficult night. Keep the experiment gentle and notice what changes over several days.', CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
('01919000-0000-7000-8000-000000000004', '01918000-0000-7000-8000-000000000002', NULL, 'SYSTEM', 'Share only what feels safe. Avoid personal contact details and use the report tools when needed.', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('01919000-0000-7000-8000-000000000005', '01918000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000001', 'USER', 'Putting both feet on the floor and naming five things I can see helped me stay with the moment.', CURRENT_TIMESTAMP - INTERVAL '35 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO reports (id, reporter_id, post_id, reason, description, status, severity, signals, created_at) VALUES
('0191a000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000003', '01913000-0000-7000-8000-000000000004', 'SELF_HARM_CONTENT', 'Automated safety review sample for the operations dashboard.', 'IN_REVIEW', 'HIGH', ARRAY['sensitive-language','human-review'], CURRENT_TIMESTAMP - INTERVAL '24 minutes'),
('0191a000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000001', '01913000-0000-7000-8000-000000000002', 'OTHER', 'Context review sample.', 'PENDING', 'LOW', ARRAY['user-report'], CURRENT_TIMESTAMP - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO audit_logs (id, actor_user_id, action, entity_type, entity_id, metadata, created_at) VALUES
('0191c000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000007', 'REPORT_ASSIGNED', 'REPORT', '0191a000-0000-7000-8000-000000000001', '{"source":"seed","note":"Safety workflow demonstration"}'::jsonb, CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
('0191c000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000007', 'PROFESSIONAL_APPLICATION_RECEIVED', 'PROFESSIONAL_VERIFICATION', '0191b000-0000-7000-8000-000000000001', '{"source":"seed"}'::jsonb, CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
