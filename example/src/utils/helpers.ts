import { URL, URLSearchParams } from 'react-native-url-polyfill';
import type { ISearchParam } from './types';

export function parseUrlParams(url: string) {
  const search = new URLSearchParams(new URL(url).search);

  const params:
    | ISearchParam
    | {
        aesKey: null;
        issuer: null;
        secret: null;
        sessionId: null;
        data: null;
      } = Array.from(search.entries()).reduce((accum, [a, b]) => {
      return { ...accum, [a]: b };
    },
    { aesKey: null, issuer: null, secret: null, sessionId: null, data: null }
  );
  return params;
}
