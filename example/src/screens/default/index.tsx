import type { RootNavigationProps } from 'example/src/navigation';
import React, { useEffect, useContext, useCallback } from 'react';
import { View, ActivityIndicator, Text, TouchableOpacity, Image } from 'react-native';
import Keyri from 'react-native-keyri';

import type { ISearchParam } from '../../utils/types';
import { parseUrlParams } from '../../utils/helpers';
import { AppLinkContext } from '../../context/linking-context';
import styles from './default-styles';
import { ICONS } from '../../assets';
import toast from '../../services/toast';
import { APP_KEY } from '../../utils/constants';
import { Camera, Code, useCameraDevice, useCameraPermission, useCodeScanner } from "react-native-vision-camera";
import { useIsFocused } from "@react-navigation/native";
interface InitialScreenProps extends RootNavigationProps<'Default'> {}

const DefaultScreen: React.FC<InitialScreenProps> = ({ navigation }) => {
  const [loading, setLoading] = React.useState<boolean>(false);
  const { deepLink } = useContext(AppLinkContext);
  const { hasPermission, requestPermission } = useCameraPermission();
  const isFocused = useIsFocused();
  const isActive = isFocused && hasPermission;
  const device = useCameraDevice('back');

  useEffect(() => {
    if (!hasPermission) {
      requestPermission();
    }
  }, [hasPermission]);

  const codeScanner = useCodeScanner({
    codeTypes: ['qr'],
    onCodeScanned: (codes: Code[]) => {
      const sessionLink = codes[0].value;

      if (sessionLink) {
        onReadSuccess(sessionLink);
      }
    },
  });

  const onReadSuccess = useCallback(async (data: string) => {
    try {
      setLoading(true);
      await Keyri.initialize({ appKey: APP_KEY });
      const params: ISearchParam = parseUrlParams(data);
      const sessionId: string = params?.sessionId ?? '';
      const session = await Keyri.initiateQrSession(sessionId, 'user@email');
      if (session) {
        setLoading(false);
        await Keyri.initializeDefaultConfirmationScreen('payload');
      }
    } catch (error) {
      toast.show(error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (deepLink) {
      onReadSuccess(deepLink);
    }
  }, [deepLink, onReadSuccess]);

  const onClosePress = () => {
    navigation.goBack();
  };
  return (
    <View style={styles.root}>
      <TouchableOpacity onPress={onClosePress} style={styles.touchable}>
        <Image source={ICONS.CLOSE} height={35} resizeMode="stretch" width={35} />
      </TouchableOpacity>
      {loading ? <View style={styles.indicator}>
          <ActivityIndicator size="large" color="#FFFFFF" />
        </View> : null}
      {device ? <Camera
        style={styles.camera}
        device={device}
        isActive={isActive}
        codeScanner={codeScanner}
      /> : null}
      <Text style={styles.text}>Powered by Keyri</Text>
    </View>
  );
};

export default DefaultScreen;
