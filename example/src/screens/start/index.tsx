import React, { useEffect } from 'react';
import { Linking, ScrollView, Text, TouchableOpacity } from 'react-native';
import type { RootNavigationProps } from 'example/src/navigation';
import { AppLinkContext } from '../../context/linking-context';
import styles from '../styles/common-styles';
import { AccountsType, AuthenticationType, KeyOperationType } from '../../utils/types';

interface StartScreenProps extends RootNavigationProps<'Start'> {}

const StartScreen: React.FC<StartScreenProps> = ({ navigation }) => {
  const { setDeepLink } = React.useContext(AppLinkContext);
  const goNextAuthentication = (type: AuthenticationType) => {
    navigation.navigate('Authentication', { type: type });
  };
  const goNextKeyOperation = (type: KeyOperationType) => {
    navigation.navigate('KeyOperation', { type: type });
  };
  const goNextAccounts = (type: AccountsType) => {
    navigation.navigate('Accounts', { type: type });
  };
  useEffect(() => {
    const handleUrl = ({ url }: { url: string }) => {
      setDeepLink(url);
    };
    Linking.addEventListener('url', handleUrl);
    return () => Linking.removeAllListeners('url');
  }, [setDeepLink]);
  return (
    <ScrollView contentContainerStyle={styles.root}>
      <Text style={styles.title}>Auth example</Text>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextAuthentication(AuthenticationType.DefaultConfirmation)}
      >
        <Text style={styles.btnText}>Default confirmation screen</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextAuthentication(AuthenticationType.CustomConfirmation)}
      >
        <Text style={styles.btnText}>Custom confirmation screen</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextAuthentication(AuthenticationType.EasyKeyriAuth)}
      >
        <Text style={styles.btnText}>Easy Keyri Auth</Text>
      </TouchableOpacity>
      <Text style={styles.title}>SDK methods</Text>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextKeyOperation(KeyOperationType.GenerateAssociationKey)}
      >
        <Text style={styles.btnText}>Generate association key</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => navigation.navigate('GenerateSignature')}
      >
        <Text style={styles.btnText}>Get user signature</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextAccounts(AccountsType.ListAssociationKey)}
      >
        <Text style={styles.btnText}>List association key</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextAccounts(AccountsType.ListUniqueAccounts)}
      >
        <Text style={styles.btnText}>List unique accounts</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextKeyOperation(KeyOperationType.GetAssociationKey)}
      >
        <Text style={styles.btnText}>Get association key</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.touchable}
        onPress={() => goNextKeyOperation(KeyOperationType.RemoveAssociationKey)}
      >
        <Text style={styles.btnText}>Remove association key</Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

export default StartScreen;
