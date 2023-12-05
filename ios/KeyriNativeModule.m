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
@property (nonatomic, strong) Session *activeSession;

@end

@implementation KeyriNativeModule

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (self = [super init]) {
        self.keyri = [[KeyriObjC alloc] init];
        self.activeSession = [[Session alloc] init];
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
    id appKey = data[@"appKey"];
    id publicApiKeyValue = data[@"publicApiKey"];
    id serviceEncryptionKeyValue = data[@"serviceEncryptionKey"];
    id blockEmulatorDetectionValue = data[@"blockEmulatorDetection"];

    if (appKey == nil || ![appKey isKindOfClass:[NSString class]]) {
        return [self handleErrorText: @"You need to provide appKey" withRejecter:reject];
    }

    BOOL blockEmulatorDetection = YES;

    if (blockEmulatorDetectionValue != nil || [blockEmulatorDetectionValue isKindOfClass:[NSNumber class]]) {
        blockEmulatorDetection = [blockEmulatorDetectionValue boolValue];
    }

    NSString *publicApiKey = [publicApiKeyValue isKindOfClass:[NSString class]] ? publicApiKeyValue : nil;
    NSString *serviceEncryptionKey = [serviceEncryptionKeyValue isKindOfClass:[NSString class]] ? serviceEncryptionKeyValue : nil;

    [self.keyri initializeKeyriWithAppKey:publicApiKey publicApiKey:publicApiKey serviceEncryptionKey:serviceEncryptionKey blockEmulatorDetection:blockEmulatorDetection];
    resolve(@(YES));
}

