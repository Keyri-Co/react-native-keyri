import * as React from 'react';
import {Button, Modal, NativeModules, StyleSheet, View} from 'react-native';
import QRCodeScanner from 'react-native-qrcode-scanner';

const {KeyriNativeModule} = NativeModules;

const APP_KEY = 'raB7SFWt27VoKqkPhaUrmWAsCJIO8Moj';
const PUBLIC_KEY =
  'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE56eKjQNfIbfWYCBQLCF2yV6VySbHMzuc07JYCOS6juySvUWE/ubYvw9pJGAgQfmNr2n4LAQggoapHgfHkTNqbg=="';
const BASE_URL = 'http://18.208.184.185:5000';
const KEYRI_CALLBACK_URL = `${BASE_URL}/users/session-mobile`;

KeyriNativeModule.initSdk(APP_KEY, PUBLIC_KEY, KEYRI_CALLBACK_URL, false);

const onReadSessionIdClick = (sessionId: string) => {
  KeyriNativeModule.onReadSessionId(
    sessionId,
    (
      serviceId: string,
      name: string,
      logo: string,
      username: string,
      isNewUser: boolean
    ) => {
      if (isNewUser) {
        signup(sessionId, serviceId, name, logo, username, 'CUSTOM');
      } else {
        // TODO Add implementation
        login()
      }
    }
  );
};

const signup = (sessionId: string, serviceId: string, name: string, logo: string, username: string, custom: string) => {
  KeyriNativeModule.signup(
    username,
    sessionId,
    serviceId,
    name,
    logo,
    custom,
    () => {
      // TODO Show dialog
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
      // TODO Show dialog
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
  const [isModalVisible, setModalVisible] = React.useState(false);

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
      <Modal
        visible={isModalVisible}
        onRequestClose={() => setModalVisible(false)}>
        <QRCodeScanner
          containerStyle={styles.camera}
          onRead={(qr) => {
            const urlParams = new URLSearchParams(qr.data);
            const sessionId = urlParams.get('sessionId')

            if (sessionId == null) return

            onReadSessionIdClick(sessionId);
            setModalVisible(false);
          }}
        />
      </Modal>

      <Button
        onPress={authWithScanner}
        title="Auth With Scanner"
        color="#841584"/>

      <Button
        onPress={() => setModalVisible(true)}
        title="Signup"
        color="#841584"/>

      <Button
        onPress={() => setModalVisible(true)}
        title="Login"
        color="#841584"/>

      <Button
        onPress={mobileSignup}
        title="Mobile Signup"
        color="#841584"/>

      <Button
        onPress={mobileLogin}
        title="Mobile Login"
        color="#841584"/>

      <Button
        onPress={accounts}
        title="Accounts"
        color="#841584"/>

      <Button
        onPress={removeAccount}
        title="Remove Account"
        color="#841584"/>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  camera: {
    flex: 1,
  },
});
