import React from 'react';
import { StatusBar } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import MaterialCommunityIcon from 'react-native-vector-icons/MaterialCommunityIcons';

import HomeScreen from './src/screens/Home';
import TestResultsScreen from './src/screens/Test';
import SurveyScreen from './src/screens/Survey';
import ChatScreen from './src/screens/Chat';
import ActivityScreen from './src/screens/Activity';

import TokensContainer from './src/screens/TokensContainer';
import StatusContainer from './src/screens/StatusContainer';
import ReportMeContainer from './src/screens/ReportMeContainer';
import ProtectContainer from './src/screens/ProtectContainer';

const Tab = createBottomTabNavigator();

const screenOptions = ({ route }) => ({
  tabBarIcon: ({ focused, color, size }) => {
    let iconName;
    if (route.name === 'Home') {
      iconName = focused ? 'home' : 'home-outline';
    } else if (route.name === 'Tokens') {
      iconName = focused ? 'account-search' : 'account-search-outline';
    } else if (route.name === 'Activity') {
      iconName = 'bluetooth';
    } else if (route.name === 'Status') {
      iconName = 'account-check-outline';
    } else if (route.name === 'Chat') {
      iconName = 'chat-outline';
    } else if (route.name === 'Report me') {
      iconName = 'emoticon-sad-outline';
    } else if (route.name === 'Protect') {
      iconName = 'security';
    } else if (route.name === 'Test') {
      iconName = 'clipboard-check';
    } else if (route.name === 'Survey') {
      iconName = 'clipboard-check';
    }
    return <MaterialCommunityIcon name={iconName} size={size} color={color} />;
  },
  unmountOnBlur: true
});

const showTabBar = route => {
  const routeName = route.state
    ? route.state.routes[route.state.index].name
    : '';

  const routesWithoutTabBar = ['Questionnaire', 'Completed'];

  return routesWithoutTabBar.includes(routeName) === false;
};

const ScreenTabs = ({ showEasterEggScreens = false }) => {
  return (
    <>
      <StatusBar
        translucent
        backgroundColor="transparent"
        barStyle="dark-content"
      />
      <NavigationContainer>
        <Tab.Navigator
          screenOptions={screenOptions}
          tabBarOptions={{
            activeTintColor: '#008bcf',
            inactiveTintColor: '#9ba2ab',
            keyboardHidesTabBar: true
          }}
        >
          <Tab.Screen name="Home" component={HomeScreen} />
          <Tab.Screen name="Test" component={TestResultsScreen} />
          <Tab.Screen
            name="Survey"
            component={SurveyScreen}
            options={({ route }) => ({
              tabBarVisible: showTabBar(route)
            })}
          />
          <Tab.Screen name="Protect" component={ProtectContainer} />
          <Tab.Screen name="Status" component={StatusContainer} />
          {showEasterEggScreens && (
            <>
              <Tab.Screen name="Tokens" component={TokensContainer} />
              <Tab.Screen name="Activity" component={ActivityScreen} />
            </>
          )}
          <Tab.Screen name="Chat" component={ChatScreen} />
          <Tab.Screen name="Report me" component={ReportMeContainer} />
        </Tab.Navigator>
      </NavigationContainer>
    </>
  );
};

export default ScreenTabs;
