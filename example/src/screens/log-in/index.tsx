import * as React from 'react';
import {
  Alert,
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import Keyri, { KeyriPublicAccount } from 'react-native-keyri';
import type { RootNavigationProps } from 'example/src/navigation';

interface ServerResponse {
  userName: string;
}

interface LogInScreenProps extends RootNavigationProps<'LogIn'> {}

const LogInScreen: React.FC<LogInScreenProps> = () => {
  const [accounts, setAccounts] = React.useState<KeyriPublicAccount[]>([]);

  React.useEffect(() => {
    Keyri.getAccounts().then(setAccounts).catch(console.error);
  }, []);

  const onAccountPress = async (account: KeyriPublicAccount) => {
    try {
      const { username, custom } = account;
      const result = await Keyri.directLogin<ServerResponse>(
        username,
        {},
        custom
      );

      Alert.alert('Success', `Hi ${result.userName}, you are logged in`);
    } catch (e) {
      console.error(e);
    }
  };

  const renderAccount = (account: KeyriPublicAccount) => {
    return (
      <TouchableOpacity
        style={styles.row}
        onPress={() => onAccountPress(account)}
      >
        <Text style={styles.accountName}>{account.username}</Text>
      </TouchableOpacity>
    );
  };

  return (
    <View style={styles.root}>
      <FlatList
        data={accounts}
        keyExtractor={(account) => account.username}
        renderItem={({ item }) => renderAccount(item)}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  row: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderColor: '#ddd',
    paddingVertical: 14,
  },
  accountName: {
    fontSize: 15,
    paddingHorizontal: 20,
  },
});

export default LogInScreen;
