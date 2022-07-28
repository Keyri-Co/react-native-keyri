import { createContext, useMemo, useState } from 'react';
import { Linking } from 'react-native';

import type { IAppLinkContext } from '../../utils/types';

export function LinkContext() {
  const [deepLink, setDeepLink] = useState<string>('');

  const linkObj = useMemo(
    () => ({
      deepLink,
      setDeepLink,
    }),
    [deepLink]
  );
  const getInitialUrl = async () => {
    const initialUrl = await Linking.getInitialURL();
    if (initialUrl) {
      setDeepLink(initialUrl);
    }
  };
  getInitialUrl();
  return linkObj;
}
export const AppLinkContext = createContext<IAppLinkContext>({
  deepLink: '',
  setDeepLink: (_link: string) => {
    console.log(_link);
  },
});
