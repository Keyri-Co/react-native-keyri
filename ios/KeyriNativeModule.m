//
//  KeyriNativeModule.m
//  Keyri
//
//  Created by Artemii Tkachuk on 18.02.2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import "KeyriNativeModule.h"
@import keyri;

NSString *const KeyriNativeModuleDomain;

enum {
    KeyriNativeModuleInitializeError = 1000,
    KeyriNativeModuleGenerateAssociationKeyError,
    KeyriNativeModuleGetUserSignatureError,
    KeyriNativeModuleGetAssociationKeyError,
    KeyriNativeModuleEasyKeyriAuthError,
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
        Session *session = [self.keyri initializeQrSessionWithUsername:publicUserId sessionId:sessionId appKey:appKey];
        [self.sessions addObject:session];
        resolve(@"Success");
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
    if ([publicUserId isKindOfClass:[NSString class]]) {
        NSString *associationKey = [self.keyri getAssociationKeyWithUsername:publicUserId];
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

RCT_EXPORT_METHOD(listAssociationKeyWithResolver:(RCTPromiseResolveBlock)resolve)
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

RCT_EXPORT_METHOD(confirmSession:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve)
{
    [self finishSession:sessionId payload:payload isApproved:YES resolver:resolve];
}

RCT_EXPORT_METHOD(denySession:(NSString *)sessionId payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve)
{
    [self finishSession:sessionId payload:payload isApproved:NO resolver:resolve];
}

- (void)finishSession:(NSString *)sessionId payload:(NSString *)payload isApproved:(BOOL)isApproved resolver:(RCTPromiseResolveBlock)resolve {
    NSString *result;
    for (Session *session in self.sessions) {
        if (session.sessionId == sessionId) {
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

@end
