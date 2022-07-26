/* eslint-disable @typescript-eslint/no-empty-function */
import { createContext, useMemo, useState } from 'react';

export function LinkContext() {
  const [deepLink, setDeepLink] = useState<string>('');

  const linkObj = useMemo(
    () => ({
      deepLink,
      setDeepLink,
    }),
    [deepLink]
  );

  return linkObj;
}
export const AppLinkContext = createContext({
  deepLink: '',
  setDeepLink: (link: string) => {},
});
