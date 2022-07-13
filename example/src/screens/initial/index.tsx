import type { RootNavigationProps } from 'example/src/navigation';
import * as React from 'react';
import QRCodeScanner from 'react-native-qrcode-scanner';
import {  
  StyleSheet,
  View, 
  ActivityIndicator, 
  Text, 
  Dimensions, 
  Modal, 
  TouchableOpacity, 
  Animated ,
  SafeAreaView} from 'react-native';
import Keyri from 'react-native-keyri';

import {parseUrlParams, ISearchParam} from '../../utils/keyri'
interface InitialScreenProps extends RootNavigationProps<'Initial'> {}

const InitialScreen: React.FC<InitialScreenProps> = ({ navigation }) => {
  const [loading, setLoading] = React.useState<boolean>(false)
  const [visible, setVisible] = React.useState<boolean>(false)
  const [customloginVisible, setCustomLoginVisible] = React.useState<boolean>(false)
  const [sessionId, setSessionId] = React.useState<string>('')
  const value = React.useRef(new Animated.Value(300)).current
  const onReadSuccess = React.useCallback(async (scan:any) => {
    try{
      setLoading(true)
      const params: ISearchParam =  parseUrlParams(scan.data);
      const sessionId = params?.sessionId ?? '';
      
      const options = {
        appKey: 'IT7VrTQ0r4InzsvCNJpRCRpi1qzfgpaj',
        sessionId: sessionId,
        payload: 'scan data',
        publicUserId: 'email@com',
      }
      const session = await Keyri.initiateQrSession(options)
      
      if(session){
        console.log(sessionId, '==session id');
        setSessionId(sessionId)
        setVisible(true)  
       
      }
    }
    catch(error){
      console.log(error,'==error initiate qr session')
    }
    finally{
      setLoading(false)
    }
   
  },[])
 const onDefaultLogin = React.useCallback(() =>{
  Animated.timing(value, { toValue: 200, duration: 200, useNativeDriver: true }).start();

  
  setTimeout(() => {
    setVisible(false)
    Keyri.initializeDefaultScreen(sessionId)
  },250)
   
 },[sessionId])
 const onCustomLogin = () => {
  Animated.timing(value, { toValue: 300, duration: 300, useNativeDriver: true }).start();
  setTimeout(() => {
    setVisible(false)
    setCustomLoginVisible(true)
  },300)
  
 }
 const onCustomLoginDeny = () => {
  Animated.timing(value, { toValue: 300, duration: 300, useNativeDriver: true }).start();
  setTimeout(() => {
    setCustomLoginVisible(false)
  })
 }
 const animateOpen = () => {
  Animated.timing(value, { toValue: 0, duration: 300, useNativeDriver: true }).start();
 }
 const closeModal = () => {
  Animated.timing(value, { toValue: 300, duration: 300, useNativeDriver: true }).start();
  setTimeout(() => {
    setVisible(false)
  },300)
 }
 const ConfirmModal = () => {
return (
  <Modal 
    presentationStyle='overFullScreen'
    visible={visible}
    animationType='fade'
    transparent={true}
    onRequestClose={closeModal}
    onShow={animateOpen}
    onDismiss={closeModal}
    >
  <View style={[styles.modalRoot]}>
    <Animated.View 
      style={[styles.modalBottom,{transform: [{ translateY: value }]}]}>
      <TouchableOpacity style={[styles.touchable,styles.border]}onPress={onDefaultLogin}>
        <Text style={styles.touchableText}>Login with default popup </Text>
      </TouchableOpacity>
     
      <TouchableOpacity style={styles.touchable}onPress={onCustomLogin}>
        <Text style={styles.touchableText}>Login with custom popup </Text>
      </TouchableOpacity>
    </Animated.View>
  </View>
</Modal>
)
 }
 const CustomLoginModal = () => {
  return (
    <Modal  
    presentationStyle='overFullScreen'
    visible={customloginVisible}
    animationType='fade'
    transparent={true}
    onRequestClose={closeModal}
    onShow={animateOpen}
    onDismiss={closeModal}>
    <View style={styles.modalRoot}>
      <Animated.View style={[styles.modalBottom,{transform: [{ translateY: value }]}]}>
      <Text style={styles.title}>Your custom popup </Text>
      <View style={styles.customPopupButtonView}>
      <TouchableOpacity style={styles.yesBtn}onPress={onCustomLoginDeny}>
          <Text style={styles.touchableText}>No</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.noBtn}onPress={onCustomLoginDeny}>
          <Text style={styles.touchableText}>Yes</Text>
        </TouchableOpacity>
        </View>
       
      </Animated.View>
    </View>
  </Modal>
  )
   }
  return (
    <View style={styles.root}>
      <ConfirmModal/>
      <CustomLoginModal/>
      {loading ? <View style={styles.indicator}>
      <ActivityIndicator size="large" color="#FFFFFF" />
      </View> : null}
      <QRCodeScanner
      
      showMarker
      cameraType="back"
      cameraStyle={styles.camera}
      onRead={onReadSuccess}
      reactivate={true}
      reactivateTimeout={9000}
      markerStyle={{borderColor: '#666'}}
      />
      <Text style={styles.text}>
        Powered by Keyri
      </Text>
    </View>
  );
};
const {width, height} = Dimensions.get('window')
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
    zIndex: 10000
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
    color: '#777'
  },
  modalRoot: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0,0,0,0.15)',
    alignItems: 'center',
    
  },
  modalBottom: {
    backgroundColor: '#FFFFFF',
    borderTopRightRadius: 30,
    borderTopLeftRadius: 30,
    
    width: width,
    paddingVertical: 30,
    justifyContent: 'center',
    alignItems: 'center'
  },
  touchable: {
    width: width * 0.8,
    alignItems: 'center',
    height: 60,
    justifyContent: 'center',
    borderRadius: 25,
    borderColor: '#e3e3e3',
    borderBottomWidth: 0.5,
    paddingBottom:3,
   
    
  },
  touchableText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#999'
  },
 
  title: {
    fontSize: 18,
    color: '#999',
    textAlign: 'center'
  },
  customPopupButtonView: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    width: width
  },
  yesBtn: {
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#03A564',
    backgroundColor: ' #E1F4ED',
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 3
  },
  noBtn: {
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#EF4D52',
    backgroundColor: ' #FEECED',
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 3
  }

})
export default InitialScreen;
