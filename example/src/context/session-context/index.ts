/* eslint-disable @typescript-eslint/no-empty-function */
import { createContext, useState, useMemo } from 'react';
import type { KeyriSession } from 'react-native-keyri';
import type { ISessionContext } from 'example/src/utils/types';
export function SessionContext() {
  const [activeSession, setActiveSession] = useState<KeyriSession | null>(null);
  const [activeSessionId, setActiveSessionId] = useState<string>('');

  const session: ISessionContext = useMemo(
    () => ({
      activeSession,
      activeSessionId,
      setActiveSession,
      setActiveSessionId,
    }),
    [activeSession, activeSessionId]
  );

  return session;
}
export const AppSessionContext = createContext({
  activeSession: null,
  activeSessionId: '',
  setActiveSession: (keyriSession: KeyriSession | null) => {},
  setActiveSessionId: (sessionId: string) => {},
});
