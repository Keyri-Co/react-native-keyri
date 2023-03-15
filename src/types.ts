export interface KeyriSession {
  sessionId: string;
  publicUserId: string;
  userParameters?: KeyriUserParameters;
  iPAddressMobile: string;
  iPAddressWidget: string;
  widgetOrigin: string;
  widgetUserAgent?: KeyriWidgetUserAgent;
  riskAnalytics?: KeyriRiskAnalytics;
}

export interface KeyriUserParameters {
  base64EncodedData?: string;
}

export interface KeyriWidgetUserAgent {
  electronVersion: string;
  isDesktop: boolean;
  os: string;
  browser: string;
  isAuthoritative: boolean;
  isWindows: boolean;
  source: string;
  version: string;
  platform: string;
  isChrome: boolean;
}

export interface KeyriRiskAnalytics {
  riskStatus?: string;
  riskFlagString: string;
  geoData: {
    mobile?: KeyriGeoData;
    browser?: KeyriGeoData;
  };
}

export interface KeyriGeoData {
  continentCode: string;
  countryCode: string;
  city: string;
  latitude: number;
  longitude: number;
  regionCode: string;
}

export interface KeyriModule {
  initKeyri: (appKey: string) => Promise<void>;

  generateAssociationKey: (publicUserId?: string) => Promise<string>;

  getUserSignature: (publicUserId?: string, customSignedData?: string) => Promise<string>;

  listAssociationKey: () => Promise<string[]>;

  listUniqueAccounts: () => Promise<string[]>;

  getAssociationKey: (publicUserId?: string) => Promise<string>;

  removeAssociationKey: (publicUserId?: string) => Promise<void>;

  initiateQrSession: (options: InitiateQrSessionOptions) => Promise<KeyriSession>;

  initializeDefaultScreen: (sessionId: string, payload: string) => Promise<boolean>;

  confirmSession: (sessionId: string, payload: string) => Promise<boolean>;

  denySession: (sessionId: string, payload: string) => Promise<boolean>;

  easyKeyriAuth: (data: EasyKeyriAuthOptions) => Promise<string>;

  processLink: (options: ProcessLinkOptions) => Promise<boolean>;
}

export interface InitiateQrSessionOptions {
  appKey: string;
  sessionId: string;
  publicUserId?: string;
}

export interface EasyKeyriAuthOptions {
  publicUserId?: string;
  appKey: string;
  payload: string;
}

export interface ProcessLinkOptions {
  appKey: string;
  url: string;
  payload: string;
  publicUserId?: string;
}
