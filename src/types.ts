export interface KeyriSession {
  sessionId: string;
  publicUserId: string;
  userParameters?: KeyriUserParameters;
  iPAddressMobile: string;
  iPAddressWidget: string;
  widgetOrigin?: string;
  widgetUserAgent?: KeyriWidgetUserAgent;
  riskAnalytics?: KeyriRiskAnalytics;
  mobileTemplateResponse?: KeyriMobileTemplateResponse;
}

export interface KeyriFingerprintEventResponse {
  apiCiphertextSignature: string;
  publicEncryptionKey: string;
  ciphertext: string;
  iv: string;
  salt: string;
}

export interface KeyriUserParameters {
  base64EncodedData?: string;
}

export interface KeyriWidgetUserAgent {
  os: string;
  browser: string;
}

export interface KeyriRiskAnalytics {
  riskStatus?: string;
  riskFlagString?: string;
  geoData: {
    mobile?: KeyriGeoData;
    browser?: KeyriGeoData;
  };
}

export interface KeyriMobileTemplateResponse {
  title: string;
  message?: string;
  widget?: KeyriTemplate;
  mobile?: KeyriTemplate;
  userAgent?: KeyriUserAgent;
}

export interface KeyriTemplate {
  location?: string;
  issue?: string;
}

export interface KeyriUserAgent {
  name?: string;
  issue?: string;
}

export interface KeyriGeoData {
  continentCode?: string;
  countryCode?: string;
  city?: string;
  regionCode?: string;
}

export interface KeyriModule {
  initialize: (data: InitializeKeyriOptions) => Promise<boolean>;

  generateAssociationKey: (publicUserId?: string) => Promise<string>;

  generateUserSignature: (data: string, publicUserId?: string) => Promise<string>;

  listAssociationKeys: () => Promise<string[]>;

  listUniqueAccounts: () => Promise<string[]>;

  getAssociationKey: (publicUserId?: string) => Promise<string>;

  removeAssociationKey: (publicUserId: string) => Promise<boolean>;

  sendEvent: (data: SendEventOptions) => Promise<KeyriFingerprintEventResponse>;

  initiateQrSession: (sessionId: string, publicUserId?: string) => Promise<KeyriSession>;

  initializeDefaultConfirmationScreen: (payload: string) => Promise<boolean>;

  confirmSession: (payload: string, trustNewBrowser?: boolean) => Promise<boolean>;

  denySession: (payload: string) => Promise<boolean>;

  easyKeyriAuth: (payload: string, publicUserId?: string) => Promise<boolean>;

  processLink: (options: ProcessLinkOptions) => Promise<boolean>;
}

export interface ProcessLinkOptions {
  url: string;
  payload: string;
  publicUserId?: string;
}

export interface InitializeKeyriOptions {
  appKey: string;
  publicApiKey?: string;
  serviceEncryptionKey?: string;
  blockEmulatorDetection?: boolean;
}

export interface SendEventOptions {
  publicUserId?: string;
  eventType: EventType;
  success: boolean;
}

export enum EventType {
  Visits = 'visits',
  Login = 'login',
  Signup = 'signup',
  AttachNewDevice = 'attach_new_device',
  EmailChange = 'email_change',
  ProfileUpdate = 'profile_update',
  PasswordReset = 'password_reset',
  Withdrawal = 'withdrawal',
  Deposit = 'deposit',
  Purchase = 'purchase',
}
