import { StyleSheet, Dimensions } from 'react-native';
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
    paddingVertical: 30,
    justifyContent: 'center',
    alignItems: 'center',
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
    marginTop: 20,
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
export default styles;
