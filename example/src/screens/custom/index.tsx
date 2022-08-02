import type { RootNavigationProps } from 'example/src/navigation';
import React, { useState, useEffect, useContext, useCallback } from 'react';
import QRCodeScanner from 'react-native-qrcode-scanner';
import type { BarCodeReadEvent } from 'react-native-camera';
import styles from './custom-styles';
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
import PopupModal from '../../components/popup-modal';
import { APP_KEY } from '../../utils/constants';
import { AppLinkContext } from '../../context/linking-context';
import { AppSessionContext } from '../../context/session-context';
import { ICONS } from '../../assets';

interface CustomScreenProps extends RootNavigationProps<'Custom'> {}

const CustomScreen: React.FC<CustomScreenProps> = ({ navigation }) => {
  const [loading, setLoading] = React.useState<boolean>(false);
  const [isPopUpVisible, setIsPopUpVisible] = useState<boolean>(false);
  const { deepLink } = useContext(AppLinkContext);
  const {
    activeSession,
    activeSessionId,
    setActiveSession,
    setActiveSessionId,
  } = useContext(AppSessionContext);

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
          setActiveSession(session);
          setIsPopUpVisible(true);
          setActiveSessionId(sessionId);
        }
      } catch (error) {
        console.log(error, '==error initiate qr session');
      } finally {
        setLoading(false);
      }
    },
    [setActiveSession, setActiveSessionId]
  );

  useEffect(() => {
    if (deepLink) {
      onReadSuccess({ data: deepLink });
    }
  }, [deepLink, onReadSuccess]);

  const closePopUp = () => {
    setIsPopUpVisible(false);
    setActiveSession(null);
    setActiveSessionId('');
  };

  const denySession = async () => {
    if (activeSessionId) {
      closePopUp();
      await Keyri.denySession(activeSessionId, 'payload');
    }
  };

  const confirmSession = async () => {
    if (activeSessionId) {
      closePopUp();
      await Keyri.confirmSession(activeSessionId, 'payload');
    }
  };
  const onClosePress = () => {
    navigation.goBack();
  };
  return (
    <View style={styles.root}>
      <PopupModal
        isPopUpVisible={isPopUpVisible}
        denySession={denySession}
        confirmSession={confirmSession}
        session={activeSession}
      />
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
export default CustomScreen;
