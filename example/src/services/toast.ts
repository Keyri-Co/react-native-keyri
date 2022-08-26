import React from 'react';
import type Toast from 'react-native-easy-toast';
import { DURATION } from '../utils/constants';
class ToastService {
  ref = React.createRef<Toast>();

  show = (error: unknown) => {
    if (error instanceof Error) return this.ref.current?.show(error.message, DURATION);
    else if (typeof error === 'string') return this.ref.current?.show(error, DURATION);
    else this.ref.current?.show('Unknown error');
  };
}

export default new ToastService();
