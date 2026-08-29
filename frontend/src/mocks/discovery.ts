export type ProfessionalArticle = {
  id: string;
  authorId: string;
  title: string;
  summary: string;
  topic: string;
  readTime: string;
  publishedAt: string;
  pinned: boolean;
  evidenceLevel: 'CLINICIAN REVIEWED' | 'RESEARCH SUMMARY' | 'PRACTICAL GUIDE';
  helpfulCount: number;
  sections: { heading: string; body: string }[];
  takeaways: string[];
  references: string[];
};

export const professionalArticles: ProfessionalArticle[] = [
  {
    id: 'article-anxiety-cycle',
    authorId: 'pro-1',
    title: 'The anxiety cycle: why avoidance brings short relief but keeps fear alive',
    summary: 'A practical, evidence-informed guide to recognizing avoidance and taking smaller, safer steps toward what matters.',
    topic: 'Anxiety',
    readTime: '7 min read',
    publishedAt: 'August 26, 2026',
    pinned: true,
    evidenceLevel: 'CLINICIAN REVIEWED',
    helpfulCount: 842,
    sections: [
      { heading: 'The short-term relief trap', body: 'Avoiding a feared situation can lower anxiety quickly. That relief teaches the brain that avoidance was what kept you safe, so the fear can feel even more convincing the next time.' },
      { heading: 'Start below the panic line', body: 'Progress does not require forcing yourself into the hardest situation. A gradual plan begins with a step that feels uncomfortable but manageable, followed by time to notice that anxiety can rise and fall.' },
      { heading: 'Measure willingness, not perfect calm', body: 'The goal is not to remove every anxious feeling. A more useful measure is whether you were able to stay present, act according to your values, and recover with compassion afterward.' },
    ],
    takeaways: ['Name the pattern without judging yourself.', 'Choose one small approach step.', 'Repeat before making the step harder.'],
    references: ['Clinical review completed by GreenOcean Professional Standards', 'Educational summary — not personal medical advice'],
  },
  {
    id: 'article-sleep-reset',
    authorId: 'pro-2',
    title: 'A gentler sleep reset after weeks of restless nights',
    summary: 'Four behavioral anchors that can support a steadier sleep rhythm without turning bedtime into another performance test.',
    topic: 'Sleep',
    readTime: '5 min read',
    publishedAt: 'August 23, 2026',
    pinned: true,
    evidenceLevel: 'PRACTICAL GUIDE',
    helpfulCount: 516,
    sections: [
      { heading: 'Anchor the morning first', body: 'A consistent wake time and morning light are often more useful starting points than trying to force an exact bedtime.' },
      { heading: 'Make room for an imperfect night', body: 'Monitoring every minute of sleep can increase pressure. Notice the pattern across several days instead of treating one difficult night as a failure.' },
    ],
    takeaways: ['Keep the wake time steady.', 'Create a short wind-down cue.', 'Seek professional care if sleep problems persist or feel unsafe.'],
    references: ['Reviewed for educational clarity by GreenOcean editors'],
  },
  {
    id: 'article-family-repair',
    authorId: 'pro-3',
    title: 'Repair after conflict: the conversation that matters more than being perfect',
    summary: 'How families can return to connection after a difficult moment using accountability, curiosity, and a clear next step.',
    topic: 'Relationships',
    readTime: '6 min read',
    publishedAt: 'August 19, 2026',
    pinned: false,
    evidenceLevel: 'CLINICIAN REVIEWED',
    helpfulCount: 391,
    sections: [
      { heading: 'Begin with impact', body: 'A useful repair starts by recognizing how the moment affected the other person before explaining your intention.' },
      { heading: 'Make the next step observable', body: 'Instead of promising to do better, name one behavior you will try in the next difficult conversation.' },
    ],
    takeaways: ['Pause before repairing.', 'Acknowledge impact.', 'Choose one observable change.'],
    references: ['GreenOcean family practice editorial review'],
  },
];

export type MediaPick = {
  id: string;
  title: string;
  kind: 'MOVIE' | 'SERIES' | 'DOCUMENTARY';
  year: string;
  duration: string;
  theme: string;
  description: string;
  discussionPrompt: string;
  contentNotes: string[];
  recommendedBy: string;
  accent: string;
  softAccent: string;
};

export const mediaPicks: MediaPick[] = [
  {
    id: 'inside-out-2',
    title: 'Inside Out 2',
    kind: 'MOVIE',
    year: '2024',
    duration: '96 min',
    theme: 'Adolescence & anxiety',
    description: 'A warm starting point for talking about new emotions, identity, perfectionism, and anxiety during adolescence.',
    discussionPrompt: 'Which emotion tries hardest to protect you, even when its strategy becomes exhausting?',
    contentNotes: ['Anxiety', 'Embarrassment', 'Adolescent stress'],
    recommendedBy: 'Dr. Maya Bennett',
    accent: '#F27D69',
    softAccent: '#FFF0EC',
  },
  {
    id: 'mind-explained',
    title: 'The Mind, Explained',
    kind: 'SERIES',
    year: '2021',
    duration: '2 seasons',
    theme: 'Brain & behavior',
    description: 'Short documentary episodes that introduce topics such as focus, personality, the teenage brain, dreams, and anxiety.',
    discussionPrompt: 'Which explanation changed the way you interpret your own habits?',
    contentNotes: ['Mental health conditions', 'Scientific explanations'],
    recommendedBy: 'GreenOcean Editorial',
    accent: '#5B9BD5',
    softAccent: '#EBF4FC',
  },
  {
    id: 'stutz',
    title: 'Stutz',
    kind: 'DOCUMENTARY',
    year: '2022',
    duration: '96 min',
    theme: 'Therapy & personal growth',
    description: 'A candid conversation about a psychiatrist’s life and a visual, tool-based approach to therapy.',
    discussionPrompt: 'Which practical tool felt realistic enough to try in daily life?',
    contentNotes: ['Grief', 'Illness', 'Therapy experiences'],
    recommendedBy: 'Dr. Daniel Green',
    accent: '#8D7CC3',
    softAccent: '#F2EFFA',
  },
  {
    id: 'xanax-documentary',
    title: 'Take Your Pills: Xanax',
    kind: 'DOCUMENTARY',
    year: '2022',
    duration: '79 min',
    theme: 'Medication & anxiety',
    description: 'Patients and experts discuss the benefits, risks, and complexity surrounding a widely prescribed anti-anxiety medication.',
    discussionPrompt: 'How can personal stories and clinical evidence be held together without turning either into universal advice?',
    contentNotes: ['Medication', 'Substance dependence', 'Anxiety'],
    recommendedBy: 'GreenOcean Editorial',
    accent: '#B06D46',
    softAccent: '#FFF6E5',
  },
];
