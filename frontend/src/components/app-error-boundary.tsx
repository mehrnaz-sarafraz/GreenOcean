import React, { ErrorInfo, PropsWithChildren } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { AppButton } from '@/components/app-button';
import { colors, spacing, typography } from '@/theme/tokens';

type State = { failed: boolean };

export class AppErrorBoundary extends React.Component<PropsWithChildren, State> {
  state: State = { failed: false };
  static getDerivedStateFromError(): State { return { failed: true }; }
  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled UI error', { message: error.message, componentStack: info.componentStack });
  }
  render() {
    if (!this.state.failed) return this.props.children;
    return <View accessibilityRole="alert" style={styles.page}>
      <Text style={styles.title}>Something went wrong</Text>
      <Text style={styles.body}>Your data is safe. Try loading this part of GreenOcean again.</Text>
      <AppButton label="Try again" onPress={() => this.setState({ failed: false })} />
    </View>;
  }
}

const styles = StyleSheet.create({
  page: { flex: 1, justifyContent: 'center', padding: spacing.xl, gap: spacing.md, backgroundColor: colors.foam },
  title: { color: colors.ink, fontSize: typography.h2, fontWeight: '900' },
  body: { color: colors.muted, lineHeight: 21 },
});
