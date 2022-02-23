export interface KeyriInitializeOptions {
  appKey: string;
  publicKey: string;
  callbackUrl: string;
  allowMultipleAccounts?: boolean
}

export interface KeyriSessionLoginOptions {
  publicAccountUsername: string,
  publicAccountCustom?: string,
  sessionId: string,
  serviceId: string,
  serviceName: string,
  serviceLogo: string,
  custom?: string
}

export interface KeyriSessionSignupOptions {
  username: string,
  sessionId: string,
  serviceId: string,
  serviceName: string,
  serviceLogo: string,
  custom?: string
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

export interface KeyriModule {
  initialize: (options: KeyriInitializeOptions) => void;

  sessionLogin: (options: KeyriSessionLoginOptions) => Promise<void>;

  sessionSignup: (options: KeyriSessionSignupOptions) => Promise<void>;

  handleSessionId: (sessionId: string) => Promise<KeyriSession>;

  directLogin: <RPServerResponse = unknown>(
    publicAccountUsername: string,
    extendedHeaders?: unknown,
    publicAccountCustom?: string
  ) => Promise<RPServerResponse>;

  directSignup: <RPServerResponse = unknown>(
    username: string,
    extendedHeaders: unknown,
    custom?: string
  ) => Promise<RPServerResponse>;

  getAccounts: () => Promise<KeyriPublicAccount[]>;

  easyKeyriAuth: (custom?: string) => Promise<void>;

  removeAccount: (
    publicAccountUsername: string,
    publicAccountCustom?: string
  ) => Promise<void>;
}
