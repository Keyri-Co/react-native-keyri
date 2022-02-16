#import "React/RCTViewManager.h"
@import keyri_pod;

@interface RCT_EXTERN_MODULE(KeyriViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(color, NSString)

RCT_EXTERN_METHOD(initialize:(NSString *)appkey rpPublicKey:(NSString *)rpPublicKey callbackUrl:(NSString *)callbackUrl)
RCT_EXTERN_METHOD(handleSessionId:(NSString *)sessionId resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(sessionSignup:(NSString *)username service:(Service *)service custom:(NSString * _Nullable)custom resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(sessionLogin:(PublicAccount *)account service:(Service *)service custom:(NSString * _Nullable)custom resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(directSignup:(String *)username custom:(NSString * _Nullable)custom extendedHeaders:(NSDictionary * _Nullable)extendedHeaders resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(directLogin:(PublicAccount *)account custom:(NSString * _Nullable)custom extendedHeaders:(NSDictionary * _Nullable)extendedHeaders resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(getAccounts:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(easyKeyriAuth:(NSString * _Nullable)custom resolver(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)

@end
