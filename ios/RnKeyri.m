// RnKeyri.m

#import "RnKeyri.h"
@import keyri_pod;

@interface RnKeyri ()

@property (nonatomic, strong) Keyri *keyri;

@end

@implementation RnKeyri

RCT_EXPORT_MODULE()

- (instancetype)init
{
    if (self = [super init]) {
        _keyri = [[Keyri alloc] init];
    }
    
    return  self;
}

RCT_EXPORT_METHOD(initialize:(NSString *)appkey rpPublicKey:(NSString *)rpPublicKey callbackUrl:(NSString *)callbackUrl)
{
    [Keyri configureWithAppkey:appkey rpPublicKey:rpPublicKey callbackUrl:[NSURL URLWithString:callbackUrl]];
}

RCT_REMAP_METHOD(onReadSession, Id:(NSString *)sessionId onReadSessionResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri onReadSessionId:sessionId completion:^(Session * _Nullable session, NSError * _Nullable error) {
        if (!error) {
            resolve(session);
        } else {
            reject(@"Error", @"there was error during fetching session", error);
        }
    }];
}

RCT_REMAP_METHOD(keyriSignUp,
                 Username:(NSString *) username
                 service:(Service *) service
                 custom:(NSString * _Nullable) custom
                 signUpWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri signupWithUsername:username service:service custom:custom completion:^(NSError * _Nullable error) {
        if (!error) {
            resolve(@"Success, user is registered");
        } else {
            reject(@"Error", @"there was error during registration", error);
        }
    }];
}

RCT_REMAP_METHOD(keyriLogin,
                 Account:(PublicAccount *) account
                 service:(Service *) service
                 custom:(NSString * _Nullable) custom
                 loginWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri loginWithAccount:account service:service custom:custom completion:^(NSError * _Nullable error) {
        if (!error) {
            resolve(@"Success, user is logged in");
        } else {
            reject(@"Error", @"there was error during login", error);
        }
    }];
}

RCT_REMAP_METHOD(rpDirectSignUp,
                 Username:(NSString *) username
                 custom:(NSString * _Nullable) custom
                 extendedHeaders:(NSDictionary * _Nullable) extendedHeaders
                 mobileSignUpWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri mobileSignupWithUsername:username custom:custom extendedHeaders:extendedHeaders completion:^(NSDictionary<NSString *,id> * _Nullable json, NSError * _Nullable error) {
        if (!error) {
            resolve(@"Success, user is registered via mobile signUp");
        } else {
            reject(@"Error", @"there was error during mobile signUp", error);
        }
    }];
}

RCT_REMAP_METHOD(rpDirectLogin,
                 Account:(PublicAccount *) account
                 custom:(NSString * _Nullable) custom
                 extendedHeaders:(NSDictionary * _Nullable) extendedHeaders
                 mobileLoginWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri mobileLoginWithAccount:account custom:custom extendedHeaders:extendedHeaders completion:^(NSDictionary<NSString *,id> * _Nullable json, NSError * _Nullable error) {
        if (!error) {
            resolve(@"Success, user is loggedin via mobile login");
        } else {
            reject(@"Error", @"there was error during mobile login", error);
        }
    }];
}

RCT_REMAP_METHOD(accounts,
                 accountsResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.keyri accountsWithCompletion:^(NSArray<PublicAccount *> * _Nullable accounts, NSError * _Nullable error) {
        if (!error) {
            resolve(accounts);
        } else {
            reject(@"Error", @"there was error during fetching accounts", error);
        }
    }];
}

RCT_REMAP_METHOD(easyKeyriAuth,
                 custom:(NSString * _Nullable) custom
                 authWithScannerResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.keyri authWithScannerFrom:nil custom:custom completion:^(NSError * _Nullable error) {
            if (!error) {
                resolve(@"Success");
            } else {
                reject(@"Error", @"there was error during auth with scanner", error);
            }
        }];
    });
    
}


@end
