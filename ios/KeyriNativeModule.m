//
//  KeyriNativeModule.m
//  Keyri
//
//  Created by Artemii Tkachuk on 18.02.2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import "KeyriNativeModule.h"
@import keyri_pod;

@interface KeyriNativeModule ()

@property (nonatomic, strong) KeyriObjC *keyri;

@end


@implementation KeyriNativeModule

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (self = [super init]) {
        _keyri = [[KeyriObjC alloc] init];
    }
    
    return  self;
}

RCT_EXPORT_METHOD(initialize:(NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve)
{
    id username = [params objectForKey:@"username"];
    id sessionId = [params objectForKey:@"sessionId"];
    id appKey = [params objectForKey:@"appKey"];
    
    if ([username isKindOfClass:[NSString class]] && [sessionId isKindOfClass:[NSString class]] && [appKey isKindOfClass:[NSString class]]) {
        Session *session = [self.keyri initializeQrSessionWithUsername:username sessionId:sessionId appKey:appKey];
#warning to be defined return values and marked it as @objc public inside Session object
        NSDictionary *resultData = @{
        };

        resolve(resultData);
    } else {
        NSLog(@"there was error during initialization of keyri sdk");
    }
}

RCT_EXPORT_METHOD(generateAssociationKey:(NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve)
{
    id username = [params objectForKey:@"username"];
    
    if ([username isKindOfClass:[NSString class]]) {
        NSString *associationKey = [self.keyri generateAssociationKeyWithUsername:username];
        NSMutableDictionary *resultData = [[NSMutableDictionary alloc] init];
        if (associationKey) {
            [resultData setValue:associationKey forKey:@"associationKey"];
        }
        
        resolve(resultData);
    } else {
        NSLog(@"there was error during generation association key");
    }
}

RCT_EXPORT_METHOD(generateUserSignature:(NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve)
{
    id username = [params objectForKey:@"username"];
    id data = [params objectForKey:@"data"];
    
    if ([username isKindOfClass:[NSString class]] && [data isKindOfClass:[NSData class]]) {
        NSString *signature = [self.keyri generateUserSignatureWithUsername:username data:data];
        
        NSMutableDictionary *resultData = [[NSMutableDictionary alloc] init];
        if (signature) {
            [resultData setValue:signature forKey:@"signature"];
        }
        
        resolve(resultData);
    } else {
        NSLog(@"there was error during generation user signature");
    }
}

RCT_EXPORT_METHOD(getAssociationKey:(NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve)
{
    id username = [params objectForKey:@"username"];
    
    if ([username isKindOfClass:[NSString class]]) {
        NSString *associationKey = [self.keyri getAssociationKeyWithUsername:username];
        
        NSMutableDictionary *resultData = [[NSMutableDictionary alloc] init];
        if (associationKey) {
            [resultData setValue:associationKey forKey:@"associationKey"];
        }
        
        resolve(resultData);
    } else {
        NSLog(@"there was error during getting association key");
    }
}

RCT_EXPORT_METHOD(listAssociactionKeysWithResolver:(RCTPromiseResolveBlock)resolve)
{
    NSDictionary *resultData = [self.keyri listAssociactionKeys];
    resolve(resultData);
}

@end
