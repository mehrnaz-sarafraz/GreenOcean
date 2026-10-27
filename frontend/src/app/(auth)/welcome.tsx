import { useWindowDimensions, View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { useAuth } from '@/features/auth/auth-provider';
import { PublicPage, PublicCard, PublicHeading, PublicLinks, PublicText, usePublicNavigation } from '@/features/public/public-page';
import { accountEntry } from '@/features/public/contracts';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { colors, spacing } from '@/theme/tokens';
export default function Welcome() {
  const {status}=useAuth();const {language,isRtl}=useLanguage();const copy=publicCopy[language];const navigate=usePublicNavigation();const {width}=useWindowDimensions();
  const entry=accountEntry(status);
  function actions() {
    if(entry==='loading')return <View role="status" accessibilityLabel={copy.checking}><ActivityIndicator color={colors.ocean700}/><PublicText>{copy.checking}</PublicText></View>;
    if(entry==='account')return <AppButton label={copy.continue} onPress={()=>navigate('/(app)')} />;
    return <><AppButton label={copy.create} onPress={()=>navigate('/(auth)/register')} /><AppButton label={copy.signIn} variant="secondary" onPress={()=>navigate('/(auth)/login')} /></>;
  }
  return <PublicPage back={false}>
    <View style={styles.hero}>
      <Text style={[styles.eyebrow,{textAlign:isRtl?'right':'left'}]}>{copy.eyebrow}</Text>
      <PublicHeading level={1}>{copy.headline}</PublicHeading><PublicText>{copy.intro}</PublicText>
      <View style={styles.actions}>{actions()}</View>
      <AppButton label={copy.publicResources} variant="secondary" onPress={()=>navigate('/(auth)/resources')} />
    </View>
    <PublicHeading>{copy.pathwaysTitle}</PublicHeading>
    <View style={styles.grid}>{copy.pathways.map(path=><View key={path.id} style={{width:width>=1000?'31%':'100%',minWidth:0}}><PublicCard>
      <AppIcon name={path.icon} color={colors.ocean700} size={30}/><PublicHeading level={3}>{path.title}</PublicHeading><PublicText>{path.body}</PublicText>
      <AppButton label={path.action} variant="secondary" onPress={()=>navigate({pathname:'/(auth)/info/[page]',params:{page:path.id}})}/>
    </PublicCard></View>)}</View>
    <PublicText>{copy.accountRequired}</PublicText><PublicCard><PublicText>{copy.availability}</PublicText><PublicText>{copy.boundary}</PublicText></PublicCard>
    <PublicHeading>{copy.howTitle}</PublicHeading>
    {copy.steps.map(step=><PublicCard key={step.title}><PublicHeading level={3}>{step.title}</PublicHeading><PublicText>{step.body}</PublicText></PublicCard>)}
    <AppButton label={copy.infoLinks[0].label} variant="secondary" onPress={()=>navigate({pathname:'/(auth)/info/[page]',params:{page:'how-it-works'}})}/>
    <PublicCard><PublicHeading>{copy.trustTitle}</PublicHeading><PublicText>{copy.trustBody}</PublicText></PublicCard>
    <PublicCard><PublicHeading>{copy.guestTitle}</PublicHeading><PublicText>{copy.guestBody}</PublicText><AppButton label={copy.publicResources} onPress={()=>navigate('/(auth)/resources')}/><AppButton label={copy.urgent} variant="secondary" onPress={()=>navigate('/(auth)/urgent-help')}/></PublicCard>
    <PublicHeading>{copy.policyTitle}</PublicHeading><PublicLinks/>
    <PublicCard><PublicHeading>{copy.invitation}</PublicHeading><PublicText>{copy.invitationBody}</PublicText><View style={styles.actions}>{actions()}</View></PublicCard>
  </PublicPage>;
}
const styles=StyleSheet.create({hero:{gap:spacing.lg,paddingVertical:spacing.lg},eyebrow:{color:colors.ocean700,fontSize:13,fontWeight:'700',letterSpacing:1},actions:{gap:spacing.sm},grid:{flexDirection:'row',flexWrap:'wrap',gap:spacing.md}});
