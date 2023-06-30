import { NativeModules, Platform } from 'react-native';
import type {
  InitiateQrSessionOptions,
  EasyKeyriAuthOptions,
  KeyriModule,
  ProcessLinkOptions,
  InitializeKeyriOptions,
  SendEventOptions,
} from './types';

const LINKING_ERROR =
  `The package 'react-native-keyri' doesn't seem to be linked. Make sure: \n\n` +
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

  getUserSignature: (publicUserId?: string, customSignedData?: string) => {
    return Module.getUserSignature(publicUserId, customSignedData);
  },

  listAssociationKey: () => {
    return Module.listAssociationKey();
  },

  listUniqueAccounts: () => {
    return Module.listUniqueAccounts();
  },

  getAssociationKey: (publicUserId?: string) => {
    return Module.getAssociationKey(publicUserId);
  },

  removeAssociationKey: (publicUserId?: string) => {
    return Module.removeAssociationKey(publicUserId);
  },

  sendEvent: (data: SendEventOptions) => {
    return Module.sendEvent(data);
  },

  initiateQrSession: (options: InitiateQrSessionOptions) => {
    return Module.initiateQrSession(options);
  },

  initializeDefaultScreen: (sessionId: string, payload: string) => {
    return Module.initializeDefaultScreen(sessionId, payload);
  },

  confirmSession: (sessionId: string, payload: string) => {
    return Module.confirmSession(sessionId, payload);
  },

  denySession: (sessionId: string, payload: string) => {
    return Module.denySession(sessionId, payload);
  },

  easyKeyriAuth: (data: EasyKeyriAuthOptions) => {
    return Module.easyKeyriAuth(data);
  },

  processLink: (options: ProcessLinkOptions) => {
    return Module.processLink(options);
  },
};

export default Keyri;

export * from './types';
