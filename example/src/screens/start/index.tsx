import React, { useEffect } from 'react';
import { Text, TouchableOpacity, View, Linking } from 'react-native';
import Keyri from 'react-native-keyri';
import axios from 'axios';

import {
  APP_KEY,
  SUPABASE_API_KEY,
  SUPABASE_APP_KEY,
  SUPABASE_PASS,
  SUPABASE_URL,
  SUPABASE_USER_EMAIL,
} from '../../utils/constants';
import type {
  RootNavigationProps,
  RootNavigatorParams,
} from 'example/src/navigation';
import { AppLinkContext } from '../../context/linking-context';
import styles from './start-styles';
import toast from '../../components/toast';
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
  }, [setDeepLink]);

  const easyAuth = async () => {
    const data = {
      publicUserId: 'user@email',
      appKey: APP_KEY,
      payload: '',
    };
    try {
      await Keyri.easyKeyriAuth(data);
    } catch {
      (error: Error) => {
        console.log(error);
        toast.show(JSON.stringify(error));
      };
    }
  };

  const supabaseEasyAuth = async () => {
    const url = SUPABASE_URL;
    const response = await axios
      .post(
        url,
        {
          email: SUPABASE_USER_EMAIL,
          password: SUPABASE_PASS,
        },
        {
          headers: {
            apiKey: SUPABASE_API_KEY,
          },
        }
      )
      .catch((error: Error) => toast.show(error?.message));
    if (response) {
      const data = {
        publicUserId: SUPABASE_USER_EMAIL,
        appKey: SUPABASE_APP_KEY,
        payload: JSON.stringify({
          refreshToken: response?.data?.refresh_token,
        }),
      };
      try {
        await Keyri.easyKeyriAuth(data);
      } catch {
        (error: Error) => toast.show(JSON.stringify(error));
      }
    }
  };
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
        <Text style={styles.text}>or</Text>
        <TouchableOpacity style={styles.touchable} onPress={supabaseEasyAuth}>
          <Text
            adjustsFontSizeToFit={true}
            style={styles.btnText}
            numberOfLines={1}
            minimumFontScale={0.7}
          >
            Supabase Easy Keyri Auth
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default StartScreen;
