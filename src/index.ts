import { NativeModules, Platform } from 'react-native';
import type { KeyriModule } from './types';

const LINKING_ERROR =
  `The package 'react-native-keyri' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Keyri = NativeModules.KeyriNativeModule;

if (!Keyri) throw new Error(LINKING_ERROR);

export default Keyri as KeyriModule;
