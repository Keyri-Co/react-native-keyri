import * as React from 'react';
import {Button, StyleSheet, Text, TextInput, View} from 'react-native';

interface SignUpScreenProps {
}

export const SignUpScreen: React.FC<SignUpScreenProps> = () => {
  const [text, setText] = React.useState('');

  const onSubmitPress = () => {
    console.log('Pressed', text);
  };

  return (
    <View style={styles.root}>
      <Text style={styles.title}>Sign Up</Text>
      <TextInput
        style={styles.input}
        placeholder="Username"
        onChangeText={setText}
        value={text}/>

      <View style={styles.button}>
        <Button title="Sign up (mobile)" onPress={onSubmitPress}/>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  title: {
    fontSize: 24,
    textAlign: 'center',
    marginVertical: 30,
  },
  input: {
    // flexDirection: 'row',
    borderBottomWidth: 1,
    borderColor: '#ddd',
    paddingHorizontal: 12,
    marginHorizontal: 30,
  },
  button: {
    marginHorizontal: 30,
    marginTop: 30,
  },
});
