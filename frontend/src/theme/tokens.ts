export const colors = {
  ocean950: '#062621', ocean900: '#083C34', ocean800: '#0C5146', ocean700: '#116B5B',
  ocean600: '#16806C', ocean500: '#20A486', ocean400: '#45BEA1', ocean300: '#86D9C5',
  ocean200: '#BCEADD', ocean100: '#E5F7F1', ocean50: '#F2FBF8', foam: '#F7FAF9',
  white: '#FFFFFF', ink: '#152522', inkSoft: '#344944', muted: '#71827E', border: '#DFE9E6',
  borderStrong: '#CBDAD6', coral: '#F27D69', coralSoft: '#FFF0EC', sun: '#F4B860', sunSoft: '#FFF6E5',
  sky: '#5B9BD5', skySoft: '#EBF4FC', lavender: '#8D7CC3', lavenderSoft: '#F2EFFA',
  danger: '#B42318', success: '#16806C', overlay: 'rgba(5, 32, 28, 0.52)',
} as const;
export const spacing = { xxs: 2, xs: 4, sm: 8, md: 16, lg: 24, xl: 32, xxl: 48, xxxl: 64 } as const;
export const radius = { xs: 8, sm: 12, md: 18, lg: 24, xl: 32, pill: 999 } as const;
export const shadow = {
  soft: { shadowColor: '#062621', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.06, shadowRadius: 20, elevation: 2 },
  floating: { shadowColor: '#062621', shadowOffset: { width: 0, height: 12 }, shadowOpacity: 0.14, shadowRadius: 26, elevation: 8 },
} as const;
export const typography = { display: 38, h1: 30, h2: 22, h3: 18, body: 16, small: 13, micro: 11 } as const;
export const layout = { maxContent: 720, maxWide: 1120 } as const;
