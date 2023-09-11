import { createContext, useEffect, useMemo, useState } from 'react';
import { Alert, Linking } from 'react-native';
import Keyri from 'react-native-keyri';

import type { IAppLinkContext } from '../../utils/types';

export function LinkContext() {
  const [deepLink, setDeepLink] = useState<string>('');

  useEffect(() => {
    Linking.getInitialURL().then((url) => {
      if (!url) return;

      setDeepLink(url);
    });
  }, []);

  useEffect(() => {
    if (!deepLink) return;

    const onPressPrecess = () => {
      Keyri.processLink({
        url: deepLink,
        publicUserId: 'user@email',
        payload: 'Custom payload',
      })
        .then(() => Alert.alert('Success', 'The link has been processed'))
        .catch((e) => Alert.alert('Error', e?.message));
    };

    Alert.alert('Linking', 'Do you want to process a link?', [
      { text: 'Yes', onPress: onPressPrecess },
      { text: 'No', style: 'cancel' },
    ]);

    setDeepLink('');
  }, [deepLink]);

  return useMemo(
    () => ({
      deepLink,
      setDeepLink,
    }),
    [deepLink]
  );
}

export const AppLinkContext = createContext<IAppLinkContext>({
  deepLink: '',
  setDeepLink: (_link: string) => {
    console.log(_link);
  },
});
