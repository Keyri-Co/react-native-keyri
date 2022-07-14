import * as React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

import type { RootNavigationProps } from 'example/src/navigation';
import { ILoginType } from '../../utils/types';
interface LogInScreenProps extends RootNavigationProps<'Start'> {}

const StartScreen: React.FC<LogInScreenProps> = ({ navigation }) => {
  const goNext = (type: ILoginType) => {
    navigation.navigate('Initial', { type: type });
  };
  return (
    <View style={styles.root}>
      <Text style={styles.title}>Choose options to login</Text>
      <View style={styles.btnView}>
        <TouchableOpacity
          style={styles.touchable}
          onPress={() => goNext(ILoginType.default)}
        >
          <Text style={styles.btnText}>Default popup</Text>
        </TouchableOpacity>
        <Text style={styles.text}>or</Text>
        <TouchableOpacity
          style={styles.touchable}
          onPress={() => goNext(ILoginType.custom)}
        >
          <Text style={styles.btnText}>Custom popup</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  text: {
    color: '#666',
    fontSize: 16,
    textAlign: 'center',
  },
  btnView: {
    height: 170,
    justifyContent: 'space-between',
    marginBottom: 70,
  },
  touchable: {
    width: 250,
    height: 50,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    backgroundColor: '#9887CB',
    paddingBottom: 3,
  },
  btnText: {
    fontWeight: '700',
    fontSize: 16,
    color: '#FFFFFF',
  },
  title: {
    color: '#666',
    fontSize: 18,
    textAlign: 'center',
    marginTop: 50,
  },
});

export default StartScreen;
