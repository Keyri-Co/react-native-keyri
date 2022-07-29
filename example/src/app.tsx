import * as React from 'react';
import RootNavigator from './navigation';
import Toast from 'react-native-easy-toast';

import { AppLinkContext, LinkContext } from './context/linking-context';
import { AppSessionContext, SessionContext } from './context/session-context';
import toast from './components/toast';
export default function App() {
  return (
    <AppLinkContext.Provider value={LinkContext()}>
      <AppSessionContext.Provider value={SessionContext()}>
        <RootNavigator />
        <Toast ref={toast.ref} />
      </AppSessionContext.Provider>
    </AppLinkContext.Provider>
  );
}
