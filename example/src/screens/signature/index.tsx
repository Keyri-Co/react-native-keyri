import React, { useState } from 'react';
import styles from '../styles/common-styles';
import Keyri from 'react-native-keyri';
import type { RootNavigationProps } from '../../navigation';
import { Alert, Text, TextInput, TouchableOpacity, View } from 'react-native';
interface GenerateSignatureScreenProps extends RootNavigationProps<'GenerateSignature'> {}

const GenerateSignatureScreen: React.FC<GenerateSignatureScreenProps> = ({ navigation }) => {
  const [username, setUsername] = useState<string>('');
  const [data, setData] = useState<string>('');
  const doOperation = async () => {
    const publicUserId = username.length > 0 ? username : 'ANON';

    const signature = await Keyri.getUserSignature(publicUserId, data);
    showAlert('User signature', 'Signature for ' + username + ': ' + signature);
  };
  const showAlert = (title: string, message: string) => {
    Alert.alert(title, message, [{ text: 'OK' }], {
      cancelable: true,
      onDismiss: () => navigation.goBack(),
    });
  };
  return (
    <View style={styles.root}>
      <TextInput style={styles.input} onChangeText={setUsername} placeholder="Username or email" />
      <TextInput style={styles.input} onChangeText={setData} placeholder="Data" />
      <TouchableOpacity
        disabled={username.length > 0 && username.length < 3}
        style={styles.touchable}
        onPress={doOperation}
      >
        <Text style={styles.btnText}>Generate key</Text>
      </TouchableOpacity>
    </View>
  );
};
export default GenerateSignatureScreen;
