export interface KeyriInitializeOptions {
  appKey: string;
  iosPublicKey: string;
  androidPublicKey: string;
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
}

export interface KeyriModule {
  initialize: (options: KeyriInitializeOptions) => void;

  sessionLogin: (options: KeyriSessionLoginOptions) => Promise<void>;

  sessionSignup: (options: KeyriSessionSignupOptions) => Promise<void>;

  handleSessionId: (sessionId: string) => Promise<KeyriSession>;

  directLogin: <RPServerResponse = unknown>(
    username: string,
    headers?: ExtendedHeaders,
    custom?: string
  ) => Promise<RPServerResponse>;

  directSignup: <RPServerResponse = unknown>(
    username: string,
    headers?: ExtendedHeaders,
    custom?: string
  ) => Promise<RPServerResponse>;

  getAccounts: () => Promise<KeyriPublicAccount[]>;

  easyKeyriAuth: (custom?: string) => Promise<void>;

  removeAccount: (username: string, custom?: string) => Promise<void>;

  whitelabelAuth:(options: WhitelabelAuthOptions) => Promise<void>;
}
