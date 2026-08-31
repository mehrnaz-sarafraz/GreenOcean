import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { AppButton } from '@/components/app-button';
import { ActionRow, Notice, ProductCard, ProductHeader, ProductPage, ProductSection } from '@/components/product-ui';
import { colors, radius, spacing } from '@/theme/tokens';

type InfoContent = { title: string; eyebrow: string; subtitle: string; sections: { title: string; body: string }[] };
const content: Record<string, InfoContent> = {
  'how-it-works': { title: 'Support that meets you where you are', eyebrow: 'HOW GREENOCEAN WORKS', subtitle: 'Move between shared experience, trained listening, professional knowledge, and local care without losing control of your privacy.', sections: [
    { title: '1. Personalise your space', body: 'Choose the feelings, conditions, and life experiences that matter to you. Your choices shape recommendations without limiting what you can explore.' },
    { title: '2. Choose the kind of support', body: 'Share a story, join a moderated circle, speak privately with a trained listener, or compare verified mental-health professionals.' },
    { title: '3. Stay in control', body: 'Post anonymously, blur sensitive stories, mute topics, separate message requests, block accounts, and review moderation outcomes.' },
    { title: '4. Build skills over time', body: 'Use check-ins, journals, goals, assessments, and guided growth paths alongside your community support.' },
  ] },
  about: { title: 'A calmer place for honest mental-health conversations', eyebrow: 'ABOUT GREENOCEAN', subtitle: 'GreenOcean brings peer community, trained listeners, verified professional knowledge, and thoughtful self-care tools into one safety-first experience.', sections: [
    { title: 'Our purpose', body: 'Help people feel less alone while preserving the boundary between lived experience, education, and clinical care.' },
    { title: 'Our principles', body: 'Safety before engagement. Choice before exposure. Clear credentials before trust. Human connection before growth metrics.' },
    { title: 'What we are not', body: 'GreenOcean is not an emergency service, does not diagnose conditions, and does not replace a licensed professional who knows your individual situation.' },
  ] },
  faq: { title: 'Frequently asked questions', eyebrow: 'HELP BEFORE YOU NEED IT', subtitle: 'Clear answers about privacy, professionals, listeners, moderation, and urgent support.', sections: [
    { title: 'Can I post anonymously?', body: 'Yes. Anonymous posts hide your public identity from other members. GreenOcean still retains the minimum account link required for safety and moderation.' },
    { title: 'Are professionals verified?', body: 'Verified profiles have submitted identity, education, licensing, and scope-of-practice information. Always confirm local licensing before booking care.' },
    { title: 'Are listeners therapists?', body: 'No. Listeners are trained peers who provide emotional presence and active listening, not diagnosis, treatment, or medical advice.' },
    { title: 'What happens after a report?', body: 'The content is hidden from your view, a safety case is created, and you can track whether it is submitted, reviewing, or resolved.' },
    { title: 'What if I am in immediate danger?', body: 'Use the Urgent Help directory to find emergency and crisis resources for your location. Do not wait for a community or listener response.' },
  ] },
  privacy: { title: 'Privacy policy', eyebrow: 'YOUR DATA, YOUR CHOICES', subtitle: 'A plain-language product summary for the prototype. Production launch still requires jurisdiction-specific legal review.', sections: [
    { title: 'Data we use', body: 'Account details, preferences, content you choose to share, safety actions, and product activity needed to provide the service.' },
    { title: 'Sensitive information', body: 'Mental-health content is treated as sensitive. It should never be sold for advertising or used to alter a professional trust score.' },
    { title: 'Your controls', body: 'You can manage visibility, download your data, remove content, block accounts, and request account deletion.' },
    { title: 'Safety exceptions', body: 'Access may be limited to trained safety staff when content is reported or automated systems identify a serious risk.' },
  ] },
  terms: { title: 'Terms of use', eyebrow: 'COMMUNITY AGREEMENT', subtitle: 'The core expectations for using GreenOcean safely and respectfully.', sections: [
    { title: 'Peer support, not medical care', body: 'Members share personal experience. Do not diagnose others or instruct anyone to start, stop, or change treatment.' },
    { title: 'Respect and consent', body: 'No harassment, hate, sexual exploitation, doxxing, impersonation, or repeated contact after someone asks you to stop.' },
    { title: 'Professional responsibility', body: 'Professionals must stay within verified scope, distinguish education from personal care, and disclose conflicts or sponsored placement.' },
    { title: 'Moderation', body: 'Content may be limited, labelled, removed, or escalated. Members can view outcomes and appeal eligible decisions.' },
  ] },
  guidelines: { title: 'Community guidelines', eyebrow: 'HOW WE CARE FOR THIS SPACE', subtitle: 'Share your story, protect privacy, and make room for support without prescribing a solution.', sections: [
    { title: 'Speak from experience', body: 'Use “what helped me” instead of telling someone what they must do.' },
    { title: 'Add content notes', body: 'Use a clear warning for grief, violence, abuse, self-harm, eating disorders, or frightening detail.' },
    { title: 'Protect identities', body: 'Do not share full names, addresses, workplaces, contact details, or identifying medical documents.' },
    { title: 'Report, do not retaliate', body: 'Report unsafe content and step away. The safety team reviews context, previous signals, and the author’s wellbeing.' },
  ] },
  help: { title: 'Help center', eyebrow: 'FIND AN ANSWER OR A PERSON', subtitle: 'Browse common tasks or contact the GreenOcean support team.', sections: [] },
  contact: { title: 'Contact GreenOcean', eyebrow: 'WE ARE LISTENING', subtitle: 'Send a product, account, accessibility, or moderation question.', sections: [] },
  'forgot-password': { title: 'Reset your password', eyebrow: 'ACCOUNT RECOVERY', subtitle: 'Enter your email and we will prepare a secure recovery link.', sections: [] },
  'verify-email': { title: 'Verify your email', eyebrow: 'ONE MORE STEP', subtitle: 'Enter the six-digit code sent to your email address.', sections: [] },
  accessibility: { title: 'Accessibility', eyebrow: 'DESIGNED FOR MORE PEOPLE', subtitle: 'GreenOcean aims for keyboard, screen reader, contrast, motion, language, and cognitive accessibility.', sections: [{ title: 'Current baseline', body: 'Interactive controls have labels, text stays selectable where practical, layouts adapt to narrow screens, and meaning does not rely on color alone.' }, { title: 'Before production', body: 'Complete WCAG 2.2 AA audits, screen-reader testing, keyboard-only testing, zoom and reflow checks, captions, reduced-motion support, and testing with disabled participants.' }] },
  cookies: { title: 'Cookie choices', eyebrow: 'CONTROL OPTIONAL STORAGE', subtitle: 'Essential storage keeps the service working. Analytics and personalization should require clear consent where law requires it.', sections: [{ title: 'Essential', body: 'Session, security, routing, accessibility, and saved preference storage required for core service behavior.' }, { title: 'Optional analytics', body: 'Production analytics should be privacy-minimized, documented, and disabled until consent is recorded where required.' }] },
};

