import { Dimensions } from 'react-native';

export const APP_KEY = 'IT7VrTQ0r4InzsvCNJpRCRpi1qzfgpaj';
export const DURATION = 1200;
export const { width, height } = Dimensions.get('window');

export const screenX = width < height ? width : height;
export const screenY = width < height ? height : width;
export const scaledSize = (n: number) => {
  const scaleX = screenX / 375;
  const scaleY = screenY / 670;
  return Math.min(scaleX, scaleY) * n;
};
