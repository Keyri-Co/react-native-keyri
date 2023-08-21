export interface KeyriSession {
  sessionId: string;
  publicUserId: string;
  userParameters?: KeyriUserParameters;
  iPAddressMobile: string;
  iPAddressWidget: string;
  widgetOrigin: string;
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
  riskFlagString: string;
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
  flags?: KeyriFlags;
}

export interface KeyriTemplate {
  location?: string;
  issue?: string;
}

export interface KeyriUserAgent {
  name?: string;
  issue?: string;
}

export interface KeyriFlags {
  isDatacenter?: boolean;
  isNewBrowser?: boolean;
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
  initialize: (data: InitializeKeyriOptions) => Promise<boolean>;

  generateAssociationKey: (publicUserId?: string) => Promise<string>;

  getUserSignature: (publicUserId?: string, customSignedData?: string) => Promise<string>;

  listAssociationKeys: () => Promise<string[]>;

  listUniqueAccounts: () => Promise<string[]>;

  getAssociationKey: (publicUserId?: string) => Promise<string>;

  removeAssociationKey: (publicUserId: string) => Promise<boolean>;

  sendEvent: (data: SendEventOptions) => Promise<KeyriFingerprintEventResponse>;

  initiateQrSession: (options: InitiateQrSessionOptions) => Promise<KeyriSession>;

  initializeDefaultScreen: (sessionId: string, payload: string) => Promise<boolean>;

  confirmSession: (sessionId: string, payload: string) => Promise<boolean>;

  denySession: (sessionId: string, payload: string) => Promise<boolean>;

  easyKeyriAuth: (data: EasyKeyriAuthOptions) => Promise<boolean>;

  processLink: (options: ProcessLinkOptions) => Promise<boolean>;
}

export interface InitiateQrSessionOptions {
  appKey: string;
  sessionId: string;
  publicUserId?: string;
}

export interface EasyKeyriAuthOptions {
  publicUserId: string;
  appKey: string;
  publicApiKey?: string;
  serviceEncryptionKey?: string;
  blockEmulatorDetection?: boolean;
  payload: string;
}

export interface ProcessLinkOptions {
  appKey: string;
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
