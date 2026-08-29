export const DATA_MODE = process.env.EXPO_PUBLIC_DATA_MODE === 'api' ? 'api' : 'mock';
export const isMockMode = DATA_MODE === 'mock';
