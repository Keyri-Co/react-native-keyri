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



interface LogInScreenProps extends RootNavigationProps<'LogIn'> {}

const LogInScreen: React.FC<LogInScreenProps> = () => {
  const [accounts, setAccounts] = React.useState<KeyriPublicAccount[]>([]);

 
  return (
    <View style={styles.root}>
      
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
