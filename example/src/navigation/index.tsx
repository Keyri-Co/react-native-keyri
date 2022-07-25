import * as React from 'react';
import { NavigationContainer, RouteProp } from '@react-navigation/native';
import {
  createNativeStackNavigator,
  NativeStackNavigationProp,
} from '@react-navigation/native-stack';
import InitialScreen from '../screens/initial';
import StartScreen from '../screens/start';
import type { ILoginType } from '../utils/types';

type RootNavigatorParams = {
  Initial: { type: ILoginType; url: string | null };
  Start: { type: ILoginType };
  SignUp: undefined;
};

const Stack = createNativeStackNavigator<RootNavigatorParams>();

interface RootNavigatorProps {}

const RootNavigator: React.FC<RootNavigatorProps> = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen
          name="Start"
          component={StartScreen}
          options={{ headerShown: false }}
        />
        <Stack.Screen
          name="Initial"
          component={InitialScreen}
          options={{ headerShown: false }}
        />
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
