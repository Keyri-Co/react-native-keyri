#import "React/RCTViewManager.h"
@import keyri_pod;

@interface RCT_EXTERN_MODULE(KeyriViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(color, NSString)

RCT_EXTERN_METHOD(initialize:(NSString *)appkey rpPublicKey:(NSString *)rpPublicKey callbackUrl:(NSString *)callbackUrl)
RCT_EXTERN_METHOD(onReadSessionId:(NSString *)sessionId resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(keyriSignUp:(NSString *)username service:(Service *)service custom:(NSString * _Nullable)custom resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(keyriLogin:(PublicAccount *)account service:(Service *)service custom:(NSString * _Nullable)custom resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(rpDirectSignUp:(String *)username custom:(NSString * _Nullable)custom extendedHeaders:(NSDictionary * _Nullable)extendedHeaders resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(rpDirectLogin:(PublicAccount *)account custom:(NSString * _Nullable)custom extendedHeaders:(NSDictionary * _Nullable)extendedHeaders resolver:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(accounts:(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)
RCT_EXTERN_METHOD(easyKeyriAuth:(NSString * _Nullable)custom resolver(RCTPromiseResolveBlock *)resolver reject:(RCTPromiseRejectBlock *)reject)

@end
