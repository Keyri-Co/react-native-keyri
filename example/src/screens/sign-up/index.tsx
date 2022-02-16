import type { RootNavigationProps } from 'example/src/navigation';
import * as React from 'react';
import { Button, StyleSheet, TextInput, View } from 'react-native';

interface SignUpScreenProps extends RootNavigationProps<'SignUp'> {}

const SignUpScreen: React.FC<SignUpScreenProps> = () => {
  const [text, setText] = React.useState('');

  const onSubmitPress = () => {
    console.log('Pressed', text);
  };

  return (
    <View style={styles.root}>
      <TextInput
        style={styles.input}
        placeholder="Username"
        onChangeText={setText}
        value={text}
      />

      <View style={styles.button}>
        <Button title="Sign up (mobile)" onPress={onSubmitPress} />
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
  },
  button: {
    marginHorizontal: 30,
    marginTop: 10,
  },
});

export default SignUpScreen;
