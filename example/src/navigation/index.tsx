import * as React from 'react';
import { NavigationContainer, RouteProp } from '@react-navigation/native';
import {
  createNativeStackNavigator,
  NativeStackNavigationProp,
} from '@react-navigation/native-stack';
import DefaultScreen from '../screens/default';
import CustomScreen from '../screens/custom';
import StartScreen from '../screens/start';
import KeyOperationScreen from '../screens/key_operation';
import AuthenticationScreen from '../screens/authentication';
import type {
  AccountsType,
  AuthenticationType,
  AuthParams,
  KeyOperationType,
} from '../utils/types';
import AccountsScreen from '../screens/accounts';
import GenerateSignatureScreen from '../screens/signature';

export type RootNavigatorParams = {
  Start: undefined;
  GenerateSignature: undefined;
  Custom: {
    authParams: AuthParams;
  };
  Default: {
    authParams: AuthParams;
  };
  Authentication: {
    type: AuthenticationType;
  };
  KeyOperation: {
    type: KeyOperationType;
  };
  Accounts: {
    type: AccountsType;
  };
};

const Stack = createNativeStackNavigator<RootNavigatorParams>();

interface RootNavigatorProps {}

const RootNavigator: React.FC<RootNavigatorProps> = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Start" component={StartScreen} options={{ headerShown: false }} />
        <Stack.Screen name="Custom" component={CustomScreen} options={{ headerShown: false }} />
        <Stack.Screen name="Default" component={DefaultScreen} options={{ headerShown: false }} />
        <Stack.Screen
          name="GenerateSignature"
          component={GenerateSignatureScreen}
          options={{ title: 'Generate signature', headerShown: false }}
        />
        <Stack.Screen
          name="Authentication"
          component={AuthenticationScreen}
          options={{ title: 'Authentication' }}
        />
        <Stack.Screen
          name="KeyOperation"
          component={KeyOperationScreen}
          options={({ route }) => ({ title: route.params.type.toString() })}
        />
        <Stack.Screen
          name="Accounts"
          component={AccountsScreen}
          options={({ route }) => ({ title: route.params.type.toString() })}
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
