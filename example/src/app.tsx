import * as React from 'react';
import RootNavigator from './navigation';

import { AppLinkContext, LinkContext } from './context/linking-context';
import { AppSessionContext, SessionContext } from './context/session-context';
export default function App() {
  return (
    <AppLinkContext.Provider value={LinkContext()}>
      <AppSessionContext.Provider value={SessionContext()}>
        <RootNavigator />
      </AppSessionContext.Provider>
    </AppLinkContext.Provider>
  );
}
