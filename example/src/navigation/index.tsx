import * as React from 'react';
import { NavigationContainer, RouteProp } from '@react-navigation/native';
import {
  createNativeStackNavigator,
  NativeStackNavigationProp,
} from '@react-navigation/native-stack';
import InitialScreen from '../screens/initial';
import StartScreen from '../screens/start';
import SignUpScreen from '../screens/sign-up';

type RootNavigatorParams = {
  Initial: undefined;
  LogIn: undefined;
  SignUp: undefined;
};

const Stack = createNativeStackNavigator<RootNavigatorParams>();

interface RootNavigatorProps {}

const RootNavigator: React.FC<RootNavigatorProps> = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen
          name="LogIn"
          component={StartScreen}
          options={{ headerShown: false }}
        />
        <Stack.Screen
          name="Initial"
          component={InitialScreen}
          options={{ headerShown: false }}
        />

        <Stack.Screen name="SignUp" component={SignUpScreen} />
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
