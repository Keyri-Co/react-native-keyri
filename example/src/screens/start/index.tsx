
import React, { useEffect } from 'react';
import { Text, TouchableOpacity, View, Linking } from 'react-native';
import Keyri from 'react-native-keyri';
const axios = require('axios');

import { APP_KEY } from '../../utils/constants';
import type {
  RootNavigationProps,
  RootNavigatorParams,
} from 'example/src/navigation';
import { AppLinkContext } from '../../context/linking-context';
import styles from './start-styles';
interface StartScreenProps extends RootNavigationProps<'Start'> {}

const StartScreen: React.FC<StartScreenProps> = ({ navigation }) => {
  const { setDeepLink } = React.useContext(AppLinkContext);
  const goNext = (screenName: keyof RootNavigatorParams) => {
    navigation.navigate(screenName);
  };
  useEffect(() => {
    const handleUrl = ({ url }: { url: string }) => {
      setDeepLink(url);
    };
    Linking.addEventListener('url', handleUrl);
    return () => Linking.removeAllListeners('url');
  }, []);

  const getInitialUrl = async () => {
    const initialUrl = await Linking.getInitialURL();
    if (initialUrl) {
      setDeepLink(initialUrl);
    }
  };
  getInitialUrl();

  const easyAuth = async () => {
    const data = {
      publicUserId: 'user@email',
      appKey: APP_KEY,
      payload: '',
      
    };
    try {
      await Keyri.easyKeyriAuth(data);
    } catch {
      
    }
  };

  const supabaseEasyAuth = async () => {
    const url =  'https://pidfgjqywchqcqdjhmsj.supabase.co/auth/v1/token?grant_type=password'
    await axios.post(url,{
      "email": "a.kuliahin@csn.khai.edu",
      "password": "4Science#",
           },
           {headers: {
            'apiKey' : 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBpZGZnanF5d2NocWNxZGpobXNqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NTQ3NzQzNTUsImV4cCI6MTk3MDM1MDM1NX0.HY0mpzolDkg5TZ7_gim6i0mzXKbhCtIMJptgLcvdZv8',
                              
          }}).catch(error => console.log(error))
      
   
  }
  return (
    <View style={styles.root}>
      <Text style={styles.title}>Choose options to login</Text>
      <View style={styles.btnView}>
        <TouchableOpacity
          style={styles.touchable}
          onPress={() => goNext('Default')}
        >
          <Text style={styles.btnText}>Default popup</Text>
        </TouchableOpacity>
        <Text style={styles.text}>or</Text>
        <TouchableOpacity
          style={styles.touchable}
          onPress={() => goNext('Custom')}
        >
          <Text style={styles.btnText}>Custom popup</Text>
        </TouchableOpacity>
        <Text style={styles.text}>or</Text>
        <TouchableOpacity style={styles.touchable} onPress={easyAuth}>
          <Text style={styles.btnText}>Easy Keyri Auth</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.touchable} onPress={supabaseEasyAuth}>
          <Text style={styles.btnText}>Easy Keyri Auth</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default StartScreen;
