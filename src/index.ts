import { NativeModules, Platform } from 'react-native';
import type {
  KeyriInitializeOptions,
  KeyriModule,
  KeyriSessionLoginOptions,
  KeyriSessionSignupOptions,
} from './types';

const LINKING_ERROR =
  `The package 'react-native-keyri' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Module = NativeModules.KeyriNativeModule;

if (!Module) throw new Error(LINKING_ERROR);

const Keyri: KeyriModule = {
  initialize: (options: KeyriInitializeOptions) => {
    Module.initialize({
      appKey: options.appKey,
      callbackUrl: options.callbackUrl,
      allowMultipleAccounts: options.allowMultipleAccounts,
      publicKey: Platform.select({
        ios: options.iosPublicKey,
        android: options.androidPublicKey,
      }),
    });
  },

  handleSessionId: (sessionId) => {
    return Module.handleSessionId(sessionId);
  },

  sessionLogin: (options: KeyriSessionLoginOptions) => {
    return Module.sessionLogin(options);
  },

  sessionSignup: (options: KeyriSessionSignupOptions) => {
    return Module.sessionSignup(options);
  },

  directLogin: (username, headers = {}, custom) => {
    return Module.directLogin(username, headers, custom ?? null);
  },

  directSignup: (username, headers = {}, custom) => {
    return Module.directSignup(username, headers, custom ?? null);
  },

  getAccounts: () => {
    return Module.getAccounts();
  },

  easyKeyriAuth: (custom) => {
    return Module.easyKeyriAuth(custom ?? null);
  },

  removeAccount: (username, custom) => {
    return Module.removeAccount(username, custom ?? null);
  },

  whitelabelAuth: (options) => {
    return Module.whitelabelAuth(options);
  }
};

export default Keyri;

export * from './types';
