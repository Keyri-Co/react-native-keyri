import type { KeyriSession } from 'react-native-keyri';
export enum ILoginType {
  custom = 'custom',
  default = 'default',
}
export interface ISearchParam {
  aesKey: string | null;
  issuer: string | null;
  secret: string | null;
  sessionId: string | null;
  data: string | null;
}
export enum IWidgetTypes {
  laptop = 'laptop',
  mobile = 'mobile',
  os = 'os',
}
export interface ISessionContext {
  activeSession: KeyriSession | null;
  activeSessionId: string;
  setActiveSession: (session: KeyriSession | null) => void;
  setActiveSessionId: (sessionId: string) => void;
}
