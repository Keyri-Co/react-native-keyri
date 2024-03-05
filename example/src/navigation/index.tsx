import * as React from 'react';
import { NavigationContainer, RouteProp } from '@react-navigation/native';
import {
  createNativeStackNavigator,
  NativeStackNavigationProp,
} from '@react-navigation/native-stack';
import DefaultScreen from '../screens/default';
import CustomScreen from '../screens/custom';

export type RootNavigatorParams = {
  Start: undefined;
  Custom: undefined;
  Default: undefined;
};

const Stack = createNativeStackNavigator<RootNavigatorParams>();

interface RootNavigatorProps {}

const RootNavigator: React.FC<RootNavigatorProps> = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Custom" component={CustomScreen} options={{ headerShown: false }} />
        <Stack.Screen name="Default" component={DefaultScreen} options={{ headerShown: false }} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

type RootRouteName = keyof RootNavigatorParams;

export interface RootNavigationProps<RouteName extends RootRouteName> {
  route: RouteProp<RootNavigatorParams, RouteName>;
  navigation: NativeStackNavigationProp<RootNavigatorParams, RouteName>;
}

export default RootNavigator;
