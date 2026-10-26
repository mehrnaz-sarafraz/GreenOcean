import { router, type Href, usePathname } from 'expo-router';
import { PropsWithChildren, useEffect, useRef } from 'react';
import { StyleSheet, Text, TextStyle, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { AppButton } from '@/components/app-button';
import { AppIcon } from '@/components/app-icon';
import { Screen } from '@/components/screen';
import { useLanguage } from '@/localization/language-provider';
import { publicCopy } from '@/localization/public-copy';
import { colors, radius, spacing } from '@/theme/tokens';

export function usePublicNavigation() {
  const path=usePathname();const navigating=useRef('');
  // The current path is part of the key: returning via Back re-enables a destination naturally.
  useEffect(()=>{navigating.current='';},[path]);
  return (href: Href) => { const key=JSON.stringify(href);if(navigating.current===key)return;navigating.current=key;router.push(href); };
}
export function PublicText({children,style}:PropsWithChildren<{style?:TextStyle}>) {
  const {isRtl}=useLanguage();return <Text testID="public-text" selectable style={[publicStyles.body,{textAlign:isRtl?'right':'left',writingDirection:isRtl?'rtl':'ltr'},style]}>{children}</Text>;
}
export function PublicHeading({children,level=2}:PropsWithChildren<{level?:1|2|3}>) {
  const {isRtl}=useLanguage();return <Text accessibilityRole="header" aria-level={level} style={[publicStyles.heading,level===1&&publicStyles.h1,{textAlign:isRtl?'right':'left'}]}>{children}</Text>;
}
export function PublicCard({children}:PropsWithChildren) {return <View style={publicStyles.card}>{children}</View>;}
export function PublicLinks() {
  const {language}=useLanguage();const copy=publicCopy[language];const navigate=usePublicNavigation();
  return <View style={publicStyles.links}>{[...copy.infoLinks,...copy.policyLinks].map(link=><AppButton key={link.id} label={link.label} variant="secondary" onPress={()=>navigate({pathname:'/(auth)/info/[page]',params:{page:link.id}})} />)}</View>;
}
export function PublicPage({children,back=true}:PropsWithChildren<{back?:boolean}>) {
  const {language,isRtl,setLanguage}=useLanguage();const copy=publicCopy[language];const navigate=usePublicNavigation();
  return <SafeAreaView style={publicStyles.safe}><Screen scroll style={{padding:0}}><View role="main" style={[publicStyles.content,{direction:isRtl?'rtl':'ltr'}]}>
    <View style={publicStyles.header}>
      <View style={publicStyles.brand}><AppIcon name="waves" color={colors.ocean700} /><Text style={publicStyles.brandText}>GreenOcean</Text></View>
      <View accessibilityLabel={copy.language} style={publicStyles.languages}>
        <AppButton label="English" selected={language==='en'} variant="secondary" onPress={()=>void setLanguage('en')} />
        <AppButton label="فارسی" selected={language==='fa'} variant="secondary" onPress={()=>void setLanguage('fa')} />
      </View>
    </View>
    <View style={publicStyles.navigation}>
      {back&&<AppButton label={copy.back} variant="secondary" onPress={()=>{if(router.canGoBack())router.back();else router.replace('/(auth)/welcome');}} />}
      <AppButton label={copy.urgent} variant="secondary" onPress={()=>navigate('/(auth)/urgent-help')} />
      {back&&<AppButton label={copy.welcome} variant="secondary" onPress={()=>router.replace('/(auth)/welcome')} />}
    </View>
    {children}
    <View style={publicStyles.footer}><PublicText>{copy.noDisclosure}</PublicText></View>
  </View></Screen></SafeAreaView>;
}
export const publicStyles=StyleSheet.create({
  safe:{flex:1,backgroundColor:colors.foam},content:{width:'100%',maxWidth:1120,alignSelf:'center',padding:spacing.md,gap:spacing.lg},
  header:{flexDirection:'row',flexWrap:'wrap',alignItems:'center',justifyContent:'space-between',gap:spacing.md},brand:{flexDirection:'row',alignItems:'center',gap:spacing.sm},brandText:{fontSize:22,fontWeight:'800',color:colors.ocean900},
  languages:{flexDirection:'row',flexWrap:'wrap',gap:spacing.sm},navigation:{flexDirection:'row',flexWrap:'wrap',gap:spacing.sm},
  body:{color:colors.inkSoft,fontSize:16,lineHeight:26,flexShrink:1},heading:{color:colors.ocean900,fontSize:24,lineHeight:34,fontWeight:'800',flexShrink:1},h1:{fontSize:34,lineHeight:46},
  card:{borderWidth:1,borderColor:colors.borderStrong,borderRadius:radius.lg,padding:spacing.lg,backgroundColor:colors.white,gap:spacing.md,minWidth:0},
  links:{gap:spacing.sm},footer:{paddingVertical:spacing.lg,borderTopWidth:1,borderColor:colors.borderStrong},status:{color:colors.danger,fontSize:16,lineHeight:26},
});
