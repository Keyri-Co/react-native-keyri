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

@property (nonatomic, strong) Keyri *keyri;

@end


@implementation KeyriNativeModule

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (self = [super init]) {
        _keyri = [[Keyri alloc] init];
    }
    
    return  self;
}

RCT_EXPORT_METHOD(initialize:(NSDictionary *)data)
{
    id appKey = [data objectForKey:@"appKey"];
    id publicKey = [data objectForKey:@"publicKey"];
    id callbackUrl = [data objectForKey:@"callbackUrl"];

    if ([appKey isKindOfClass:[NSString class]] && [publicKey isKindOfClass:[NSString class]] && [callbackUrl isKindOfClass:[NSString class]]) {
        [Keyri initializeWithAppkey:appKey rpPublicKey:publicKey callbackUrl:[NSURL URLWithString:callbackUrl]];
    } else {
        NSLog(@"there was error during initialization of keyri sdk");
    }
}

RCT_REMAP_METHOD(handleSessionId,
                 Id:(NSString *)sessionId
                 onReadSessionResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri handleSessionId:sessionId completion:^(Session * _Nullable session, NSError * _Nullable error) {
        if (!error) {
            NSMutableDictionary *resultData = [NSMutableDictionary new];
            [resultData setValue:session.service.id forKey:@"serviceId"];
            [resultData setValue:session.service.name forKey:@"serviceName"];
            [resultData setValue:session.service.logo forKey:@"serviceLogo"];
            [resultData setValue:@(session.isNewUser) forKey:@"isNewUser"];
            
            if (session.username) {
                [resultData setValue:session.username forKey:@"username"];
            }
            resolve(resultData);
        } else {
            reject(@"Error", @"there was error during fetching session", error);
        }
    }];
}

RCT_REMAP_METHOD(sessionSignup,
                 Data:(NSDictionary *)data
                 signUpWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *serviceId = [NSString stringWithFormat:@"%@", [data objectForKey:@"serviceId"]];
    NSString *serviceName = [NSString stringWithFormat:@"%@", [data objectForKey:@"serviceName"]];
    NSString *serviceLogo = [NSString stringWithFormat:@"%@", [data objectForKey:@"serviceLogo"]];
    NSString *username = [NSString stringWithFormat:@"%@", [data objectForKey:@"username"]];
    NSString *custom = [NSString stringWithFormat:@"%@", [data objectForKey:@"custom"]];
    
    Service *service = [[Service alloc] initWithId:serviceId name:serviceName logo:serviceLogo];
    [self.keyri sessionSignupWithUsername:username service:service custom:custom completion:^(NSError * _Nullable error) {
        if (!error) {
            resolve(@"Success, user is registered");
        } else {
            reject(@"Error", @"there was error during registration", error);
        }
    }];
}

RCT_REMAP_METHOD(sessionLogin,
                 Data:(NSDictionary *)data
                 loginWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *publicAccountUsername = [NSString stringWithFormat:@"%@", [data objectForKey:@"publicAccountUsername"]];
    NSString *serviceId = [NSString stringWithFormat:@"%@", [data objectForKey:@"serviceId"]];
    NSString *serviceName = [NSString stringWithFormat:@"%@", [data objectForKey:@"serviceName"]];
    NSString *serviceLogo = [NSString stringWithFormat:@"%@", [data objectForKey:@"serviceLogo"]];
    NSString *custom = [NSString stringWithFormat:@"%@", [data objectForKey:@"custom"]];
    NSString *publicAccountCustom = [NSString stringWithFormat:@"%@", [data objectForKey:@"publicAccountCustom"]];
    

    PublicAccount *account = [[PublicAccount alloc] initWithUsername:publicAccountUsername custom:publicAccountCustom];
    Service *service = [[Service alloc] initWithId:serviceId name:serviceName logo:serviceLogo];
    [self.keyri sessionLoginWithAccount:account service:service custom:custom completion:^(NSError * _Nullable error) {
        if (!error) {
            resolve(@"Success, user is logged in");
        } else {
            reject(@"Error", @"there was error during login", error);
        }
    }];
}

