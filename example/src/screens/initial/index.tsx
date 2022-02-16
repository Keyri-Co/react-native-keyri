import type { RootNavigationProps } from 'example/src/navigation';
import * as React from 'react';
import { Button, StyleSheet, View } from 'react-native';
import Keyri from 'react-native-keyri';

interface InitialScreenProps extends RootNavigationProps<'Initial'> {}

const InitialScreen: React.FC<InitialScreenProps> = ({ navigation }) => {
  return (
    <View style={styles.root}>
      <View style={styles.buttonsContainer}>
        <View style={styles.buttonContainer}>
          <Button
            title="This Device Log In"
            onPress={() => navigation.navigate('LogIn')}
          />
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="This Device Sign Up"
            onPress={() => navigation.navigate('SignUp')}
          />
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="Easy Keyri Auth"
            onPress={() => Keyri.easyKeyriAuth(null)}
          />
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  buttonsContainer: {
    paddingVertical: 30,
    paddingHorizontal: 40,
  },
  buttonContainer: {
    marginVertical: 5,
  },
});

export default InitialScreen;
