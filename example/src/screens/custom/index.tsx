import type { RootNavigationProps } from "example/src/navigation";
import React, { useState, useEffect, useContext, useCallback } from "react";
import styles from "./../default/default-styles";
import { View, ActivityIndicator, Text, TouchableOpacity, Image } from "react-native";
import Keyri, { KeyriSession } from "react-native-keyri";

import type { ISearchParam } from "../../utils/types";
import { parseUrlParams } from "../../utils/helpers";
import PopupModal from "../../components/popup-modal";
import { APP_KEY } from "../../utils/constants";
import { AppLinkContext } from "../../context/linking-context";
import { ICONS } from "../../assets";
import toast from "../../services/toast";
import { Camera, Code, useCameraDevice, useCameraPermission, useCodeScanner } from "react-native-vision-camera";
import { useIsFocused } from "@react-navigation/native";

interface CustomScreenProps extends RootNavigationProps<"Custom"> {
}

const CustomScreen: React.FC<CustomScreenProps> = ({ navigation }) => {
  const [loading, setLoading] = React.useState<boolean>(false);
  const [isPopUpVisible, setIsPopUpVisible] = useState<boolean>(false);
  const { deepLink } = useContext(AppLinkContext);
  const [ activeSession, setActiveSession ] = useState<KeyriSession | undefined>(undefined);

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

  const onReadSuccess = useCallback(
    async (data: string) => {
      try {
        setLoading(true);
        await Keyri.initialize({ appKey: APP_KEY });
        const params: ISearchParam = parseUrlParams(data);
        const sessionId: string = params?.sessionId ?? "";
        const session = await Keyri.initiateQrSession(sessionId, "user@email");
        if (session) {
          setActiveSession(session);
          setIsPopUpVisible(true);
          setActiveSession(session);
        }
      } catch (error) {
        toast.show(error);
      } finally {
        setLoading(false);
      }
    },
    [setActiveSession]
  );

  useEffect(() => {
    if (deepLink) {
      onReadSuccess(deepLink);
    }
  }, [deepLink, onReadSuccess]);

  const closePopUp = () => {
    setIsPopUpVisible(false);
    setActiveSession(undefined);
  };

  const denySession = async () => {
    if (activeSession) {
      closePopUp();
      try {
        await Keyri.denySession("payload");
      } catch (error) {
        toast.show(error);
      }
    }
  };

  const confirmSession = async () => {
    if (activeSession) {
      closePopUp();
      try {
        await Keyri.confirmSession("payload");
      } catch (error) {
        toast.show(error);
      }
    }
  };
  const onClosePress = () => {
    navigation.goBack();
  };
  return (
    <View style={styles.root}>
      {activeSession ? <PopupModal
        isPopUpVisible={isPopUpVisible}
        denySession={denySession}
        confirmSession={confirmSession}
        session={activeSession}
      /> : null}
      <TouchableOpacity onPress={onClosePress} style={styles.touchable}>
        <Image source={ICONS.CLOSE} height={35} resizeMode="stretch" width={35} />
      </TouchableOpacity>
      {loading ? (
        <View style={styles.indicator}>
          <ActivityIndicator size="large" color="#FFFFFF" />
        </View>
      ) : null}
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
export default CustomScreen;
