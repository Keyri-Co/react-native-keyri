//
//  KeyriNativeModule.m
//  Keyri
//
//  Created by Artemii Tkachuk on 18.02.2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import "KeyriNativeModule.h"
#import <objc/runtime.h>
@import Keyri;

NSString *const KeyriNativeModuleDomain = @"KeyriNativeModule";
NSString *const KeyriNativeModuleGeneralCodeString = @"0";
NSInteger const KeyriNativeModuleGeneralCode = 0;

@interface KeyriNativeModule ()

@property (nonatomic, strong) KeyriObjC *keyri;
@property (nonatomic, strong) NSString *appKey;
@property (nonatomic, strong) NSMutableArray<Session *> *sessions;

@end


@implementation KeyriNativeModule

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (self = [super init]) {
        _sessions = [NSMutableArray array];
        _keyri = [[KeyriObjC alloc] init];
    }

    return  self;
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

- (void)handleErrorText:(NSString *)errorText withRejecter:(RCTPromiseRejectBlock)reject
{
    NSDictionary *details = @{NSLocalizedDescriptionKey : errorText};
    reject(
           KeyriNativeModuleGeneralCodeString,
           errorText,
           [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleGeneralCode userInfo:details]
    );
}

- (void)handleError:(NSError *)error withRejecter:(RCTPromiseRejectBlock)reject
{
    [self handleErrorText:error.localizedDescription withRejecter:reject];
}

RCT_EXPORT_METHOD(initialize:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    _appKey = data[@"appKey"];
    NSString *publicApiKey = data[@"publicAPIKey"];
    NSString *serviceEncryptionKey = data[@"serviceEncryptionKey"];

    if (_appKey == nil || ![_appKey isKindOfClass:[NSString class]]) {
      reject(@"InvalidArguments", @"You need to provide appKey", nil);
      return;
    }

    [self.keyri initializeKeyriWithAppKey:_appKey publicAPIKey:publicApiKey, serviceEncryptionKey:serviceEncryptionKey];
    resolve(@(YES));
}


RCT_EXPORT_METHOD(initiateQrSession:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *publicUserId = data[@"publicUserId"];
    NSString *sessionId = data[@"sessionId"];
    NSString *appKey = data[@"appKey"];

    if (![sessionId isKindOfClass:[NSString class]]) {
      return [self handleErrorText:@"You need to provide sessionId" withRejecter:reject];
    }

    __weak typeof (self) weakSelf = self;
    [self.keyri initiateQrSessionWithPublicUserId:publicUserId sessionId:sessionId appKey:appKey completion:^(Session * _Nullable session, NSError * _Nullable error) {
        typeof (self) strongSelf = weakSelf;

        if (error != nil) {
            return [strongSelf handleError:error withRejecter:reject];
        }

        if (session != nil) {
            [strongSelf.sessions addObject:session];
            NSDictionary *dict = [strongSelf dictionaryWithPropertiesOfObject:session];
            resolve(dict);
        } else {
            [strongSelf handleErrorText:@"Session not found" withRejecter:reject];
        }
    }];
}

// TODO Uncomment when available
//RCT_EXPORT_METHOD(sendEvent:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
//{
//    id publicUserId = [data objectForKey:@"publicUserId"];
//    id eventType = [data objectForKey:@"eventType"];
//    id success = [data objectForKey:@"success"];
//
//    if (![eventType isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide eventType" withRejecter:reject]; }
//    if (![success isKindOfClass:[BOOL class]]) { return [self handleErrorText:@"You need to provide success" withRejecter:reject]; }
//
//        __weak typeof (self) weakSelf = self;
//        [self.keyri sendEvent:publicUserId publicUserId:publicUserId eventType:eventType success:success completion:^(BOOL success, NSError * _Nullable error) {
//            typeof (self) strongSelf = weakSelf;
//            if (error != nil) {
//                return [strongSelf handleError:error withRejecter:reject];
//            }
//
//            resolve(@(success));
//        }];
//}

RCT_EXPORT_METHOD(initializeDefaultScreen:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Session *session;
    for (Session *_session in _sessions) {
        if ([_session.sessionId isEqualToString:sessionId]) {
            session = _session;
            break;
        }
    }

    if (session == nil) {
        return [self handleErrorText:@"Session not found" withRejecter:reject];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [_keyri initializeDefaultConfirmationScreenWithSession:session payload:payload completion:^(BOOL isApproved) {
            resolve(@(isApproved));
        }];
    });
}

RCT_EXPORT_METHOD(generateAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSError *error;
    NSString *generatedKey = [_keyri generateAssociationKeyWithPublicUserId:publicUserId error:&error];
    if (error != nil) {
        return [self handleError:error withRejecter:reject];
    }
    resolve(generatedKey);
}

