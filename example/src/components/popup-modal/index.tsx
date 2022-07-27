import * as React from 'react';
import {
  View,
  Text,
  Dimensions,
  Modal,
  TouchableOpacity,
  Animated,
  Image,
} from 'react-native';

import { IWidgetTypes } from '../../utils/types';
import { ICONS } from '../../assets/index';
import type { KeyriSession } from '../../../../src/types';
import styles from './popup-modal-styles';

const { height } = Dimensions.get('window');

interface IPopupModalProps {
  session: KeyriSession | null;
  isPopUpVisible: boolean;
  denySession: () => void;
  confirmSession: () => void;
}

const PopupModal: React.FC<IPopupModalProps> = ({
  isPopUpVisible,
  denySession,
  confirmSession,
  session,
}) => {
  const value = React.useRef(new Animated.Value(height)).current;
  const animateOpen = () => {
    Animated.timing(value, {
      toValue: 0,
      duration: 300,
      useNativeDriver: true,
    }).start();
  };
  const onCustomLoginDeny = () => {
    closeModal(denySession);
  };

  const closeModal = (func: () => void) => {
    Animated.timing(value, {
      toValue: height,
      duration: 300,
      useNativeDriver: true,
    }).start(() => func());
  };
  const onCustomLoginOk = () => {
    closeModal(confirmSession);
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
    session?.riskAnalytics?.geoData?.browser?.countryCode;
  const cityBrowserLaptop = session?.riskAnalytics?.geoData?.browser?.city;
  const countryMobile = session?.riskAnalytics?.geoData?.mobile?.countryCode;
  const cityMobile = session?.riskAnalytics?.geoData?.mobile?.city;
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
      visible={isPopUpVisible}
      animationType="fade"
      transparent={true}
      onRequestClose={() => closeModal(denySession)}
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

export default PopupModal;
