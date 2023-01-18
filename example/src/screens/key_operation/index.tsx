import React, { useState } from 'react';
import styles from '../styles/common-styles';
import Keyri from 'react-native-keyri';
import type { RootNavigationProps } from '../../navigation';
import { Alert, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { KeyOperationType } from '../../utils/types';
import toast from '../../services/toast';
interface KeyOperationScreenProps extends RootNavigationProps<'KeyOperation'> {}

const KeyOperationScreen: React.FC<KeyOperationScreenProps> = ({ navigation, route }) => {
  const [username, setUsername] = useState<string>('');
  const doOperation = async () => {
    const publicUserId = username.length > 0 ? username : 'ANON';
    switch (route.params.type) {
      case KeyOperationType.GenerateAssociationKey: {
        const key = await Keyri.generateAssociationKey(publicUserId);
        showAlert('Key generated', publicUserId + ': ' + key);
        break;
      }
      case KeyOperationType.GetAssociationKey: {
        const key = await Keyri.getAssociationKey(publicUserId);
        showAlert('Key retrieved', publicUserId + ': ' + key);
        toast.show('Key for ' + publicUserId + ': ' + key);
        break;
      }
      case KeyOperationType.RemoveAssociationKey: {
        const map = await Keyri.listAssociationKey();
        if (Object.keys(map).includes(username)) {
          await Keyri.removeAssociationKey(publicUserId);
          showAlert('Key removed', 'Key for ' + publicUserId + ' successfully removed');
        } else {
          showAlert('Key not found', 'No key found for ' + publicUserId);
        }

        break;
      }
    }
  };
  const showAlert = (title: string, message: string) => {
    Alert.alert(title, message, [{ text: 'OK' }], {
      cancelable: true,
      onDismiss: () => navigation.goBack(),
    });
  };
  const getOperationText = (): string => {
    switch (route.params.type) {
      case KeyOperationType.GenerateAssociationKey:
        return 'Generate key';
      case KeyOperationType.GetAssociationKey:
        return 'Get key';
      case KeyOperationType.RemoveAssociationKey:
        return 'Remove key';
    }
  };
  return (
    <View style={styles.root}>
      <TextInput style={styles.input} onChangeText={setUsername} placeholder="Username or email" />
      <TouchableOpacity
        disabled={username.length > 0 && username.length < 3}
        style={styles.touchable}
        onPress={doOperation}
      >
        <Text style={styles.btnText}>{getOperationText()}</Text>
      </TouchableOpacity>
    </View>
  );
};
export default KeyOperationScreen;
