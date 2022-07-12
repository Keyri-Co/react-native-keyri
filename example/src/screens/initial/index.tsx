import type { RootNavigationProps } from 'example/src/navigation';
import * as React from 'react';
import QRCodeScanner from 'react-native-qrcode-scanner';
import {  StyleSheet, View, ActivityIndicator, Text, Dimensions } from 'react-native';
import Keyri from 'react-native-keyri';

import {parseUrlParams, ISearchParam} from '../../utils/keyri'
interface InitialScreenProps extends RootNavigationProps<'Initial'> {}

const InitialScreen: React.FC<InitialScreenProps> = ({ navigation }) => {
  const [loading, setLoading] = React.useState<boolean>(false)
  const onReadSuccess = React.useCallback(async (scan:any) => {
    try{
      setLoading(true)
      const params: ISearchParam =  parseUrlParams(scan.data);
      const sessionId = params?.sessionId ?? '';
      console.log(params, 'params');
      const options = {
        appKey: 'IT7VrTQ0r4InzsvCNJpRCRpi1qzfgpaj',
        sessionId: sessionId,
        payload: 'scan data',
        publicUserId: 'email@com',
      }
      const session = await Keyri.initiateQrSession(options)
      
      if(session){
        const resp = await Keyri.initializeDefaultScreen(sessionId)
       
      }
    }
    catch(error){
      console.log(error,'==error initiate qr session')
    }
    finally{
      setLoading(false)
    }
   
  },[])
 
  return (
    <View style={styles.root}>
      { loading ? <View style={styles.indicator}>
      <ActivityIndicator size="large" color="#00ff00" />
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
    justifyContent: 'center'
  },
  indicator: {
    position: 'absolute',
    alignSelf: 'center',
    top: 200
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
  }
  

})
export default InitialScreen;
