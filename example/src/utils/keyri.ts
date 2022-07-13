import Keyri from 'react-native-keyri';
import { URL, URLSearchParams } from 'react-native-url-polyfill';
const BASE_URL = 'http://18.208.184.185:5000';

export function initializeKeyri() {
  Keyri.initialize({
    callbackUrl: `${BASE_URL}/users/session-mobile`,
    appKey: 'raB7SFWt27VoKqkPhaUrmWAsCJIO8Moj',
    iosPublicKey:
      'BOenio0DXyG31mAgUCwhdslelckmxzM7nNOyWAjkuo7skr1FhP7m2L8PaSRgIEH5ja9p+CwEIIKGqR4Hx5Ezam4=',
    androidPublicKey:
      'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE56eKjQNfIbfWYCBQLCF2yV6VySbHMzuc07JYCOS6juySvUWE/ubYvw9pJGAgQfmNr2n4LAQggoapHgfHkTNqbg==',
  });
}
export interface ISearchParam {
  aesKey: any;
  issuer: string;
  secret: string;
  sessionId: string;
  data: string;
}
export function parseUrlParams(url: string) {
  const search = new URLSearchParams(new URL(url).search);

  const params: ISearchParam = Array.from(search.entries()).reduce(
    (accum, [a, b]) => {
      return { ...accum, [a]: b };
    },
    {}
  );
  return params;
}
export enum ILoginType {
  custom = 'custom',
  default = 'default',
}
