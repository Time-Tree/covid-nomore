import * as React from 'react';
import { Text, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import MaterialCommunityIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import NearbyContainer from './NearbyContainer';
import HandshakesContainer from './HandshakesContainer';
import BackgroundAPI from '../utils/backgroundAPI';

function HomeScreen() {
  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Text>Nearby Demo with UltraSonic!</Text>
    </View>
  );
}

const Tab = createBottomTabNavigator();

const screenOptions = ({ route }) => ({
  tabBarIcon: ({ focused, color, size }) => {
    let iconName;
    if (route.name === 'Home') {
      iconName = focused ? 'home' : 'home-outline';
    } else if (route.name === 'Nearby Handshakes') {
      iconName = focused ? 'account-search' : 'account-search-outline';
    } else if (route.name === 'Nearby Logs') {
      iconName = focused
        ? 'format-list-numbered-rtl'
        : 'format-list-numbered-rtl';
    }
    return <MaterialCommunityIcon name={iconName} size={size} color={color} />;
  }
});

export default function MainContainer() {
  BackgroundAPI.init();
  return (
    <NavigationContainer>
      <Tab.Navigator
        screenOptions={screenOptions}
        tabBarOptions={{
          activeTintColor: 'darkblue',
          inactiveTintColor: 'gray'
        }}
      >
        <Tab.Screen name="Home" component={HomeScreen} />
        <Tab.Screen name="Nearby Handshakes" component={HandshakesContainer} />
        <Tab.Screen name="Nearby Logs" component={NearbyContainer} />
      </Tab.Navigator>
    </NavigationContainer>
  );
}
