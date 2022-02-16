import * as React from 'react';
import {
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import type { RootNavigationProps } from 'example/src/navigation';

interface Account {
  custom: string;
  name: string;
  // ...
}

const accounts: Account[] = [
  {
    custom: 'uniq1',
    name: 'Account1',
  },
  {
    custom: 'null',
    name: 'Account2',
  },
  {
    custom: 'uniq3',
    name: 'Account3',
  },
];

interface LogInScreenProps extends RootNavigationProps<'LogIn'> {}

const LogInScreen: React.FC<LogInScreenProps> = () => {
  const onAccountPress = (account: Account) => {
    console.log('Pressed', account);
  };

  const renderAccount = (item: Account) => {
    return (
      <TouchableOpacity style={styles.row} onPress={() => onAccountPress(item)}>
        <Text style={styles.accountName}>{item.name}</Text>
      </TouchableOpacity>
    );
  };

  return (
    <View style={styles.root}>
      <FlatList
        data={accounts}
        keyExtractor={(account) => account.custom}
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
