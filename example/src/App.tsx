import * as React from 'react';

import { Button, NativeModules, StyleSheet, View } from 'react-native';

const { KeyriNativeModule } = NativeModules;

const APP_KEY = 'raB7SFWt27VoKqkPhaUrmWAsCJIO8Moj';
const PUBLIC_KEY =
  'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE56eKjQNfIbfWYCBQLCF2yV6VySbHMzuc07JYCOS6juySvUWE/ubYvw9pJGAgQfmNr2n4LAQggoapHgfHkTNqbg=="';
const BASE_URL = 'http://18.208.184.185:5000';
const KEYRI_CALLBACK_URL = `${BASE_URL}/users/session-mobile`;

KeyriNativeModule.initSdk(APP_KEY, PUBLIC_KEY, KEYRI_CALLBACK_URL, false);

const onReadSessionIdClick = () => {
  const sessionId = '60f180ac10cdf71478f462c7';
  KeyriNativeModule.onReadSessionId(
    sessionId,
    (
      serviceId: string,
      name: string,
      logo: string,
      username: string,
      isNewUser: boolean
    ) => {
      console.log(
        `New session: ${serviceId}, ${name}, ${logo}, ${username}, ${isNewUser}`
      );
    }
  );
};

const signup = () => {
  KeyriNativeModule.signup(
    // TODO Params from onReadSessionId() if isNewUser == true
    'username',
    'sessionId',
    'serviceId',
    'serviceName',
    'serviceLogo',
    'custom',
    () => {
      console.log(`Signed up`);
    }
  );
};

const login = () => {
  KeyriNativeModule.login(
    // TODO Params from onReadSessionId() if isNewUser == false
    'publicAccountUsername',
    'publicAccountCustom',
    'sessionId',
    'service',
    'custom',
    () => {
      console.log(`Signed up`);
    }
  );
};

// TODO Functions Below need to implement and test
const mobileSignup = () => {
  KeyriNativeModule.mobileSignup('1364');
};

const mobileLogin = () => {
  KeyriNativeModule.mobileLogin('1364');
};

const accounts = () => {
  KeyriNativeModule.accounts('1364');
};

const removeAccount = () => {
  KeyriNativeModule.removeAccount('1364');
};

const authWithScanner = () => {
  KeyriNativeModule.authWithScanner('custom');
};

export default function App() {
  React.useEffect(() => {
    KeyriNativeModule.listenActivityResult(() => {
      console.log(`Authenticated`);
    });

    KeyriNativeModule.listenErrors((errorMessage: string) => {
      console.log(`Something went wrong: ${errorMessage}`);
    });

    // return () => { removeListeners() }
  }, []);

  return (
    <View style={styles.container}>
      <Button
        onPress={onReadSessionIdClick}
        title="On ReadSession Id"
        color="#841584"
      />

      <Button onPress={signup} title="Signup" color="#841584" />

      <Button onPress={login} title="Login" color="#841584" />

      <Button onPress={mobileSignup} title="Mobile Signup" color="#841584" />

      <Button onPress={mobileLogin} title="Mobile Login" color="#841584" />

      <Button onPress={accounts} title="Accounts" color="#841584" />

      <Button onPress={removeAccount} title="Remove Account" color="#841584" />

      <Button
        onPress={authWithScanner}
        title="Auth With Scanner"
        color="#841584"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
