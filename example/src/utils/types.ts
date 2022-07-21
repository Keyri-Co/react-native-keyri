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
