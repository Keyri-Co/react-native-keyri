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

API_AVAILABLE(ios(14.0))
@interface KeyriNativeModule ()

@property (nonatomic, strong) KeyriObjC *keyri;
@property (nonatomic, strong) Session *latestSession;

@end

@implementation KeyriNativeModule

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (@available(iOS 14, *)) {
        if (self = [super init]) {
            _keyri = [[KeyriObjC alloc] init];
            _latestSession = [[Session alloc] init];
        }
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
    NSString *appKey = data[@"appKey"];
    NSString *publicApiKey = data[@"publicAPIKey"];
    NSString *serviceEncryptionKey = data[@"serviceEncryptionKey"];

    if (appKey == nil || ![appKey isKindOfClass:[NSString class]]) {
        reject(@"InvalidArguments", @"You need to provide appKey", nil);
        return;
    }

    [self.keyri initializeKeyriWithAppKey:appKey publicAPIKey:publicApiKey serviceEncryptionKey:serviceEncryptionKey];
    resolve(@(YES));
}


RCT_EXPORT_METHOD(initiateQrSession:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *publicUserId = data[@"publicUserId"];
    NSString *sessionId = data[@"sessionId"];

    if (![sessionId isKindOfClass:[NSString class]]) {
        return [self handleErrorText:@"You need to provide sessionId" withRejecter:reject];
    }

    __weak typeof (self) weakSelf = self;
    if (@available(iOS 14.0, *)) {
        [self.keyri initiateQrSessionWithSessionId:sessionId publicUserId:publicUserId completion:^(Session * _Nullable session, NSError * _Nullable error) {
            typeof (self) strongSelf = weakSelf;

            if (error != nil) {
                return [strongSelf handleError:error withRejecter:reject];
            }

            if (session != nil) {
                strongSelf.latestSession = session;
                NSDictionary *dict = [self dictionaryWithPropertiesOfObject:session];
                resolve(dict);
            } else {
                [self handleErrorText:@"Session not found" withRejecter:reject];
            }
        }];
    } else {
        return [self handleErrorText:@"Available only from iOS 14" withRejecter:reject];
    }
}

RCT_EXPORT_METHOD(sendEvent:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id publicUserId = [data objectForKey:@"publicUserId"];
    id eventType = [data objectForKey:@"eventType"];
    id success = [data objectForKey:@"success"];

    if (![eventType isKindOfClass:[NSString class]]) {
        return [self handleErrorText:@"You need to provide eventType" withRejecter:reject];
    }

    __weak typeof (self) weakSelf = self;
    [self.keyri sendEventWithPublicUserId:publicUserId eventType:eventType success:success completion:^(FingerprintResponse * _Nullable fingerprintResponse, NSError * _Nullable error) {
        typeof (self) strongSelf = weakSelf;

        if (error != nil) {
            return [self handleError:error withRejecter:reject];
        }

        if (fingerprintResponse != nil) {
            NSDictionary *dict = [strongSelf dictionaryWithPropertiesOfObject:fingerprintResponse];
            resolve(dict);
        } else {
            [strongSelf handleErrorText:@"Fingerprint response is null" withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(initializeDefaultScreen:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (self.latestSession == nil) {
        return [self handleErrorText:@"Session not found" withRejecter:reject];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.keyri initializeDefaultConfirmationScreenWithSession:self.latestSession payload:payload completion:^(BOOL isApproved) {
            resolve(@(isApproved));
        }];
    });
}

RCT_EXPORT_METHOD(generateAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [_keyri generateAssociationKeyWithPublicUserId:publicUserId completion:^(NSString * _Nullable generatedKey, NSError * _Nullable error) {
        if (generatedKey != nil) {
            resolve(generatedKey);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(getUserSignature:(NSString *)publicUserId customSignedData:(NSString *)customSignedData resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSData *data = [customSignedData dataUsingEncoding:NSUTF8StringEncoding];
    if (![data isKindOfClass:[NSData class]]) {
        return [self handleErrorText:@"You need to provide customSignedData" withRejecter:reject];
    }

    [_keyri generateUserSignatureWithPublicUserId:publicUserId data:data completion:^(NSString * _Nullable signature, NSError * _Nullable error) {
        if (signature != nil) {
            resolve(signature);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(getAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [_keyri getAssociationKeyWithPublicUserId:publicUserId completion:^(NSString * _Nullable associationKey, NSError * _Nullable error) {
        if (associationKey != nil) {
            resolve(associationKey);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(removeAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (![publicUserId isKindOfClass:[NSData class]]) {
        return [self handleErrorText:@"You need to provide publicUserId" withRejecter:reject];
    }

    [_keyri removeAssociationKeyWithPublicUserId: publicUserId completion:^(NSError * _Nullable error) {
        if (error != nil) {
            resolve(@(true));
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(listAssociationKeys:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri listAssociactionKeysWithCompletion:^(NSDictionary<NSString *,NSString *> * _Nullable associationKeys, NSError * _Nullable error) {
        if (associationKeys != nil) {
            resolve(associationKeys);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(listUniqueAccounts:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri listUniqueAccountsWithCompletion:^(NSDictionary<NSString *,NSString *> * _Nullable associationKeys, NSError * _Nullable error) {
        if (associationKeys != nil) {
            resolve(associationKeys);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(easyKeyriAuth:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id publicUserId = [data objectForKey:@"publicUserId"];
    id payload = [data objectForKey:@"payload"];

    if (![payload isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide payload" withRejecter:reject]; }
    if (![publicUserId isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide publicUserId" withRejecter:reject]; }

    dispatch_async(dispatch_get_main_queue(), ^{
        __weak typeof (self) weakSelf = self;
        [self.keyri easyKeyriAuthWithPayload:payload publicUserId:publicUserId completion:^(BOOL success, NSError * _Nullable error) {
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
    [self.keyri processLinkWithUrl:[NSURL URLWithString:urlString] payload:payload publicUserId:publicUserId completion:^(BOOL success, NSError * _Nullable error) {
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

    self.latestSession.payload = payload;

    if (isApproved) {
        [self.latestSession confirmWithCompletion:^(NSError * _Nullable error) {
            if (error == nil) {
                resolve(@(true));
            } else {
                return [self handleError:error withRejecter:reject];
            }
        }];
    } else {
        [self.latestSession denyWithCompletion:^(NSError * _Nullable error) {
            if (error == nil) {
                resolve(@(true));
            } else {
                return [self handleError:error withRejecter:reject];
            }
        }];
    }
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
