import Keyri from 'react-native-keyri';

const APP_KEY = 'raB7SFWt27VoKqkPhaUrmWAsCJIO8Moj';
const PUBLIC_KEY =
  'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE56eKjQNfIbfWYCBQLCF2yV6VySbHMzuc07JYCOS6juySvUWE/ubYvw9pJGAgQfmNr2n4LAQggoapHgfHkTNqbg=="';
const BASE_URL = 'http://18.208.184.185:5000';
const KEYRI_CALLBACK_URL = `${BASE_URL}/users/session-mobile`;

export function initializeKeyri() {
  Keyri.initialize(APP_KEY, PUBLIC_KEY, KEYRI_CALLBACK_URL, false);
}
