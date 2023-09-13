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

  generateUserSignature: (publicUserId?: string, data?: string) => {
    return Module.generateUserSignature(publicUserId, data);
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

  initiateQrSession: (options: InitiateQrSessionOptions) => {
    return Module.initiateQrSession(options);
  },

  initializeDefaultScreen: (payload: string) => {
    return Module.initializeDefaultScreen(payload);
  },

  confirmSession: (payload: string, trustNewBrowser?: boolean) => {
    return Module.confirmSession(payload, trustNewBrowser);
  },

  denySession: (payload: string) => {
    return Module.denySession(payload);
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
