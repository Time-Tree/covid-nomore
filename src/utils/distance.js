import DeviceInfo from 'react-native-device-info';

const devicesRSSIValuesAt2m = {
  'SM-N960F': -60,
  'SM-G950F': -69,
  'SM-G975F': -58,
  'SM-N975F': -50,
  'LYA-L29': -71,
  'SM-A705MN': -62,
  G8342: -60,
  'Mi A1': -64,
  'Pixel 3a XL': -58,
  'Pocophone F1': -56,
  'Mi A2 Lite': -59,
  'Nexus 5X': -67,
  'SM-A107F': -67,
  CPH1909: -57,
  RMX1911: -67,
  'Mi Max 3': -55,
  'YAL-L21': -57,
  'vivo 1915': -60
};

export function getCoarseProximity(rssi) {
  if (rssi >= -70) {
    return 'close';
  }

  if (rssi >= -80) {
    return 'nearby';
  }

  return 'distant';
}

export function approximateDistance(rssi) {
  const deviceModel = DeviceInfo.getModel() || DeviceInfo.getDeviceId() || null;

  // Signal strength at 2 meters from the device. For devices not on the list, the average is used.
  let rssiAt2m;

  if (devicesRSSIValuesAt2m.hasOwnProperty(deviceModel)) {
    rssiAt2m = devicesRSSIValuesAt2m[deviceModel];
  } else {
    const values = Object.values(devicesRSSIValuesAt2m);
    rssiAt2m =
      values.reduce((sum, current) => sum + current, 0) / values.length;
  }

  // Converting signal strength from logarithmic units to mW.
  // https://en.wikipedia.org/wiki/DBm#Unit_conversions
  const mwLevelAt2m = Math.pow(10, rssiAt2m / 10);
  const mwLevel = Math.pow(10, rssi / 10);

  // Inverse sq. law:
  // (intensity at distance 1 / intensity at distance 2) = (distance 2 squared / distance 1 squared)
  const d = Math.sqrt(4 * (mwLevelAt2m / mwLevel));
  return Math.round(d * 100) / 100;
}
