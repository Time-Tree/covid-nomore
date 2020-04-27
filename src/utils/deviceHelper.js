import { StatusBar, Platform, Dimensions, PixelRatio } from 'react-native';

export const DEVICE_HEIGHT = Dimensions.get('screen').height;
export const DEVICE_WIDTH = Dimensions.get('window').width;

const platform = Platform.OS;
export const isIOS = platform === 'ios';
export const isAndroid = platform === 'android';

const iPhoneXHeight = 812;
const iPhoneXRHeight = 896;
export const isIphoneX =
  isIOS &&
  (DEVICE_HEIGHT === iPhoneXHeight ||
    DEVICE_WIDTH === iPhoneXHeight ||
    DEVICE_HEIGHT === iPhoneXRHeight ||
    DEVICE_WIDTH === iPhoneXRHeight);
export const statusBarHeight = StatusBar.currentHeight || 0;
export const borderWidth = 1 / PixelRatio.getPixelSizeForLayoutSize(1);
