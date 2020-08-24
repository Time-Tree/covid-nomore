import * as React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import Main from './Main';
import Search from './Search';
import Results from './Results';

const Stack = createStackNavigator();

export default () => (
  <Stack.Navigator screenOptions={{ headerShown: false }}>
    <Stack.Screen name="Main" component={Main} />
    <Stack.Screen name="Search" component={Search} />
    <Stack.Screen name="Results" component={Results} />
  </Stack.Navigator>
);
