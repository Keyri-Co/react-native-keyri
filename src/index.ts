import { NativeModules, Platform } from 'react-native';
import type {
  KeyriModule,
  ProcessLinkOptions,
  InitializeKeyriOptions,
  SendEventOptions,
} from './types';

const LINKING_ERROR =
  "The package 'react-native-keyri' doesn't seem to be linked. Make sure: \n\n" +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Module = NativeModules.KeyriNativeModule;

if (!Module) throw new Error(LINKING_ERROR);

const Keyri: KeyriModule = {
  initialize: (options: InitializeKeyriOptions) => {
    return Module.initialize(options);
  },

  generateAssociationKey: (publicUserId?: string) => {
    return Module.generateAssociationKey(publicUserId);
  },

  generateUserSignature: (data: string, publicUserId?: string) => {
    return Module.generateUserSignature(data, publicUserId);
  },

  listAssociationKeys: () => {
    return Module.listAssociationKeys();
  },

  listUniqueAccounts: () => {
    return Module.listUniqueAccounts();
  },

  getAssociationKey: (publicUserId?: string) => {
    return Module.getAssociationKey(publicUserId);
  },

  removeAssociationKey: (publicUserId: string) => {
    return Module.removeAssociationKey(publicUserId);
  },

  sendEvent: (data: SendEventOptions) => {
    return Module.sendEvent(data);
  },

  initiateQrSession: (sessionId: string, publicUserId?: string) => {
    return Module.initiateQrSession(sessionId, publicUserId);
  },

  initializeDefaultConfirmationScreen: (payload: string) => {
    return Module.initializeDefaultConfirmationScreen(payload);
  },

  confirmSession: (payload: string, trustNewBrowser?: boolean) => {
    return Module.confirmSession(payload, trustNewBrowser);
  },

  denySession: (payload: string) => {
    return Module.denySession(payload);
  },

  easyKeyriAuth: (payload: string, publicUserId?: string) => {
    return Module.easyKeyriAuth(payload, publicUserId);
  },

  processLink: (options: ProcessLinkOptions) => {
    return Module.processLink(options);
  },
};

export default Keyri;

export * from './types';
