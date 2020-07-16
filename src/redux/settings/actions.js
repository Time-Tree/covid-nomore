import { ActionTypes } from './store';

class SettingsActions {
  changeStatusAction(status) {
    return dispatch => {
      dispatch({
        type: ActionTypes.CHANGE_STATUS,
        payload: status
      });
    };
  }

  setClientIdAction(uuid) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_CLIENT_ID,
        payload: uuid
      });
    };
  }

  setEasterEggAction(value) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_EASTER_EGG,
        payload: value
      });
    };
  }

  setProcessAction(type, value) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_PROCESS,
        payload: { type, value }
      });
    };
  }

  setNearbyProcessAction(value) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_NEARBY_PROCESS,
        payload: value
      });
    };
  }

  setBleProcessAction(value) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_BLE_PROCESS,
        payload: value
      });
    };
  }
}

const settingsActions = new SettingsActions();
export default settingsActions;
