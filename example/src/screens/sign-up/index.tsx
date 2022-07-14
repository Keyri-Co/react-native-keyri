import type { RootNavigationProps } from 'src/navigation';
import * as React from 'react';
import { Alert, Button, StyleSheet, TextInput, View } from 'react-native';
import Keyri from 'react-native-keyri';

interface ServerResponse {
  userName: string;
}

interface SignUpScreenProps extends RootNavigationProps<'SignUp'> {}

const SignUpScreen: React.FC<SignUpScreenProps> = () => {
  const [username, setUsername] = React.useState('');

  const onSubmitPress = async () => {
    try {
      const result = await Keyri.directSignup<ServerResponse>(username, {});

      Alert.alert('Success', `Hi ${result?.userName}, you are signed up`);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <View style={styles.root}>
      <TextInput
        style={styles.input}
        placeholder="Username"
        onChangeText={setUsername}
        value={username}
      />

      <View style={styles.button}>
        <Button title="Submit" onPress={onSubmitPress} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  input: {
    borderBottomWidth: 1,
    borderColor: '#ddd',
    paddingHorizontal: 12,
    marginHorizontal: 30,
    marginVertical: 20,
    paddingVertical: 10,
  },
  button: {
    marginHorizontal: 30,
    marginTop: 10,
  },
});

export default SignUpScreen;
