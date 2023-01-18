import React, { useState } from 'react';
import styles from '../styles/common-styles';
import Keyri from 'react-native-keyri';
import type { RootNavigationProps } from '../../navigation';
import { Text, TextInput, TouchableOpacity, View } from 'react-native';
import { AuthenticationType } from '../../utils/types';
import toast from '../../services/toast';
import { APP_KEY } from '../../utils/constants';
interface AuthenticationScreenProps extends RootNavigationProps<'Authentication'> {}

const AuthenticationScreen: React.FC<AuthenticationScreenProps> = ({ navigation, route }) => {
  const [username, setUsername] = useState<string>('');
  const [payload, setPayload] = useState<string>('');
  const doOperation = async () => {
    const publicUserId = username.length > 0 ? username : 'ANON';
    switch (route.params.type) {
      case AuthenticationType.DefaultConfirmation: {
        navigation.replace('Default', {
          authParams: { publicUserId: publicUserId, payload: payload },
        });
        break;
      }
      case AuthenticationType.CustomConfirmation: {
        navigation.replace('Custom', {
          authParams: { publicUserId: publicUserId, payload: payload },
        });
        break;
      }
      case AuthenticationType.EasyKeyriAuth: {
        navigation.goBack();
        await easyAuth(publicUserId);
        break;
      }
    }
  };
  const easyAuth = async (publicUserId?: string) => {
    const data = {
      publicUserId: publicUserId,
      appKey: APP_KEY,
      payload: payload,
    };
    try {
      await Keyri.easyKeyriAuth(data);
    } catch (error) {
      toast.show(error);
    }
  };
  return (
    <View style={styles.root}>
      <TextInput style={styles.input} onChangeText={setUsername} placeholder="Username or email" />
      <TextInput style={styles.input} onChangeText={setPayload} placeholder="Payload" />
      <TouchableOpacity
        disabled={username.length > 0 && username.length < 3}
        style={styles.touchable}
        onPress={doOperation}
      >
        <Text style={styles.btnText}>Authenticate</Text>
      </TouchableOpacity>
    </View>
  );
};
export default AuthenticationScreen;
