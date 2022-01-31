import * as React from 'react';

import { Button, NativeModules, StyleSheet, View } from 'react-native';

const { KeyriNativeModule } = NativeModules;

const onReadSessionIdClick = () => {
  KeyriNativeModule.onReadSessionId(
    'sessionId', // TODO Scanned sessionId param
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
  KeyriNativeModule.listenActivityResult(() => {
    console.log(`Authenticated`);
  });

  KeyriNativeModule.listenErrors((errorMessage: string) => {
    console.log(`Something went wrong: ${errorMessage}`);
  });

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
