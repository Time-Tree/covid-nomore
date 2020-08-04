export const initialState = {
  isSubscribing: false,
  isConnected: false,
  status: 0,
  clientId: null,
  easterEgg: false,
  bleProcess: true,
  nearbyProcess: true
};

export const ActionTypes = {
  CHANGE_STATUS: 'CHANGE_STATUS',
  SET_CLIENT_ID: 'SET_CLIENT_ID',
  SET_EASTER_EGG: 'SET_EASTER_EGG',
  SET_NEARBY_PROCESS: 'SET_NEARBY_PROCESS',
  SET_BLE_PROCESS: 'SET_BLE_PROCESS',
  SET_PROCESS: 'SET_PROCESS'
};
