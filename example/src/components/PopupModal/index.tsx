import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Dimensions,
  Modal,
  TouchableOpacity,
  Animated,
  Image,
} from 'react-native';
import Keyri from 'react-native-keyri';

import { IWidgetTypes } from '../../utils/types';
import { ICONS } from '../../assets/index';
import type { KeyriSession } from '../../../../src/types';

interface IPopupModalProps {
  session: KeyriSession | null;
  setSession: (session: KeyriSession | null) => void;
  id: string;
  customLoginVisible: boolean;
  setCustomLoginVisible: (visible: boolean) => void;
}

const PopupModal: React.FC<IPopupModalProps> = ({
  session,
  setSession,
  id,
  customLoginVisible,
  setCustomLoginVisible,
}) => {
  const value = React.useRef(new Animated.Value(670)).current;

  const animateOpen = React.useCallback(() => {
    Animated.timing(value, {
      toValue: 0,
      duration: 300,
      useNativeDriver: true,
    }).start();
  }, [value]);

  const onCustomLoginDeny = () => {
    Keyri.denySession(id, 'payload');
    closeModal();
  };

  const closeModal = () => {
    Animated.timing(value, {
      toValue: 670,
      duration: 300,
      useNativeDriver: true,
    }).start();
    setTimeout(() => {
      setSession(null);
      setCustomLoginVisible(false);
    }, 350);
  };
  const onCustomLoginOk = () => {
    Keyri.confirmSession(id, 'payload');
    closeModal();
  };
  const getSource = (widgetType: IWidgetTypes) => {
    switch (widgetType) {
      case IWidgetTypes.laptop:
        return ICONS.LAP_TOP_GEO;

      case IWidgetTypes.mobile:
        return ICONS.MOBILE;

      default:
        return ICONS.OS;
    }
  };

  const countryCodeLaptop =
    session?.riskAnalytics?.geoDataMap?.browser?.countryCode;
  const cityBrowserLaptop = session?.riskAnalytics?.geoDataMap?.browser?.city;
  const countryMobile = session?.riskAnalytics?.geoDataMap?.mobile?.countryCode;
  const cityMobile = session?.riskAnalytics?.geoDataMap?.mobile?.city;
  const os = session?.widgetUserAgent?.os;
  const browser = session?.widgetUserAgent?.browser;
  const authenticationDenied =
    session?.riskAnalytics?.riskAttributes?.isAnonymous;
  const Widget = ({
    type,
    text,
    vpn,
  }: {
    type: IWidgetTypes;
    text: string;
    vpn: boolean | undefined;
  }) => {
    return (
      <View style={styles.widget}>
        <Image
          source={getSource(type)}
          style={styles.image}
          resizeMode="contain"
        />
        <View style={styles.widgetTextview}>
          <Text style={[styles.widgetText, vpn && styles.red]}>{text}</Text>
          {vpn ? (
            <Text style={[styles.widgetText, styles.red]}>VPN detected</Text>
          ) : null}
        </View>
      </View>
    );
  };
  return (
    <Modal
      presentationStyle="overFullScreen"
      visible={customLoginVisible}
      animationType="fade"
      transparent={true}
      onRequestClose={closeModal}
      onShow={animateOpen}
    >
      <View style={styles.modalRoot}>
        <Animated.View
          style={[styles.modalBottom, { transform: [{ translateY: value }] }]}
        >
          <Text style={styles.title}>Your custom popup </Text>
          <Text style={styles.title}>Are you trying to log in? </Text>
          {authenticationDenied ? (
            <Text style={styles.title}>
              Your login attempt was denied. If you would still like to log in,
              please turn off your VPN then rescan the QR code{' '}
            </Text>
          ) : null}
          {countryCodeLaptop && cityBrowserLaptop ? (
            <Widget
              type={IWidgetTypes.laptop}
              text={`Near ${countryCodeLaptop} ${cityBrowserLaptop}`}
              vpn={authenticationDenied}
            />
          ) : null}
          {countryMobile && cityMobile ? (
            <Widget
              type={IWidgetTypes.mobile}
              text={`Near ${countryMobile} ${cityMobile}`}
              vpn={authenticationDenied}
            />
          ) : null}
          {os && browser ? (
            <Widget
              type={IWidgetTypes.os}
              text={`${browser} on ${os}`}
              vpn={authenticationDenied}
            />
          ) : null}
          <View style={styles.customPopupButtonView}>
            <TouchableOpacity
              style={[styles.noBtn, styles.btn]}
              onPress={onCustomLoginDeny}
            >
              <Text style={[styles.touchableText]}>No</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.yesBtn, styles.btn]}
              onPress={onCustomLoginOk}
            >
              <Text style={[styles.touchableText, styles.white]}>Yes</Text>
            </TouchableOpacity>
          </View>
        </Animated.View>
      </View>
    </Modal>
  );
};
const { width } = Dimensions.get('window');
const styles = StyleSheet.create({
  modalRoot: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0,0,0,0.7)',
    alignItems: 'center',
  },
  modalBottom: {
    backgroundColor: '#FFFFFF',
    borderTopRightRadius: 30,
    borderTopLeftRadius: 30,

    width: width,
    paddingVertical: 30,
    justifyContent: 'center',
    alignItems: 'center',
  },
  touchable: {
    width: width * 0.8,
    alignItems: 'center',
    height: 60,
    justifyContent: 'center',
    borderRadius: 25,
    borderColor: '#e3e3e3',
    borderBottomWidth: 0.5,
    paddingBottom: 3,
  },
  touchableText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#9887CB',
  },

  title: {
    fontSize: 18,
    color: '#5A4384',
    textAlign: 'center',
  },
  customPopupButtonView: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    width: width,
  },
  yesBtn: {
    borderColor: '#9887CB',
    backgroundColor: '#9887CB',
  },
  noBtn: {
    borderColor: '#9887CB',
    backgroundColor: '#FFFFFF',
  },
  btn: {
    borderRadius: 8,
    borderWidth: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 3,
    height: 50,
    width: 100,
    marginTop: 50,
  },
  widget: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    width: width * 0.8,
    height: 40,
    marginVertical: 10,
  },
  image: {
    width: 18,
    height: 18,
    marginRight: 20,
  },
  widgetTextview: {
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
  widgetText: {
    fontSize: 13,
    fontWeight: '400',
    color: '#595959',
  },
  red: {
    color: '#EF4D52',
  },
  green: {
    color: '#5A4384',
  },
  white: {
    color: '#FFFFFF',
  },
});
export default PopupModal;