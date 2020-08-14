import crashlytics from '@react-native-firebase/crashlytics';
import DeviceInfo from 'react-native-device-info';

export async function setCrashlytics() {
  try {
    await crashlytics().setCrashlyticsCollectionEnabled(true);
    await crashlytics().setAttribute('uniqueId', DeviceInfo.getUniqueId());
    console.log('Crashlythics init OK');
  } catch (error) {
    console.error('Crashlythics', error);
  }
}
