import * as React from 'react';
import RootNavigator from './navigation';
import { initializeKeyri } from './utils/keyri';

initializeKeyri();

export default function App() {
  return <RootNavigator />;
}
