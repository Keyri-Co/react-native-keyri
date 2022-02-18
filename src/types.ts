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
  initialize: (
    appKey: string,
    publicKey: string,
    callbackUrl: string,
    allowMultipleAccounts: boolean
  ) => void;

  handleSessionId: (sessionId: string) => Promise<KeyriSession>;

  sessionLogin: (
    publicAccountUsername: string,
    sessionId: string,
    serviceId: string,
    serviceName: string,
    serviceLogo: string,
    publicAccountCustom?: string,
    custom?: string
  ) => void;

  sessionSignup: (
    username: string,
    sessionId: string,
    serviceId: string,
    serviceName: string,
    serviceLogo: string,
    custom?: string
  ) => Promise<string>;

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
  ) => Promise<string>;
}
