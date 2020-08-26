import * as React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import Main from './Main';
import Agreement from './Agreement';
import Questionnaire from './Questionnaire';
import Completed from './Completed';

const Stack = createStackNavigator();

export default () => (
  <Stack.Navigator screenOptions={{ headerShown: false }}>
    <Stack.Screen name="Main" component={Main} />
    <Stack.Screen name="Agreement" component={Agreement} />
    <Stack.Screen name="Questionnaire" component={Questionnaire} />
    <Stack.Screen name="Completed" component={Completed} />
  </Stack.Navigator>
);
