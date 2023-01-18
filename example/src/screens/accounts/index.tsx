import React, { useEffect, useState } from 'react';
import styles from '../styles/common-styles';
import Keyri from 'react-native-keyri';
import type { RootNavigationProps } from '../../navigation';
import { ScrollView, Text } from 'react-native';
import { AccountsType } from '../../utils/types';
interface AccountsScreenScreenProps extends RootNavigationProps<'Accounts'> {}

const AccountsScreen: React.FC<AccountsScreenScreenProps> = ({ route }) => {
  const [text, setText] = useState<string>('');
  useEffect(() => {
    const type = route.params.type;

    if (type === AccountsType.ListUniqueAccounts) {
      Keyri.listUniqueAccounts().then(showAccounts);
    } else {
      Keyri.listAssociationKey().then(showAccounts);
    }
  }, [route.params.type]);

  const showAccounts = (map: string[]) => {
    const list = Object.entries(map).map(([key, value]) => key + ': ' + value);

    setText(list.length > 0 ? list.join('\n\n') : 'No accounts found');
  };
  return (
    <ScrollView contentContainerStyle={styles.root}>
      <Text style={styles.title}>{text}</Text>
    </ScrollView>
  );
};
export default AccountsScreen;
