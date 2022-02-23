import Keyri from 'react-native-keyri';

const BASE_URL = 'http://18.208.184.185:5000';

export function initializeKeyri() {
  Keyri.initialize({
    callbackUrl: `${BASE_URL}/users/session-mobile`,
    appKey: 'raB7SFWt27VoKqkPhaUrmWAsCJIO8Moj',
    publicKey: 'MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE56eKjQNfIbfWYCBQLCF2yV6VySbHMzuc07JYCOS6juySvUWE/ubYvw9pJGAgQfmNr2n4LAQggoapHgfHkTNqbg=="',
  });
}
