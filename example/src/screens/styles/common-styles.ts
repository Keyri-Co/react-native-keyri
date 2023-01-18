import { Dimensions, StyleSheet } from 'react-native';
import { scaledSize } from '../../utils/constants';
const { width, height } = Dimensions.get('window');
const styles = StyleSheet.create({
  root: {
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  input: {
    height: scaledSize(42),
    width: scaledSize(280),
    color: '#FFFFFF',
    backgroundColor: '#9887CB',
    borderRadius: scaledSize(7),
    borderWidth: scaledSize(0.5),
    fontSize: scaledSize(16),
    marginTop: scaledSize(12),
    paddingHorizontal: scaledSize(20),
  },
  markerStyle: { borderColor: '#666' },
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
    color: '#666',
    fontSize: 16,
    textAlign: 'center',
  },
  touchable: {
    width: 250,
    height: 50,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginVertical: 8,
    backgroundColor: '#9887CB',
    paddingBottom: 3,
    paddingHorizontal: 20,
  },
  btnText: {
    fontWeight: '700',
    fontSize: 16,
    color: '#FFFFFF',
  },
  title: {
    color: '#666',
    fontSize: 18,
    textAlign: 'center',
    marginVertical: 12,
  },
});
export default styles;