RCT_EXPORT_METHOD(getUserSignature:(NSString *)publicUserId customSignedData:(NSString *)customSignedData resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSData *data = [customSignedData dataUsingEncoding:NSUTF8StringEncoding];
    if (![data isKindOfClass:[NSData class]]) {
        return [self handleErrorText:@"You need to provide customSignedData" withRejecter:reject];
    }

    NSError *error;
    NSString *signature = [_keyri generateUserSignatureWithPublicUserId:publicUserId data:data error:&error];
    if (error != nil) {
        return [self handleError:error withRejecter:reject];
    }

    resolve(signature);
}

RCT_EXPORT_METHOD(getAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSError *error;
    NSString *associationKey = [_keyri getAssociationKeyWithUsername:publicUserId error:&error];
    if (error != nil) {
        return [self handleError:error withRejecter:reject];
    }

    resolve(associationKey);
}

RCT_EXPORT_METHOD(removeAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSError *error;
    [self.keyri removeAssociationKeyWithPublicUserId:publicUserId error:&error];
    if (error != nil) {
        return [self handleError:error withRejecter:reject];
    }

    resolve(@"Success");
}

RCT_EXPORT_METHOD(listAssociationKeys:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSDictionary *associationKeys = [self.keyri listAssociactionKeys];
    resolve(associationKeys);
}

RCT_EXPORT_METHOD(listUniqueAccounts:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSDictionary *associationKeys = [self.keyri listUniqueAccounts];
    resolve(associationKeys);
}

RCT_EXPORT_METHOD(easyKeyriAuth:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id publicUserId = [data objectForKey:@"publicUserId"];
    id payload = [data objectForKey:@"payload"];
    id publicApiKey = [data objectForKey:@"publicApiKey"];
    id serviceEncryptionKey = [data objectForKey:@"serviceEncryptionKey"];
    id blockEmulatorDetection = [data objectForKey:@"blockEmulatorDetection"]; // TODO Add param as arg

    if (![payload isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide payload" withRejecter:reject]; }
    if (![publicUserId isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide publicUserId" withRejecter:reject]; }
    if (![publicApiKey isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide publicApiKey" withRejecter:reject]; }
    if (![serviceEncryptionKey isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide serviceEncryptionKey" withRejecter:reject]; }

    dispatch_async(dispatch_get_main_queue(), ^{
        __weak typeof (self) weakSelf = self;
        [self.keyri easyKeyriAuthWithPublicUserId:publicUserId payload:payload completion:^(BOOL success, NSError * _Nullable error) {
            typeof (self) strongSelf = weakSelf;
            if (error != nil) {
                return [strongSelf handleError:error withRejecter:reject];
            }

            resolve(@(success));
        }];
    });
}

RCT_EXPORT_METHOD(processLink:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id urlString = [data objectForKey:@"url"];
    id publicUserId = [data objectForKey:@"publicUserId"];
    id payload = [data objectForKey:@"payload"];

    if (![payload isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide payload" withRejecter:reject]; }
    if (![publicUserId isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide publicUserId" withRejecter:reject]; }
    if (![urlString isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide url" withRejecter:reject]; }

    __weak typeof (self) weakSelf = self;
    [self.keyri processLinkWithUrl:[NSURL URLWithString:urlString] publicUserId:publicUserId appKey:_appKey payload:payload completion:^(BOOL success, NSError * _Nullable error) {
        typeof (self) strongSelf = weakSelf;
        if (error != nil) {
            return [strongSelf handleError:error withRejecter:reject];
        }

        resolve(@(success));
    }];
}

RCT_EXPORT_METHOD(confirmSession:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self finishSession:sessionId payload:payload isApproved:YES resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(denySession:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self finishSession:sessionId payload:payload isApproved:NO resolver:resolve rejecter:reject];
}

- (void)finishSession:(NSString *)sessionId payload:(NSString *)payload isApproved:(BOOL)isApproved resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSString *result;
    for (Session *session in self.sessions) {
        if ([session.sessionId isEqualToString:sessionId]) {
            session.payload = payload;
            if (isApproved) {
                result = session.confirm;
            } else {
                result = session.deny;
            }
            break;
        }
    }

    resolve(@([result isEqualToString:@"success"]));
}

- (NSDictionary *)dictionaryWithPropertiesOfObject:(id)object
{
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];

    unsigned count;
    objc_property_t *properties = class_copyPropertyList([object class], &count);

    for (int i = 0; i < count; i++) {
        NSString *key = [NSString stringWithUTF8String:property_getName(properties[i])];
        id value = [object valueForKey:key];
        if ([value isKindOfClass:[NSString class]] || [value isKindOfClass:[NSNumber class]]) {
            [dict setObject:value forKey:key];
        } else {
            id valueDict = [self dictionaryWithPropertiesOfObject:value];
            if (valueDict && [[valueDict allKeys] count] > 0) {
                [dict setObject:valueDict forKey:key];
            }
        }
    }

    free(properties);

    return [NSDictionary dictionaryWithDictionary:dict];
}

@end
