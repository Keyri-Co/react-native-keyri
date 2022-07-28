import { StyleSheet, Dimensions } from 'react-native';
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
  touchable: {
    height: 50,
    width: 50,
    borderRadius: 25,
    backgroundColor: 'rgba(0 ,0, 0, 0.1)',
    position: 'absolute',
    top: 30,
    right: 25,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 10000,
  },
});
export default styles;
