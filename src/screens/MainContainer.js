import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import MaterialCommunityIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import { WebView } from 'react-native-webview';
import NearbyContainer from './NearbyContainer';
import HandshakesContainer from './HandshakesContainer';
import NavbarComponent from './components/NavbarComponent';

function HomeScreen() {
  return (
    <>
      <NavbarComponent title="Home" />
      <WebView
        source={{
          uri:
            'https://docs.google.com/forms/d/e/1FAIpQLScIMZNW6RFo_mH2zzj7-sV9zifeP-nlFy1MzQL7ibanJ9TOvA/viewform?embedded=true'
        }}
      />
    </>
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
