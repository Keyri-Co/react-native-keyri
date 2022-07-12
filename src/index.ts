import { NativeModules, Platform } from 'react-native';
import type {
  InitiateQrSessionOptions,
  EasyKeyriAuthOptions,
  KeyriModule,
 
} from './types';

const LINKING_ERROR =
  `The package 'react-native-keyri' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Module = NativeModules.KeyriNativeModule;

if (!Module) throw new Error(LINKING_ERROR);

 const Keyri: KeyriModule = {
  generateAssociationKey:(publicUserId: string) => {
    return Module.generateAssociationKey(publicUserId)
   },
  getUserSignature: (publicUserId?: string, customSignedData?: string) => {
    return Module.getUserSignature(publicUserId, customSignedData)
   },
   listAssociationKey: () => {
    return Module.listAssociationKey()
   },

   getAssociationKey: (publicUserId?: string) => {
    return Module.getAssociationKey(publicUserId)
   },

   initiateQrSession: (options: InitiateQrSessionOptions) => {
    return Module.initiateQrSession(options)
   },
   initializeDefaultScreen: (sessionId: string) => {
    return Module.initializeDefaultScreen(sessionId)
   },
   confirmSession: (sessionId: string) => {
    return Module.confirmSession(sessionId)
   },
   denySession: (sessionId: string) => {
    return Module.denySession(sessionId)
   },
   easyKeyriAuth: (data: EasyKeyriAuthOptions) => {
    return Module.easyKeyriAuth(data)
   },
   

};

export default Keyri;

export * from './types';