export default function InfoPage() {
  const { page } = useLocalSearchParams<{ page: string }>(); const current = content[page] ?? content.help;
  const [value, setValue] = useState(''); const [message, setMessage] = useState('');
  const isForm = ['contact', 'forgot-password', 'verify-email'].includes(page);
  function submit() { if (!value.trim()) return setMessage('Add the requested information first.'); setMessage(page === 'verify-email' ? 'Email verified for this demo.' : page === 'forgot-password' ? 'Recovery instructions prepared for this demo.' : 'Your support request has been recorded.'); }
  return <ProductPage><ProductHeader title={current.title} subtitle={current.subtitle} eyebrow={current.eyebrow} />
    {current.sections.map(section => <ProductCard key={section.title}><Text style={styles.sectionTitle}>{section.title}</Text><Text style={styles.body}>{section.body}</Text></ProductCard>)}
    {page === 'help' && <ProductSection title="Popular help topics"><ActionRow icon="lock_reset" title="Account and sign-in" subtitle="Passwords, email verification, and sessions" onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'forgot-password' } } as never)} /><ActionRow icon="shield" title="Privacy and reporting" subtitle="Blocks, reports, moderation, and appeals" onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'privacy' } } as never)} /><ActionRow icon="verified" title="Professional verification" subtitle="Documents, review status, and scope" onPress={() => router.push({ pathname: '/professional/[id]/[action]' as never, params: { id: 'apply', action: 'start' } } as never)} /><ActionRow icon="support_agent" title="Listener support" subtitle="Matching, boundaries, and feedback" onPress={() => router.push({ pathname: '/listener/[action]' as never, params: { action: 'match' } } as never)} /></ProductSection>}
    {isForm && <ProductCard><TextInput value={value} onChangeText={setValue} multiline={page === 'contact'} placeholder={page === 'verify-email' ? 'Six-digit code' : page === 'contact' ? 'How can we help?' : 'Email address'} placeholderTextColor={colors.muted} style={[styles.input, page === 'contact' && styles.largeInput]} /><AppButton label={page === 'contact' ? 'Send request' : page === 'verify-email' ? 'Verify email' : 'Send recovery link'} onPress={submit} />{!!message && <Notice icon="check_circle" title="Request updated" body={message} />}</ProductCard>}
    <View style={styles.links}><Pressable onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'accessibility' } } as never)}><Text style={styles.link}>Accessibility</Text></Pressable><Text style={styles.dot}>·</Text><Pressable onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'cookies' } } as never)}><Text style={styles.link}>Cookie choices</Text></Pressable><Text style={styles.dot}>·</Text><Pressable onPress={() => router.push({ pathname: '/info/[page]' as never, params: { page: 'guidelines' } } as never)}><Text style={styles.link}>Safety</Text></Pressable></View>
  </ProductPage>;
}

const styles = StyleSheet.create({ sectionTitle: { color: colors.ink, fontSize: 15, fontWeight: '900' }, body: { color: colors.inkSoft, fontSize: 12, lineHeight: 20 }, input: { minHeight: 52, borderWidth: 1, borderColor: colors.border, borderRadius: radius.md, padding: spacing.md, color: colors.ink, backgroundColor: colors.foam }, largeInput: { minHeight: 140, textAlignVertical: 'top' }, links: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'center', gap: spacing.sm }, link: { color: colors.ocean700, fontSize: 10, fontWeight: '800' }, dot: { color: colors.muted } });
