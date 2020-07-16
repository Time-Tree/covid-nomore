import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import crashlytics from '@react-native-firebase/crashlytics';
import DeviceInfo from 'react-native-device-info';
import MaterialCommunityIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import { WebView } from 'react-native-webview';
import settingsActions from '../redux/settings/actions';
import reduxContainer from '../redux/reduxContainer';
import NearbyContainer from './NearbyContainer';
import TokensContainer from './TokensContainer';
import StatusContainer from './StatusContainer';
import ReportMeContainer from './ReportMeContainer';
import ProtectContainer from './ProtectContainer';
import NavbarComponent from './components/NavbarComponent';
import { store } from '../redux/store';
import { generateRandomUUID } from '../utils/uuid';

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
    } else if (route.name === 'Tokens') {
      iconName = focused ? 'account-search' : 'account-search-outline';
    } else if (route.name === 'Logs') {
      iconName = 'format-list-numbered-rtl';
    } else if (route.name === 'Status') {
      iconName = 'account-check-outline';
    } else if (route.name === 'Report me') {
      iconName = 'emoticon-sad-outline';
    } else if (route.name === 'Protect') {
      iconName = 'security';
    }
    return <MaterialCommunityIcon name={iconName} size={size} color={color} />;
  }
});

async function setCrashlytics() {
  try {
    await crashlytics().setCrashlyticsCollectionEnabled(true);
    await crashlytics().setAttribute('uniqueId', DeviceInfo.getUniqueId());
  } catch (error) {
    // console.log('error', error);
  }
}

const MainContainer = props => {
  useEffect(() => {
    setCrashlytics();
    if (!props.clientId) {
      store.dispatch(settingsActions.setClientIdAction(generateRandomUUID()));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <NavigationContainer>
      <Tab.Navigator
        screenOptions={screenOptions}
        tabBarOptions={{
          activeTintColor: 'darkblue',
          inactiveTintColor: 'gray',
          keyboardHidesTabBar: true
        }}
      >
        <Tab.Screen name="Home" component={HomeScreen} />
        <Tab.Screen name="Protect" component={ProtectContainer} />
        <Tab.Screen name="Status" component={StatusContainer} />
        {props.easterEgg && (
          <>
            <Tab.Screen name="Tokens" component={TokensContainer} />
            <Tab.Screen name="Logs" component={NearbyContainer} />
          </>
        )}
        <Tab.Screen name="Report me" component={ReportMeContainer} />
      </Tab.Navigator>
    </NavigationContainer>
  );
};

function mapStateToProps(state) {
  return {
    easterEgg: state.settings.easterEgg,
    clientId: state.settings.clientId
  };
}

const dispatchToProps = {
  setClientIdAction: settingsActions.setClientIdAction
};

export default reduxContainer(MainContainer, mapStateToProps, dispatchToProps);
