import type { RootNavigationProps } from 'example/src/navigation';
import React, { useEffect, useContext, useCallback } from 'react';
import QRCodeScanner from 'react-native-qrcode-scanner';
import type { BarCodeReadEvent } from 'react-native-camera';
import {
  View,
  ActivityIndicator,
  Text,
  TouchableOpacity,
  Image,
} from 'react-native';
import Keyri from 'react-native-keyri';

import type { ISearchParam } from '../../utils/types';
import { parseUrlParams } from '../../utils/helpers';
import { APP_KEY } from '../../utils/constants';
import { AppLinkContext } from '../../context/linking-context';
import styles from './default-styles';
import { ICONS } from '../../assets';
import toast from '../../services/toast';
interface InitialScreenProps extends RootNavigationProps<'Default'> {}

const DefaultScreen: React.FC<InitialScreenProps> = ({ navigation }) => {
  const [loading, setLoading] = React.useState<boolean>(false);

  const { deepLink } = useContext(AppLinkContext);

  const onReadSuccess = useCallback(
    async (scan: BarCodeReadEvent | { data: string }) => {
      try {
        setLoading(true);
        const params: ISearchParam = parseUrlParams(scan.data);
        const sessionId: string = params?.sessionId ?? '';
        const options = {
          appKey: APP_KEY,
          sessionId: sessionId,
          publicUserId: 'user@email',
        };
        const session = await Keyri.initiateQrSession(options);
        if (session) {
          setLoading(false);
          await Keyri.initializeDefaultScreen(sessionId, 'payload');
        }
      } catch (error) {
        toast.show(error);
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    if (deepLink) {
      onReadSuccess({ data: deepLink });
    }
  }, [deepLink, onReadSuccess]);

  const onClosePress = () => {
    navigation.goBack();
  };
  return (
    <View style={styles.root}>
      <TouchableOpacity onPress={onClosePress} style={styles.touchable}>
        <Image
          source={ICONS.CLOSE}
          height={35}
          resizeMode="stretch"
          width={35}
        />
      </TouchableOpacity>
      {loading ? (
        <View style={styles.indicator}>
          <ActivityIndicator size="large" color="#FFFFFF" />
        </View>
      ) : null}
      <QRCodeScanner
        showMarker
        cameraType="back"
        cameraStyle={styles.camera}
        onRead={onReadSuccess}
        reactivate={true}
        reactivateTimeout={9000}
        markerStyle={styles.markerStyle}
      />
      <Text style={styles.text}>Powered by Keyri</Text>
    </View>
  );
};

export default DefaultScreen;
