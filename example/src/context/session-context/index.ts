import { createContext, useState, useMemo } from 'react';
import type { KeyriSession } from 'react-native-keyri';
import type { ISessionContext } from '../../utils/types';
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
export const AppSessionContext = createContext<ISessionContext>({
  activeSession: null,
  activeSessionId: '',
  setActiveSession: (_keyriSession: KeyriSession | null) => {
    console.log(_keyriSession);
  },
  setActiveSessionId: (_sessionId: string) => {
    console.log(_sessionId);
  },
});