RCT_REMAP_METHOD(directSignup,
                 Username:(NSString *) username
                 extendedHeaders:(NSDictionary * _Nullable) extendedHeaders
                 custom:(NSString * _Nullable) custom
                 mobileSignUpWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri directSignupWithUsername:username custom:custom extendedHeaders:extendedHeaders completion:^(AuthMobileResponse * _Nullable response, NSError * _Nullable error) {
        if (!error) {
            NSDictionary *resultData = @{
                @"userId": response.user.id,
                @"userName": response.user.name,
                @"token": response.token,
                @"refreshToken": response.refreshToken
            };

            resolve(resultData);
        } else {
            reject(@"Error", @"there was error during mobile signUp", error);
        }
    }];
}

RCT_REMAP_METHOD(directLogin,
                 PublicAccountUsername:(NSString *) publicAccountUsername
                 extendedHeaders:(NSDictionary * _Nullable) extendedHeaders
                 publicAccountCustom:(NSString * _Nullable) publicAccountCustom
                 mobileLoginWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    PublicAccount *account = [[PublicAccount alloc] initWithUsername:publicAccountUsername custom:publicAccountCustom];
    [self.keyri directLoginWithAccount:account custom:publicAccountCustom extendedHeaders:extendedHeaders completion:^(AuthMobileResponse * _Nullable response, NSError * _Nullable error) {
        if (!error) {
            NSDictionary *resultData = @{
                @"userId": response.user.id,
                @"userName": response.user.name,
                @"token": response.token,
                @"refreshToken": response.refreshToken
            };

            resolve(resultData);
        } else {
            reject(@"Error", @"there was error during mobile login", error);
        }
    }];
}

RCT_REMAP_METHOD(getAccounts,
                 accountsResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri getAccountsWithCompletion:^(NSArray<PublicAccount *> * _Nullable accounts, NSError * _Nullable error) {
        if (!error) {
            NSMutableArray *resultData = [[NSMutableArray alloc] init];
            for (PublicAccount *account in accounts) {
                NSMutableDictionary *acc = [[NSMutableDictionary alloc] init];
                if (account.username) {
                    [acc setValue:account.username forKey:@"username"];
                    if (account.custom) {
                        [acc setValue:account.custom forKey:@"custom"];
                    } else {
                        [acc setValue:@"" forKey:@"custom"];
                    }
                    [resultData addObject:acc];
                }
            }
            resolve(resultData);
        } else {
            reject(@"Error", @"there was error during fetching accounts", error);
        }
    }];
}

RCT_REMAP_METHOD(removeAccount,
                 PublicAccountUsername:(NSString *) publicAccountUsername
                 publicAccountCustom:(NSString * _Nullable) publicAccountCustom
                 accountsResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    PublicAccount *account = [[PublicAccount alloc] initWithUsername:publicAccountUsername custom:publicAccountCustom];
    [self.keyri removeAccountWithAccount:account completion:^(NSError * _Nullable error) {
        if (!error) {
            resolve(@"Account removed");
        } else {
            reject(@"Error", @"there was error during removing account", error);
        }
    }];
}

RCT_REMAP_METHOD(easyKeyriAuth,
                 custom:(NSString * _Nullable) custom
                 authWithScannerResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.keyri easyKeyriAuthFrom:nil custom:custom completion:^(NSError * _Nullable error) {
            if (!error) {
                resolve(@"Successfully authenticated");
            } else {
                reject(@"Error", @"there was error during auth with scanner", error);
            }
        }];
    });
}

RCT_REMAP_METHOD(whitelabelAuth,
                 Data:(NSDictionary *)data
                 whitelabelAuthResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *sessionId = [NSString stringWithFormat:@"%@", [data objectForKey:@"sessionId"]];
        NSString *custom = [NSString stringWithFormat:@"%@", [data objectForKey:@"custom"]];
        [self.keyri whitelabelAuthWithSessionId:sessionId custom:custom completion:^(NSError * _Nullable error) {
            if (!error) {
                resolve(@"Successfully whitelabel authenticated");
            } else {
                reject(@"Error", @"there was error during whitelabel auth", error);
            }
        }];
    });
}

@end



