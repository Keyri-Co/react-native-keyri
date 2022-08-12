//
//  KeyriNativeModule.m
//  Keyri
//
//  Created by Artemii Tkachuk on 18.02.2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import "KeyriNativeModule.h"
#import <objc/runtime.h>
@import keyri_pod;

NSString *const KeyriNativeModuleDomain = @"KeyriNativeModule";

enum {
    KeyriNativeModuleInitializeError = 1000,
    KeyriNativeModuleDefaultScreenInitializeError,
    KeyriNativeModuleGenerateAssociationKeyError,
    KeyriNativeModuleGetUserSignatureError,
    KeyriNativeModuleGetAssociationKeyError,
    KeyriNativeModuleEasyKeyriAuthError,
    KeyriNativeModuleProcessLinkError,
};

@interface KeyriNativeModule ()

@property (nonatomic, strong) KeyriObjC *keyri;
@property (nonatomic, strong) NSMutableArray<Session *> *sessions;

@end


@implementation KeyriNativeModule

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (self = [super init]) {
        _keyri = [[KeyriObjC alloc] init];
        _sessions = [NSMutableArray array];
    }
    
    return  self;
}

RCT_EXPORT_METHOD(initiateQrSession:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id publicUserId = [data objectForKey:@"publicUserId"];
    id sessionId = [data objectForKey:@"sessionId"];
    id appKey = [data objectForKey:@"appKey"];
    
    if ([publicUserId isKindOfClass:[NSString class]] && [sessionId isKindOfClass:[NSString class]] && [appKey isKindOfClass:[NSString class]]) {
        __weak typeof (self) weakSelf = self;
        [self.keyri initializeQrSessionWithUsername:publicUserId sessionId:sessionId appKey:appKey completion:^(Session * _Nullable session, NSError * _Nullable error) {
            typeof (self) strongSelf = weakSelf;
            if (session != nil) {
                [strongSelf.sessions addObject:session];
                NSDictionary *dict = [self dictionaryWithPropertiesOfObject:session];
                resolve(dict);
            } else {
                NSString *errorText = @"there was error during initialization of keyri sdk";
                NSLog(@"%@", errorText);
                NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
                reject(
                       @"Error",
                       errorText,
                       [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleInitializeError userInfo:details]
                );
            }
        }];
    } else {
        NSString *errorText = @"there was error during initialization of keyri sdk";
        NSLog(@"%@", errorText);
        NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
        reject(
               @"Error",
               errorText,
               [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleInitializeError userInfo:details]
        );
    }
}

RCT_EXPORT_METHOD(initializeDefaultScreen:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    Session *session;
    for (Session *_session in self.sessions) {
        if ([_session.sessionId isEqualToString:sessionId]) {
            session = _session;
            break;
        }
    }
    
    if (session) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.keyri initializeDefaultScreenWithSessionId:session.sessionId completion:^(BOOL isApproved) {
                resolve(@(isApproved));
            }];
        });
    } else {
        NSString *errorText = @"there was error during default screen initialization of keyri sdk";
        NSLog(@"%@", errorText);
        NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
        reject(
               @"Error",
               errorText,
               [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleDefaultScreenInitializeError userInfo:details]
        );
    }
}

RCT_EXPORT_METHOD(generateAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([publicUserId isKindOfClass:[NSString class]]) {
        NSString *generatedKey = [self.keyri generateAssociationKeyWithUsername:publicUserId];
        resolve(generatedKey);
    } else {
        NSString *errorText = @"there was error during generation association key";
        NSLog(@"%@", errorText);
        NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
        reject(
               @"Error",
               errorText,
               [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleGenerateAssociationKeyError userInfo:details]
        );
    }
}

RCT_EXPORT_METHOD(getUserSignature:(NSString *)publicUserId customSignedData:(NSString *)customSignedData resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSData *data = [customSignedData dataUsingEncoding:NSUTF8StringEncoding];
    if ([publicUserId isKindOfClass:[NSString class]] && [data isKindOfClass:[NSData class]]) {
        NSString *signature = [self.keyri generateUserSignatureWithUsername:publicUserId data:data];
        resolve(signature);
    } else {
        NSString *errorText = @"there was error during generation user signature";
        NSLog(@"%@", errorText);
        NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
        reject(
               @"Error",
               errorText,
               [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleGetUserSignatureError userInfo:details]
        );
    }
}

RCT_EXPORT_METHOD(getAssociationKey:(NSString *)publicUserId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *associationKey = [self.keyri getAssociationKeyWithUsername:publicUserId];
    if ([publicUserId isKindOfClass:[NSString class]]) {
        resolve(associationKey);
    } else {
        NSString *errorText = @"there was error during getting association key";
        NSLog(@"%@", errorText);
        NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
        reject(
               @"Error",
               errorText,
               [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleGetAssociationKeyError userInfo:details]
        );
    }
}

RCT_EXPORT_METHOD(listAssociationKey:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSDictionary *associationKeys = [self.keyri listAssociactionKeys];
    resolve(associationKeys);
}

RCT_EXPORT_METHOD(easyKeyriAuth:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id publicUserId = [data objectForKey:@"publicUserId"];
    id payload = [data objectForKey:@"payload"];
    id appKey = [data objectForKey:@"appKey"];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *errorText = @"there was error during easy keyri auth";
        if ([publicUserId isKindOfClass:[NSString class]] && [payload isKindOfClass:[NSString class]] && [appKey isKindOfClass:[NSString class]]) {
            [self.keyri easyKeyriAuthWithPublicUserId:publicUserId appKey:appKey payload:payload completion:^(BOOL success) {
                if (success) {
                    resolve(@(success));
                } else {
                    NSLog(@"%@", errorText);
                    NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
                    reject(
                           @"Error",
                           errorText,
                           [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleEasyKeyriAuthError userInfo:details]
                    );
                }
            }];
        } else {
            NSLog(@"%@", errorText);
            NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
            reject(
                   @"Error",
                   errorText,
                   [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleEasyKeyriAuthError userInfo:details]
            );
        }
    });
}

RCT_EXPORT_METHOD(processLink:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    id urlString = [data objectForKey:@"url"];
    id publicUserId = [data objectForKey:@"publicUserId"];
    id appKey = [data objectForKey:@"appKey"];
    id payload = [data objectForKey:@"payload"];
    
    if (
        [publicUserId isKindOfClass:[NSString class]] &&
        [payload isKindOfClass:[NSString class]] &&
        [appKey isKindOfClass:[NSString class]] &&
        [urlString isKindOfClass:[NSString class]] &&
        [NSURL URLWithString:urlString] != nil
    ) {
        [self.keyri processLinkWithUrl:[NSURL URLWithString:urlString] publicUserId:publicUserId appKey:appKey payload:payload completion:^(BOOL success) {
            resolve(@(success));
        }];
    } else {
        NSString *errorText = @"there was error during process link";
        NSLog(@"%@", errorText);
        NSDictionary *details = @{ NSLocalizedDescriptionKey : errorText };
        reject(
               @"Error",
               errorText,
               [NSError errorWithDomain:KeyriNativeModuleDomain code:KeyriNativeModuleProcessLinkError userInfo:details]
        );
    }
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
