import * as React from 'react';
import RootNavigator from './navigation';
import Toast from 'react-native-easy-toast';

import { AppLinkContext, LinkContext } from './context/linking-context';
import toast from './services/toast';
export default function App() {
  return (
    <AppLinkContext.Provider value={LinkContext()}>
        <RootNavigator />
        <Toast ref={toast.ref} />
    </AppLinkContext.Provider>
  );
}