RCT_EXPORT_METHOD(easyKeyriAuth:(NSString *)payload publicUserId:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (![payload isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide payload" withRejecter:reject]; }

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

RCT_EXPORT_METHOD(generateAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri generateAssociationKeyWithPublicUserId:publicUserId completion:^(NSString * _Nullable generatedKey, NSError * _Nullable error) {
        if (generatedKey != nil) {
            resolve(generatedKey);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(generateUserSignature:(NSString *)data publicUserId:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSData *dataToSign = [data dataUsingEncoding:NSUTF8StringEncoding];

    if (![dataToSign isKindOfClass:[NSData class]]) {
        return [self handleErrorText:@"You need to provide data" withRejecter:reject];
    }

    [self.keyri generateUserSignatureWithPublicUserId:publicUserId data:dataToSign completion:^(NSString * _Nullable signature, NSError * _Nullable error) {
        if (signature != nil) {
            resolve(signature);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(listAssociationKeys:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri listAssociationKeysWithCompletion:^(NSDictionary<NSString *,NSString *> * _Nullable associationKeys, NSError * _Nullable error) {
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

RCT_EXPORT_METHOD(getAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri getAssociationKeyWithPublicUserId:publicUserId completion:^(NSString * _Nullable associationKey, NSError * _Nullable error) {
        if (associationKey != nil) {
            resolve(associationKey);
        } else {
            return [self handleError:error withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(removeAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri removeAssociationKeyWithPublicUserId:publicUserId completion:^(NSError * _Nullable error) {
        if (error != nil) {
            return [self handleError:error withRejecter:reject];
        }

        resolve(@(true));
    }];
}

RCT_EXPORT_METHOD(sendEvent:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id publicUserIdValue = [data objectForKey:@"publicUserId"];
    id eventType = [data objectForKey:@"eventType"];
    id successValue = [data objectForKey:@"success"];

    NSString *publicUserId = [publicUserIdValue isKindOfClass:[NSString class]] ? publicUserIdValue : nil;

    BOOL success = NO;

    if (successValue != nil || [successValue isKindOfClass:[NSNumber class]]) {
        success = [successValue boolValue];
    } else {
        return [self handleErrorText:@"You need to provide success" withRejecter:reject];
    }

    if (eventType == nil || ![eventType isKindOfClass:[NSString class]]) {
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

RCT_EXPORT_METHOD(initiateQrSession:(NSString *)sessionId publicUserId:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    __weak typeof (self) weakSelf = self;
    [self.keyri initiateQrSessionWithSessionId:sessionId publicUserId:publicUserId completion:^(Session * _Nullable session, NSError * _Nullable error) {
        typeof (self) strongSelf = weakSelf;

        if (error != nil) {
            return [strongSelf handleError:error withRejecter:reject];
        }

        if (session != nil) {
            strongSelf.activeSession = session;
            NSDictionary *dict = [self dictionaryWithPropertiesOfObject:session];
            resolve(dict);
        } else {
            [self handleErrorText:@"Session not found" withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(login:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri loginWithPublicUserId:publicUserId completion:^(LoginObject * _Nullable loginObject, NSError * _Nullable error) {
        if (error != nil) {
            return [self handleError:error withRejecter:reject];
        }

        if (loginObject != nil) {
            resolve([self dictionaryWithPropertiesOfObject:loginObject]);
        } else {
            return [self handleErrorText:@"LoginObject is nil" withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(register:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.keyri registerWithPublicUserId:publicUserId completion:^(RegisterObject * _Nullable registerObject, NSError * _Nullable error) {
        if (error != nil) {
            return [self handleError:error withRejecter:reject];
        }

        if (registerObject != nil) {
            resolve([self dictionaryWithPropertiesOfObject:registerObject]);
        } else {
            return [self handleErrorText:@"RegisterObject is nil" withRejecter:reject];
        }
    }];
}

RCT_EXPORT_METHOD(initializeDefaultConfirmationScreen:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (self.activeSession == nil) {
        return [self handleErrorText:@"Session not found" withRejecter:reject];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.keyri initializeDefaultConfirmationScreenWithSession:self.activeSession payload:payload completion:^(BOOL isApproved, NSError * _Nullable error) {
            if (isApproved) {
                resolve(@(true));
            } else {
                if (![error isEqual:nil]) {
                    if ([error.localizedDescription isEqualToString:@"Denied by user"]) {
                        resolve(@(false));
                    } else {
                        return [self handleError:error withRejecter:reject];
                    }
                }
            }
        }];
    });
}

RCT_EXPORT_METHOD(processLink:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id urlString = [data objectForKey:@"url"];
    id publicUserIdValue = [data objectForKey:@"publicUserId"];
    id payload = [data objectForKey:@"payload"];

    NSString *publicUserId = [publicUserIdValue isKindOfClass:[NSString class]] ? publicUserIdValue : nil;

    if (payload == nil || ![payload isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide payload" withRejecter:reject]; }
    if (urlString == nil || ![urlString isKindOfClass:[NSString class]]) { return [self handleErrorText:@"You need to provide url" withRejecter:reject]; }

    __weak typeof (self) weakSelf = self;
    [self.keyri processLinkWithUrl:[NSURL URLWithString:urlString] payload:payload publicUserId:publicUserId completion:^(BOOL success, NSError * _Nullable error) {
        typeof (self) strongSelf = weakSelf;
        if (error != nil) {
            return [strongSelf handleError:error withRejecter:reject];
        }

        resolve(@(success));
    }];
}

RCT_EXPORT_METHOD(confirmSession:(NSString *)payload trustNewBrowser:(BOOL *)trustNewBrowser resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self finishSession:payload isApproved:YES trustNewBrowser:trustNewBrowser resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(denySession:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self finishSession:payload isApproved:NO trustNewBrowser:NO resolver:resolve rejecter:reject];
}

- (void)finishSession:(NSString *)payload isApproved:(BOOL)isApproved trustNewBrowser:(BOOL)trustNewBrowser resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    if (isApproved) {
        [self.activeSession confirmWithPayload:payload trustNewBrowser:trustNewBrowser completion:^(NSError * _Nullable error) {
            if (error == nil) {
                resolve(@(true));
            } else {
                return [self handleError:error withRejecter:reject];
            }
        }];
    } else {
        [self.activeSession denyWithPayload:payload completion:^(NSError * _Nullable error) {
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
