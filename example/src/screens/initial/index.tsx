import type { RootNavigationProps } from 'example/src/navigation';
import * as React from 'react';
import QRCodeScanner from 'react-native-qrcode-scanner';
import type { BarCodeReadEvent } from 'react-native-camera';
import {
  StyleSheet,
  View,
  ActivityIndicator,
  Text,
  Dimensions,
} from 'react-native';
import Keyri from 'react-native-keyri';
import type { KeyriSession } from '../../../../src/types';
import { ISearchParam, ILoginType } from '../../utils/types';
import { parseUrlParams } from '../../utils/helpers';
import PopupModal from '../../components/PopupModal';
import { APP_KEY } from '../../utils/constants';
interface InitialScreenProps extends RootNavigationProps<'Initial'> {}

const InitialScreen: React.FC<InitialScreenProps> = ({ route }) => {
  const type: ILoginType = route.params?.type ?? ILoginType.default;
  const [loading, setLoading] = React.useState<boolean>(false);
  const [sessionObj, setSession] = React.useState<KeyriSession | null>(null);
  const [id, setSessionId] = React.useState<string>('');
  const [customLoginVisible, setCustomLoginVisible] =
    React.useState<boolean>(false);
  const onReadSuccess = async (scan: BarCodeReadEvent) => {
    try {
      setLoading(true);
      const params: ISearchParam = parseUrlParams(scan.data);
      const sessionId: string = params?.sessionId ?? '';
      const options = {
        appKey: APP_KEY,
        sessionId: sessionId,
        publicUserId: '',
      };
      const session = await Keyri.initiateQrSession(options);
      if (session) {
        if (type === ILoginType.default) {
          setLoading(false);
          await Keyri.initializeDefaultScreen(sessionId, 'payload');
        } else {
          setSession(session);
          setCustomLoginVisible(true);
          setSessionId(sessionId);
        }
      }
    } catch (error) {
      console.log(error, '==error initiate qr session');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.root}>
      <PopupModal
        session={sessionObj}
        setSession={() => setSession(null)}
        id={id}
        customLoginVisible={customLoginVisible}
        setCustomLoginVisible={(visible: boolean) =>
          setCustomLoginVisible(visible)
        }
      />
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
const { width, height } = Dimensions.get('window');
const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  indicator: {
    position: 'absolute',
    alignSelf: 'center',
    top: 100,
    zIndex: 10000,
  },
  camera: {
    width: width,
    zIndex: -1,
    height: height,
  },
  text: {
    position: 'absolute',
    alignSelf: 'center',
    bottom: 38,
    fontSize: 12,
    fontWeight: '500',
    color: '#777',
  },
  markerStyle: { borderColor: '#666' },
});
export default InitialScreen;