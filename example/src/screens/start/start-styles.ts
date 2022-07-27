import { StyleSheet } from 'react-native';
const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  text: {
    color: '#666',
    fontSize: 16,
    textAlign: 'center',
  },
  btnView: {
    height: 220,
    justifyContent: 'space-between',
    marginBottom: 70,
  },
  touchable: {
    width: 250,
    height: 50,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    backgroundColor: '#9887CB',
    paddingBottom: 3,
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
    marginTop: 50,
  },
});
export default styles;
