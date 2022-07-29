import React from 'react';
import type Toast from 'react-native-easy-toast';

class ToastService {
  ref = React.createRef<Toast>();

  show = (message: string, duration = 1200) => {
    this.ref.current?.show(message, duration);
  };
}

export default new ToastService();
