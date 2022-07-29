export interface KeyriInitializeOptions {
  appKey: string;
  rpPublicKey: string;
  callbackUrl: string;
  allowMultipleAccounts?: boolean;
}

export interface KeyriSessionLoginOptions {
  publicAccountUsername: string;
  publicAccountCustom?: string;
  sessionId: string;
  serviceId: string;
  serviceName: string;
  serviceLogo: string;
  custom?: string;
}

export interface KeyriSessionSignupOptions {
  username: string;
  sessionId: string;
  serviceId: string;
  serviceName: string;
  serviceLogo: string;
  custom?: string;
}

export interface KeyriSession {
  serviceId: string;
  serviceName: string;
  serviceLogo?: string;
  username?: string;
  isNewUser: boolean;

  widgetOrigin: string;
  sessionId: string;
  iPAddressMobile: string;
  iPAddressWidget: string;
  widgetUserAgent: {
    isDesktop: boolean;
    os: string;
    browser: string;
  };
  userParameters: {
    custom: string | null;
  };
  riskAnalytics: {
    riskFlagString: string;
    riskStatus: string;
    riskAttributes: {
      isKnownAbuser: boolean;
      isIcloudRelay: boolean;
      isKnownAttacker: boolean;
      isAnonymous: boolean;
      isThreat: boolean;
      isBogon: boolean;
      blocklists: boolean;
      isDatacenter: boolean;
      isTor: boolean;
      isProxy: boolean;
    };
    geoData: {
      mobile: {
        continentCode: string;
        countryCode: string;
        city: string;
        latitude: number;
        longitude: number;
        regionCode: number;
      };
      browser: {
        city: string;
        continentCode: string;
        countryCode: string;
        latitude: number;
        longitude: number;
        regionCode: string;
      };
    };
  };
}

export interface KeyriPublicAccount {
  username: string;
  custom?: string;
}

export interface ExtendedHeaders {
  [key: string]: string;
}

export interface WhitelabelAuthOptions {
  sessionId: string;
  custom: string;
  aesKey?: string;
}

export interface KeyriModule {
  generateAssociationKey: (publicUserId: string) => Promise<string>;
  getUserSignature: (
    publicUserId?: string,
    customSignedData?: string
  ) => Promise<string>;
  listAssociationKey: () => Promise<string[]>;
  getAssociationKey: (publicUserId?: string) => Promise<string>;
  initiateQrSession: (
    options: InitiateQrSessionOptions
  ) => Promise<KeyriSession>;
  initializeDefaultScreen: (
    sessionId: string,
    payload: string
  ) => Promise<boolean>;
  confirmSession: (sessionId: string, payload: string) => Promise<boolean>;
  denySession: (sessionId: string, payload: string) => Promise<boolean>;
  easyKeyriAuth: (data: EasyKeyriAuthOptions) => Promise<string>;
}
export interface InitiateQrSessionOptions {
  appKey: string;
  sessionId: string;
  publicUserId?: string;
}
export interface EasyKeyriAuthOptions {
  publicUserId: string;
  appKey: string;
  payload: string;
}
